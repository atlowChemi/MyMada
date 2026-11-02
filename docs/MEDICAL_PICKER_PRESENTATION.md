# Medical-code picker presentation

Source `b9ba858a` restores native dialog width/insets, catalog density and RTL
action placement. Filtering, selection, catalog order and returned values remain
unchanged. Evidence: `parallel-visual-audit/picker-density/INDEX.md`.

Four initial EN/HE font1/1.5 tests and two final Hebrew action-direction checks
pass. Independent review opened all four applicable matched catalog pairs:
940px modal, ten complete codes at normal font and eight at large font, with
complete search/title/actions. EN pairs bind the first artifact; final Hebrew
pairs bind APK `509c1403af0fa4f703158f5e9cdac9d7294429aa52931a561175dd7c73904d59`.
Search/clear/no-match/returned selection, reverse selection order and recreation
also pass. Device APK/data/config/isolation restored. This evidence predates
the separate shared native-field follow-up; that integration needs its own check.
