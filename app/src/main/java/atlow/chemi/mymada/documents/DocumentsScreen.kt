package atlow.chemi.mymada.documents

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import atlow.chemi.mymada.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// A cancelled gesture render finishes before the newest one starts, bounding transient bitmap memory too.
private val rasterDispatcher = Dispatchers.IO.limitedParallelism(1)

@Composable
internal fun DocumentsScreen(kind: DocumentKind,
    load: (suspend (Boolean) -> StoredDocument)? = null) {
    val context = LocalContext.current
    val store = remember { DocumentStore(File(context.filesDir, "documents")) }
    var attempt by remember { mutableIntStateOf(0) }
    var document by remember { mutableStateOf<StoredDocument?>(null) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(kind, attempt) {
        loading = true
        failed = false
        try {
            document = load?.invoke(attempt > 0) ?: store.load(kind, attempt > 0)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            failed = true
        } finally {
            loading = false
        }
    }
    MaterialTheme {
        Column(Modifier.fillMaxSize().then(if (document != null) Modifier.background(Color.White) else Modifier)) {
            if (loading && document == null) {
                Box(Modifier.fillMaxSize().testTag("documentLoading"), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (loading) {
                Row(Modifier.fillMaxWidth().padding(16.dp).testTag("documentLoading"),
                    verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(32.dp))
                    Text(stringResource(R.string.document_loading), Modifier.padding(start = 12.dp))
                }
            }
            if (failed) {
                if (document == null) {
                    // Retain the published local ECG error artwork/locale; it never loads network content.
                    AndroidView(factory = { context ->
                        android.webkit.WebView(context).apply {
                            layoutParams = android.view.ViewGroup.LayoutParams(-1, -1)
                            settings.javaScriptEnabled = true
                            settings.blockNetworkLoads = true
                            val language = context.resources.configuration.locales[0].language
                            loadUrl("file:///android_asset/err.html?lng=$language")
                        }
                    }, onRelease = { it.destroy() }, modifier = Modifier.weight(1f).fillMaxWidth().testTag("documentError"))
                } else Text(stringResource(R.string.document_error), Modifier.padding(16.dp).testTag("documentError"))
                TextButton(onClick = { attempt++ }) { Text(stringResource(R.string.document_retry)) }
            }
            document?.let { ready ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (ready.cached) Text(stringResource(R.string.document_cached), Modifier.weight(1f).padding(8.dp))
                    else Spacer(Modifier.weight(1f))
                    TextButton(onClick = { attempt++ }, enabled = !loading) { Text(stringResource(R.string.document_refresh)) }
                }
                // Key a refreshed file separately so visible page rasters are replaced too.
                key(ready) {
                    LazyColumn(Modifier.fillMaxSize().testTag("documentPages")) {
                        items((0 until ready.pageCount).toList(), key = { it }) { index ->
                            DocumentPage(ready, index)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentPage(document: StoredDocument, index: Int) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var renderedZoom by remember { mutableFloatStateOf(1f) }
    var failed by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val label = stringResource(R.string.document_page, index + 1, document.pageCount)
    BoxWithConstraints(Modifier.fillMaxWidth().clipToBounds()) {
        val width = constraints.maxWidth
        LaunchedEffect(document, index, width, retry, zoom, offset) {
            failed = false
            try {
                val height = bitmap?.let { width.toFloat() * it.height / it.width } ?: 1f
                bitmap = withContext(rasterDispatcher) {
                    renderPage(document.file, index, width, zoom, offset.x / width, offset.y / height)
                }
                renderedZoom = zoom
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { failed = true }
        }
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(label, Modifier.weight(1f).padding(8.dp))
                val out = stringResource(R.string.document_zoom_out)
                val into = stringResource(R.string.document_zoom_in)
                TextButton(onClick = { zoom = (zoom - .5f).coerceAtLeast(1f); offset = Offset.Zero },
                    modifier = Modifier.semantics { contentDescription = out }) { Text("−") }
                TextButton(onClick = { zoom = (zoom + .5f).coerceAtMost(4f) },
                    modifier = Modifier.semantics { contentDescription = into }) { Text("+") }
            }
            if (failed) {
                Text(stringResource(R.string.document_error), Modifier.padding(16.dp))
                TextButton(onClick = { retry++ }) { Text(stringResource(R.string.document_retry)) }
            } else bitmap?.let { image ->
                val height = width.toFloat() * image.height / image.width
                val transform = rememberTransformableState { factor, pan, _ ->
                    zoom = (zoom * factor).coerceIn(1f, 4f)
                    val next = offset + pan
                    offset = Offset(next.x.coerceIn(-width * (zoom - 1) / 2, width * (zoom - 1) / 2),
                        next.y.coerceIn(-height * (zoom - 1) / 2, height * (zoom - 1) / 2))
                }
                Image(image.asImageBitmap(), label, Modifier.fillMaxWidth()
                    .aspectRatio(image.width.toFloat() / image.height)
                    .transformable(transform, canPan = { zoom > 1f })
                    .semantics { stateDescription = "${(renderedZoom * 100).toInt()}%" })
            } ?: Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
