package atlow.chemi.mymada.documents

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.helpers.LocalHelper
import com.google.android.material.navigation.NavigationView
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
                    scenario.onActivity {
                        it.onNavigationItemSelected(it.findViewById<NavigationView>(R.id.navigation).menu.findItem(route))
                    }
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
                scenario.onActivity {
                    it.onNavigationItemSelected(it.findViewById<NavigationView>(R.id.navigation).menu.findItem(route))
                }
                val label = "Page 1 of ${counts[index]}"
                compose.waitUntil(30_000) { compose.onAllNodesWithContentDescription(label).fetchSemanticsNodes().isNotEmpty() }
                screenshot("actual-${DocumentKind.entries[index]}-offline")
            }
        }
    }

    @Test fun realOfflineStorageFailureRetainsPublishedErrorArtwork() {
        File(context.filesDir, "documents").deleteRecursively()
        for (language in listOf("en", "he")) {
            prepare(language)
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                scenario.onActivity {
                    it.onNavigationItemSelected(it.findViewById<NavigationView>(R.id.navigation).menu.findItem(R.id.bls))
                }
                compose.waitUntil(60_000) { compose.onAllNodesWithTag("documentError").fetchSemanticsNodes().isNotEmpty() }
                // Wait for the local asset to render before capturing the production comparison.
                val painted = java.util.concurrent.atomic.AtomicBoolean(false)
                compose.waitUntil(10_000) {
                    scenario.onActivity { activity ->
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
                screenshot("candidate-$language-bls-offline")
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
