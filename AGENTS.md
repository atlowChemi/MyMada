# Repository workflow

- Use the [GitHub project's execution order](https://github.com/users/atlowChemi/projects/1)
  and issue prerequisites. The project is the roadmap; do not create a duplicate
  roadmap issue or silently skip prerequisites.
- Work in small, coherent PRs and ask the maintainer before merging. Update issue
  checklists from verified evidence; use `Fixes #N` only when that issue's scope
  is complete. Partial slices use `Refs #N`.
- Push and open dependent PRs when authorized; merge approval is separate.
  Verify published-production parity per migrated screen (#35), not as a global
  implementation pause. Unresolved parity blocks the affected slice's acceptance.
- Preserve every existing feature family. When native and RN behavior disagree,
  use the assessment in #19 and ask about unresolved product behavior rather than
  treating a missing feature or an implementation bug as the specification.
- Add focused local tests and update documentation alongside each migration.
  CI (#13) follows working local compatibility/Kotlin migration; do not introduce
  it earlier merely to automate an incomplete baseline.
- Do not add Python tooling to this repository. Prefer Android/Kotlin tests and
  standard Android SDK/NDK tools. Maintained application code is moving to Kotlin;
  Java interoperability is a temporary migration bridge, not a new-code default.
