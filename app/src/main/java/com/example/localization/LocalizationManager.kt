package com.example.localization

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val isRtl: Boolean
) {
    ARABIC("ar", "العربية", true),
    FRENCH("fr", "Français", false),
    ENGLISH("en", "English", false);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

data class AppStrings(
    val appName: String,
    val tabMusic: String,
    val tabVideo: String,
    val tabUsb: String,
    val tabSettings: String,
    
    // Music Categorization / Playlists
    val allTracks: String,
    val artists: String,
    val albums: String,
    val folders: String,
    val tracksCount: String,
    val searchPlaceholder: String,
    val noTracksFound: String,
    val noVideosFound: String,
    val noUsbFound: String,
    val unknownArtist: String,
    val unknownAlbum: String,
    val selectArtistToView: String,
    val selectAlbumToView: String,
    val backToArtists: String,
    val backToAlbums: String,
    val backToFolders: String,

    // Player
    val nowPlaying: String,
    val tapToFullscreen: String,
    val minimize: String,
    val fullscreen: String,
    val exitFullscreen: String,
    val play: String,
    val pause: String,
    val next: String,
    val previous: String,
    val selectMediaToPlay: String,
    val selectVideoToPlay: String,
    val videoPlayerHeader: String,
    val audioPlayerHeader: String,
    
    // USB & Storage
    val storageTitle: String,
    val storageScanButton: String,
    val scanningStorage: String,
    val storageMounted: String,
    val storageUnmounted: String,
    val storageRoot: String,
    val storageId: String,
    
    // Language & Car Settings
    val settingsTitle: String,
    val languageSection: String,
    val systemDefaultRuleNotice: String,
    val langArabic: String,
    val langFrench: String,
    val langEnglish: String,
    
    // Theme & Sensor & Dynamic Color
    val themeSection: String,
    val themeDark: String,
    val themeLight: String,
    val themeAutoSensor: String,
    val themeAutoTime: String,
    val themeAutoScreen: String,
    val lightSensorStatusAvailable: String,
    val lightSensorStatusUnavailable: String,
    val lightSensorAlternativeNotice: String,
    val dynamicColorTitle: String,
    val dynamicColorSubtitle: String,
    val compactScreenTitle: String,
    val compactScreenSubtitle: String,
    val compactScreenBadge: String,
    val keepScreenOnTitle: String,
    val keepScreenOnSubtitle: String,
    val keepScreenOnBadge: String,
    val fullscreenTitle: String,
    val fullscreenSubtitle: String,
    val fullscreenBadge: String,
    
    // Video Player Extra
    val videoSpeed: String,
    val videoAspect: String,
    val forward10: String,
    val replay10: String,

    // SWC
    val swcSimultaneousToast: String,
    val swcSimultaneousToggleToast: String,

    // Notifications & Extra controls
    val usbConnectedToast: String,
    val usbDisconnectedToast: String,
    val resumedPlayback: String,
    val fastForwarding: String,
    val rewinding: String,
    val internalStorage: String,

    // Video Playlists, Folders & Categorization
    val allVideos: String,
    val videoFolders: String,
    val videoPlaylists: String,
    val videoDrives: String,
    val backToVideoFolders: String,
    val backToVideoPlaylists: String,
    val backToVideoDrives: String,
    val playlistFavorites: String,
    val playlistShortClips: String,
    val playlistMovies: String,
    val playlistRecent: String,
    val searchVideosPlaceholder: String,
    val videoCountLabel: String,
    val addToFavorites: String,
    val removeFromFavorites: String,

    // Storage drives & USBs
    val storageDrives: String,
    val backToStorageDrives: String,
    val tabletInternalStorage: String,
    val sdCardStorage: String,
    val usbDriveLabel: String,
    val allStorageSources: String,

    // About App & Developer
    val aboutSectionTitle: String,
    val appDeveloperLabel: String,
    val developerName: String,
    val appVersionLabel: String,
    val appVersionValue: String,
    val appFeaturesSummary: String,

    // Auto open on USB insertion
    val autoOpenOnUsbTitle: String,
    val autoOpenOnUsbSubtitle: String,
    val autoOpenOnUsbBadge: String,

    // Fullscreen Visualizer Switch
    val visualizerModeCover: String,
    val visualizerModeBars: String,
    val toggleVisualizerTooltip: String
)

val ArabicStrings = AppStrings(
    appName = "مشغل وسائط السيارة",
    tabMusic = "موسيقى",
    tabVideo = "فيديو",
    tabUsb = "USB التخزين",
    tabSettings = "الإعدادات",
    allTracks = "جميع المقاطع",
    artists = "المغنين",
    albums = "الألبومات",
    folders = "المجلدات",
    tracksCount = "مقطع",
    searchPlaceholder = "بحث بالاسم أو الفنان...",
    noTracksFound = "لم يتم العثور على مقاطع صوتية في الـ USB",
    noVideosFound = "لم يتم العثور على مقاطع فيديو في الـ USB",
    noUsbFound = "لم يتم العثور على وحدات USB متصلة. يرجى توصيل فلاش ديسك",
    unknownArtist = "فنان غير معروف",
    unknownAlbum = "ألبوم غير معروف",
    selectArtistToView = "اختر فناناً لعرض ألبوماته وأغانيه",
    selectAlbumToView = "اختر ألبوماً لعرض مقاطعه",
    backToArtists = "العودة لقائمة المغنين",
    backToAlbums = "العودة لقائمة الألبومات",
    backToFolders = "العودة للمجلدات",
    nowPlaying = "قيد التشغيل الآن",
    tapToFullscreen = "انقر لملء الشاشة",
    minimize = "تصغير المشغل",
    fullscreen = "ملء الشاشة",
    exitFullscreen = "إنهاء ملء الشاشة",
    play = "تشغيل",
    pause = "إيقاف مؤقت",
    next = "التالي",
    previous = "السابق",
    selectMediaToPlay = "اختر مقطعاً للتشغيل في شاشة السيارة",
    selectVideoToPlay = "اختر فيديو لتشغيله في شاشة السيارة",
    videoPlayerHeader = "مشغل الفيديو الذكي",
    audioPlayerHeader = "مشغل الصوت الفائق",
    storageTitle = "وحدات التخزين (USB)",
    storageScanButton = "فحص التخزين",
    scanningStorage = "جاري الفحص التلقائي...",
    storageMounted = "متصل وجاهز",
    storageUnmounted = "غير متصل",
    storageRoot = "المسار",
    storageId = "المعرف",
    settingsTitle = "إعدادات السيارة واللغة",
    languageSection = "لغة التطبيق",
    systemDefaultRuleNotice = "اللغة الافتراضية تتبع لغة شاشة السيارة (عربي / فرنسي / إنجليزي تلقائياً، وإذا كانت لغة أخرى يتم فرض الإنجليزية)",
    langArabic = "العربية (Arabic)",
    langFrench = "الفرنسية (Français)",
    langEnglish = "الإنجليزية (English)",
    themeSection = "مظهر التطبيق والسمات",
    themeDark = "الوضع الليلي (داكن)",
    themeLight = "الوضع النهاري (فاتح)",
    themeAutoSensor = "تلقائي بحساس السيارة",
    themeAutoTime = "تلقائي حسب توقيت الساعة (شروق وغروب)",
    themeAutoScreen = "تلقائي حسب سطوع الشاشة والمصابيح",
    lightSensorStatusAvailable = "حساس إضاءة السيارة متاح: يتكيف تلقائياً بين الليل والنهار",
    lightSensorStatusUnavailable = "حساس الإضاءة غير مدعوم في جهاز سيارتك: يمكنك الاعتماد على خياري الساعة أو سطوع الشاشة",
    lightSensorAlternativeNotice = "بدائل ذكية للمركبات بدون مستشعر: التبديل التلقائي حسب توقيت الساعة أو حسب سطوع الشاشة ومصابيح السيارة!",
    dynamicColorTitle = "تلوين ديناميكي حسب غلاف الأغنية",
    dynamicColorSubtitle = "تغيير لمسات وأزرار التطبيق تلقائياً مع كل أغنية",
    compactScreenTitle = "احترام الشاشات الصغيرة",
    compactScreenSubtitle = "تحسين وضبط الأحجام والأزرار لشاشات السيارات المدمجة (7 بوصة وأقل)",
    compactScreenBadge = "وضع الشاشات المدمجة",
    keepScreenOnTitle = "إبقاء الشاشة قيد التشغيل دائماً",
    keepScreenOnSubtitle = "منع انطفاء أو قفل شاشة السيارة تلقائياً طالما أن التطبيق مفتوح",
    keepScreenOnBadge = "الشاشة تعمل دوماً",
    fullscreenTitle = "ملء الشاشة بالكامل (Fullscreen)",
    fullscreenSubtitle = "إخفاء أشرطة النظام بالكامل واستغلال شاشة السيارة بنسبة 100%",
    fullscreenBadge = "ملء الشاشة نشط",
    videoSpeed = "السرعة",
    videoAspect = "الأبعاد",
    forward10 = "+10 ث",
    replay10 = "-10 ث",
    swcSimultaneousToast = "تم الكشف عن الضغط المتزامن لأزرار المقود: إيقاف مؤقت",
    swcSimultaneousToggleToast = "أزرار المقود: تشغيل / إيقاف مؤقت",
    usbConnectedToast = "تم توصيل وحدة تخزين USB: %s",
    usbDisconnectedToast = "تم فصل وحدة تخزين USB: %s",
    resumedPlayback = "تم استئناف التشغيل من آخر نقطة توقف",
    fastForwarding = "تقديم سريع >>",
    rewinding = "<< إرجاع سريع",
    internalStorage = "ذاكرة الجهاز الداخلية",
    allVideos = "جميع الفيديوهات",
    videoFolders = "المجلدات",
    videoPlaylists = "قوائم التشغيل",
    videoDrives = "وسائط التخزين",
    backToVideoFolders = "العودة لمجلدات الفيديو",
    backToVideoPlaylists = "العودة لقوائم التشغيل",
    backToVideoDrives = "العودة لوحدات التخزين",
    playlistFavorites = "المفضلة ⭐",
    playlistShortClips = "مقاطع قصيرة (< 5 د)",
    playlistMovies = "أفلام ومسلسلات (> 10 د)",
    playlistRecent = "أحدث الفيديوهات",
    searchVideosPlaceholder = "بحث في الفيديوهات أو المجلد...",
    videoCountLabel = "%d فيديو",
    addToFavorites = "إضافة للمفضلة",
    removeFromFavorites = "إزالة من المفضلة",
    storageDrives = "وسائط التخزين",
    backToStorageDrives = "العودة لوسائط التخزين",
    tabletInternalStorage = "ذاكرة التابلت الداخلية",
    sdCardStorage = "بطاقة ذاكرة SD",
    usbDriveLabel = "فلاش USB",
    allStorageSources = "جميع الوسائط",
    aboutSectionTitle = "حول التطبيق والمعلومات",
    appDeveloperLabel = "المطور",
    developerName = "achour tech pro",
    appVersionLabel = "الإصدار",
    appVersionValue = "v2.5 (Car Head Unit Edition)",
    appFeaturesSummary = "مشغل وسائط متطور مصمم خصيصاً لشاشات سيارات أندرويد مع أزرار لمس كبيرة ودعم الفلاشات ومفاتيح المقود ومؤثرات صوتية بصرية.",
    autoOpenOnUsbTitle = "فتح التطبيق تلقائياً عند إدخال USB",
    autoOpenOnUsbSubtitle = "تشغيل التطبيق مباشرة على شاشة السيارة بمجرد توصيل فلاش ميموري أو كارت ميموري",
    autoOpenOnUsbBadge = "فتح تلقائي",
    visualizerModeCover = "غلاف الأغنية",
    visualizerModeBars = "مؤثرات الصوت (الأعمدة)",
    toggleVisualizerTooltip = "التبديل بين الغلاف وأعمدة الصوت"
)

val EnglishStrings = AppStrings(
    appName = "Car Media Player",
    tabMusic = "Music",
    tabVideo = "Video",
    tabUsb = "USB Storage",
    tabSettings = "Settings",
    allTracks = "All Tracks",
    artists = "Artists",
    albums = "Albums",
    folders = "Folders",
    tracksCount = "tracks",
    searchPlaceholder = "Search by title or artist...",
    noTracksFound = "No music tracks found on USB",
    noVideosFound = "No videos found on USB",
    noUsbFound = "No connected USB drives found. Please insert a USB drive",
    unknownArtist = "Unknown Artist",
    unknownAlbum = "Unknown Album",
    selectArtistToView = "Select an artist to view their songs",
    selectAlbumToView = "Select an album to view its tracks",
    backToArtists = "Back to Artists",
    backToAlbums = "Back to Albums",
    backToFolders = "Back to Folders",
    nowPlaying = "Now Playing",
    tapToFullscreen = "Tap for Fullscreen",
    minimize = "Minimize Player",
    fullscreen = "Fullscreen",
    exitFullscreen = "Exit Fullscreen",
    play = "Play",
    pause = "Pause",
    next = "Next",
    previous = "Previous",
    selectMediaToPlay = "Select a track to play on car display",
    selectVideoToPlay = "Select a video to play on car display",
    videoPlayerHeader = "Smart Video Player",
    audioPlayerHeader = "Car Audio Player",
    storageTitle = "USB Storage Devices",
    storageScanButton = "Scan Devices",
    scanningStorage = "Scanning USB media...",
    storageMounted = "Mounted & Ready",
    storageUnmounted = "Unmounted",
    storageRoot = "Path",
    storageId = "ID",
    settingsTitle = "Car & Language Settings",
    languageSection = "App Language",
    systemDefaultRuleNotice = "Defaults to car system language (Arabic / French / English, and strictly English for any other language)",
    langArabic = "Arabic (العربية)",
    langFrench = "French (Français)",
    langEnglish = "English (English)",
    themeSection = "App Theme & Appearance",
    themeDark = "Night Mode (Dark)",
    themeLight = "Day Mode (Light)",
    themeAutoSensor = "Auto (Car Light Sensor)",
    themeAutoTime = "Auto by Time (Sunrise/Sunset)",
    themeAutoScreen = "Auto by Screen Brightness & Lights",
    lightSensorStatusAvailable = "Car light sensor active: auto-switches day & night",
    lightSensorStatusUnavailable = "Light sensor not detected: smart alternatives (time / screen brightness) active",
    lightSensorAlternativeNotice = "Smart alternatives for vehicles without sensor: Auto-switch by Time or by Screen Brightness & Headlights!",
    dynamicColorTitle = "Dynamic Colors from Album Art",
    dynamicColorSubtitle = "Adapts accent styling to the currently playing song",
    compactScreenTitle = "Compact Screen Mode",
    compactScreenSubtitle = "Optimize layouts, margins, and controls for small car displays (≤ 7 inches)",
    compactScreenBadge = "Compact Display Mode",
    keepScreenOnTitle = "Keep Screen On Always",
    keepScreenOnSubtitle = "Prevent the car display from sleeping or dimming while the app is open",
    keepScreenOnBadge = "Screen Always On",
    fullscreenTitle = "Immersive Fullscreen",
    fullscreenSubtitle = "Hide system navigation and status bars to use 100% of the car display",
    fullscreenBadge = "Fullscreen Active",
    videoSpeed = "Speed",
    videoAspect = "Aspect",
    forward10 = "+10s",
    replay10 = "-10s",
    swcSimultaneousToast = "Steering wheel simultaneous press detected: Paused",
    swcSimultaneousToggleToast = "Steering wheel simultaneous press: Play / Pause toggle",
    usbConnectedToast = "USB Storage Connected: %s",
    usbDisconnectedToast = "USB Storage Removed: %s",
    resumedPlayback = "Resuming playback from where you left off",
    fastForwarding = "Fast Forwarding >>",
    rewinding = "<< Rewinding",
    internalStorage = "Internal Storage",
    allVideos = "All Videos",
    videoFolders = "Folders",
    videoPlaylists = "Playlists",
    videoDrives = "Drives",
    backToVideoFolders = "Back to Folders",
    backToVideoPlaylists = "Back to Playlists",
    backToVideoDrives = "Back to Drives",
    playlistFavorites = "Favorites ⭐",
    playlistShortClips = "Short Clips (< 5m)",
    playlistMovies = "Movies & Episodes (> 10m)",
    playlistRecent = "Recently Added",
    searchVideosPlaceholder = "Search videos or folder...",
    videoCountLabel = "%d videos",
    addToFavorites = "Add to Favorites",
    removeFromFavorites = "Remove from Favorites",
    storageDrives = "Storage & USB",
    backToStorageDrives = "Back to Storage Drives",
    tabletInternalStorage = "Tablet Internal Storage",
    sdCardStorage = "SD Card Storage",
    usbDriveLabel = "USB Drive",
    allStorageSources = "All Storage Sources",
    aboutSectionTitle = "About App & System",
    appDeveloperLabel = "Developer",
    developerName = "achour tech pro",
    appVersionLabel = "Version",
    appVersionValue = "v2.5 (Car Head Unit Edition)",
    appFeaturesSummary = "Automotive-tailored media player designed for car Android head units with massive controls, USB OTG detection, steering wheel controls, and live audio visualizers.",
    autoOpenOnUsbTitle = "Auto-launch on USB Insertion",
    autoOpenOnUsbSubtitle = "Automatically bring Car Media Player to the screen when a USB drive is connected",
    autoOpenOnUsbBadge = "Auto Launch",
    visualizerModeCover = "Album Cover",
    visualizerModeBars = "Audio Visualizer Bars",
    toggleVisualizerTooltip = "Toggle between Album Art and Dynamic Equalizer Bars"
)

val FrenchStrings = AppStrings(
    appName = "Lecteur Média Voiture",
    tabMusic = "Musique",
    tabVideo = "Vidéo",
    tabUsb = "Stockage USB",
    tabSettings = "Paramètres",
    allTracks = "Tous les titres",
    artists = "Artistes",
    albums = "Albums",
    folders = "Dossiers",
    tracksCount = "pistes",
    searchPlaceholder = "Rechercher par titre ou artiste...",
    noTracksFound = "Aucune piste musicale trouvée sur la clé USB",
    noVideosFound = "Aucune vidéo trouvée sur la clé USB",
    noUsbFound = "Aucun support USB détecté. Veuillez brancher une clé USB",
    unknownArtist = "Artiste Inconnu",
    unknownAlbum = "Album Inconnu",
    selectArtistToView = "Sélectionnez un artiste pour afficher ses titres",
    selectAlbumToView = "Sélectionnez un album pour afficher ses pistes",
    backToArtists = "Retour aux artistes",
    backToAlbums = "Retour aux albums",
    backToFolders = "Retour aux dossiers",
    nowPlaying = "Lecture en cours",
    tapToFullscreen = "Appuyez pour plein écran",
    minimize = "Réduire le lecteur",
    fullscreen = "Plein écran",
    exitFullscreen = "Quitter plein écran",
    play = "Lecture",
    pause = "Pause",
    next = "Suivant",
    previous = "Précédent",
    selectMediaToPlay = "Sélectionnez un titre à lire sur l'écran du véhicule",
    selectVideoToPlay = "Sélectionnez une vidéo à lire sur l'écran du véhicule",
    videoPlayerHeader = "Lecteur Vidéo Intelligent",
    audioPlayerHeader = "Lecteur Audio Voiture",
    storageTitle = "Stockage USB",
    storageScanButton = "Analyser",
    scanningStorage = "Analyse des médias USB en cours...",
    storageMounted = "Monté et prêt",
    storageUnmounted = "Non monté",
    storageRoot = "Chemin",
    storageId = "Identifiant",
    settingsTitle = "Paramètres du véhicule et langue",
    languageSection = "Langue de l'application",
    systemDefaultRuleNotice = "Par défaut selon la langue du véhicule (Arabe / Français / Anglais, et strictement Anglais pour toute autre langue)",
    langArabic = "Arabe (العربية)",
    langFrench = "Français (Français)",
    langEnglish = "Anglais (English)",
    themeSection = "Thème de l'application",
    themeDark = "Mode Nuit (Sombre)",
    themeLight = "Mode Jour (Clair)",
    themeAutoSensor = "Auto (Capteur de lumière)",
    themeAutoTime = "Auto selon l'heure (Lever/Coucher)",
    themeAutoScreen = "Auto selon luminosité de l'écran",
    lightSensorStatusAvailable = "Capteur de luminosité actif : bascule auto jour/nuit",
    lightSensorStatusUnavailable = "Capteur non détecté : alternatives intelligentes (heure / luminosité) actives",
    lightSensorAlternativeNotice = "Alternatives pour véhicules sans capteur : bascule selon l'heure ou la luminosité de l'écran !",
    dynamicColorTitle = "Couleurs dynamiques selon la pochette",
    dynamicColorSubtitle = "Adapte les accents et touches selon la chanson en cours",
    compactScreenTitle = "Mode Petits Écrans",
    compactScreenSubtitle = "Optimiser les marges et les boutons pour petits écrans de voiture (≤ 7 pouces)",
    compactScreenBadge = "Mode Écran Compact",
    keepScreenOnTitle = "Garder l'écran allumé",
    keepScreenOnSubtitle = "Empêcher la mise en veille de l'écran pendant que l'application est ouverte",
    keepScreenOnBadge = "Écran toujours actif",
    fullscreenTitle = "Plein écran immersif",
    fullscreenSubtitle = "Masquer les barres système pour occuper 100% de l'écran de la voiture",
    fullscreenBadge = "Plein Écran Actif",
    videoSpeed = "Vitesse",
    videoAspect = "Format",
    forward10 = "+10s",
    replay10 = "-10s",
    swcSimultaneousToast = "Commandes au volant : touches simultanées détectées (Pause)",
    swcSimultaneousToggleToast = "Commandes au volant : touches simultanées (Lecture / Pause)",
    usbConnectedToast = "Clé USB connectée : %s",
    usbDisconnectedToast = "Clé USB retirée : %s",
    resumedPlayback = "Reprise de la lecture là où vous vous étiez arrêté",
    fastForwarding = "Avance rapide >>",
    rewinding = "<< Retour rapide",
    internalStorage = "Stockage interne",
    allVideos = "Toutes les vidéos",
    videoFolders = "Dossiers",
    videoPlaylists = "Listes de lecture",
    videoDrives = "Supports",
    backToVideoFolders = "Retour aux dossiers",
    backToVideoPlaylists = "Retour aux listes",
    backToVideoDrives = "Retour aux supports",
    playlistFavorites = "Favoris ⭐",
    playlistShortClips = "Courts métrages (< 5m)",
    playlistMovies = "Films & Épisodes (> 10m)",
    playlistRecent = "Récemment ajoutées",
    searchVideosPlaceholder = "Rechercher des vidéos ou dossiers...",
    videoCountLabel = "%d vidéos",
    addToFavorites = "Ajouter aux favoris",
    removeFromFavorites = "Retirer des favoris",
    storageDrives = "Supports & USB",
    backToStorageDrives = "Retour aux supports",
    tabletInternalStorage = "Stockage interne tablette",
    sdCardStorage = "Carte mémoire SD",
    usbDriveLabel = "Clé USB",
    allStorageSources = "Tous les supports",
    aboutSectionTitle = "À propos de l'application",
    appDeveloperLabel = "Développeur",
    developerName = "achour tech pro",
    appVersionLabel = "Version",
    appVersionValue = "v2.5 (Car Head Unit Edition)",
    appFeaturesSummary = "Lecteur multimédia automobile optimisé pour écrans de voiture Android, commandes tactiles géantes, boutons au volant et visualiseur audio dynamique.",
    autoOpenOnUsbTitle = "Ouverture automatique à l'insertion USB",
    autoOpenOnUsbSubtitle = "Lancer ou afficher l'application directement à la connexion d'une clé USB",
    autoOpenOnUsbBadge = "Lancement auto",
    visualizerModeCover = "Pochette d'album",
    visualizerModeBars = "Spectre audio (Barres)",
    toggleVisualizerTooltip = "Basculer entre pochette et barres d'égaliseur"
)

class LocalizationManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("car_media_lang_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_LANGUAGE = "key_user_selected_language"

        /**
         * Strict Rule:
         * If device system language is Arabic -> Arabic
         * If device system language is French -> French
         * If device system language is anything else -> STRICTLY English
         */
        fun getSystemDefaultLanguage(): AppLanguage {
            val systemLang = Locale.getDefault().language.lowercase()
            return when {
                systemLang.startsWith("ar") -> AppLanguage.ARABIC
                systemLang.startsWith("fr") -> AppLanguage.FRENCH
                else -> AppLanguage.ENGLISH
            }
        }
    }

    private val _currentLanguage = MutableStateFlow(determineInitialLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private fun determineInitialLanguage(): AppLanguage {
        val savedCode = prefs.getString(KEY_USER_LANGUAGE, null)
        return if (savedCode != null) {
            AppLanguage.fromCode(savedCode)
        } else {
            getSystemDefaultLanguage()
        }
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_USER_LANGUAGE, language.code).apply()
        _currentLanguage.value = language
    }

    fun resetToSystemDefault() {
        prefs.edit().remove(KEY_USER_LANGUAGE).apply()
        _currentLanguage.value = getSystemDefaultLanguage()
    }

    fun getStrings(language: AppLanguage = _currentLanguage.value): AppStrings {
        return when (language) {
            AppLanguage.ARABIC -> ArabicStrings
            AppLanguage.FRENCH -> FrenchStrings
            AppLanguage.ENGLISH -> EnglishStrings
        }
    }
}

val LocalAppStrings = compositionLocalOf { EnglishStrings }
