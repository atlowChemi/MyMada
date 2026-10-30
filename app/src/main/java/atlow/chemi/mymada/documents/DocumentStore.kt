package atlow.chemi.mymada.documents

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal enum class DocumentKind(val objectName: String) {
    BLS("bls[1].pdf"), ALS("als[1].pdf"), KUNTRES("kuntres.pdf")
}

internal data class StoredDocument(val file: File, val pageCount: Int, val cached: Boolean,
    val revision: Long = file.lastModified())

/** Firebase reads only. No download tokens or document bytes are stored in preferences. */
internal class DocumentStore(private val directory: File) {
    suspend fun load(kind: DocumentKind, refresh: Boolean = false,
        download: suspend (String, File) -> Unit = ::downloadFromStorage): StoredDocument = withContext(Dispatchers.IO) {
        check(directory.isDirectory || directory.mkdirs())
        val destination = File(directory, kind.objectName)
        if (!refresh && destination.exists()) {
            try {
                return@withContext StoredDocument(destination, pageCount(destination), true)
            } catch (_: Exception) {
                // An interrupted/invalid old copy must not prevent a fresh download.
            }
        }
        val temporary = File.createTempFile("download-", ".pdf", directory)
        try {
            download(kind.objectName, temporary)
            ensureActive()
            val pages = pageCount(temporary)
            check(temporary.renameTo(destination)) { "Cannot save document" }
            StoredDocument(destination, pages, false)
        } finally {
            temporary.delete()
        }
    }
}

private suspend fun downloadFromStorage(path: String, destination: File) = suspendCancellableCoroutine<Unit> { continuation ->
    val storage = FirebaseStorage.getInstance().apply { maxDownloadRetryTimeMillis = 30_000 }
    val task = storage.reference.child(path).getFile(destination)
    continuation.invokeOnCancellation { task.cancel() }
    task.addOnSuccessListener { continuation.resume(Unit) }
        .addOnFailureListener { continuation.resumeWithException(it) }
}

internal fun pageCount(file: File): Int = withPdf(file) { renderer ->
    require(renderer.pageCount > 0) { "Empty PDF" }
    renderer.pageCount
}

internal fun renderPage(file: File, index: Int, width: Int): Bitmap = withPdf(file) { renderer ->
    renderer.openPage(index).use { page ->
        // ponytail: bound visible + prefetched rasters on 48-MiB heaps; tile rendering for high-resolution zoom.
        val smallHeap = Runtime.getRuntime().maxMemory() <= 64L * 1024 * 1024
        val scale = minOf(width.coerceIn(1, if (smallHeap) 720 else 1440).toFloat() / page.width,
            (if (smallHeap) 1024f else 2048f) / page.height)
        Bitmap.createBitmap((page.width * scale).toInt().coerceAtLeast(1),
            (page.height * scale).toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888).also {
            it.eraseColor(Color.WHITE)
            page.render(it, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        }
    }
}

private inline fun <T> withPdf(file: File, block: (PdfRenderer) -> T): T {
    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
        PdfRenderer(descriptor).use { return block(it) }
    }
}
