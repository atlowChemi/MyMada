# Overlay removal target in Hebrew

The removal target is a directionless icon. Its wrap-content RelativeLayout
inherited RTL and measured the icon outside its own window (retained probe:
child x=-604..-446 in a210px root). Set only that container to LTR before attaching
it; retain the existing real window enlargement, hit region and drag lifecycle.

Evidence: `parallel-visual-audit/overlay-he-fix-20261102T085832Z/`.
Two focused EN/HE API37 runs pass: actual child containment at normal/highlight
size, window enlargement, repeated MOVE, leave/reenter, CANCEL, next DOWN and UP
removal. All four normal/highlight images were opened; the Hebrew ring is visible.
Background Info content differs by locale and is excluded from overlay comparison.
Debug/test assembly, release assembly and both lints pass; release/lint output is
retained in `parallel-visual-audit/overlay-he-release-lints.log`.

The first EN attempt is preserved in `overlay-he-fix-20261102T085650Z/`:
accessibility returned the previous bubble position before WindowManager's move.
The test now waits for the snapped bubble before comparing repeated MOVE bounds.
It also inspects actual child-local bounds, since accessibility clipping alone
can hide an off-root child. No global RTL, child scaling or new offsets were added.

Frozen APKs, hashes, app/config/channel backups, snapshot and restoration receipts
remain in each evidence directory. No native same-HE pixel identity is claimed;
the demonstrated missing target is corrected and its full gesture path exercised.
