# Review checklist — dhis2-android

Verify before committing or marking a task done. Applies to any task that adds or modifies Kotlin/Java code in `app/`, `commons/`, `form/`, `tracker/`, or any other module.

---

## Architecture

- [ ] New code lives in the correct layer: entities, value objects and repository interfaces in domain packages; implementations, SDK access and mappers in data packages; Activities/Fragments/ViewModels/Composables in presentation
- [ ] Domain code has zero Android/framework imports — no `android.*`, no `androidx.*`, no `org.hisp.dhis.android.core.*`
- [ ] If a new repository is added, both the interface (domain) **and** the implementation (data) exist
- [ ] Repository interfaces express domain capabilities — method names reflect what the domain needs, not how the SDK stores it
- [ ] Repositories do not call other repositories — if multiple data sources are needed, the use case coordinates them
- [ ] Repositories treat data as a collection — no business logic, no calculated fields inside the repository
- [ ] Repositories return domain entities, not primitives or SDK types
- [ ] Each repository defines only the methods its use cases need — no generic `Repository<T>` base interface
- [ ] Use cases expose only one public method: `operator fun invoke()` or `execute()` — shared logic belongs in entity methods or helpers, not in a second public method
- [ ] Use cases do not call other use cases — shared logic belongs in domain entities, value objects, or repositories
- [ ] Independent domain entities relate by id (`String` uid), not by object reference — if a use case needs related data, it fetches it through its own repository
- [ ] Values with domain meaning or constraints are modeled as entities or value objects (`data class`, `value class`, `enum class`) — not passed as raw `String`/`Int` between layers
- [ ] Entities contain business logic methods — they are not plain data containers; rules like `enrollment.isActive()` live in the entity, not in the use case or ViewModel
- [ ] No business logic in Activities, Fragments, or Composables — presentation collects state and delegates; ViewModels orchestrate use cases

## Dependency Injection

- [ ] New features wire dependencies with Koin modules (e.g. `app/src/main/java/org/dhis2/di/`, per-feature `di/` packages) — Dagger 2 only when extending existing components in `app/src/main/java/org/dhis2/data/`
- [ ] Dependencies are constructor-injected — no instantiation of repositories or use cases inside ViewModels, Activities, or Composables
- [ ] Koin modules provide use cases and repository interfaces — presentation never resolves a repository implementation directly
- [ ] No new Dagger components/modules for new features — register a Koin module in `KoinInitialization` instead

## Coroutines / Flow

- [ ] New async code uses coroutines + `Flow`/`suspend` — RxJava only when extending an existing Rx chain
- [ ] Rx and coroutines are not mixed within the same feature
- [ ] No blocking SDK calls (`blockingGet()`, `blockingFirst()`, `blockingCount()`) on the main thread — use `suspend` functions on `Dispatchers.IO` or reactive queries
- [ ] Repository methods expose `Flow<T>` or `suspend fun` — not raw callbacks or `LiveData`
- [ ] Coroutines launch in the right scope (`viewModelScope` in ViewModels) — no `GlobalScope`

## DHIS2 Android SDK (d2)

- [ ] `D2` / SDK access happens only inside repository implementations — never from ViewModels, Activities, Fragments, or Composables
- [ ] No N+1 per-item blocking queries inside `.map` loops (e.g. `items.map { d2.…uid(it.uid).blockingGet() }`) — fetch related collections in bulk and join in memory; this repo has real ANR/OOM incidents from this pattern
- [ ] SDK collection queries are filtered at the SDK level (`byProgram()`, `byOrganisationUnit()`, …) — not loaded entirely and filtered in memory
- [ ] Repositories map SDK models to domain entities — SDK types do not leak into domain or presentation

## Compose / Android

- [ ] UI state is modeled as `sealed interface` with `Loading`, `Content`, `Error` variants
- [ ] ViewModel exposes `StateFlow`; Compose collects with `collectAsStateWithLifecycle()`
- [ ] New composables have `@Preview` functions
- [ ] All user-facing strings live in `strings.xml` — never hardcoded
- [ ] Interactive elements have 48dp minimum touch targets and content descriptions
- [ ] Composables contain only render logic and event forwarding — no business logic, no SDK access
- [ ] New screens use Compose — XML/Data Binding only when modifying existing View-based screens

## Tests

- [ ] Use case tests use test doubles (MockK/Mockito) of repository interfaces — not real implementations or the SDK
- [ ] Domain and use case tests do not import from data packages or Android framework
- [ ] If the change affects observable behavior, a test covering that behavior is added or updated — not just existing tests re-run
- [ ] Assertions check concrete values — no `assertNotNull(x)` or `assertTrue(result != null)` as the only assertion
- [ ] Test names are descriptive backtick names: `` `should do X when Y` ``
- [ ] `Flow` emissions are tested with Turbine
- [ ] Tests pass (`./gradlew test`) before closing the task

## Functional Programming

- [ ] No `for` / `forEach` loops with a mutable accumulator — use `map`, `flatMap`, `filter`, or `fold` instead
- [ ] No in-place mutation (`add()`, `removeAt()`) — return new lists instead
- [ ] Searching a collection uses `find` / `first` / `filter` — not a loop with a `break`
- [ ] Function arguments are not mutated — transformations return new values
- [ ] `val` and immutable `List`/`Map` by default — `var` and `MutableList` only when genuinely required

## Kotlin

- [ ] No `!!` operator unless absolutely justified with a comment
- [ ] `when` over sealed types is exhaustive — no `else` branch that hides new variants
- [ ] State and errors are modeled with `sealed class`/`sealed interface`; data is immutable `data class`
- [ ] Utility operations on existing types use extension functions, not helper classes
- [ ] New code is Kotlin — Java only when modifying existing Java files

---
