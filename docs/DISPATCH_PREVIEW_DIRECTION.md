# Full-width, content-directed dispatch preview

The shared summary Text now fills its content width and resolves paragraph
direction from content. Hebrew paragraphs align right and English paragraphs
align left, including mixed Hebrew-role/Latin-name lines. SMS body, count,
destination, punctuation, native medical-code order and full Team region are
unchanged. Source handoff: `fff4f796`.

Evidence: `parallel-visual-audit/dispatch-paragraph-runtime/HANDOFF.md`.
Final main SHA256: `220e68d01fed72dd4168fb22863ead282966baded80c2a40f0baa16e59545fec`.
Four focused EN/HE actual-font1/1.3 runs pass width/direction, complete action
bounds, literal body/count, recreation and intent checks with zero SMS launches.
Independent review opened all12 final captures and two native/final references.
The previous renderer fails the paragraph-direction assertion. Native reference
backgrounds do not establish editor acceptance. Device APK/data/config/isolation
was restored; no send/delivery result is inferred from preview checks.
