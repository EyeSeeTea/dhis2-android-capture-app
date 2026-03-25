---
name: android-compose
description: >
  Skill for building Android UI with Jetpack Compose. Covers composable patterns,
  state management, navigation, theming, and migration from XML Views.
  Trigger on: Compose screen, composable, UI component, Material 3, or
  View-to-Compose migration requests.
---

# Android Jetpack Compose Development

## Compose Screen Pattern

Every new screen in this project follows this structure:

```kotlin
// 1. UI State — sealed interface
sealed interface FeatureUiState {
    data object Loading : FeatureUiState
    data class Content(val data: List<Item>) : FeatureUiState
    data class Error(val message: String) : FeatureUiState
}

// 2. ViewModel — exposes StateFlow
class FeatureViewModel(
    private val useCase: GetFeatureDataUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<FeatureUiState>(FeatureUiState.Loading)
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            useCase()
                .onSuccess { _uiState.value = FeatureUiState.Content(it) }
                .onFailure { _uiState.value = FeatureUiState.Error(it.message ?: "Unknown error") }
        }
    }
}

// 3. Screen composable — collects state, delegates to content
@Composable
fun FeatureScreen(viewModel: FeatureViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureContent(uiState = uiState)
}

// 4. Content composable — stateless, previewable
@Composable
fun FeatureContent(uiState: FeatureUiState) {
    when (uiState) {
        is FeatureUiState.Loading -> LoadingIndicator()
        is FeatureUiState.Content -> { /* render content */ }
        is FeatureUiState.Error -> ErrorMessage(uiState.message)
    }
}

// 5. Preview
@Preview(showBackground = true)
@Composable
fun FeatureContentPreview() {
    Dhis2Theme {
        FeatureContent(uiState = FeatureUiState.Content(sampleData))
    }
}
```

## Theming

- Use the DHIS2 design system theme when available
- Fall back to Material 3 `MaterialTheme`
- Access colors: `MaterialTheme.colorScheme.primary`
- Access typography: `MaterialTheme.typography.bodyLarge`
- Support dark theme via `isSystemInDarkTheme()`

## Navigation

- Use Navigation Compose for new screen flows
- Define routes as sealed classes or string constants
- Pass arguments via type-safe navigation arguments

## Interop with Views

When embedding Compose in existing View-based screens:
```kotlin
// In Fragment/Activity
ComposeView(requireContext()).apply {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent {
        Dhis2Theme { MyComposable() }
    }
}
```

## Performance
- Use `remember` for expensive computations
- Use `derivedStateOf` for derived values
- Use `LazyColumn`/`LazyRow` for lists (never `Column` with `forEach` for large lists)
- Profile with Layout Inspector and recomposition counts
