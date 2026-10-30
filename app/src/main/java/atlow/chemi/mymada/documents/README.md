# Documents migration (#23)

## Contract traced

Native `b36f4a9c`, `fragments/BlsProtocoles.java`: three tokenized Firebase URLs,
wrapped in the HTTP Google Drive viewer; spinner, local `assets/err.html`, no
managed PDF cache. `UtillitiesAndTools` maps BLS/ALS/deceased drawer items to
`setCurrentPage(0/1/2)`; `Settings.Tools` key `prot` enables that family.

RN `be72a163`, `src/containers/Tools/DocsViewer.tsx`: Storage `getDownloadURL()`
for `bls[1].pdf`, `als[1].pdf`, `kuntres.pdf`, then `react-native-view-pdf`;
pending spinner and `ConnectionError`. No document-specific authentication,
database schema, upload, edit, share, or explicit persistent offline cache is
present in that screen. Existing Firebase project/bucket configuration is retained.
The pinned RN package is `react-native-view-pdf` 0.14.0, using AndroidPdfViewer
2.8.2. Its `PDFView.java` creates a temporary `pdfDocument*.pdf` in `cacheDir`,
deletes it on cleanup, starts at page zero, scrolls vertically with 10px spacing,
and disables annotation rendering by default. The app does not override those
settings. Its checked-in patch adds an Android namespace, not a cache/auth change.

`BlsProtocoles.kt` keeps the drawer/Fragment contract and saves the selected family.
Compose owns loading, retry, continuous page scrolling, zoom/pan and refresh.
The original local ECG error asset is hosted without network access, using the
selected app locale. A failed refresh keeps the displayed saved copy usable.

## Rendering choice

Use Android's maintained `android.graphics.pdf.PdfRenderer`, available since API 21
(app minimum 24). No third-party PDF engine or app-packaged PDF `.so` is required;
the OS supplies its page-size-compatible renderer, including actual 16-KB devices.
AndroidX PDF's newer-platform viewer is unnecessary for this API-24 contract.
Firebase Storage (existing BOM) is the **only added build dependency**. Unlike the
old external Drive viewer, document rendering needs neither a browser service nor
public bearer tokens in source. The shared local error HTML remains a WebView;
it is not used for PDFs.

Pages are rendered off-main-thread into bounded raster images, with only visible
lazy-list pages retained. Zoom is bounded at 4× and rerenders the visible PDF
region into the same bounded buffer instead of enlarging the original bitmap.
Rendering is serialized so superseded gesture requests cannot multiply bitmap
allocations. Searchable/selectable PDF text is not implemented. These were not app-level
controls in either compared source, but full text accessibility remains a review
limitation of raster rendering. Page/zoom controls have accessible labels.
On heaps of 64 MiB or less the raster ceiling is 720×1024; larger heaps use
1440×2048. This avoids retaining several full-resolution page buffers alongside
Compose/WebView on API 24's stock 48-MiB heap. The crop gains detail as the user
zooms; the raster ceiling still limits pixels across each displayed viewport.

## Persistence and failures

Validated downloads are atomically renamed into private `files/documents`, never
external/shared storage. Temporary or malformed downloads cannot replace a good
copy. Leaving a screen cancels its transfer; corruption triggers a fresh download.
Reopening uses the saved copy (explicitly labeled), including offline; **Refresh**
requests the latest object. Cache-first content is not claimed to be current:
the saved-copy label and refresh action remain visible. There is no TTL or backend
write. App data clearing removes the cache. This is additive offline behavior,
not a claim that RN had a persistent-cache guarantee.

## Reproducible checks

Build debug/test, optimized unsigned release, bundle and lint using README commands.
Run `atlow.chemi.mymada.documents.DocumentsTest` on a firewalled disposable emulator.
Disable window/transition/animator animations on that emulator for pixel comparisons;
activity transitions are outside Compose's idling clock. The small-text regression
checks actual pages at 1×/4×, unchanged header pixels, pan, and bounded allocation.
The actual-document test requires read-only downloaded Storage objects in
`/sdcard/Android/data/atlow.chemi.mymada.debug/files/live-documents/`; absence fails
explicitly. Do not commit PDFs, bearer tokens, or authenticated responses.

Tests cover all object names, valid download/cache, corrupt cache and replacement,
interrupted/cancelled refresh, loading/error/retry, offline rendering, page controls,
both locales, each drawer route and recreation/back. Actual-document checks render
first/middle/last pages of all three PDFs and verify nonblank output. This proves
renderer/file compatibility; it does not substitute for live Android SDK transport
or visual approval against published production 12822.

Persistent execution logs, actual PDF hashes, screenshots and acceptance gates:
`~/.local/share/opencode/evidence/documents/`. Consult that run's evidence summary
for exact revisions, device page size and results; do not infer runtime success
from a build or from the presence of tests.
