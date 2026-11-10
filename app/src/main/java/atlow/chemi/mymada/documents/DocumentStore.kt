package atlow.chemi.mymada.documents

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
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

internal fun renderPage(file: File, index: Int, width: Int, zoom: Float = 1f,
    panX: Float = 0f, panY: Float = 0f): Bitmap = withPdf(file) { renderer ->
    require(zoom.isFinite() && zoom in 1f..4f && panX.isFinite() && panY.isFinite())
    renderer.openPage(index).use { page ->
        // Render the zoomed region, not an enlarged low-resolution bitmap; memory stays bounded at every zoom.
        val smallHeap = Runtime.getRuntime().maxMemory() <= 64L * 1024 * 1024
        val scale = minOf(width.coerceIn(1, if (smallHeap) 720 else 1440).toFloat() / page.width,
            (if (smallHeap) 1024f else 2048f) / page.height)
        Bitmap.createBitmap((page.width * scale).toInt().coerceAtLeast(1),
            (page.height * scale).toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888).also {
            it.eraseColor(Color.WHITE)
            val transform = Matrix().apply {
                setScale(scale * zoom, scale * zoom)
                postTranslate((it.width - page.width * scale * zoom) / 2 + panX * it.width,
                    (it.height - page.height * scale * zoom) / 2 + panY * it.height)
            }
            page.render(it, null, transform, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        }
    }
}

// Android 7's PDFium calls share native state across renderer instances.
// ponytail: process-wide lock; revisit only if supported runtimes safely allow parallel PDF work.
private val pdfLock = Any()

private inline fun <T> withPdf(file: File, block: (PdfRenderer) -> T): T = synchronized(pdfLock) {
    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
        PdfRenderer(descriptor).use { block(it) }
    }
}
