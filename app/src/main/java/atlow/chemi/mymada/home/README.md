# Home platform boundary

`HomePage` retains the installed launcher, channel IDs/groups/importance, onboarding
order and destinations. `HomeActions` owns navigation, test-call broadcasts and ad
continuations; load callbacks retain only weak owners and skip destroyed Activities.
Test/contact/debug-dispatch dialogs restore through FragmentManager. Compose owns
the published artwork/layout and accessible actions. Detection controls are shared
with Settings/privacy rather than copied; the existing listener/accessibility
fallback remains until current-target service/permission migration in #24.

`HomeScreenTest` covers navigation, back, onboarding and action-dialog recreation,
locale return, large text and repeated dialogs. `HomeMemoryTest` checks destroyed
Activity collection. Live ads/backend, current-target permission behavior and final
published screenshot approval are separate acceptance checks, not implied by offline
tests. Current-target UI still needs the delegated API37 runtime verification.
