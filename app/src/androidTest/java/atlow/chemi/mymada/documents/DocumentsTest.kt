package atlow.chemi.mymada.documents

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.selectShellDestination
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.IOException

/** Run only on a disposable, firewalled emulator. Downloads are local fixtures, never backend writes. */
class DocumentsTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    private fun prepare(language: String = "en") {
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
        context.getSharedPreferences("Settings", 0).edit().putString("lng", language)
            .putBoolean("Mapp", false).putBoolean("WasLangChanged", false)
            .putStringSet("Tools", setOf("metro", "prot")).commit()
    }

    private fun fixture(file: File, pages: Int = 3) {
        file.parentFile!!.mkdirs()
        val pdf = PdfDocument()
        try {
            repeat(pages) { index ->
                val page = pdf.startPage(PdfDocument.PageInfo.Builder(300, 400, index + 1).create())
                page.canvas.drawColor(Color.WHITE)
                page.canvas.drawRect(20f, 20f, 280f, 200f, Paint().apply { color = Color.rgb(180, 30 + index * 40, 30) })
                page.canvas.drawText("Document fixture ${index + 1}", 20f, 240f, Paint().apply { textSize = 20f })
                pdf.finishPage(page)
            }
            file.outputStream().use { pdf.writeTo(it) }
        } finally {
            pdf.close()
        }
    }

    @Test fun cacheDownloadCorruptionCancellationAndRefreshAreAtomic(): Unit = runBlocking {
        val root = File(context.cacheDir, "documents-store-test").apply { deleteRecursively(); mkdirs() }
        val store = DocumentStore(root)
        for (kind in DocumentKind.entries) {
            val loaded = store.load(kind) { name, output ->
                assertEquals(kind.objectName, name)
                fixture(output)
            }
            assertFalse(loaded.cached)
            assertEquals(3, loaded.pageCount)
            val original = loaded.file.readBytes()
            assertTrue(store.load(kind) { _, _ -> fail("Offline cache must not access the network") }.cached)
            for (cancel in listOf(false, true)) {
                try {
                    store.load(kind, true) { _, output ->
                        output.writeText("partial PDF")
                        if (cancel) throw CancellationException("left screen") else throw IOException("offline")
                    }
                    fail("Failed download must propagate")
                } catch (_: IOException) { } catch (_: CancellationException) { }
                assertArrayEquals(original, loaded.file.readBytes())
                assertFalse(root.listFiles()!!.any { it.name.startsWith("download-") })
            }
            try {
                store.load(kind, true) { _, output -> output.writeText("not a PDF") }
                fail("Invalid replacement must fail")
            } catch (_: IOException) { }
            assertArrayEquals(original, loaded.file.readBytes())
            loaded.file.writeText("corrupt old copy")
            assertEquals(2, store.load(kind) { _, output -> fixture(output, 2) }.pageCount)
        }
        root.deleteRecursively()
        Unit
    }

    @Test fun loadingErrorRetryRefreshOfflineAndPageNavigation() {
        prepare()
        val pending = CompletableDeferred<StoredDocument>()
        val file = File(context.cacheDir, "documents-ui.pdf").also { fixture(it) }
        var calls = 0
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            compose.waitForIdle()
            scenario.onActivity { activity ->
                activity.setContent {
                    DocumentsScreen(DocumentKind.BLS) {
                        when (++calls) {
                            1 -> pending.await()
                            2 -> StoredDocument(file, 3, true)
                            else -> throw IOException("offline refresh")
                        }
                    }
                }
            }
            compose.onNodeWithTag("documentLoading").assertIsDisplayed()
            screenshot("fixture-loading")
            pending.completeExceptionally(IOException("offline first load"))
            compose.waitUntil(5_000) { compose.onAllNodesWithTag("documentError").fetchSemanticsNodes().isNotEmpty() }
            screenshot("fixture-error")
            compose.onNodeWithText("Retry").performClick()
            compose.onNodeWithText("Saved copy · available offline").assertIsDisplayed()
            compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Page 1 of 3").fetchSemanticsNodes().isNotEmpty() }
            compose.onAllNodesWithContentDescription("Zoom in")[0].performClick()
            compose.onAllNodesWithContentDescription("Zoom out")[0].performClick()
            compose.onNodeWithTag("documentPages").performScrollToIndex(2)
            compose.onNodeWithText("Page 3 of 3").assertIsDisplayed()
            screenshot("fixture-offline-page3")
            compose.onNodeWithText("Refresh").performClick()
            compose.onNodeWithTag("documentError").assertIsDisplayed()
            compose.onNodeWithText("Page 3 of 3").assertIsDisplayed()
            assertEquals(3, calls)
        }
    }

    @Test fun allDrawerRoutesRecreationAndBackInBothLocales() {
        val root = File(context.filesDir, "documents")
        DocumentKind.entries.forEachIndexed { index, kind -> fixture(File(root, kind.objectName), 3 + index) }
        for (language in listOf("en", "he")) {
            prepare(language)
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                for ((index, route) in listOf(R.id.bls, R.id.als, R.id.death).withIndex()) {
                    compose.selectShellDestination(route)
                    val label = if (language == "en") "Page 1 of ${3 + index}" else "עמוד 1 מתוך ${3 + index}"
                    compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription(label).fetchSemanticsNodes().isNotEmpty() }
                    screenshot("fixture-$language-${DocumentKind.entries[index]}")
                    scenario.recreate()
                    compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription(label).fetchSemanticsNodes().isNotEmpty() }
                    scenario.onActivity {
                        assertEquals(if (language == "he") 1 else 0, it.resources.configuration.layoutDirection)
                    }
                }
                compose.waitUntil(5_000) {
                    var focused = false
                    scenario.onActivity { focused = it.hasWindowFocus() }
                    focused
                }
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                compose.waitUntil(5_000) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
            }
        }
    }

    @Test fun existingProductionDocumentsRenderFirstMiddleLastAndOffline() {
        // Host-fetched read-only Storage objects, supplied outside git. Missing inputs FAIL, never fake acceptance.
        val input = context.getExternalFilesDir("live-documents")!!
        val root = File(context.cacheDir, "documents-live-test").apply { deleteRecursively(); mkdirs() }
        for (kind in DocumentKind.entries) {
            val original = File(input, kind.objectName)
            assertTrue("Supply the actual Storage object: ${kind.objectName}", original.isFile)
            val loaded = runBlocking { DocumentStore(root).load(kind) { _, output -> original.copyTo(output, overwrite = true) } }
            assertTrue(loaded.pageCount > 0)
            for (page in setOf(0, loaded.pageCount / 2, loaded.pageCount - 1)) {
                val image = renderPage(loaded.file, page, 1080)
                assertTrue(image.width in 1..1440)
                assertTrue(image.height in 1..2048)
                try {
                    val pixels = IntArray(image.width)
                    val hasInk = (0 until image.height).any { y ->
                        image.getPixels(pixels, 0, image.width, 0, y, image.width, 1)
                        pixels.any { it != Color.WHITE }
                    }
                    assertTrue("Actual page must contain ink: $kind/$page", hasInk)
                    val evidence = File(context.getExternalFilesDir("documents-evidence"), "$kind-page-${page + 1}.png")
                    evidence.outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
                } finally { image.recycle() }
            }
            assertTrue(runBlocking { DocumentStore(root).load(kind) { _, _ -> fail("Offline must use saved copy") } }.cached)
            android.util.Log.i("DocumentsEvidence", "$kind bytes=${original.length()} pages=${loaded.pageCount}")
        }
        root.deleteRecursively()
    }

    @Test fun actualDocumentsOpenThroughDrawerOffline() {
        prepare()
        val root = File(context.filesDir, "documents").apply { mkdirs() }
        val counts = DocumentKind.entries.map { kind ->
            val source = File(context.getExternalFilesDir("live-documents"), kind.objectName)
            assertTrue("Actual document required", source.isFile)
            source.copyTo(File(root, kind.objectName), overwrite = true)
            pageCount(source)
        }
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            for ((index, route) in listOf(R.id.bls, R.id.als, R.id.death).withIndex()) {
                compose.selectShellDestination(route)
                val label = "Page 1 of ${counts[index]}"
                compose.waitUntil(30_000) { compose.onAllNodesWithContentDescription(label).fetchSemanticsNodes().isNotEmpty() }
                screenshot("actual-${DocumentKind.entries[index]}-offline")
            }
        }
    }

    @Test fun actualSmallTextZoomRerendersWithoutCoveringControls() {
        prepare()
        for ((kind, page) in listOf(DocumentKind.BLS to 68, DocumentKind.ALS to 145, DocumentKind.KUNTRES to 15)) {
            val file = File(context.getExternalFilesDir("live-documents"), kind.objectName)
            assertTrue("Actual document required", file.isFile)
            val count = pageCount(file)
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                compose.waitForIdle()
                scenario.onActivity { activity ->
                    activity.setContent { DocumentsScreen(kind) { StoredDocument(file, count, true) } }
                }
                compose.onNodeWithTag("documentPages").performScrollToIndex(page)
                val label = "Page ${page + 1} of $count"
                fun rendered(percent: String) = compose.onAllNodes(
                    hasContentDescription(label) and SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, percent)
                ).fetchSemanticsNodes().isNotEmpty()
                compose.waitUntil(30_000) { rendered("100%") }
                screenshot("$kind-small-text-1x")
                val captureGeometry = mutableListOf<String>()
                fun headerImage(): android.graphics.Bitmap {
                    compose.waitForIdle()
                    // Activity window transitions also move pixels, outside Compose's idling clock.
                    instrumentation.uiAutomation.waitForIdle(500, 5_000)
                    val bounds = compose.onNodeWithText(label).fetchSemanticsNode().boundsInWindow
                    val screen = instrumentation.uiAutomation.takeScreenshot()!!
                    scenario.onActivity {
                        val location = IntArray(2)
                        it.window.decorView.getLocationOnScreen(location)
                        captureGeometry += "bounds=$bounds window=${location.toList()} screen=${screen.width}x${screen.height}"
                    }
                    return try {
                        android.graphics.Bitmap.createBitmap(screen, bounds.left.toInt(), bounds.top.toInt(),
                            bounds.width.toInt(), bounds.height.toInt())
                    } finally { screen.recycle() }
                }
                val header = headerImage()
                repeat(6) { compose.onAllNodesWithContentDescription("Zoom in")[0].performClick() }
                compose.waitUntil(30_000) { rendered("400%") }
                val zoomedHeader = headerImage()
                screenshot("$kind-small-text-4x")
                if (!header.sameAs(zoomedHeader)) {
                    for ((phase, image) in listOf("before" to header, "after" to zoomedHeader)) {
                        File(context.getExternalFilesDir("documents-evidence"), "$kind-header-$phase.png")
                            .outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
                    }
                    File(context.getExternalFilesDir("documents-evidence"), "$kind-header-geometry.txt")
                        .writeText(captureGeometry.joinToString("\n"))
                }
                assertTrue("Zoom must not draw over its own controls", header.sameAs(zoomedHeader))
                header.recycle()
                zoomedHeader.recycle()
                compose.onNodeWithContentDescription(label).performTouchInput { swipeRight() }
                compose.waitForIdle()
                screenshot("$kind-small-text-4x-panned")
                repeat(6) { compose.onAllNodesWithContentDescription("Zoom out")[0].performClick() }
                compose.waitUntil(30_000) { rendered("100%") }
                // Raster allocation must not grow with zoom, and the crop must contain newly rendered detail.
                val normal = renderPage(file, page, 720)
                val zoomed = renderPage(file, page, 720, 4f)
                try {
                    assertEquals(normal.allocationByteCount, zoomed.allocationByteCount)
                    assertFalse(normal.sameAs(zoomed))
                } finally { normal.recycle(); zoomed.recycle() }
            }
        }
    }

    @Test fun realOfflineStorageFailureRetainsPublishedErrorArtwork() {
        File(context.filesDir, "documents").deleteRecursively()
        for (language in listOf("en", "he")) {
            prepare(language)
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                lateinit var activity: UtillitiesAndTools
                scenario.onActivity { activity = it }
                compose.selectShellDestination(R.id.bls)
                compose.waitUntil(60_000) { compose.onAllNodesWithTag("documentError").fetchSemanticsNodes().isNotEmpty() }
                // Wait for the local asset to render before capturing the production comparison.
                val painted = java.util.concurrent.atomic.AtomicBoolean(false)
                compose.waitUntil(10_000) {
                    // err.html continuously animates its ECG bar. API24 may never
                    // satisfy onActivity's main-queue idle wait while it is visible.
                    instrumentation.runOnMainSync {
                        findWeb(activity.window.decorView)?.let { web ->
                            web.evaluateJavascript("Boolean(document.getElementById('header'))") { present ->
                                if (present == "true") web.postVisualStateCallback(0,
                                    object : android.webkit.WebView.VisualStateCallback() {
                                        override fun onComplete(requestId: Long) { painted.set(true) }
                                    })
                            }
                        }
                    }
                    painted.get()
                }
                val image = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
                try {
                    File(context.getExternalFilesDir("documents-evidence"), "candidate-$language-bls-offline.png")
                        .outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
                } finally { image.recycle() }
            }
        }
    }

    private fun findWeb(view: android.view.View): android.webkit.WebView? {
        if (view is android.webkit.WebView) return view
        if (view is android.view.ViewGroup) for (index in 0 until view.childCount) {
            findWeb(view.getChildAt(index))?.let { return it }
        }
        return null
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
            File(context.getExternalFilesDir("documents-evidence"), "$name.png").outputStream().use {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            bitmap.recycle()
        }
    }
}
