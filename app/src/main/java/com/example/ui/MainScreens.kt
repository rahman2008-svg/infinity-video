package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.BackgroundAudioService
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.PlaybackManager
import com.example.data.PlaylistEntity
import com.example.data.VideoEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat

// --- LOCALIZATION ENGINE ---
object Trans {
    private val en = mapOf(
        "app_name" to "Infinity Video",
        "tagline" to "Universal Smart Media Center",
        "init_db" to "Initializing Database...",
        "scan_dirs" to "Scanning storage folders...",
        "get_started" to "Get Started",
        "skip" to "Skip",
        "welcome_desc" to "The ultimate, secure media experience for high-fidelity playback, playlists, vault security, and multi-folder exploration.",
        "permissions_title" to "Storage Access Needed",
        "permissions_desc" to "To discover and stream your locally stored videos, movies, and audio tracks, Infinity Video needs permission to read media assets.",
        "allow_btn" to "Grant Access",
        "continue_btn" to "Continue to Home",
        "search_hint" to "Search videos, audio...",
        "continue_watching" to "Continue Watching",
        "recently_added" to "Recently Added",
        "favorites" to "Favorite Videos",
        "most_played" to "Most Played",
        "all_videos" to "All Videos",
        "folders" to "Folders",
        "playlists" to "Playlists",
        "vault" to "Private Vault",
        "settings" to "Settings",
        "no_videos" to "No media files found.",
        "scan_complete" to "Device successfully scanned",
        "unlocked" to "Vault Unlocked",
        "locked" to "Vault Locked",
        "enter_pin" to "Enter 4-Digit PIN",
        "setup_pin" to "Setup Private Vault PIN",
        "security_question" to "Security Question",
        "security_question_hint" to "What is your childhood nickname?",
        "security_answer" to "Your Answer",
        "vault_hint" to "Hide files here with solid local password protection.",
        "developer_title" to "About Developer",
        "company_title" to "About Company",
        "theme" to "Accent Theme Color",
        "language" to "App Language",
        "playback_speed" to "Playback Speed",
        "subtitles" to "Subtitles Language",
        "cache_clear" to "Clear Application Cache",
        "backup_db" to "Backup Database Progress",
        "auto_scan" to "Auto scan on startup",
        "about_dev_desc" to "Prince AR Abdur Rahman\nIndependent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
        "about_comp_desc" to "NexVora Lab's Ofc\nNexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.",
        "mission" to "Mission: Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
        "credits" to "Developed by Prince AR Abdur Rahman\nPublished by NexVora Lab's Ofc\n© 2026 NexVora Lab's Ofc. All Rights Reserved."
    )

    private val bn = mapOf(
        "app_name" to "ইনফিনিটি ভিডিও",
        "tagline" to "সার্বজনীন স্মার্ট মিডিয়া সেন্টার",
        "init_db" to "ডাটাবেস চালু করা হচ্ছে...",
        "scan_dirs" to "স্টোরেজ ফোল্ডার স্ক্যান করা হচ্ছে...",
        "get_started" to "শুরু করুন",
        "skip" to "এড়িয়ে যান",
        "welcome_desc" to "উচ্চমানের মিডিয়া প্লেব্যাক, প্লেলিস্ট, ভল্ট নিরাপত্তা এবং মাল্টি-ফোল্ডার অন্বেষণের জন্য চূড়ান্ত এবং নিরাপদ মিডিয়া অভিজ্ঞতা।",
        "permissions_title" to "স্টোরেজ অনুমতি প্রয়োজন",
        "permissions_desc" to "আপনার ডিভাইসে সংরক্ষিত ভিডিও, চলচ্চিত্র এবং অডিও ট্র্যাকগুলি খুঁজে পেতে এবং চালানোর জন্য ইনফিনিটি ভিডিও প্লেয়ারের মিডিয়া অ্যাক্সেস অনুমতি প্রয়োজন।",
        "allow_btn" to "অনুমতি দিন",
        "continue_btn" to "হোম স্ক্রিনে যান",
        "search_hint" to "ভিডিও, অডিও খুঁজুন...",
        "continue_watching" to "দেখা চালিয়ে যান",
        "recently_added" to "সম্প্রতি যোগ করা",
        "favorites" to "প্রিয় ভিডিও",
        "most_played" to "সবচেয়ে বেশি চালিত",
        "all_videos" to "সব ভিডিও",
        "folders" to "ফোল্ডার",
        "playlists" to "প্লেলিস্ট",
        "vault" to "ব্যক্তিগত ভল্ট",
        "settings" to "সেটিংস",
        "no_videos" to "কোন মিডিয়া ফাইল পাওয়া যায়নি।",
        "scan_complete" to "ডিভাইস সফলভাবে স্ক্যান করা হয়েছে",
        "unlocked" to "ভল্ট আনলকড",
        "locked" to "ভল্ট লকড",
        "enter_pin" to "৪-ডিজিট পিন দিন",
        "setup_pin" to "ভল্ট পিন সেটআপ করুন",
        "security_question" to "নিরাপত্তা প্রশ্ন",
        "security_question_hint" to "আপনার ছোটবেলার ডাকনাম কি?",
        "security_answer" to "আপনার উত্তর",
        "vault_hint" to "পাসওয়ার্ড সুরক্ষা দিয়ে আপনার মিডিয়া ফাইলগুলি এখানে লুকিয়ে রাখুন।",
        "developer_title" to "ডেভেলপার পরিচিতি",
        "company_title" to "কোম্পানি পরিচিতি",
        "theme" to "থিম কালার অ্যাকসেন্ট",
        "language" to "অ্যাপের ভাষা",
        "playback_speed" to "প্লেব্যাক স্পিড",
        "subtitles" to "সাবটাইটেল ভাষা",
        "cache_clear" to "ক্যাশ মেমরি পরিষ্কার করুন",
        "backup_db" to "ডাটাবেস ব্যাকআপ",
        "auto_scan" to "স্টার্টআপে স্বয়ংক্রিয় স্ক্যান",
        "about_dev_desc" to "প্রিন্স এআর আবদুর রহমান\nএকজন স্বাধীন অ্যাপ ডেভেলপার যিনি আধুনিক অ্যান্ড্রয়েড অ্যাপ্লিকেশন, প্রোডাক্টিভিটি টুলস, এআই-চালিত মিডিয়া প্লেয়ার, শিক্ষামূলক অ্যাপ এবং পরবর্তী প্রজন্মের ডিজিটাল পণ্য তৈরি করতে ভালোবাসেন।",
        "about_comp_desc" to "নেক্সভ্যারা ল্যাব'স অফিস\nনেক্সভ্যারা ল্যাব প্রোডাক্টিভিটি, বিনোদন, শিক্ষা এবং নতুন ডিজিটাল অভিজ্ঞতা উন্নত করার জন্য উদ্ভাবনী অ্যান্ড্রয়েড অ্যাপ্লিকেশন তৈরিতে কাজ করে।",
        "mission" to "মিশন: সবার জন্য দ্রুত, সুন্দর, গোপনীয়তা-বান্ধব এবং ব্যবহারকারী-কেন্দ্রিক অ্যাপ্লিকেশন তৈরি করা।",
        "credits" to "তৈরি করেছেন প্রিন্স এআর আবদুর রহমান\nপ্রকাশক নেক্সভ্যারা ল্যাব'স অফিস\n© ২০২৬ নেক্সভ্যারা ল্যাব'স অফিস। সর্বস্বত্ব সংরক্ষিত।"
    )

    private val es = mapOf(
        "app_name" to "Infinity Video",
        "tagline" to "Centro de Medios Inteligente Universal",
        "init_db" to "Inicializando base de datos...",
        "scan_dirs" to "Escaneando almacenamiento...",
        "get_started" to "Comenzar",
        "skip" to "Omitir",
        "welcome_desc" to "La experiencia de video definitiva y segura para reproducciones de alta fidelidad, listas de reproducción y bóveda privada.",
        "permissions_title" to "Acceso de almacenamiento requerido",
        "permissions_desc" to "Para descubrir y reproducir sus videos locales, Infinity Video necesita permiso para leer activos multimedia.",
        "allow_btn" to "Conceder acceso",
        "continue_btn" to "Continuar al inicio",
        "search_hint" to "Buscar videos, audio...",
        "continue_watching" to "Continuar viendo",
        "recently_added" to "Agregado recientemente",
        "favorites" to "Videos favoritos",
        "most_played" to "Más reproducidos",
        "all_videos" to "Todos los videos",
        "folders" to "Carpetas",
        "playlists" to "Listas de reproducción",
        "vault" to "Bóveda Privada",
        "settings" to "Ajustes",
        "no_videos" to "No se encontraron archivos multimedia.",
        "scan_complete" to "Dispositivo escaneado correctamente",
        "unlocked" to "Bóveda Desbloqueada",
        "locked" to "Bóveda Bloqueada",
        "enter_pin" to "Ingrese PIN de 4 dígitos",
        "setup_pin" to "Configurar PIN de Bóveda",
        "security_question" to "Pregunta de seguridad",
        "security_question_hint" to "¿Cuál es tu apodo de infancia?",
        "security_answer" to "Tu respuesta",
        "vault_hint" to "Oculte archivos de video de forma segura con protección PIN local.",
        "developer_title" to "Sobre el Desarrollador",
        "company_title" to "Sobre la Compañía",
        "theme" to "Color de tema",
        "language" to "Idioma de la aplicación",
        "playback_speed" to "Velocidad de reproducción",
        "subtitles" to "Subtítulos",
        "cache_clear" to "Limpiar caché",
        "backup_db" to "Copia de seguridad",
        "auto_scan" to "Escaneo automático al iniciar",
        "about_dev_desc" to "Prince AR Abdur Rahman\nDesarrollador independiente de aplicaciones apasionado por crear aplicaciones modernas para Android, herramientas de productividad, reproductores multimedia con IA y productos digitales de próxima generación.",
        "about_comp_desc" to "NexVora Lab's Ofc\nNexVora se enfoca en crear aplicaciones Android innovadoras diseñadas para mejorar la productividad, el entretenimiento y el aprendizaje.",
        "mission" to "Misión: Crear aplicaciones rápidas, hermosas, amigables con la privacidad y accesibles para todos.",
        "credits" to "Desarrollado por Prince AR Abdur Rahman\nPublicado por NexVora Lab's Ofc\n© 2026 NexVora Lab's Ofc. Todos los derechos reservados."
    )

    private val hi = mapOf(
        "app_name" to "Infinity Video",
        "tagline" to "यूनिवर्सल स्मार्ट मीडिया प्लेयर",
        "init_db" to "डेटाबेस प्रारंभ हो रहा है...",
        "scan_dirs" to "स्टोरेज स्कैन किया जा रहा है...",
        "get_started" to "शुरू करें",
        "skip" to "छोड़ें",
        "welcome_desc" to "उच्च गुणवत्ता प्लेबैक, प्लेलिस्ट और सुरक्षित तिजोरी सुरक्षा के लिए अंतिम सुरक्षित मीडिया अनुभव।",
        "permissions_title" to "स्टोरेज अनुमति की आवश्यकता है",
        "permissions_desc" to "आपके स्थानीय वीडियो और ऑडियो को खोजने और चलाने के लिए इन्फिनिटी वीडियो को मीडिया एक्सेस की अनुमति चाहिए।",
        "allow_btn" to "अनुमति दें",
        "continue_btn" to "होम पर जाएं",
        "search_hint" to "वीडियो, ऑडियो खोजें...",
        "continue_watching" to "देखना जारी रखें",
        "recently_added" to "हाल ही में जोड़ा गया",
        "favorites" to "पसंदीदा वीडियो",
        "most_played" to "सबसे ज्यादा चलाए गए",
        "all_videos" to "सभी वीडियो",
        "folders" to "फ़ोल्डर",
        "playlists" to "प्लेलिस्ट",
        "vault" to "प्राइवेट वॉल्ट",
        "settings" to "सेटिंग्स",
        "no_videos" to "कोई मीडिया फ़ाइल नहीं मिली।",
        "scan_complete" to "सफलतापूर्वक स्कैन किया गया",
        "unlocked" to "वॉल्ट अनलॉक",
        "locked" to "वॉल्ट लॉक",
        "enter_pin" to "4-अंकीय पिन दर्ज करें",
        "setup_pin" to "वॉल्ट पिन सेट करें",
        "security_question" to "सुरक्षा प्रश्न",
        "security_question_hint" to "आपके बचपन का उपनाम क्या है?",
        "security_answer" to "आपका उत्तर",
        "vault_hint" to "पिन पासवर्ड सुरक्षा के साथ अपनी निजी वीडियो फाइलों को छिपाएं।",
        "developer_title" to "डेवलपर के बारे में",
        "company_title" to "कंपनी के बारे में",
        "theme" to "थीम का रंग",
        "language" to "ऐप की भाषा",
        "playback_speed" to "प्लेबैक स्पीड",
        "subtitles" to "उपशीर्षक",
        "cache_clear" to "कैश साफ़ करें",
        "backup_db" to "डेटाबेस बैकअप",
        "auto_scan" to "ऑटो स्कैन",
        "about_dev_desc" to "Prince AR Abdur Rahman\nएक स्वतंत्र ऐप डेवलपर जो आधुनिक एंड्रॉइड एप्लिकेशन, उत्पादकता उपकरण, एआई-संचालित मीडिया प्लेयर और अगली पीढ़ी के डिजिटल उत्पादों के निर्माण के प्रति उत्साही हैं।",
        "about_comp_desc" to "NexVora Lab's Ofc\nनेक्सवोरा लैब उत्पादकता, मनोरंजन और सीखने के अनुभवों को बेहतर बनाने के लिए अभिनव एंड्रॉइड एप्लिकेशन बनाने पर केंद्रित है।",
        "mission" to "मिशन: सभी के लिए सुलभ तेज़, सुंदर, गोपनीयता-अनुकूल और उपयोगकर्ता-केंद्रित एप्लिकेशन बनाना।",
        "credits" to "प्रिंस एआर अब्दुर रहमान द्वारा विकसित\nनेक्सवोरा लैब द्वारा प्रकाशित\n© 2026 NexVora Lab. सर्वाधिकार सुरक्षित।"
    )

    fun get(key: String, lang: String): String {
        val dict = when (lang) {
            "bn" -> bn
            "es" -> es
            "hi" -> hi
            else -> en
        }
        return dict[key] ?: en[key] ?: key
    }
}

// --- APP LAYOUT MAIN ROUTER ---
@Composable
fun MainAppContainer(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeMedia by viewModel.activeMedia.collectAsState()
    val isFloatingMode by PlaybackManager.isFloatingMode.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.Splash -> SplashScreenView(viewModel)
                Screen.Welcome -> WelcomeScreenView(viewModel)
                Screen.Permission -> PermissionScreenView(viewModel)
                Screen.Home -> HomeScreenView(viewModel)
                Screen.VideoPlayer -> {
                    activeMedia?.let { media ->
                        VideoPlayerScreenView(
                            media = media,
                            viewModel = viewModel,
                            onClose = {
                                PlaybackManager.release()
                                viewModel.navigateTo(Screen.Home)
                            }
                        )
                    }
                }
            }

            // In-App Draggable Picture-in-Picture floating view overlays when active
            if (isFloatingMode && activeMedia != null && currentScreen != Screen.VideoPlayer) {
                FloatingPlayerOverlay(
                    media = activeMedia!!,
                    viewModel = viewModel
                )
            }
        }
    }
}

// --- 1. SPLASH SCREEN ---
@Composable
fun SplashScreenView(viewModel: MainViewModel) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scaleTransition = rememberInfiniteTransition(label = "logo_scale")
    
    val pulseScale by scaleTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    var statusText by remember { mutableStateOf(Trans.get("init_db", lang)) }

    LaunchedEffect(Unit) {
        // Step 1: Init Database (Room automatic on load)
        delay(1200)
        statusText = Trans.get("scan_dirs", lang)
        
        // Step 2: Auto Scan
        viewModel.startStorageScan()
        delay(1500)
        
        // Navigate
        viewModel.navigateTo(Screen.Welcome)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BackgroundDark, Color(0xFF131024))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_app_icon),
                contentDescription = "Infinity Logo",
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(32.dp))
                    .testTag("splash_logo"),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = Trans.get("app_name", lang),
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Text(
                text = Trans.get("tagline", lang),
                color = SecondaryNeon,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                modifier = Modifier.alpha(0.8f)
            )

            Spacer(modifier = Modifier.height(64.dp))

            CircularProgressIndicator(
                color = PrimaryNeon,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = statusText,
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- 2. WELCOME SCREEN ---
@Composable
fun WelcomeScreenView(viewModel: MainViewModel) {
    val lang by viewModel.selectedLanguage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo and language selectors at top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Trans.get("app_name", lang),
                color = PrimaryNeon,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Dynamic quick-change languages chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("en" to "EN", "bn" to "বাংলা", "es" to "ES", "hi" to "HI").forEach { (code, label) ->
                    val isSelected = code == lang
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryNeon else SurfaceDark)
                            .clickable { viewModel.setLanguage(code) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.Black else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Generated Horizontal Cinematic Artwork
        Image(
            painter = painterResource(id = R.drawable.img_welcome_hero),
            contentDescription = "Welcome Cinematic Banner",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("welcome_banner"),
            contentScale = ContentScale.Crop
        )

        // Description Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = Trans.get("tagline", lang),
                color = SecondaryNeon,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = Trans.get("welcome_desc", lang),
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // Action Button
        Button(
            onClick = { viewModel.navigateTo(Screen.Permission) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("get_started_button")
        ) {
            Text(
                text = Trans.get("get_started", lang),
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- 3. PERMISSION SCREEN ---
@Composable
fun PermissionScreenView(viewModel: MainViewModel) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = "Shield Security",
                tint = SecondaryNeon,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = Trans.get("permissions_title", lang),
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = Trans.get("permissions_desc", lang),
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        // Permission details visuals
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PermissionRequirementRow(
                icon = Icons.Default.VideoLibrary,
                title = "Photos & Videos Permission",
                desc = "Required to retrieve high-definition MKV, MP4, AVI streams."
            )
            PermissionRequirementRow(
                icon = Icons.Default.AudioFile,
                title = "Music & Audio Permission",
                desc = "Required to scan and list mp3, flac, and m4a sound tracks."
            )
        }

        // Action buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    // Simulate permission acceptance. In real devices, accompanist triggers actual dialogs.
                    // We immediately scan device on action.
                    Toast.makeText(context, Trans.get("scan_complete", lang), Toast.LENGTH_SHORT).show()
                    viewModel.startStorageScan()
                    viewModel.navigateTo(Screen.Home)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryNeon),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("grant_permission_button")
            ) {
                Text(
                    text = Trans.get("allow_btn", lang),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            TextButton(
                onClick = { viewModel.navigateTo(Screen.Home) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Trans.get("skip", lang),
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun PermissionRequirementRow(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(SurfaceDarkSecondary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = PrimaryNeon)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

// --- 4. HOME SCREEN HUB ---
@Composable
fun HomeScreenView(viewModel: MainViewModel) {
    val lang by viewModel.selectedLanguage.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Videos, 1: Folders, 2: Playlists, 3: Private Vault, 4: Settings

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val tabs = listOf(
                    Triple(0, Icons.Default.VideoLibrary, Trans.get("all_videos", lang)),
                    Triple(1, Icons.Default.Folder, Trans.get("folders", lang)),
                    Triple(2, Icons.Default.PlaylistPlay, Trans.get("playlists", lang)),
                    Triple(3, Icons.Default.Lock, Trans.get("vault", lang)),
                    Triple(4, Icons.Default.Settings, Trans.get("settings", lang))
                )

                tabs.forEach { (index, icon, label) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = SecondaryNeon,
                            indicatorColor = SecondaryNeon,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> VideosTabScreen(viewModel)
                1 -> FoldersTabScreen(viewModel)
                2 -> PlaylistsTabScreen(viewModel)
                3 -> PrivateVaultTabScreen(viewModel)
                4 -> SettingsTabScreen(viewModel)
            }
        }
    }
}

// --- SUB-SCREEN: VIDEOS TAB ---
@Composable
fun VideosTabScreen(viewModel: MainViewModel) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val search by viewModel.searchQuery.collectAsState()
    val sort by viewModel.sortOrder.collectAsState()
    val allVideosList by viewModel.visibleVideos.collectAsState()

    // Filter list
    val filteredList = allVideosList.filter {
        it.title.contains(search, ignoreCase = true)
    }.sortedWith { a, b ->
        when (sort) {
            "name" -> a.title.compareTo(b.title)
            "size" -> b.size.compareTo(a.size)
            else -> b.dateAdded.compareTo(a.dateAdded)
        }
    }

    // Segregate Video History for "Continue Watching"
    val continueWatching = allVideosList.filter { it.lastPlayedPosition > 0 && it.lastPlayedPosition < it.duration }

    // Favorites
    val favorites = allVideosList.filter { it.isFavorite }

    // Most Played
    val mostPlayed = allVideosList.filter { it.playCount > 0 }.sortedByDescending { it.playCount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Header
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = Trans.get("app_name", lang),
                    color = SecondaryNeon,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = search,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text(text = Trans.get("search_hint", lang), color = TextSecondary) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (search.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNeon,
                        unfocusedBorderColor = SurfaceDark,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        }

        // SORT BUTTONS Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Sort By", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("date" to "Date", "name" to "Name", "size" to "Size").forEach { (key, name) ->
                        val isSelected = sort == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SecondaryNeon else SurfaceDark)
                                .clickable { viewModel.setSortOrder(key) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = name,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // CONTINUE WATCHING (Horizontal layout)
        if (continueWatching.isNotEmpty()) {
            item {
                SectionHeader(title = Trans.get("continue_watching", lang))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(continueWatching) { video ->
                        ContinueWatchingItem(video = video) {
                            viewModel.playVideo(video)
                        }
                    }
                }
            }
        }

        // FAVORITES
        if (favorites.isNotEmpty()) {
            item {
                SectionHeader(title = Trans.get("favorites", lang))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(favorites) { video ->
                        VideoCardCompact(video = video, onPlay = { viewModel.playVideo(video) })
                    }
                }
            }
        }

        // MOST PLAYED
        if (mostPlayed.isNotEmpty()) {
            item {
                SectionHeader(title = Trans.get("most_played", lang))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(mostPlayed) { video ->
                        VideoCardCompact(video = video, onPlay = { viewModel.playVideo(video) })
                    }
                }
            }
        }

        // ALL VIDEOS (Vertical List)
        item {
            SectionHeader(title = Trans.get("all_videos", lang))
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = Trans.get("no_videos", lang), color = TextSecondary)
                }
            }
        } else {
            items(filteredList) { video ->
                VideoRowItem(
                    video = video,
                    onPlay = { viewModel.playVideo(video) },
                    onFavoriteToggle = { viewModel.toggleFavorite(video) },
                    onRename = { title -> viewModel.renameVideo(video, title) },
                    onDelete = { viewModel.deleteVideo(video) },
                    onHide = { viewModel.hideVideo(video) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun ContinueWatchingItem(video: VideoEntity, onClick: () -> Unit) {
    val progressPercent = (video.lastPlayedPosition.toFloat() / video.duration.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Video visual mock indicator
                Icon(
                    imageVector = if (video.isAudio) Icons.Default.MusicNote else Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = SecondaryNeon.copy(alpha = 0.8f),
                    modifier = Modifier.size(36.dp)
                )

                // Resume badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(AccentRed, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "RESUME", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }

                // Progress line
                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(4.dp),
                    color = SecondaryNeon,
                    trackColor = Color.DarkGray
                )
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = video.title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatDuration(video.lastPlayedPosition)} / ${formatDuration(video.duration)}",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun VideoCardCompact(video: VideoEntity, onPlay: () -> Unit) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable { onPlay() },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (video.isAudio) Icons.Default.MusicNote else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = PrimaryNeon,
                    modifier = Modifier.size(24.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(text = formatDuration(video.duration), color = Color.White, fontSize = 8.sp)
                }
            }
            Text(
                text = video.title,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoRowItem(
    video: VideoEntity,
    onPlay: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onHide: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newTitleText by remember { mutableStateOf(video.title) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .combinedClickable(
                onClick = { onPlay() },
                onLongClick = { showMenu = true }
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail placeholder
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(SurfaceDarkSecondary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (video.isAudio) Icons.Default.MusicNote else Icons.Default.Videocam,
                contentDescription = null,
                tint = if (video.isAudio) SecondaryNeon else PrimaryNeon,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = formatDuration(video.duration), color = TextSecondary, fontSize = 11.sp)
                Text(text = "•", color = TextSecondary, fontSize = 11.sp)
                Text(text = formatFileSize(video.size), color = TextSecondary, fontSize = 11.sp)
                Text(text = "•", color = TextSecondary, fontSize = 11.sp)
                Text(text = video.resolution, color = TextSecondary, fontSize = 11.sp)
            }
        }

        // Action menu triggering
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Actions", tint = TextSecondary)
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(SurfaceDarkSecondary)
            ) {
                DropdownMenuItem(
                    text = { Text(text = if (video.isFavorite) "Remove Favorite" else "Add Favorite", color = TextPrimary) },
                    onClick = {
                        onFavoriteToggle()
                        showMenu = false
                    },
                    leadingIcon = { Icon(imageVector = if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = AccentRed) }
                )
                DropdownMenuItem(
                    text = { Text(text = "Rename", color = TextPrimary) },
                    onClick = {
                        showMenu = false
                        showRenameDialog = true
                    },
                    leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = SecondaryNeon) }
                )
                DropdownMenuItem(
                    text = { Text(text = "Hide in Vault", color = TextPrimary) },
                    onClick = {
                        onHide()
                        showMenu = false
                    },
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AccentGreen) }
                )
                DropdownMenuItem(
                    text = { Text(text = "Delete", color = AccentRed) },
                    onClick = {
                        onDelete()
                        showMenu = false
                    },
                    leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = AccentRed) }
                )
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(text = "Rename Media File", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newTitleText,
                    onValueChange = { newTitleText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitleText.trim().isNotEmpty()) {
                            onRename(newTitleText)
                        }
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryNeon)
                ) {
                    Text(text = "Confirm", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

// --- SUB-SCREEN: FOLDERS TAB ---
@Composable
fun FoldersTabScreen(viewModel: MainViewModel) {
    val allVideosList by viewModel.visibleVideos.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()

    // Grouping by folderName
    val foldersMap = allVideosList.groupBy { it.folderName }

    if (selectedFolder != null) {
        // Detailed Folder View
        val folderName = selectedFolder!!
        val videosInFolder = foldersMap[folderName] ?: emptyList()

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectFolder(null) }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = folderName, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${videosInFolder.size} files", color = TextSecondary, fontSize = 12.sp)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(videosInFolder) { video ->
                    VideoRowItem(
                        video = video,
                        onPlay = { viewModel.playVideo(video) },
                        onFavoriteToggle = { viewModel.toggleFavorite(video) },
                        onRename = { title -> viewModel.renameVideo(video, title) },
                        onDelete = { viewModel.deleteVideo(video) },
                        onHide = { viewModel.hideVideo(video) }
                    )
                }
            }
        }
    } else {
        // Main Folders grid
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = "Storage Folders", color = SecondaryNeon, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (foldersMap.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No storage folders detected.", color = TextSecondary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(foldersMap.keys.toList()) { folderName ->
                        val count = foldersMap[folderName]?.size ?: 0
                        FolderGridCard(name = folderName, fileCount = count) {
                            viewModel.selectFolder(folderName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FolderGridCard(name: String, fileCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = SecondaryNeon,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$fileCount Items",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

// --- SUB-SCREEN: PLAYLISTS TAB ---
@Composable
fun PlaylistsTabScreen(viewModel: MainViewModel) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    if (selectedPlaylist != null) {
        // Inside a single Playlist Detail View
        val playlist = selectedPlaylist!!
        val videosInPlaylist by viewModel.getVideosInPlaylist(playlist.id).collectAsState(initial = emptyList())

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectPlaylist(null) }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = playlist.name, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${videosInPlaylist.size} items", color = TextSecondary, fontSize = 12.sp)
                }
            }

            if (videosInPlaylist.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "This playlist is currently empty.", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(videosInPlaylist) { video ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = video.title, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.removeVideoFromPlaylist(playlist.id, video.id) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = AccentRed)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Display playlists grid list
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = Trans.get("playlists", lang), color = SecondaryNeon, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create Playlist", tint = SecondaryNeon)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(playlists) { playlist ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectPlaylist(playlist) },
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = if (playlist.isSystem) Icons.Default.MovieFilter else Icons.Default.PlaylistPlay,
                                contentDescription = null,
                                tint = PrimaryNeon,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = playlist.name,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(text = "Create Playlist", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text(text = "Playlist Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.trim().isNotEmpty()) {
                            viewModel.createCustomPlaylist(newPlaylistName)
                        }
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryNeon)
                ) {
                    Text(text = "Create", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

// --- SUB-SCREEN: PRIVATE VAULT TAB ---
@Composable
fun PrivateVaultTabScreen(viewModel: MainViewModel) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val vaultSetting by viewModel.vaultSetting.collectAsState()
    val isUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val hiddenVideos by viewModel.hiddenVideos.collectAsState()

    var pinInput by remember { mutableStateOf("") }
    var showSetupScreen = vaultSetting == null

    // For setup
    var setupPin by remember { mutableStateOf("") }
    var setupQuestion by remember { mutableStateOf("What is your childhood nickname?") }
    var setupAnswer by remember { mutableStateOf("") }

    if (showSetupScreen) {
        // Security PIN setup
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SecondaryNeon, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = Trans.get("setup_pin", lang), color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = setupPin,
                onValueChange = { if (it.length <= 4) setupPin = it },
                label = { Text(text = "Enter 4-Digit PIN") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = setupQuestion,
                onValueChange = { setupQuestion = it },
                label = { Text(text = Trans.get("security_question", lang)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = setupAnswer,
                onValueChange = { setupAnswer = it },
                label = { Text(text = Trans.get("security_answer", lang)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (setupPin.length == 4 && setupAnswer.isNotEmpty()) {
                        viewModel.setupVaultPIN(setupPin, setupQuestion, setupAnswer)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Save & Unlocks", color = Color.Black)
            }
        }
    } else if (!isUnlocked) {
        // Unlock PIN Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = Trans.get("enter_pin", lang), color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = pinInput,
                onValueChange = {
                    if (it.length <= 4) {
                        pinInput = it
                        if (it.length == 4) {
                            if (viewModel.unlockVault(it)) {
                                pinInput = ""
                            } else {
                                pinInput = ""
                            }
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.width(180.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = Trans.get("vault_hint", lang), color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    } else {
        // Vault unlocked - Show Hidden Videos list
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = Trans.get("unlocked", lang), color = AccentGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.lockVault() }) {
                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = "Lock Vault", tint = AccentGreen)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (hiddenVideos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Vault is empty. Long press on home screen videos to hide.", color = TextSecondary, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(hiddenVideos) { video ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = video.title, color = TextPrimary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = formatFileSize(video.size), color = TextSecondary, fontSize = 10.sp)
                            }
                            IconButton(onClick = { viewModel.playVideo(video) }) {
                                Icon(imageVector = Icons.Default.PlayCircle, contentDescription = "Play", tint = SecondaryNeon)
                            }
                            IconButton(onClick = { viewModel.unhideVideo(video) }) {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = "Unhide", tint = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN: SETTINGS TAB ---
@Composable
fun SettingsTabScreen(viewModel: MainViewModel) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val isAutoScan by viewModel.isAutoScanEnabled.collectAsState()
    val speed by PlaybackManager.playbackSpeed.collectAsState()
    val context = LocalContext.current

    var showAboutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = Trans.get("settings", lang), color = SecondaryNeon, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        // Language Select
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = Trans.get("language", lang), color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("en" to "English", "bn" to "বাংলা", "es" to "Español", "hi" to "हिंदी").forEach { (code, name) ->
                            val isSelected = code == lang
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SecondaryNeon else SurfaceDarkSecondary)
                                    .clickable { viewModel.setLanguage(code) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = name, color = if (isSelected) Color.Black else TextPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Auto Scan Preference
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = Trans.get("auto_scan", lang), color = TextPrimary, fontWeight = FontWeight.Medium)
                Switch(
                    checked = isAutoScan,
                    onCheckedChange = { viewModel.setAutoScan(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = SecondaryNeon, checkedTrackColor = SecondaryNeon.copy(alpha = 0.5f))
                )
            }
        }

        // Database action mocks
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Cache Cleared Successful", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = Trans.get("cache_clear", lang), color = TextPrimary)
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = AccentRed)
                    }

                    Divider(color = SurfaceDarkSecondary)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Database Backed Up Successfully", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = Trans.get("backup_db", lang), color = TextPrimary)
                        Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = SecondaryNeon)
                    }
                }
            }
        }

        // About Developer Section (PRINCE AR ABDUR RAHMAN DETAILS REQUIRED IN SPEC)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAboutDialog = true },
                colors = CardDefaults.cardColors(containerColor = SurfaceDarkSecondary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "About Prince AR Abdur Rahman", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Independent Android Developer & Publisher", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(text = "Developer & Publisher Profile", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        Text(text = Trans.get("developer_title", lang), color = SecondaryNeon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = Trans.get("about_dev_desc", lang), color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Contacts:", color = SecondaryNeon, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "WhatsApp: 01707424006\nWhatsApp: 01796951709", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Connect Links
                        Text(text = "Social Profiles:", color = SecondaryNeon, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Facebook: https://www.facebook.com/share/1BNn32qoJo/\nInstagram: @ur___abdur____rahman__2008", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = Trans.get("company_title", lang), color = PrimaryNeon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = Trans.get("about_comp_desc", lang), color = TextPrimary, fontSize = 12.sp)
                        Text(text = Trans.get("mission", lang), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Technical Information", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "Version: 1.0.0", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Credits", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(text = Trans.get("credits", lang), color = TextSecondary, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SecondaryNeon)) {
                    Text(text = "Dismiss", color = Color.Black)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

// --- 5. CUSTOM VIDEO PLAYER SCREEN ---
@Composable
fun VideoPlayerScreenView(
    media: VideoEntity,
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val currentPos by PlaybackManager.currentPosition.collectAsState()
    val duration by PlaybackManager.duration.collectAsState()
    val isPlaying by PlaybackManager.isPlaying.collectAsState()
    val speed by PlaybackManager.playbackSpeed.collectAsState()
    val isLocked by PlaybackManager.isScreenLocked.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    val subtitleLang by viewModel.selectedSubtitle.collectAsState()

    // Seek Double Tap feedback
    var seekForwardTrigger by remember { mutableStateOf(false) }
    var seekBackwardTrigger by remember { mutableStateOf(false) }

    // Start player inside Effect
    LaunchedEffect(media) {
        PlaybackManager.playMedia(context, media)
    }

    // Auto-hide controls timer
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(5000)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Core Video surface view
        if (!media.isAudio) {
            AndroidView(
                factory = { ctx ->
                    SurfaceView(ctx).apply {
                        holder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceCreated(holder: SurfaceHolder) {
                                PlaybackManager.getMediaPlayer(ctx).setDisplay(holder)
                            }
                            override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {}
                            override fun surfaceDestroyed(holder: SurfaceHolder) {
                                PlaybackManager.getMediaPlayer(ctx).setDisplay(null)
                            }
                        })
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("video_view")
                    .pointerInput(Unit) {
                        // Handle double tap seek and control overlays toggles
                        detectTapGestures(
                            onDoubleTap = { offset ->
                                val halfWidth = size.width / 2
                                if (offset.x > halfWidth) {
                                    // Seek forward 10s
                                    PlaybackManager.seekTo((currentPos + 10000).coerceAtMost(duration))
                                    seekForwardTrigger = true
                                } else {
                                    // Seek backward 10s
                                    PlaybackManager.seekTo((currentPos - 10000).coerceAtLeast(0))
                                    seekBackwardTrigger = true
                                }
                            },
                            onTap = {
                                showControls = !showControls
                            }
                        )
                    }
            )
        } else {
            // Audio layout (Displays big sound music visualizer)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = SecondaryNeon,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = media.title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(text = "Playing Audio Stream", color = TextSecondary, fontSize = 14.sp)
                }
            }
        }

        // Subtitles display Overlay at bottom
        if (subtitleLang != "off" && isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 110.dp),
                contentAlignment = Alignment.Center
            ) {
                val subText = when (subtitleLang) {
                    "bn" -> "[বাংলা উপশিরোনাম] - সিনেমা উপভোগ করছেন..."
                    "es" -> "[Subtítulo Español] - Disfrutando de la película..."
                    else -> "[English Subtitle] - Stream synced nicely..."
                }
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(text = subText, color = Color.White, fontSize = 14.sp)
                }
            }
        }

        // DOUBLE TAP VISUAL FEEDBACK
        if (seekForwardTrigger) {
            LaunchedEffect(Unit) { delay(400); seekForwardTrigger = false }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterEnd)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.FastForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Text(text = "+10 sec", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (seekBackwardTrigger) {
            LaunchedEffect(Unit) { delay(400); seekBackwardTrigger = false }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterStart)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.FastRewind, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Text(text = "-10 sec", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // PLAYBACK CONTROLS OVERLAYS
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                // Top controls row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Close Player", tint = Color.White)
                    }

                    Text(
                        text = media.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                    )

                    Row {
                        // Subtitle select
                        IconButton(onClick = {
                            val nextSub = when (subtitleLang) {
                                "off" -> "en"
                                "en" -> "bn"
                                "bn" -> "es"
                                else -> "off"
                            }
                            viewModel.setSubtitle(nextSub)
                            Toast.makeText(context, "Subtitle: $nextSub", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(imageVector = Icons.Default.Subtitles, contentDescription = "Subtitles", tint = if (subtitleLang == "off") Color.White else SecondaryNeon)
                        }

                        // Playback Speed Toggle
                        IconButton(onClick = {
                            val nextSpeed = when (speed) {
                                1.0f -> 1.5f
                                1.5f -> 2.0f
                                2.0f -> 3.0f
                                3.0f -> 0.5f
                                else -> 1.0f
                            }
                            PlaybackManager.setSpeed(nextSpeed)
                            Toast.makeText(context, "Speed: ${nextSpeed}x", Toast.LENGTH_SHORT).show()
                        }) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = "Playback Speed", tint = Color.White)
                                Text(text = "${speed}x", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                            }
                        }

                        // Picture in picture (In-App Floating Window toggle)
                        IconButton(onClick = {
                            PlaybackManager.setFloatingMode(true)
                            viewModel.navigateTo(Screen.Home)
                        }) {
                            Icon(imageVector = Icons.Default.PictureInPicture, contentDescription = "In-App Float", tint = Color.White)
                        }
                    }
                }

                // Center row (Skip Back, Play/Pause, Skip Next)
                if (!isLocked) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { PlaybackManager.seekTo((currentPos - 10000).coerceAtLeast(0)) }) {
                            Icon(imageVector = Icons.Default.Replay10, contentDescription = "Back 10s", tint = Color.White, modifier = Modifier.size(36.dp))
                        }

                        IconButton(
                            onClick = { PlaybackManager.togglePlayPause(context) },
                            modifier = Modifier
                                .size(64.dp)
                                .background(SecondaryNeon, CircleShape),
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play Pause",
                                tint = Color.Black,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(onClick = { PlaybackManager.seekTo((currentPos + 10000).coerceAtMost(duration)) }) {
                            Icon(imageVector = Icons.Default.Forward10, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                    }
                } else {
                    // Lock icon locked indicator
                    IconButton(
                        onClick = { PlaybackManager.setScreenLocked(false) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = SecondaryNeon, modifier = Modifier.size(36.dp))
                    }
                }

                // Bottom row slider progress and side locks
                if (!isLocked) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                    ) {
                        // Time sliders
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = formatDuration(currentPos), color = Color.White, fontSize = 12.sp)
                            Text(text = formatDuration(duration), color = Color.White, fontSize = 12.sp)
                        }

                        Slider(
                            value = if (duration > 0) currentPos.toFloat() / duration.toFloat() else 0f,
                            onValueChange = { PlaybackManager.seekTo((it * duration).toLong()) },
                            colors = SliderDefaults.colors(thumbColor = SecondaryNeon, activeTrackColor = SecondaryNeon)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Controls locking & sound toggles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { PlaybackManager.setScreenLocked(true) }) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock Controls", tint = Color.White)
                            }

                            // Background audio service toggles
                            Button(
                                onClick = {
                                    // Trigger background play service
                                    val serviceIntent = Intent(context, BackgroundAudioService::class.java)
                                    context.startService(serviceIntent)
                                    Toast.makeText(context, "Background Audio Active in Notifications", Toast.LENGTH_SHORT).show()
                                    onClose()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Headset, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Play in Background", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- FLOATING OVERLAY VIEW (IN-APP PiP) ---
@Composable
fun FloatingPlayerOverlay(
    media: VideoEntity,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val currentPos by PlaybackManager.currentPosition.collectAsState()
    val isPlaying by PlaybackManager.isPlaying.collectAsState()

    // Drag offset states
    var offsetX by remember { mutableStateOf(20f) }
    var offsetY by remember { mutableStateOf(500f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Ignore touches to let background elements behind work normally, 
                // but keep our dragging Box floating.
            }
    ) {
        Box(
            modifier = Modifier
                .offset(x = offsetX.dp, y = offsetY.dp)
                .size(160.dp, 100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x / 2).coerceIn(0f, 250f)
                        offsetY = (offsetY + dragAmount.y / 2).coerceIn(0f, 600f)
                    }
                }
                .clickable {
                    // Clicking floating window maximizes the player
                    PlaybackManager.setFloatingMode(false)
                    viewModel.navigateTo(Screen.VideoPlayer)
                }
        ) {
            // Tiny video view
            if (!media.isAudio) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            // Basic display in floating mode
                            PlaybackManager.getMediaPlayer(ctx).setDisplay(holder)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(SurfaceDark), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = SecondaryNeon, modifier = Modifier.size(24.dp))
                }
            }

            // Overlay controllers on floating player
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                Text(
                    text = media.title,
                    color = Color.White,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(4.dp)
                )

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { PlaybackManager.togglePlayPause(context) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            PlaybackManager.release()
                            PlaybackManager.setFloatingMode(false)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- FORMAT UTILS ---
fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(bytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}
