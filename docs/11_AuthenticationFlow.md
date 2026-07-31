# Authentication Flow
## Smart Dairy Farm Management System

---

## Flow Diagram

```
App Launch
    │
    ▼
SplashScreen (1.5s animation)
    │
    ├── [Session exists + valid] ──────────────────────► MainNavGraph → Dashboard
    │
    └── [No session / expired] ────────────────────────► LoginScreen
                                                              │
                              ┌───────────────────────────────┼───────────────────────┐
                              │                               │                       │
                              ▼                               ▼                       ▼
                      Phone OTP Flow                 Email Login Flow          Register Flow
                              │                               │                       │
                     Enter Phone Number              Enter Email + Password    Enter Name + Phone
                              │                               │                       │
                     Tap "Get OTP"                   Tap "Login"               + Email (optional)
                              │                               │                + Password
                     OTP sent via SMS                Supabase validates             │
                              │                               │                 "Create Account"
                     OtpVerificationScreen           ┌────────┴──────┐               │
                              │                      │               │         Supabase creates user
                     Enter 6-digit OTP              Success        Error             │
                              │                      │               │         OTP sent to phone
                     Auto-verify on last digit       │          Show error      (verify phone)
                              │                      │               │               │
                      Supabase verifies OTP          │         Retry / Reset         │
                              │                      │                               │
                      ┌───────┴──────┐               │                               │
                      │             │                │                               │
                    Valid         Invalid           ─┘                              ─┘
                      │             │                           All success paths
                      │          Show error                           │
                      │          Resend timer                         ▼
                      │                                    [First login ever?]
                      │                                         │         │
                      │                                        YES        NO
                      │                                         │         │
                      └─────────────────────────────────────►  ▼         │
                                                         FarmSetupScreen  │
                                                              │            │
                                                         Fill farm details │
                                                              │            │
                                                         "Start Managing" │
                                                              │            │
                                                         Farm created      │
                                                              └────────────┘
                                                                    │
                                                                    ▼
                                                              Dashboard (MainNavGraph)
```

---

## Session Management

```kotlin
// AuthRepository.kt
interface AuthRepository {
    fun getCurrentSession(): Flow<AuthSession?>
    suspend fun loginWithPhone(phone: String): Result<Unit>
    suspend fun verifyOtp(phone: String, otp: String): Result<AuthSession>
    suspend fun loginWithEmail(email: String, password: String): Result<AuthSession>
    suspend fun register(name: String, phone: String, email: String?, password: String): Result<AuthSession>
    suspend fun logout(): Result<Unit>
    suspend fun isFirstLogin(): Boolean
}

// AuthRepositoryImpl.kt
class AuthRepositoryImpl @Inject constructor(
    private val supabaseAuth: GoTrue,
    private val userPrefs: UserPreferencesDataStore
) : AuthRepository {

    override fun getCurrentSession(): Flow<AuthSession?> =
        supabaseAuth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session
                else -> null
            }
        }

    override suspend fun loginWithPhone(phone: String): Result<Unit> = runCatching {
        supabaseAuth.signInWith(OTP) {
            this.phone = phone
        }
    }

    override suspend fun verifyOtp(phone: String, otp: String): Result<AuthSession> = runCatching {
        supabaseAuth.verifyPhoneOtp(type = OtpType.Phone.SMS, phone = phone, token = otp)
        supabaseAuth.currentSessionOrNull() ?: error("Session not found after OTP verification")
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<AuthSession> = runCatching {
        supabaseAuth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        supabaseAuth.currentSessionOrNull() ?: error("Session not found")
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        supabaseAuth.signOut()
        userPrefs.clearActiveFarm()
    }

    override suspend fun isFirstLogin(): Boolean =
        userPrefs.getActiveFarmId() == null
}
```

---

## Auth Screens

### LoginScreen.kt
```kotlin
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToRegister: () -> Unit,
    onNavigateToOtp: (String) -> Unit,
    onLoginSuccess: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showEmailLogin by remember { mutableStateOf(false) }

    val uiState by viewModel.authUiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(painter = painterResource(R.drawable.ic_cow_logo),
                contentDescription = null, modifier = Modifier.size(80.dp))
            
            Spacer(Modifier.height(8.dp))
            
            Text("Smart Dairy Farm",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
            
            Text("Manage your herd with ease",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(Modifier.height(40.dp))

            if (!showEmailLogin) {
                // Phone OTP flow
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    prefix = { Text("+91") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.sendOtp("+91$phone")
                        onNavigateToOtp("+91$phone")
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = phone.length >= 10 && !uiState.isLoading
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(Modifier.size(20.dp))
                    else Text("Get OTP", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { showEmailLogin = true }) {
                    Text("Login with Email instead")
                }
            } else {
                // Email flow (similar structure)
            }

            Spacer(Modifier.height(24.dp))
            Row {
                Text("New farmer? ")
                TextButton(onClick = onNavigateToRegister) { Text("Register here") }
            }
        }
    }
}
```

### OtpVerificationScreen.kt
```kotlin
@Composable
fun OtpVerificationScreen(
    phone: String,
    viewModel: AuthViewModel = hiltViewModel(),
    onVerified: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var resendTimer by remember { mutableStateOf(60) }
    val uiState by viewModel.authUiState.collectAsStateWithLifecycle()

    // Countdown timer
    LaunchedEffect(Unit) {
        while (resendTimer > 0) {
            delay(1000)
            resendTimer--
        }
    }

    // Auto-verify when 6 digits entered
    LaunchedEffect(otp) {
        if (otp.length == 6) {
            viewModel.verifyOtp(phone, otp)
        }
    }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onVerified()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter OTP", style = MaterialTheme.typography.headlineMedium)
        Text("Sent to $phone", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(32.dp))

        // 6-digit OTP boxes
        OtpInputField(
            otpLength = 6,
            value = otp,
            onValueChange = { otp = it }
        )

        Spacer(Modifier.height(24.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.error?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        if (resendTimer > 0) {
            Text("Resend OTP in ${resendTimer}s",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            TextButton(onClick = { viewModel.sendOtp(phone); resendTimer = 60 }) {
                Text("Resend OTP")
            }
        }
    }
}
```

---

## AuthViewModel.kt

```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginWithPhoneUseCase: LoginWithPhoneUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val registerUseCase: RegisterUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val isFirstLoginUseCase: IsFirstLoginUseCase
) : ViewModel() {

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    fun sendOtp(phone: String) {
        viewModelScope.launch {
            _authUiState.update { it.copy(isLoading = true, error = null) }
            loginWithPhoneUseCase(phone)
                .onSuccess { _authUiState.update { it.copy(isLoading = false) } }
                .onFailure { e -> _authUiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            _authUiState.update { it.copy(isLoading = true) }
            verifyOtpUseCase(phone, otp)
                .onSuccess { session ->
                    val isFirst = isFirstLoginUseCase()
                    _authUiState.update { it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        isFirstLogin = isFirst
                    )}
                }
                .onFailure { e -> _authUiState.update { it.copy(isLoading = false, error = "Invalid OTP. Try again.") } }
        }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isFirstLogin: Boolean = false,
    val error: String? = null
)
```

---

## NavGraph Auth Guard

```kotlin
// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartDairyTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Screen.Splash.route) {
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            onAuthReady = { isAuthenticated, isFirstLogin ->
                                when {
                                    !isAuthenticated -> navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                    isFirstLogin -> navController.navigate(Screen.FarmSetup.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                    else -> navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                    authNavGraph(navController)
                    mainNavGraph(navController)
                }
            }
        }
    }
}
```

---

## Token Storage

```kotlin
// Tokens stored in EncryptedSharedPreferences (not plain SharedPreferences)
@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "user_secure_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getActiveFarmId(): String? = encryptedPrefs.getString("active_farm_id", null)
    fun setActiveFarmId(id: String) = encryptedPrefs.edit().putString("active_farm_id", id).apply()
    fun clearActiveFarm() = encryptedPrefs.edit().remove("active_farm_id").apply()
}
```
