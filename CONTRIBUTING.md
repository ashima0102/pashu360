# Contributing to Pashu360

## Branching & PR Workflow

**All changes must go through a pull request. Direct pushes to `main` are blocked.**

### Standard flow

```bash
# 1. Start from an up-to-date main
git checkout main
git pull

# 2. Create a branch for your work
git checkout -b feature/phase-2-milk-entry
# or fix/foo, docs/bar, chore/baz, refactor/qux

# 3. Make changes, commit
git add .
git commit -m "Add bulk milk entry screen"

# 4. Push and open a PR
git push -u origin feature/phase-2-milk-entry
gh pr create --title "Phase 2: Bulk milk entry" --body "..."

# 5. Merge via GitHub UI or gh CLI once ready
gh pr merge --squash --delete-branch
```

### Branch naming

| Prefix | Use for |
|---|---|
| `feature/` | New features or phases (e.g. `feature/phase-3-vaccination`) |
| `fix/` | Bug fixes (e.g. `fix/room-suspend-dao`) |
| `docs/` | Documentation only (e.g. `docs/contributing-workflow`) |
| `refactor/` | Refactors without behavior change |
| `chore/` | Build config, dependencies, tooling |

### Commit messages

- One-line summary at the top (imperative mood: "Add", "Fix", "Update")
- Optional blank line + body explaining *why*
- Group related changes into one commit — don't spam tiny commits

### PR title & description

- **Title:** brief and specific (e.g. "Phase 2: Bulk milk entry screen")
- **Description:** what changed, why, and how to test locally
- Link related issues if any

### Merging

- **Squash and merge** is the default — keeps main history linear
- **Rebase and merge** is fine for tiny PRs (1-2 commits)
- Avoid regular merge commits

### Branch protection

`main` is protected on GitHub:
- Direct pushes blocked
- PR required
- Force pushes disabled
- Branch deletion disabled

To change protection rules, use `gh api repos/ashima0102/pashu360/branches/main/protection`.

---

## Local development

### First-time setup

```bash
git clone https://github.com/ashima0102/pashu360.git
cd pashu360
# open in Android Studio → wait for Gradle sync
```

### Add Supabase credentials to `local.properties`

```
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
```

*`local.properties` is git-ignored — never commit it.*

### Building

- **Android Studio** — click ▶️ Play
- **CLI** — `./gradlew assembleDebug`

### Running tests

```bash
./gradlew testDebugUnitTest
```

---

## Architecture rules

See `docs/09_MVVMArchitecture.md` for the full spec. Key rules:

1. **Domain layer** has no Android imports — pure Kotlin
2. **ViewModels** never import Room or Supabase — go through UseCases/Repositories
3. **Composables** never make DB calls directly — always via ViewModel
4. **Repositories** own dispatcher choice (`withContext(Dispatchers.IO)` for DB work)
5. Every mutating DAO method returns `Long`/`Int` — never `Unit` (KSP2 bug workaround)

---

## Documentation to update on every change

| Change | Also update |
|---|---|
| New feature | `PROGRESS.md` + `CHANGELOG.md` |
| User-visible change | `CHANGELOG.md` |
| New user story completed | `docs/03_UserStories.md` (status marker) |
| New functional requirement met | `docs/04_FunctionalRequirements.md` |
| Architecture change | `docs/09_MVVMArchitecture.md` |

---

## Getting help

Open an issue: https://github.com/ashima0102/pashu360/issues
