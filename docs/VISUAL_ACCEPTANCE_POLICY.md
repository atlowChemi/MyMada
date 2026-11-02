# Visual acceptance policy

Maintainer approval, 2026-11-02, recorded in
[#35](https://github.com/atlowChemi/MyMada/issues/35#issuecomment-6571785809):

- Incidental1px placement differences are acceptable when content remains
  visible/readable and controls remain reachable and functional. Pixel identity
  is not required; do not add fixes or tests solely for these differences.
- Apply this practical criterion to small rounding differences without inventing
  a new numerical threshold.
- This does not approve clipping, missing controls, substantial layout changes,
  broken RTL, or unverified behavior.

Existing inspected Home Privacy/importance rows4/5 and Settings detection rows30/31
meet those conditions and are accepted within their recorded evidence scope.
The immediate priority is completing the fixed28 unresolved comparison/reference/
product-decision states before further defect implementation. Reuse existing
pairs awaiting decisions; uninspected states are not automatically defects.
