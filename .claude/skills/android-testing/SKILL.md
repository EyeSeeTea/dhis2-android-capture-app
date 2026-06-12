---
name: android-testing
description: >
  Skill for writing Android tests. Covers unit tests (JUnit + MockK/Mockito),
  Compose UI tests, Espresso tests, and Flow testing with Turbine.
  Trigger on: test, unit test, UI test, integration test, or testing requests.
---

# Android Testing

## Unit Tests

Located in `<module>/src/test/`. Run with:
```bash
./gradlew :app:testDhis2DebugUnitTest   # app module, dhis2 flavor
./gradlew :commons:test                   # commons module
./gradlew test                            # all modules
```

### Pattern
```kotlin
class GetFeatureDataUseCaseTest {

    private val repository: FeatureRepository = mock()
    private val useCase = GetFeatureDataUseCase(repository)

    @Test
    fun `should return data when repository succeeds`() = runTest {
        // Given
        val expected = listOf(Item("1"), Item("2"))
        whenever(repository.getData()).thenReturn(Result.success(expected))

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `should return error when repository fails`() = runTest {
        // Given
        whenever(repository.getData()).thenReturn(Result.failure(Exception("Network error")))

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
    }
}
```

## Flow Testing with Turbine

```kotlin
@Test
fun `should emit loading then content`() = runTest {
    viewModel.uiState.test {
        assertEquals(FeatureUiState.Loading, awaitItem())
        assertEquals(FeatureUiState.Content(expectedData), awaitItem())
        cancelAndConsumeRemainingEvents()
    }
}
```

## Compose UI Tests

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun `should display items when content state`() {
    composeTestRule.setContent {
        FeatureContent(uiState = FeatureUiState.Content(testItems))
    }

    composeTestRule
        .onNodeWithText("Item 1")
        .assertIsDisplayed()
}
```

## Espresso Tests (View-based UI)

Located in `<module>/src/androidTest/`. Run with:
```bash
./gradlew :app:connectedDhis2DebugAndroidTest
```

```kotlin
@Test
fun shouldDisplayLoginScreen() {
    onView(withId(R.id.login_button))
        .check(matches(isDisplayed()))

    onView(withId(R.id.server_url))
        .perform(typeText("https://play.dhis2.org"))

    onView(withId(R.id.login_button))
        .perform(click())
}
```

## Test Naming Convention
- File: `<ClassName>Test.kt`
- Methods: backtick descriptive names: `` `should do X when Y` ``

## Mocking
- **Mockito** (existing codebase) or **MockK** (preferred for new Kotlin tests)
- Never mock data classes — create real instances
- Mock interfaces and abstract classes, not concrete implementations
