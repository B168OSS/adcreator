package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// THEME COLORS (Cinematic Premium Midnight Gold)
// ==========================================
val DarkBg = Color(0xFF0C0E17)
val CardBg = Color(0xFF161A29)
val PrimaryGold = Color(0xFFFFB300)
val AccentGold = Color(0xFFFFD54F)
val SecondaryBlue = Color(0xFF4AC2FF)
val WarningRed = Color(0xFFFF4B4B)
val LightText = Color(0xFFE3E8F3)
val MutedText = Color(0xFF909BB4)
val SuccessGreen = Color(0xFF3CD18A)
val BorderGray = Color(0xFF252D42)

// ==========================================
// MODELS & STATES
// ==========================================
enum class AppScreen {
    LANDING,
    AUTH,
    DASHBOARD,
    GENERATOR_RESULTS,
    ADMIN
}

enum class AuthTab {
    REGISTRATION,
    LOGIN,
    GUEST
}

// User registration model
data class UserAccount(
    val username: String,
    val email: String,
    val passwordPass: String,
    val ipAddress: String,
    val isAdmin: Boolean = false,
    val timestamp: String
)

// System log entry
data class SystemLog(
    val timestamp: String,
    val level: String,
    val message: String
)

// Model classification results
data class SmartModelAnalysis(
    val type: String,
    val score: Int,
    val confidence: String,
    val isNsfwSafe: Boolean
)

// Prompt output formats
data class PromptScene(
    val sceneNum: Int,
    val title: String,
    val imagePrompt: String,
    val videoPrompt: String,
    val caption: String,
    val voiceOverText: String,
    val affiliateLink: String
)

// Main Activity definition
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdCreatorAppTheme {
                val viewModel: AdCreatorViewModel = viewModel()
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

// Custom Material 3 Theme
@Composable
fun AdCreatorAppTheme(content: @Composable () -> Unit) {
    val darkColors = darkColorScheme(
        primary = PrimaryGold,
        onPrimary = Color.Black,
        secondary = SecondaryBlue,
        onSecondary = Color.Black,
        background = DarkBg,
        surface = CardBg,
        onBackground = LightText,
        onSurface = LightText,
        error = WarningRed
    )
    MaterialTheme(
        colorScheme = darkColors,
        typography = Typography(),
        content = content
    )
}

// ==========================================
// VIEWMODEL IMPLEMENTATION
// ==========================================
class AdCreatorViewModel : ViewModel() {
    // Basic States
    private val _currentScreen = MutableStateFlow(AppScreen.LANDING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _isGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    // Network & IP simulations (Fun tool for security checking!)
    private val _simulatedIp = MutableStateFlow("192.168.1.104")
    val simulatedIp: StateFlow<String> = _simulatedIp.asStateFlow()

    private val _isUsingPublicIp = MutableStateFlow(false)
    val isUsingPublicIp: StateFlow<Boolean> = _isUsingPublicIp.asStateFlow()

    // Registered users (Seed with a couple details)
    private val _registeredUsers = MutableStateFlow<List<UserAccount>>(
        listOf(
            UserAccount("creative_guru", "guru@gmail.com", "gurutrans1", "192.168.1.104", false, "2026-05-28 01:22:43"),
            UserAccount("admin_vip", "hijr.time+ads@gmail.com", "admin123", "127.0.0.1", true, "2026-05-28 00:01:05")
        )
    )
    val registeredUsers: StateFlow<List<UserAccount>> = _registeredUsers.asStateFlow()

    // System Logs
    private val _systemLogs = MutableStateFlow<List<SystemLog>>(
        listOf(
            SystemLog("02:40:11", "INFO", "AdCreator Core Engine V2.5 initialized gracefully."),
            SystemLog("02:41:05", "SECURITY", "IP binding manager configured: Local Intranet Mode.")
        )
    )
    val systemLogs: StateFlow<List<SystemLog>> = _systemLogs.asStateFlow()

    // Form parameter inputs
    var selectedProductName by mutableStateOf("Sandal Slide Kulit Premium")
    var marketplaceLink by mutableStateOf("https://shopee.co.id/product/sandal_slide_kulit_retro")
    var selectedMarketplace by mutableStateOf("Shopee")
    var affiliateIdInput by mutableStateOf("AFF99281")
    
    // Model Selection Simulation
    var uploadedModelName by mutableStateOf<String?>(null)
    var modelAnalysisResult by mutableStateOf<SmartModelAnalysis?>(null)
    var isModelVerificationChecked by mutableStateOf(false)

    // Advanced choices
    var visualStylePreferences by mutableStateOf("Cinematic")
    var shootingAnglePreferences by mutableStateOf("Wide Shot")
    var ttsVoicePreferences by mutableStateOf("TTS Wanita")
    var selectedVoIntonation by mutableStateOf("Elegan")
    
    // Generator Logic states
    private val _generationResult = MutableStateFlow<List<PromptScene>>(emptyList())
    val generationResult: StateFlow<List<PromptScene>> = _generationResult.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Guest limits
    private val _guestAttempts = MutableStateFlow(0)
    val guestAttempts: StateFlow<Int> = _guestAttempts.asStateFlow()

    var showGuestBlockAlert by mutableStateOf(false)

    private fun addLog(level: String, message: String) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeString = sdf.format(Date())
        _systemLogs.value = listOf(SystemLog(timeString, level, message)) + _systemLogs.value
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        addLog("NAV", "Navigated to screen: ${screen.name}")
    }

    fun toggleSimulatedNetwork() {
        if (_isUsingPublicIp.value) {
            _simulatedIp.value = "192.168.1.104"
            _isUsingPublicIp.value = false
            addLog("SECURITY", "Network switched to Registered Home Base (192.168.1.104).")
        } else {
            _simulatedIp.value = "103.42.235.87"
            _isUsingPublicIp.value = true
            addLog("SECURITY", "Network switched to Public Open IP (103.42.235.87). Target warning mode.")
        }
    }

    fun registerNewAccount(usernameInput: String, emailInput: String, pwdInput: String): String? {
        if (usernameInput.length < 4) return "Username minimal 4 karakter!"
        if (!emailInput.endsWith("@gmail.com")) return "Email harus berakhiran @gmail.com!"
        if (pwdInput.length < 5) return "Password minimal 5 karakter!"
        
        val isDuplicated = _registeredUsers.value.any { it.username.lowercase() == usernameInput.lowercase() }
        if (isDuplicated) return "Username sudah terdaftar! Gunakan yang lain."

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateTime = sdf.format(Date())
        
        val newUser = UserAccount(
            username = usernameInput,
            email = emailInput,
            passwordPass = pwdInput,
            ipAddress = _simulatedIp.value, // Bind current simulated IP
            isAdmin = false,
            timestamp = currentDateTime
        )
        
        _registeredUsers.value = _registeredUsers.value + newUser
        addLog("AUTH", "User registered successfully: $usernameInput (Bound to IP: ${newUser.ipAddress})")
        return null // Success
    }

    fun loginAccount(usernameInput: String, pwdInput: String): Pair<Boolean, String> {
        // Dev backdoor admin checker
        if (usernameInput == "/dev_admin_init" || pwdInput == "/dev_admin_init") {
            navigateTo(AppScreen.ADMIN)
            addLog("BACKDOOR", "Developer bypass initialized. Loading Administrator Panel.")
            return Pair(true, "ADMIN_BYPASS")
        }

        val user = _registeredUsers.value.find { it.username == usernameInput }
        if (user == null) {
            addLog("AUTH", "Login failed: Username '$usernameInput' not found.")
            return Pair(false, "Username tidak ditemukan.")
        }

        if (user.passwordPass != pwdInput) {
            addLog("AUTH", "Login failed for '$usernameInput': Incorrect Password.")
            return Pair(false, "Password salah!")
        }

        // IP Binding Validation
        if (user.ipAddress != _simulatedIp.value) {
            addLog("SECURITY", "BLOCK INTREGITY: Login rejected for '${user.username}'. Attempt IP '${_simulatedIp.value}' mismatch with Bound IP '${user.ipAddress}'")
            return Pair(false, "IP_BLOCK")
        }

        _currentUser.value = user
        _isGuest.value = false
        addLog("AUTH", "Login successful: ${user.username}. API KEY Gemini verified.")
        return Pair(true, "MEMBER")
    }

    fun enterAsGuest() {
        _currentUser.value = null
        _isGuest.value = true
        addLog("AUTH", "Entered as guest. Account limitations loaded.")
        navigateTo(AppScreen.DASHBOARD)
    }

    fun logout() {
        addLog("AUTH", "User ${_currentUser.value?.username ?: "Guest"} logged out.")
        _currentUser.value = null
        _isGuest.value = false
        navigateTo(AppScreen.LANDING)
    }

    // Interactive product upload modeling
    fun simulateProductPhotoUpload(product: String) {
        uploadedModelName = product
        val analysisTypes = listOf("3D Model Setup", "Kartun Kreatif", "Humanoid Realistic", "Manekin Etalase", "Stickman Minimal")
        val isNsfw = false // Ensure NSFW is automatically green for mock
        val confidenceRating = listOf("98.1% Excellent", "92.4% Perfect", "89.5% Confirmed")
        
        modelAnalysisResult = SmartModelAnalysis(
            type = analysisTypes.random(),
            score = (85..99).random(),
            confidence = confidenceRating.random(),
            isNsfwSafe = !isNsfw
        )
        addLog("ANALYSER", "Auto-Engine classified uploaded item '$product' as ${modelAnalysisResult?.type}")
    }

    fun removeUploadedPhoto() {
        uploadedModelName = null
        modelAnalysisResult = null
        isModelVerificationChecked = false
        addLog("ANALYSER", "Uploaded product visual removed.")
    }

    // Cinematic script processor
    fun executePromptGeneration(onComplete: () -> Unit) {
        if (_isGuest.value) {
            if (_guestAttempts.value >= 1) {
                showGuestBlockAlert = true
                addLog("LIMIT", "Guest blocked from generating. Limit of 1 attempt reached.")
                return
            }
        }

        _isGenerating.value = true
        addLog("AI_ENGINE", "Compiling parameters. Custom style: $visualStylePreferences, Angle: $shootingAnglePreferences")
        
        // Simulating processing delay coroutine style
        _generationResult.value = emptyList()
        _guestAttempts.value += 1

        val finalProductName = selectedProductName.ifBlank { "Sandal Slide Kulit Retro" }
        val finalAffiliateId = affiliateIdInput.ifBlank { "AFF_GLOBAL" }
        val marketDomain = when (selectedMarketplace) {
            "Tokopedia" -> "tokopedia.com"
            "TikTok Shop" -> "tiktok.com"
            else -> "shopee.co.id"
        }
        val targetAffiliateLink = "https://link.$marketDomain/affiliate/$finalProductName?ref=$finalAffiliateId"

        // Generate Cinematic script based on user style and inputs
        val mockData = mutableListOf<PromptScene>()
        val maxScenes = if (_isGuest.value) 1 else 5

        for (i in 1..maxScenes) {
            val imageP = when (visualStylePreferences) {
                "Studio Ghibli" -> "Style Studio Ghibli, hand-drawn pastel colors, nostalgic overhead lighting, beautiful fantasy glow showing $finalProductName with a mystical forest backdrop, $shootingAnglePreferences style, ultra-detailed."
                "Realistic" -> "Photoreal ultra-rich textures, dramatic split lighting, bokeh background focal focus on $finalProductName on sleek marble table, soft water droplets on physical surfaces, 8k Resolution, angle: $shootingAnglePreferences."
                "Review Produk Faceless" -> "Minimal aesthetic layout. Focusing closely on a pair of elegant model hands holding and adjusting $finalProductName. Pure studio background, clean lighting accents, cinematic depth, angle: $shootingAnglePreferences."
                "Review Produk di Etalase" -> "A glowing pristine glass shelf display of $finalProductName. No people. Soft warm spotlights from up top casting subtle shadows, geometric arrangement, modern boutique style, angle: $shootingAnglePreferences."
                else -> "Extreme Cinematic Cinematic golden hour film drama look. Foggy atmospheric backdrop, rich neon backlight edge glow on $finalProductName, 35mm lens anamorphic flare, professional studio presentation, angle: $shootingAnglePreferences."
            }

            val videoP = "Camera action: smooth slow-motion $shootingAnglePreferences camera panning around $finalProductName. Add physics properties: slight particle dust floating in golden light ray beams, cinematic shutter speed 1/50, cinematic frame tracking, 60fps."

            val (title, caption, vo) = getCinematicEpisodeData(i, finalProductName, targetAffiliateLink, ttsVoicePreferences, selectedVoIntonation)
            
            mockData.add(
                PromptScene(
                    sceneNum = i,
                    title = title,
                    imagePrompt = imageP,
                    videoPrompt = videoP,
                    caption = caption,
                    voiceOverText = vo,
                    affiliateLink = targetAffiliateLink
                )
            )
        }

        // Complete generating
        _generationResult.value = mockData
        _isGenerating.value = false
        addLog("AI_ENGINE", "Successfully composed 5-act cinematic flow in Indonesian. Target affiliates tagged.")
        onComplete()
    }

    private fun getCinematicEpisodeData(
        scene: Int,
        prod: String,
        link: String,
        voice: String,
        intonation: String
    ): Triple<String, String, String> {
        return when (scene) {
            1 -> Triple(
                "Adegan 1: The Viral Hook",
                "🔥 AWAS MENYESAL! Jangan beli jika kamu ga mau tampil kece instan. Sumpah ini produk parah banget kualitasnya! Cek link affiliate original di sini 👉 $link",
                "[$voice - Intonasi $intonation] (Mulai video dengan hentakan visual) Siapa sangka, barang sekecil ini bisa bikin gaya kamu meningkat drastis?!"
            )
            2 -> Triple(
                "Adegan 2: Contextual Build-up",
                "Selalu nemenin aktivitas malas maupun rajin kamu sepanjang hari. Nyaman parah ga ada tanding! 👉 Miliki sekarang: $link",
                "[$voice - Intonasi $intonation] Mau nongkrong, kerja, atau sekadar rebahan santai... Rasakan kenyamanan tak tertandingi sepanjang hari!"
            )
            3 -> Triple(
                "Adegan 3: Narrative Development",
                "Alasan kenapa produk ini wajib dicoba: pengerjaan premium mendetail, anti goyah, dan tentu super awet! 🔥 Cek Detail: $link",
                "[$voice - Intonasi $intonation] Tiap detailnya dibuat khusus dari bahan berkualitas tinggi, bebas lecet, elastis, dan pastinya awet jangka panjang."
            )
            4 -> Triple(
                "Adegan 4: Climax & Hero Reveal",
                "BOOM! Ini dia transformasi visual sesungguhnya. Desain minimalis kekinian yang lagi viral banget! 💎 Beli Sekarang: $link",
                "[$voice - Intonasi $intonation] (Visual transisi dramatis) Lihat kemewahan desainnya yang elegan! Bikin semua mata langsung melirik ke arahmu!"
            )
            else -> Triple(
                "Adegan 5: Emotional Resolution",
                "Spesial promo hari ini saja! Amankan ukuran dan warna favoritmu sebelum kehabisan slot diskon 👉 Klik link di bio / deskripsi! $link",
                "[$voice - Intonasi $intonation] Stok menipis cepat. Jangan nunggu harga normal! Klik tautan di bawah dan checkout sekarang juga sebelum kehabisan promo!"
            )
        }
    }
}

// ==========================================
// COMPOSABLE VISUAL ELEMENTS (UI LAYOUTS)
// ==========================================
@Composable
fun MainAppScaffold(viewModel: AdCreatorViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Scaffold(
        topBar = { AppGlobalHeader(viewModel) },
        bottomBar = { AppGlobalFooter(viewModel) },
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_root_scaffold"),
        containerColor = DarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentScreen,
                animationSpec = tween(durationMillis = 350)
            ) { screen ->
                when (screen) {
                    AppScreen.LANDING -> LandingScreen(viewModel)
                    AppScreen.AUTH -> AuthScreen(viewModel)
                    AppScreen.DASHBOARD -> DashboardFormScreen(viewModel)
                    AppScreen.GENERATOR_RESULTS -> ResultsScreen(viewModel)
                    AppScreen.ADMIN -> AdminDashboardScreen(viewModel)
                }
            }
        }
    }
}

// Global Custom App Bar (Translate Simulation)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppGlobalHeader(viewModel: AdCreatorViewModel) {
    Surface(
        color = CardBg,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_global_header")
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📽️ AdCreator",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pro",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Light,
                            color = LightText
                        )
                    )
                }
                Text(
                    text = "Affiliate Cinema Auto-Suite",
                    fontSize = 11.sp,
                    color = MutedText
                )
            }

            // Google Translate Tag Simulation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color(0xFF242C44))
                    .border(1.dp, BorderGray, RoundedCornerShape(30.dp))
                    .clickable { }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🌐 Translate: Auto (Google)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryBlue
                )
            }
        }
    }
}

// Global Footer matching strict guidelines
@Composable
fun AppGlobalFooter(viewModel: AdCreatorViewModel) {
    Surface(
        color = Color(0xFF080A10),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_global_footer")
            .navigationBarsPadding(),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "by Te_eR™ Inovative",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MutedText,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Divider(
                    modifier = Modifier
                        .height(12.dp)
                        .width(1.dp), color = BorderGray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        // Click mimics email sending intent
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Contact us",
                        tint = PrimaryGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Contact Us",
                        color = PrimaryGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 0: LANDING PAGE (STATE: WELCOME)
// ==========================================
@Composable
fun LandingScreen(viewModel: AdCreatorViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(DarkBg)
            .padding(16.dp)
            .testTag("landing_screen_container")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Hero Title Hook
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardBg,
            border = BorderStroke(1.dp, PrimaryGold.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryGold.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚡ ADCREATOR AFFILIATE SUITE",
                        color = PrimaryGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ubah Foto Produk Biasa Menjadi Iklan Sinematik Kelas Dunia!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        lineHeight = 32.sp,
                        textAlign = TextAlign.Center,
                        color = LightText
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Dapatkan konversi klik melimpah dengan formula naskah 5 babak linier terstruktur dan perintah visual (prompt) khusus untuk model AI favorit Anda secara instan.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedText,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Portfolio visual elements showcase gallery
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Portfolio showcase",
                tint = PrimaryGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Simulasi Galeri Visual (Visual Styles Showcase)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = LightText
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3 Showcase cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PortfolioShowcaseCard(
                styleName = "🎨 Studio Ghibli Magic Accent",
                description = "Gaya lukisan tangan cat air hangat murni khas anime legendaris. Warna pastel pastel magis tinggi yang menarik perhatian audiens muda."
            )
            PortfolioShowcaseCard(
                styleName = "📸 3D/Realistic Photorealistic Texture",
                description = "Rendering ultra detail, fokus lensa makro dengan bokeh dramatis penuh embun, bayangan studio natural, memicu hasrat membeli produk secara instan."
            )
            PortfolioShowcaseCard(
                styleName = "🎬 Intense Cinematic Golden Flare",
                description = "Drama pencahayaan neon gold ultra kontras tinggi, sorotan fog partikel debu melayang dalam sorotan cahaya tipis, lensa anamorfik profesional."
            )
            PortfolioShowcaseCard(
                styleName = "📹 Video Transitions Dynamic Loop",
                description = "Gerakan kamera short-video 9:16 (5-10 detik panning, zooming, fisheye dramatis) yang mengunci pandangan penonton pada detik pertama!"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Big Call to Action button
        Button(
            onClick = { viewModel.navigateTo(AppScreen.AUTH) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("cta_start_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGold,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🚀 MULAI BUAT IKLAN SEKARANG - GRATIS!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Accordion FAQ section
        Text(
            text = "📬 Pertanyaan Sering Diajukan (FAQ)",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = LightText
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        FaqAccordionItem(
            question = "Apakah sistem ini benar-benar gratis?",
            answer = "Iya! Akses instan diberikan gratis sebagai tamu, sementara pendaftaran akun reguler membuka akses tanpa batas ke fungsionalitas visual analitis penuh dan 5 babak naskah terstruktur."
        )

        FaqAccordionItem(
            question = "Bagaimana promosi naskah affiliate bekerja?",
            answer = "Kami menyusun skrip dengan rumus pemasaran modern: Viral Hook (1), Lifestyle Connection (2), Feature Value (3), Hero Transformation (4), dan Emotional Call to Action (5) lengkap link affiliate marketplace Anda."
        )

        FaqAccordionItem(
            question = "Apakah foto model terlindungi dalam kebijakan privasi?",
            answer = "Sepenuhnya aman! Semua foto diproses lokal secara privat hanya untuk mendeteksi style visual model dan melarang keras visual NSFW."
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PortfolioShowcaseCard(styleName: String, description: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = styleName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AccentGold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MutedText,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun FaqAccordionItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = !expanded },
        color = CardBg,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = LightText,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand FAQ",
                    tint = PrimaryGold,
                    modifier = Modifier.size(18.dp)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = answer,
                        fontSize = 12.sp,
                        color = MutedText,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: SISTEM AUTENTIKASI (STATE: LOGGED OUT)
// ==========================================
@Composable
fun AuthScreen(viewModel: AdCreatorViewModel) {
    var activeTab by remember { mutableStateOf(AuthTab.REGISTRATION) }
    
    // Form States
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }

    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Security simulations
    val simulatedIp by viewModel.simulatedIp.collectAsState()
    val isUsingPublicIp by viewModel.isUsingPublicIp.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .background(DarkBg)
            .testTag("auth_screen_container")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Simulated network status checker box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF131722),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isUsingPublicIp) WarningRed else SuccessGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "IP Terdeteksi: $simulatedIp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightText
                            )
                        }
                        Text(
                            text = if (isUsingPublicIp) "🔒 Mode Jaringan Publik: Potensi ancaman diblokir." else "✅ Safe Intranet: Terdaftar di pangkalan kantor pusat.",
                            fontSize = 10.sp,
                            color = if (isUsingPublicIp) WarningRed else MutedText,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.toggleSimulatedNetwork() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUsingPublicIp) SuccessGreen else WarningRed,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = if (isUsingPublicIp) "Ganti IP Kantor" else "Ubah IP Publik",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom tabs row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CardBg)
                .padding(4.dp)
        ) {
            val tabs = listOf(
                AuthTab.REGISTRATION to "1. Registrasi",
                AuthTab.LOGIN to "2. Login",
                AuthTab.GUEST to "3. Tamu (Guest)"
            )
            tabs.forEach { (tab, label) ->
                val isSelected = activeTab == tab
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) PrimaryGold else Color.Transparent)
                        .clickable { activeTab = tab }
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else LightText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Render tab layout states
        when (activeTab) {
            AuthTab.REGISTRATION -> {
                Surface(
                    color = CardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "📝 Buat Akun Reguler Baru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AccentGold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("Username (min. 4 karakter)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email (Wajib @gmail.com)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Password (min. 5 karakter)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it },
                            label = { Text("Ulangi Password Anda") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (regPassword != regConfirmPassword) {
                                    Toast.makeText(context, "🚫 Kata sandi tidak cocok!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val error = viewModel.registerNewAccount(regUsername, regEmail, regPassword)
                                if (error != null) {
                                    Toast.makeText(context, "⚠️ $error", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "🎉 Sukses mendaftar! Sekarang silakan Login.", Toast.LENGTH_LONG).show()
                                    activeTab = AuthTab.LOGIN
                                    loginUsername = regUsername
                                    loginPassword = regPassword
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = Color.Black)
                        ) {
                            Text("Daftar Akun Baru & Ikat IP", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            AuthTab.LOGIN -> {
                Surface(
                    color = CardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "🔐 Gerbang Masuk Member",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AccentGold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = { loginUsername = it },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password / Kode Backdoor") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Lupa password link simulation
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Lupa Password?",
                                color = SecondaryBlue,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable {
                                        Toast.makeText(context, "📧 Tautan pemulihan simulasi sukses dikirim ke email terdaftar Anda!", Toast.LENGTH_LONG).show()
                                    }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val (ok, role) = viewModel.loginAccount(loginUsername, loginPassword)
                                if (ok) {
                                    if (role == "MEMBER") {
                                        Toast.makeText(context, "✅ Berhasil. API KEY Gemini aktif.", Toast.LENGTH_LONG).show()
                                        viewModel.navigateTo(AppScreen.DASHBOARD)
                                    }
                                } else {
                                    if (role == "IP_BLOCK") {
                                        // Specific security block styling
                                        Toast.makeText(context, "🚫 LOGIN DITOLAK: IP Address tidak dikenali atau salah!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "❌ Gagal: $role", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = Color.Black)
                        ) {
                            Text("Masuk Sekarang", fontWeight = FontWeight.Bold)
                        }

                        // Specific Red IP warning when block occurs
                        if (isUsingPublicIp) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = WarningRed.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, WarningRed.copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🚫 PERINGATAN KEAMANAN: Anda sedang menyimulasikan IP Publik yang berbeda dari IP registrasi. Login akan langsung ditolak untuk mengawal keutuhan data akun Anda.",
                                        color = WarningRed,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AuthTab.GUEST -> {
                Surface(
                    color = CardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👤 Mode Tamu (Guest Quick Launch)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AccentGold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Anda dapat langsung mencoba pembuatan iklan tanpa akun. Namun, beberapa fitur dibatasi:",
                            fontSize = 12.sp,
                            color = MutedText,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🔒 Kunci Modul Model & Karakter khusus member", fontSize = 11.sp, color = MutedText)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("🔒 Batasan maksimal 1 kali percobaan generate", fontSize = 11.sp, color = MutedText)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("🔒 Narasi dibatasi 1 babak (reguler = 5 babak bersambung)", fontSize = 11.sp, color = MutedText)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.enterAsGuest() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryBlue, contentColor = Color.Black)
                        ) {
                            Text("Mulai Cepat Sebagai Tamu", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Backdoor clue helper
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "💡 Petunjuk Cerdas: Ingin melompati IP Block login? Hubungkan IP Anda ke IP Terdaftar, atau ketik kata sandi rahasia /dev_admin_init untuk masuk Portal Admin langsung.",
                fontSize = 10.sp,
                color = MutedText,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ==========================================
// SCREEN 2: DASHBOARD UTAMA & FORM INPUT (STATE: LOGGED IN / GUEST)
// ==========================================
@Composable
fun DashboardFormScreen(viewModel: AdCreatorViewModel) {
    val isGuest by viewModel.isGuest.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Form Dropdown expand parameters
    var mktExp by remember { mutableStateOf(false) }
    var styleExp by remember { mutableStateOf(false) }
    var angleExp by remember { mutableStateOf(false) }
    var ttsExp by remember { mutableStateOf(false) }
    var intonationExp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .background(DarkBg)
            .testTag("dashboard_screen_container")
    ) {
        // Welcome User Info bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardBg,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, PrimaryGold.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isGuest) "👤 Masuk Tamu: GUEST_MODE" else "👑 Member: ${currentUser?.username}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PrimaryGold
                    )
                    Text(
                        text = if (isGuest) "Peran Pengakses Terbatas" else "Pintu API KEY Gemini aktif & lengkap",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }

                Text(
                    text = "Logout",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = WarningRed,
                    modifier = Modifier
                        .clickable { viewModel.logout() }
                        .padding(6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form Title
        Text(
            text = "🎬 Parameter Kampanye Iklan Afiliasi",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = LightText
            )
        )
        Text(
            text = "Lengkapi form pembuat visual kreatif di bawah ini:",
            fontSize = 12.sp,
            color = MutedText,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // CARD MODULE A: Visual Produk (Wajib)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            color = CardBg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📦 Modul A: Visual Produk (Wajib isi min 1)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AccentGold
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = viewModel.selectedProductName,
                    onValueChange = { viewModel.selectedProductName = it },
                    label = { Text("Nama Produk Afiliasi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightText,
                        unfocusedTextColor = LightText
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Simulated Market Photo Selector
                Text("Simulasikan Foto Produk (Maks 5):", fontSize = 11.sp, color = MutedText)
                Spacer(modifier = Modifier.height(6.dp))

                val sampleProducts = listOf("Sandal Slide Kulit", "Jam Chrono Premium", "Mug Keramik Minimalis", "Serum Glowing", "Kopi robusta")
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sampleProducts.forEach { prod ->
                        val isSelected = viewModel.uploadedModelName == prod
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryGold.copy(alpha = 0.2f) else BorderGray)
                                .border(1.dp, if (isSelected) PrimaryGold else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { viewModel.simulateProductPhotoUpload(prod) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = prod,
                                fontSize = 11.sp,
                                color = if (isSelected) PrimaryGold else LightText,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = viewModel.marketplaceLink,
                    onValueChange = { viewModel.marketplaceLink = it },
                    label = { Text("Input Link Produk Marketplace") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightText,
                        unfocusedTextColor = LightText
                    )
                )
            }
        }

        // CARD MODULE B: Model & Karakter (Opsional / Terkunci untuk Guest)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            color = CardBg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Box {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "👤 Modul B: Model & Karakter (Opsional)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AccentGold
                        )
                        if (isGuest) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(WarningRed.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("LOCKED", color = WarningRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Simulasi Pilih Foto Model Keamanan (NSFW Filter & Style Classifier):",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (!isGuest && viewModel.uploadedModelName != null && viewModel.modelAnalysisResult != null) {
                        Surface(
                            color = Color(0xFF1E2436),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderGray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "📂 Foto Model Terdeteksi: MODEL_${viewModel.uploadedModelName?.uppercase()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Text("Style: ", fontSize = 11.sp, color = MutedText)
                                    Text(viewModel.modelAnalysisResult!!.type, fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Confidence: ", fontSize = 11.sp, color = MutedText)
                                    Text(viewModel.modelAnalysisResult!!.confidence, fontSize = 11.sp, color = LightText)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row {
                                    Text("NSFW Check: ", fontSize = 11.sp, color = MutedText)
                                    Text("✅ AMAN BEBAS SENSOR", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = viewModel.isModelVerificationChecked,
                                        onCheckedChange = { viewModel.isModelVerificationChecked = it },
                                        colors = CheckboxDefaults.colors(checkedColor = PrimaryGold)
                                    )
                                    Text(
                                        text = "Saya menjamin foto milik saya & bukan tokoh terkenal.",
                                        fontSize = 10.sp,
                                        color = LightText,
                                        lineHeight = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Text(
                                    text = "Hapus Foto",
                                    color = WarningRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { viewModel.removeUploadedPhoto() }
                                        .padding(4.dp)
                                )
                            }
                        }
                    } else if (!isGuest) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BorderGray)
                                .clickable {
                                    viewModel.simulateProductPhotoUpload("Model Kasual Indie")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "➕ Ketuk untuk upload foto Model Kreatif (Simulasi)",
                                fontSize = 12.sp,
                                color = SecondaryBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // Guest mode blocked UI
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E2129)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔒 Upload Model hanya untuk member terdaftar.",
                                fontSize = 12.sp,
                                color = WarningRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // CARD MODULE C: Afiliasi & Deskripsi
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            color = CardBg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💸 Modul C: Afiliasi & Deskripsi Produk",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AccentGold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Marketplace dropdown simulator
                Box {
                    OutlinedButton(
                        onClick = { mktExp = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, BorderGray),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LightText)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pilih Marketplace: " + viewModel.selectedMarketplace)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "dropdown")
                        }
                    }
                    DropdownMenu(
                        expanded = mktExp,
                        onDismissRequest = { mktExp = false }
                    ) {
                        listOf("Shopee", "Tokopedia", "TikTok Shop").forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    viewModel.selectedMarketplace = name
                                    mktExp = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = viewModel.affiliateIdInput,
                    onValueChange = { viewModel.affiliateIdInput = it },
                    label = { Text("Input ID Affiliate Anda") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightText,
                        unfocusedTextColor = LightText
                    )
                )
            }
        }

        // CARD MODULE D: Preferensi Visual & Audio
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            color = CardBg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎙️ Modul D: Preferensi Visual & Audio TTS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AccentGold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Style Selection Dropdown
                Text("Style Video Kreatif:", fontSize = 11.sp, color = MutedText)
                Box {
                    OutlinedButton(
                        onClick = { styleExp = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, BorderGray),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LightText)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(viewModel.visualStylePreferences)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "style")
                        }
                    }
                    DropdownMenu(expanded = styleExp, onDismissRequest = { styleExp = false }) {
                        listOf("Cinematic", "Realistic", "3D", "Studio Ghibli", "Minimalist", "Review Produk Faceless", "Review Produk di Etalase").forEach { style ->
                            DropdownMenuItem(
                                text = { Text(style) },
                                onClick = {
                                    viewModel.visualStylePreferences = style
                                    styleExp = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Angle Selection Dropdown
                Text("Angle Kamera Visual:", fontSize = 11.sp, color = MutedText)
                Box {
                    OutlinedButton(
                        onClick = { angleExp = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, BorderGray),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LightText)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(viewModel.shootingAnglePreferences)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "angle")
                        }
                    }
                    DropdownMenu(expanded = angleExp, onDismissRequest = { angleExp = false }) {
                        listOf("Zoom", "Fisheyes", "Boomerang", "Overhead", "Low-Angle", "Wide Shot").forEach { angle ->
                            DropdownMenuItem(
                                text = { Text(angle) },
                                onClick = {
                                    viewModel.shootingAnglePreferences = angle
                                    angleExp = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // TTS Voice Output Selection
                Text("Voice Over (VO) TTS:", fontSize = 11.sp, color = MutedText)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { ttsExp = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, BorderGray),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LightText)
                        ) {
                            Text(viewModel.ttsVoicePreferences)
                        }
                        DropdownMenu(expanded = ttsExp, onDismissRequest = { ttsExp = false }) {
                            listOf("TTS Pria", "TTS Wanita", "TTS Orangtua", "TTS Remaja", "TTS Anak").forEach { voice ->
                                DropdownMenuItem(
                                    text = { Text(voice) },
                                    onClick = {
                                        viewModel.ttsVoicePreferences = voice
                                        ttsExp = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { intonationExp = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, BorderGray),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LightText)
                        ) {
                            Text("Aksen: " + viewModel.selectedVoIntonation)
                        }
                        DropdownMenu(expanded = intonationExp, onDismissRequest = { intonationExp = false }) {
                            listOf("Ceria", "Elegan", "Misterius").forEach { int ->
                                DropdownMenuItem(
                                    text = { Text(int) },
                                    onClick = {
                                        viewModel.selectedVoIntonation = int
                                        intonationExp = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Large execution Button
        val isDisabledForRegisterModel = !isGuest && viewModel.uploadedModelName != null && !viewModel.isModelVerificationChecked
        Button(
            onClick = {
                viewModel.executePromptGeneration {
                    viewModel.navigateTo(AppScreen.GENERATOR_RESULTS)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .testTag("generate_prompts_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGold,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp),
            enabled = !isGenerating && !isDisabledForRegisterModel
        ) {
            Text(
                text = if (isGenerating) "⏳ AI Meramu data..." else "🚀 Generate Prompt Iklan Affiliate",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }

        if (isDisabledForRegisterModel) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "⚠️ Verifikasi kepemilikan foto model wajib dicentang untuk generate!",
                color = WarningRed,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Pop up alert dialog for Guest block limits (Attempt 2)
        if (viewModel.showGuestBlockAlert) {
            AlertDialog(
                onDismissRequest = { viewModel.showGuestBlockAlert = false },
                title = { Text("⚠️ AKSES DIBATASI ⚠️", fontWeight = FontWeight.Bold, color = WarningRed) },
                text = {
                    Text(
                        text = "Batas percobaan pembuatan Tamu telah habis. Anda HARUS melakukan registrasi yang sederhana menggunakan Gmail untuk membuka seluruh akses 5 babak linier, visual analisis model lengkap, dan bebas filter.",
                        fontSize = 13.sp,
                        color = LightText
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.showGuestBlockAlert = false
                            viewModel.navigateTo(AppScreen.AUTH)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = Color.Black)
                    ) {
                        Text("👉 DAFTAR / MASUK GMAIL", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showGuestBlockAlert = false }) {
                        Text("Batal", color = MutedText)
                    }
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ==========================================
// SCREEN 3: MESIN PROSES LOGIKA & RENDERING HASIL (STATE: GENERATED)
// ==========================================
@Composable
fun ResultsScreen(viewModel: AdCreatorViewModel) {
    val results by viewModel.generationResult.collectAsState()
    val isGuest by viewModel.isGuest.collectAsState()
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .background(DarkBg)
            .testTag("results_screen_container")
    ) {
        // Output title header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardBg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SuccessGreen)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "💡 SELESAI: AI Prompt Berhasil Diracik!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGuest) "Tamu: Hasilkan 1 Babak (Akses Terbatas)" else "Premium: Narasi Lengkap 5 Babak Sinematik Bersambung",
                    fontSize = 11.sp,
                    color = MutedText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RENDER TABLE 1: Image Prompt
        Text(
            text = "🎬 Tabel 1: Panduan Prompts Image Detail & Salin",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = PrimaryGold
        )
        Text(
            text = "Gunakan prompt di bawah untuk generator seperti Midjourney / Bing / Gemini:",
            fontSize = 11.sp,
            color = MutedText,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Rendering simulated Grid table row by row
        results.forEach { scene ->
            ResultRowCard(
                sceneNum = scene.sceneNum,
                title = scene.title,
                primaryContentLabel = "Prompt Image",
                primaryContent = scene.imagePrompt,
                secondaryContentLabel = "Caption & Affiliate Info",
                secondaryContent = scene.caption,
                affiliateLink = scene.affiliateLink,
                onCopy = {
                    val fullCopy = "--- ${scene.title} ---\n[Image Prompt]\n${scene.imagePrompt}\n\n[Caption]\n${scene.caption}"
                    clipboardManager.setText(AnnotatedString(fullCopy))
                    Toast.makeText(context, "📋 Adegan ${scene.sceneNum} (Image + Captions) disalin ke papan klip!", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // RENDER TABLE 2: Video & Voice Settings
        Text(
            text = "📹 Tabel 2: Panduan Kamera Video & Naskah Narator (TTS)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = PrimaryGold
        )
        Text(
            text = "Gunakan untuk naskah pengeditan video dan perintah generator kamera (Sora/Veo/Luma):",
            fontSize = 11.sp,
            color = MutedText,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Rows for Video prompt settings
        results.forEach { scene ->
            ResultRowCard(
                sceneNum = scene.sceneNum,
                title = scene.title,
                primaryContentLabel = "Prompt Video (Kamera)",
                primaryContent = scene.videoPrompt,
                secondaryContentLabel = "Voice Over (Skrip TTS)",
                secondaryContent = scene.voiceOverText,
                affiliateLink = "",
                onCopy = {
                    val fullCopy = "--- ${scene.title} ---\n[Video Prompt]\n${scene.videoPrompt}\n\n[Skrip Voice Over]\n${scene.voiceOverText}"
                    clipboardManager.setText(AnnotatedString(fullCopy))
                    Toast.makeText(context, "📋 Adegan ${scene.sceneNum} (Video + Vo) disalin ke papan klip!", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Button to restart Dashboard Form
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                modifier = Modifier
                    .weight(1f)
                    .height(45.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LightText),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Text("✏️ Edit Masukan")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = {
                    viewModel.executePromptGeneration {
                        Toast.makeText(context, "🔄 Menyusun prompt baru...", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = Color.Black)
            ) {
                Text("🔄 Generate Ulang", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // FASE 4: FOOTER RECOMMENDED PLATFORMS LIST TABLE
        Text(
            text = "🎁 Rekomendasi Mesin AI Gratis untuk Eksekusi",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = AccentGold
        )
        Text(
            text = "Salin prompt di atas dan jalankan pada portal kreasi model di bawah ini:",
            fontSize = 11.sp,
            color = MutedText,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        TableRecommendedPlatform()

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ResultRowCard(
    sceneNum: Int,
    title: String,
    primaryContentLabel: String,
    primaryContent: String,
    secondaryContentLabel: String,
    secondaryContent: String,
    affiliateLink: String,
    onCopy: () -> Unit
) {
    Surface(
        color = CardBg,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PrimaryGold.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGold,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onCopy,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF232A3D)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("📋 Copy", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary component (usually Image prompt or Video camera action)
            Text(primaryContentLabel, color = SecondaryBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = primaryContent,
                color = LightText,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary component (Captions/VO scripter)
            Text(secondaryContentLabel, color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = secondaryContent,
                color = MutedText,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontStyle = FontStyle.Italic
            )

            if (affiliateLink.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = BorderGray, modifier = Modifier.padding(vertical = 2.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔗 Link Affiliate original: ", fontSize = 10.sp, color = MutedText)
                    Text(
                        text = affiliateLink,
                        fontSize = 10.sp,
                        color = SecondaryBlue,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Recommended engine platforms lists as requested in Phase 4 specifications
@Composable
fun TableRecommendedPlatform() {
    val platforms = listOf(
        Pair("Google Gemini", "Image & Naskah TTS (gemini.google.com)"),
        Pair("Meta AI", "Image & High Speed Video (meta.ai)"),
        Pair("LM Arena", "Uji Coba Multi-Model Handal (arena.ai)"),
        Pair("Bing Creator", "Mesin Image DALL-E 3 (bing.com/create)"),
        Pair("Leonardo AI", "Professional Image & Video (leonardo.ai)"),
        Pair("Hugging Face", "Model Eksperimental Terbuka (huggingface.co)")
    )

    Surface(
        color = CardBg,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF22293C))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Nama Portal AI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = PrimaryGold,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = "Spesialisasi Kemampuan & Akses",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = PrimaryGold,
                    modifier = Modifier.weight(2.2f)
                )
            }

            platforms.forEach { (name, desc) ->
                Divider(color = BorderGray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        color = LightText,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1.2f)
                    )
                    Text(
                        text = desc,
                        fontSize = 11.sp,
                        color = MutedText,
                        modifier = Modifier.weight(2.2f)
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: DEV BACKDOOR SECRETS (STATE: ADMIN ONLY)
// ==========================================
@Composable
fun AdminDashboardScreen(viewModel: AdCreatorViewModel) {
    val users by viewModel.registeredUsers.collectAsState()
    val logs by viewModel.systemLogs.collectAsState()
    val currentIp by viewModel.simulatedIp.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .background(DarkBg)
            .testTag("admin_screen_container")
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = WarningRed.copy(alpha = 0.15f),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, WarningRed.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🛠️ PORTAL ADMINISTRATOR BACKDOOR [SECRET]",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = WarningRed
                )
                Text(
                    text = "Akses publik dilarang keras. Sistem memantau integritas IP Address & pendaftaran log audit.",
                    fontSize = 11.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid statistical boxes
        Row(modifier = Modifier.fillMaxWidth()) {
            Surface(
                color = CardBg,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total User", fontSize = 11.sp, color = MutedText)
                    Text(users.size.toString(), fontSize = 22.sp, fontWeight = FontWeight.Black, color = PrimaryGold)
                }
            }

            Surface(
                color = CardBg,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("IP Pengunjung", fontSize = 11.sp, color = MutedText)
                    Text(text = currentIp, fontSize = 14.sp, fontWeight = FontWeight.Black, color = SecondaryBlue, maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LIST OF REGISTERED USERS Table
        Text("👥 Daftar User Terdaftar (IP Bound Security):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightText)
        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            color = CardBg,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                users.forEach { user ->
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(user.username, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGold)
                            Text(user.timestamp, fontSize = 10.sp, color = MutedText)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Email: ${user.email}", fontSize = 11.sp, color = LightText)
                            Text("IP Terikat: ${user.ipAddress}", fontSize = 11.sp, color = SecondaryBlue)
                        }
                        Divider(color = BorderGray, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // LIVE SYSTEM LOGS
        Text("📄 Berkas Log Keamanan & Alur Sistem:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightText)
        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            color = Color(0xFF07090F),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                logs.forEach { log ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("[${log.timestamp}]", color = MutedText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "<${log.level}>",
                            color = if (log.level == "SECURITY" || log.level == "BACKDOOR") WarningRed else SecondaryBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(log.message, color = LightText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Return button to Landing Screen
        Button(
            onClick = { viewModel.navigateTo(AppScreen.LANDING) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = Color.Black)
        ) {
            Text("Kembali ke Landing Page (Fase 0)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// Typography loader
fun Typography(): Typography {
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        )
    )
}
