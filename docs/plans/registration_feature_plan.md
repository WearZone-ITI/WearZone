# Registration Feature — Implementation Plan

**Owner:** Hend
**Screens:** Registration Screen
**Excluded:** Login, logout, password reset, any other auth screens

---

## Design Translation

### Screen 1 — Register (`stitch_luxecart_premium_shopify_commerce/screen.png`)

| Element | Compose spec |
|---|---|
| Background | `Color(0xFFF9F9F9)` Off-White |
| Heading "Create Account" | Playfair Display Bold, 28sp |
| Subtitle text | Inter Regular 16sp, `Color(0xFF46474A)` |
| Label caps (FULL NAME, etc.) | Inter SemiBold 12sp, `letterSpacing = 0.05.em`, uppercase |
| Input field (ghost style) | bg `Color(0xFFEFEDED)`, 8dp radius, no border at rest → 1.5dp `MidnightSlate` border on focus |
| Password visibility toggle | Eye / eye-off icon inside field |
| Password strength bar | 4 animated segments below PASSWORD field |
| Terms checkbox | Material3 `Checkbox` + `AnnotatedString` (underlined "Terms of Service" + "Privacy Policy") |
| CTA button "Create Account" | Midnight Slate `Color(0xFF1A1A1B)`, white text, 56dp height, 18dp radius, full width |
| Footer link "Log in" | `Color(0xFF1A1A1B)` underlined, navigate to Login (handled by login team) |

---

## ⚠️ CRITICAL SECURITY WARNING — Shopify Admin REST API

The Shopify Admin REST API (`POST /admin/api/latest/customers.json`) requires the Admin Access Token (`X-Shopify-Access-Token: shpat_...`).
This token grants **full store-admin scope** and **must never appear in the Android APK**.

Per `AGENTS.md` §0: The Admin API token lives **only on the backend/BFF**.

### Chosen approach: BFF Proxy

```
Android App
   │  POST /bff/customers  { name, email }
   ▼
BFF Server  (holds the Admin token server-side)
   │  POST /admin/api/latest/customers.json
   │  X-Shopify-Access-Token: shpat_...
   ▼
Shopify Admin REST API
```

`BFF_BASE_URL` stored in `local.properties` → exposed via `BuildConfig`.

Shopify customer body sent via BFF:
```json
{
  "customer": {
    "first_name": "Jane",
    "last_name": "Doe",
    "email": "jane@example.com",
    "verified_email": false,
    "send_email_welcome": false
  }
}
```

---

## Registration Flow

```
User fills form → SubmitRegister intent
       │
       ▼
RegisterUseCase  ← validates locally (name, email, password, confirm, terms)
       │
       ▼
IAuthRepository.register(name, email, password)
       │
       ├─ Step 1: Firebase Auth createUserWithEmailAndPassword()
       │          + updateProfile(displayName = name)
       │
       ├─ Step 2: BFF POST /customers  { name, email }
       │          → Shopify creates customer record
       │
       └─ Result<User> returned to ViewModel
              │
              ▼
    NavigateToHome effect / NavigateToLogin effect
```

---

## Proposed Changes

### 1 — Build Files

#### `gradle/libs.versions.toml`
Add (inside `#Hend` markers):
- `hilt-navigation-compose` v1.2.0
- `lifecycle-viewmodel-compose` v2.9.1
- `coil-compose` v3.1.0

#### `app/build.gradle.kts`
Add (inside `//Hend` markers):
- `kotlinx-coroutines-play-services`
- `hilt-navigation-compose`
- `lifecycle-viewmodel-compose`
- `coil-compose`

#### `presentation/build.gradle.kts`
Add Compose, Hilt, KSP, lifecycle plugins and dependencies.

---

### 2 — Theme & Design System (`presentation` module)

- `presentation/.../common/theme/AppColors.kt` — VogueVibe color tokens
- `presentation/.../common/theme/AppTypography.kt` — Playfair Display + Inter via GoogleFont
- `presentation/.../common/theme/AppShapes.kt` — 8dp inputs, 18dp buttons, 20dp cards
- `presentation/.../common/theme/AppTheme.kt` — `WearZoneTheme` composable
- `app/.../ui/theme/Theme.kt` — delegates to presentation theme

---

### 3 — Domain Layer (`domain` module)

- `auth/model/User.kt` — `data class User(uid, email, displayName)`
- `auth/repository/IAuthRepository.kt` — `register()`
- `auth/usecase/RegisterUseCase.kt` — validates input + calls repository
- `common/result/DataResult.kt` — `sealed class DataResult<T>`

---

### 4 — Data Layer (`data` module)

- `remote/api/CustomerApiService.kt` — Retrofit `@POST("customers")`
- `remote/dto/CreateCustomerRequestDto.kt` — request DTO
- `remote/dto/CreateCustomerResponseDto.kt` — response DTO
- `repository/AuthRepositoryImpl.kt` — Firebase Auth + BFF call

---

### 5 — Presentation Layer (`presentation` module)

#### Register Screen
- `RegisterUiState.kt` — `RegisterFormState` + `RegisterUiState` sealed interface
- `RegisterUiIntent.kt` — all user actions
- `RegisterUiEffect.kt` — `NavigateToLogin`, `ShowSnackbar`
- `RegisterViewModel.kt` — MVI ViewModel
- `RegisterScreen.kt` — top-level Composable
- `components/LuxeTextField.kt` — ghost-style input
- `components/PasswordStrengthBar.kt` — 4-segment animated bar
- `components/TermsCheckbox.kt` — checkbox with clickable annotated text

---

### 6 — Navigation (`app` module)

#### `Route.kt` (inside `// Hend` markers)
```kotlin
@Serializable data object RegisterRoute : Route
```

#### `NavGraph.kt` (inside `// Hend` markers)
- `composable<Route.RegisterRoute>` → `RegisterScreen`

---

### 7 — DI Wiring (`app` module)

- `NetworkModule.kt` — BFF Retrofit instance + `CustomerApiService` + `FirebaseAuth`
- `RepositoryModule.kt` — bind `AuthRepositoryImpl` to `IAuthRepository`
- `DispatcherModule.kt` — `@IoDispatcher`, `@DefaultDispatcher` qualifiers

---

## File Count: 34 files (new/modified)

---

## Verification Plan

### Build
```
./gradlew :app:assembleDebug
```

### Unit Tests
- `RegisterUseCaseTest` — all validation rules
- `RegisterViewModelTest` — intent → state transitions + effect emissions
- `AuthRepositoryImplTest` — Firebase + BFF success/failure paths

### Manual Checks
- Ghost field focus animation
- Password show/hide toggle
- Strength bar 4 segments
- Terms checkbox enables/disables CTA
- Loading indicator on submit
- Navigate to Login or home on success
