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

interface AppStrings {
    val appName: String
    val tabMusic: String
    val tabVideo: String
    val tabUsb: String
    val tabSettings: String
    
    // Music Categorization / Playlists
    val allTracks: String
    val artists: String
    val albums: String
    val folders: String
    val tracksCount: String
    val searchPlaceholder: String
    val noTracksFound: String
    val noVideosFound: String
    val noUsbFound: String
    val unknownArtist: String
    val unknownAlbum: String
    val selectArtistToView: String
    val selectAlbumToView: String
    val backToArtists: String
    val backToAlbums: String
    val backToFolders: String
    val musicPlaylists: String
    val backToMusicPlaylists: String

    // Player
    val nowPlaying: String
    val tapToFullscreen: String
    val minimize: String
    val fullscreen: String
    val exitFullscreen: String
    val play: String
    val pause: String
    val next: String
    val previous: String
    val selectMediaToPlay: String
    val selectVideoToPlay: String
    val videoPlayerHeader: String
    val audioPlayerHeader: String
    
    // USB & Storage
    val storageTitle: String
    val storageScanButton: String
    val scanningStorage: String
    val storageMounted: String
    val storageUnmounted: String
    val storageRoot: String
    val storageId: String
    
    // Language & Car Settings
    val settingsTitle: String
    val languageSection: String
    val systemDefaultRuleNotice: String
    val langArabic: String
    val langFrench: String
    val langEnglish: String
    
    // Theme & Sensor & Dynamic Color
    val themeSection: String
    val themeDark: String
    val themeLight: String
    val themeAutoSensor: String
    val themeAutoTime: String
    val themeAutoScreen: String
    val lightSensorStatusAvailable: String
    val lightSensorStatusUnavailable: String
    val lightSensorAlternativeNotice: String
    val dynamicColorTitle: String
    val dynamicColorSubtitle: String
    val compactScreenTitle: String
    val compactScreenSubtitle: String
    val compactScreenBadge: String
    val keepScreenOnTitle: String
    val keepScreenOnSubtitle: String
    val keepScreenOnBadge: String
    val fullscreenTitle: String
    val fullscreenSubtitle: String
    val fullscreenBadge: String
    
    // Video Player Extra
    val videoSpeed: String
    val videoAspect: String
    val forward10: String
    val replay10: String

    // SWC
    val swcSimultaneousToast: String
    val swcSimultaneousToggleToast: String
    val swcSettingsSectionTitle: String
    val swcSettingsSectionSubtitle: String
    val swcPresetTitle: String
    val swcPresetSmartAuto: String
    val swcPresetDedicatedPause: String
    val swcPresetSlowCanbus: String
    val swcPresetPeugeot: String
    val swcPresetCustom: String
    val swcDualPressModeTitle: String
    val swcDualPressModeSubtitle: String
    val swcDualModeBoth: String
    val swcDualModeStrictSimultaneous: String
    val swcDualModeSequential: String
    val swcDualModeDisabled: String
    val swcDualPressWindowLabel: String
    val swcDedicatedPauseTitle: String
    val swcDedicatedPauseSubtitle: String
    val swcLongPressModeTitle: String
    val swcLongPressModeSubtitle: String
    val swcLongPressModeContinuous: String
    val swcLongPressModeStep: String
    val swcLongPressModeFolder: String
    val swcLongPressModeDisabled: String
    val swcSeekStepLabel: String
    val swcHoldThresholdLabel: String
    val swcExtendedKeysTitle: String
    val swcExtendedKeysSubtitle: String
    val swcDebounceTitle: String
    val swcDebounceSubtitle: String
    val swcTesterTitle: String
    val swcTesterSubtitle: String
    val swcTesterWaiting: String
    val swcTesterKeyDetected: String
    val swcTesterAction: String
    val swcTesterMappedFunction: String
    val swcCustomizationTabTitle: String
    val swcMapFunctionsTitle: String
    val swcMapNextLabel: String
    val swcMapPrevLabel: String
    val swcMapPlayPauseLabel: String
    val swcMapSelectHint: String
    val swcMapWaiting: String

    // Notifications & Extra controls
    val usbConnectedToast: String
    val usbDisconnectedToast: String
    val resumedPlayback: String
    val fastForwarding: String
    val rewinding: String
    val internalStorage: String

    // Video Playlists, Folders & Categorization
    val allVideos: String
    val videoFolders: String
    val videoPlaylists: String
    val videoDrives: String
    val backToVideoFolders: String
    val backToVideoPlaylists: String
    val backToVideoDrives: String
    val playlistFavorites: String
    val playlistShortClips: String
    val playlistMovies: String
    val playlistRecent: String
    val searchVideosPlaceholder: String
    val videoCountLabel: String
    val addToFavorites: String
    val removeFromFavorites: String

    // Custom Playlists & Actions
    val createPlaylist: String
    val playlistNamePlaceholder: String
    val customPlaylists: String
    val addToPlaylist: String
    val removeFromPlaylist: String
    val deleteMediaItem: String
    val deleteMediaConfirmTitle: String
    val deleteMediaConfirmMessage: String
    val deletePlaylistConfirmTitle: String
    val deletePlaylistConfirmMessage: String
    val itemAddedToPlaylist: String
    val itemRemovedFromPlaylist: String
    val itemDeleted: String
    val noCustomPlaylists: String
    val trackActionTitle: String
    val emptyPlaylist: String
    val selectPlaylistToAdd: String
    val newPlaylistTitle: String
    val confirmDelete: String
    val bulkDeleteConfirmMessage: String
    val noPlaylistsFound: String

    // Storage drives & USBs
    val storageDrives: String
    val backToStorageDrives: String
    val tabletInternalStorage: String
    val sdCardStorage: String
    val usbDriveLabel: String
    val allStorageSources: String

    // About App & Developer
    val aboutSectionTitle: String
    val appDeveloperLabel: String
    val developerName: String
    val appVersionLabel: String
    val appVersionValue: String
    val appFeaturesSummary: String

    // Auto open on USB insertion
    val autoOpenOnUsbTitle: String
    val autoOpenOnUsbSubtitle: String
    val autoOpenOnUsbBadge: String

    // Fullscreen Visualizer Switch
    val visualizerModeCover: String
    val visualizerModeBars: String
    val toggleVisualizerTooltip: String

    // Screen Scale & Font Size Settings
    val screenSizeTitle: String
    val screenSizeSubtitle: String
    val fontSizeTitle: String
    val fontSizeSubtitle: String

    // Custom SWC Profile & Buttons
    val swcCustomSectionTitle: String
    val swcCustomSectionSubtitle: String
    val swcCustomBtn1Label: String
    val swcCustomBtn2Label: String
    val swcCustomAccelLabel: String
    val saveCustomProfileBtn: String

    // Dashboard Widgets
    val widgetsSectionTitle: String
    val widgetsSectionSubtitle: String
    val showClock: String
    val showDate: String
    val showTemp: String
    val showSpeed: String
    val speedUnit: String
    val tempUnit: String
    val speedUnitMph: String
    val tempUnitFahrenheit: String
    val tempCalibrationLabel: String
    val speedSourceGps: String
    val speedSourceCan: String
    val gpsPermissionRequired: String
    val tempSensorUnavailable: String
    val tempSourceCanBus: String
    val tempSourceSensor: String

    // Dedicated Sensors & Telemetry Management
    val sensorsSectionTitle: String
    val sensorsSectionSubtitle: String
    val tempSensorSourceTitle: String
    val tempSensorSourceSubtitle: String
    val tempSourceAuto: String
    val tempSourceAmbientHardware: String
    val tempSourceBattery: String
    val tempSourceCpuSysfs: String
    val tempSourceManual: String
    val speedSensorSourceTitle: String
    val speedSensorSourceSubtitle: String
    val speedSourceGpsHighPrecision: String
    val speedSourceNetworkCell: String
    val speedSourceSimulated: String
    val sensorDiagnosticsTitle: String
    val sensorDiagnosticsSubtitle: String
    val sensorStatusDetected: String
    val sensorStatusNotFound: String
    val sensorStatusActive: String
    val manualTempPresetTitle: String
    val simulatedSpeedPresetTitle: String
    val speedMultiplierLabel: String
    val sensorsTabTitle: String

    // Sensors Search & Discovery
    val scanSensorsButton: String
    val scanSensorsSubtitle: String
    val sensorScannerDialogTitle: String
    val filterAll: String
    val filterTemp: String
    val filterSpeed: String
    val filterThermalSysfs: String
    val filterGps: String
    val setAsTempSensor: String
    val setAsSpeedSensor: String
    val activeSensorBadge: String
    val grantLocationPermission: String
    val openLocationSettings: String
    val gpsPermissionGranted: String
    val gpsPermissionDenied: String
    val rescanSensors: String
    val tempSourceCustomSensor: String
    val tempSourceCustomSysfs: String
    val speedSourceCustomSensor: String
    val liveSensorValues: String
    val searchSensorsPlaceholder: String
    val noSensorsFoundForFilter: String
    val sensorsFoundCountSummary: String

    // CAN-Bus Integration
    val canBusSectionTitle: String
    val canBusSectionSubtitle: String
    val canBusKeyRemapTitle: String
    val canBusWheelSwitchTitle: String
    val canBusWheelSwitchSubtitle: String
    val canBusProtocolTitle: String
    val canBusCarStatusTitle: String
    val canBusVoltage: String
    val canBusFuel: String
    val canBusDoors: String
    val canBusCleanUpSwc: String
    val canBusSection: String
    val carSpeedLabel: String
    val carTempLabel: String
    val canBusWheelModeLabel: String
    val canBusWheelModeNormal: String
    val canBusWheelModeReversed: String
    val canBusWheelModeSmart: String
    val canBusProtocolLabel: String
    val canBusProtocolGeneric: String
    val peugeotModeBadge: String
    val peugeotModeHelp: String

    // Additional Localization fields
    val resetToSystemLanguage: String
    val mediaSessionActiveTitle: String
    val mediaSessionActiveDesc: String
    val supportedSwcButtonsTitle: String
    val btnPlayPause: String
    val btnNextTrack: String
    val btnPrevTrack: String
    val btnFastForward: String
    val btnRewind: String
    val btnStop: String
    val audioFocusTitle: String
    val audioFocusDesc: String
    val canBusIntegrationTitle: String
    val canBusIntegrationDesc: String
    val systemOptimizationsTitle: String
    val optImmersiveFullscreen: String
    val optKeepScreenOn: String
    val optBufferControl: String
    val statusActive: String
    val createVideoPlaylist: String
    val smartPlaylists: String
    val multiSelect: String
    val cancel: String
    val delete: String
    val close: String
    val createPlaylistSubtitleMusic: String
    val createPlaylistSubtitleVideo: String
}

object ArabicStrings : AppStrings {
    override val appName = "مشغل وسائط السيارة"
    override val tabMusic = "موسيقى"
    override val tabVideo = "فيديو"
    override val tabUsb = "وحدات التخزين"
    override val tabSettings = "الإعدادات"
    override val allTracks = "جميع المقاطع"
    override val artists = "الفنانون"
    override val albums = "الألبومات"
    override val folders = "المجلدات"
    override val tracksCount = "مقطع"
    override val searchPlaceholder = "بحث بالاسم أو الفنان..."
    override val noTracksFound = "لم يتم العثور على مقاطع صوتية في وحدة التخزين"
    override val noVideosFound = "لم يتم العثور على مقاطع فيديو في وحدة التخزين"
    override val noUsbFound = "لم يتم العثور على وحدات تخزين متصلة. يرجى توصيل وحدة تخزين"
    override val unknownArtist = "فنان غير معروف"
    override val unknownAlbum = "ألبوم غير معروف"
    override val selectArtistToView = "اختر فناناً لعرض ألبوماته وأغانيه"
    override val selectAlbumToView = "اختر ألبوماً لعرض مقاطعه"
    override val backToArtists = "العودة لقائمة الفنانين"
    override val backToAlbums = "العودة لقائمة الألبومات"
    override val backToFolders = "العودة للمجلدات"
    override val musicPlaylists = "قوائم التشغيل"
    override val backToMusicPlaylists = "العودة لقوائم التشغيل"
    override val nowPlaying = "قيد التشغيل الآن"
    override val tapToFullscreen = "انقر لملء الشاشة"
    override val minimize = "تصغير المشغل"
    override val fullscreen = "ملء الشاشة"
    override val exitFullscreen = "إنهاء ملء الشاشة"
    override val play = "تشغيل"
    override val pause = "إيقاف مؤقت"
    override val next = "التالي"
    override val previous = "السابق"
    override val selectMediaToPlay = "اختر مقطعاً للتشغيل في شاشة السيارة"
    override val selectVideoToPlay = "اختر فيديو لتشغيله في شاشة السيارة"
    override val videoPlayerHeader = "مشغل الفيديو الذكي"
    override val audioPlayerHeader = "مشغل الصوت الفائق"
    override val storageTitle = "وحدات التخزين"
    override val storageScanButton = "فحص التخزين"
    override val scanningStorage = "جاري الفحص التلقائي..."
    override val storageMounted = "متصل وجاهز"
    override val storageUnmounted = "غير متصل"
    override val storageRoot = "المسار"
    override val storageId = "المعرف"
    override val settingsTitle = "إعدادات السيارة واللغة"
    override val languageSection = "لغة التطبيق"
    override val systemDefaultRuleNotice = "اللغة الافتراضية تتبع لغة شاشة السيارة (عربي / فرنسي / إنجليزي تلقائياً، وإذا كانت لغة أخرى يتم اختيار الإنجليزية)"
    override val langArabic = "العربية"
    override val langFrench = "الفرنسية"
    override val langEnglish = "الإنجليزية"
    override val themeSection = "مظهر التطبيق والسمات"
    override val themeDark = "الوضع الليلي (داكن)"
    override val themeLight = "الوضع النهاري (فاتح)"
    override val themeAutoSensor = "تلقائي بحساس السيارة"
    override val themeAutoTime = "تلقائي حسب توقيت الساعة (شروق وغروب)"
    override val themeAutoScreen = "تلقائي حسب سطوع الشاشة والمصابيح"
    override val lightSensorStatusAvailable = "حساس إضاءة السيارة متاح: يتكيف تلقائياً بين الليل والنهار"
    override val lightSensorStatusUnavailable = "حساس الإضاءة غير مدعوم في جهاز سيارتك: يمكنك الاعتماد على خياري الساعة أو سطوع الشاشة"
    override val lightSensorAlternativeNotice = "بدائل ذكية للمركبات بدون مستشعر: التبديل التلقائي حسب توقيت الساعة أو حسب سطوع الشاشة ومصابيح السيارة!"
    override val dynamicColorTitle = "تلوين ديناميكي حسب غلاف المقطع"
    override val dynamicColorSubtitle = "تغيير لمسات وأزرار التطبيق تلقائياً مع كل مقطع"
    override val compactScreenTitle = "مراعاة الشاشات المدمجة"
    override val compactScreenSubtitle = "تحسين وضبط الأحجام والأزرار لشاشات السيارات المدمجة (7 بوصات وأقل)"
    override val compactScreenBadge = "وضع الشاشات المدمجة"
    override val keepScreenOnTitle = "إبقاء الشاشة قيد التشغيل دائماً"
    override val keepScreenOnSubtitle = "منع انطفاء أو قفل شاشة السيارة تلقائياً طالما أن التطبيق مفتوح"
    override val keepScreenOnBadge = "الشاشة تعمل دوماً"
    override val fullscreenTitle = "ملء الشاشة بالكامل"
    override val fullscreenSubtitle = "إخفاء أشرطة النظام بالكامل واستغلال مساحة شاشة السيارة"
    override val fullscreenBadge = "ملء الشاشة نشط"
    override val videoSpeed = "السرعة"
    override val videoAspect = "الأبعاد"
    override val forward10 = "+10 ث"
    override val replay10 = "-10 ث"
    override val swcSimultaneousToast = "تم الكشف عن الضغط المتزامن لأزرار المقود: إيقاف مؤقت"
    override val swcSimultaneousToggleToast = "أزرار المقود: تشغيل / إيقاف مؤقت"
    override val swcSettingsSectionTitle = "أزرار عجلة القيادة"
    override val swcSettingsSectionSubtitle = "تخصيص استجابة أزرار المقود حسب نوع سيارتك وشاشتك لاختيار التوافق المثالي"
    override val swcPresetTitle = "الوضع المسبق للسيارة"
    override val swcPresetSmartAuto = "ذكي تلقائي (موصى به)"
    override val swcPresetDedicatedPause = "سيارة بزر إيقاف مؤقت مستقل"
    override val swcPresetSlowCanbus = "نظام اتصال بطيء / أزرار مقاومة"
    override val swcPresetPeugeot = "بيجو 207 (ذراع تحكم المقود)"
    override val swcPresetCustom = "تخصيص يدوي"
    override val swcDualPressModeTitle = "سلوك الضغط على زري التبديل (التالي + السابق)"
    override val swcDualPressModeSubtitle = "اختر كيفية استقبال الشاشة للضغط على الزرين لتفعيل التشغيل / الإيقاف المؤقت"
    override val swcDualModeBoth = "متزامن ومتتابع معاً (الذكي)"
    override val swcDualModeStrictSimultaneous = "متزامن فقط (في نفس اللحظة)"
    override val swcDualModeSequential = "متتابع فقط (الأول ثم الثاني)"
    override val swcDualModeDisabled = "معطل (اعتماد أزرار منفصلة)"
    override val swcDualPressWindowLabel = "نافذة زمن التقاط الزرين"
    override val swcDedicatedPauseTitle = "السيارة تملك زر تشغيل/إيقاف مؤقت أو كتم مستقل"
    override val swcDedicatedPauseSubtitle = "إذا كانت سيارتك مزودة بزر تشغيل وإيقاف أو كتم، يمكنك تعطيل الجمع بين الزرين لتفادي الإيقاف بالخطأ"
    override val swcLongPressModeTitle = "وظيفة الضغط المطول على زري التبديل"
    override val swcLongPressModeSubtitle = "حدد ما يحدث عند الاستمرار بالضغط على زر التالي أو السابق"
    override val swcLongPressModeContinuous = "تقديم وإرجاع مستمر (تقديم سريع / إرجاع سريع)"
    override val swcLongPressModeStep = "قفزة زمنية واحدة"
    override val swcLongPressModeFolder = "تخطي المجلد / الألبوم بالكامل"
    override val swcLongPressModeDisabled = "معطل (تبديل مقطع فقط)"
    override val swcSeekStepLabel = "مقدار القفزة بالثواني"
    override val swcHoldThresholdLabel = "مدة تفعيل الضغط المطول"
    override val swcExtendedKeysTitle = "دعم الأكواد الموسعة لشاشات وأنظمة السيارات"
    override val swcExtendedKeysSubtitle = "التعرف على أزرار القنوات والأسهم وأزرار التوجيه كأزرار تبديل"
    override val swcDebounceTitle = "مانع تكرار الإشارات المزدوجة"
    override val swcDebounceSubtitle = "تجاهل النبضات السريعة المكررة التي يرسلها نظام السيارة لمنع تخطي مقطعين"
    override val swcTesterTitle = "لوحة الفحص المباشر لأزرار المقود"
    override val swcTesterSubtitle = "اضغط على أي زر على عجلة القيادة الآن لرؤية الرمز المستلم وكيف يفسره التطبيق فوراً"
    override val swcTesterWaiting = "بانتظار الضغط على أي زر من المقود..."
    override val swcTesterKeyDetected = "الزر الملتقط"
    override val swcTesterAction = "نوع الحدث"
    override val swcTesterMappedFunction = "الوظيفة المنفذة"
    override val swcCustomizationTabTitle = "تخصيص الأزرار"
    override val swcMapFunctionsTitle = "تعيين وظائف الأزرار"
    override val swcMapNextLabel = "زر التالي"
    override val swcMapPrevLabel = "زر السابق"
    override val swcMapPlayPauseLabel = "زر التشغيل والإيقاف المؤقت"
    override val swcMapSelectHint = "اضغط على الزر المراد في المقود لتعيينه"
    override val swcMapWaiting = "بانتظار الضغط..."
    override val usbConnectedToast = "تم توصيل وحدة التخزين: %s"
    override val usbDisconnectedToast = "تم فصل وحدة التخزين: %s"
    override val resumedPlayback = "تم استئناف التشغيل من آخر نقطة توقف"
    override val fastForwarding = "تقديم سريع >>"
    override val rewinding = "<< إرجاع سريع"
    override val internalStorage = "ذاكرة الجهاز الداخلية"
    override val allVideos = "جميع مقاطع الفيديو"
    override val videoFolders = "المجلدات"
    override val videoPlaylists = "قوائم التشغيل"
    override val videoDrives = "وسائط التخزين"
    override val backToVideoFolders = "العودة لمجلدات الفيديو"
    override val backToVideoPlaylists = "العودة لقوائم التشغيل"
    override val backToVideoDrives = "العودة لوسائط التخزين"
    override val playlistFavorites = "المفضلة ⭐"
    override val playlistShortClips = "مقاطع قصيرة (أقل من 5 دقائق)"
    override val playlistMovies = "أفلام ومقاطع طويلة (أكثر من 10 دقائق)"
    override val playlistRecent = "أحدث مقاطع الفيديو المضافة"
    override val searchVideosPlaceholder = "بحث في الفيديوهات أو المجلد..."
    override val videoCountLabel = "%d مقطع فيديو"
    override val addToFavorites = "إضافة للمفضلة"
    override val removeFromFavorites = "إزالة من المفضلة"
    override val createPlaylist = "إنشاء قائمة تشغيل جديدة"
    override val playlistNamePlaceholder = "اكتب اسم قائمة التشغيل..."
    override val customPlaylists = "قوائم التشغيل الخاصة بي"
    override val addToPlaylist = "إضافة إلى قائمة تشغيل"
    override val removeFromPlaylist = "إزالة من قائمة التشغيل"
    override val deleteMediaItem = "حذف الملف"
    override val deleteMediaConfirmTitle = "تأكيد حذف الملف"
    override val deleteMediaConfirmMessage = "هل أنت متأكد من رغبتك في حذف هذا الملف نهائياً؟"
    override val deletePlaylistConfirmTitle = "حذف قائمة التشغيل"
    override val deletePlaylistConfirmMessage = "هل أنت متأكد من حذف قائمة التشغيل هذه؟"
    override val itemAddedToPlaylist = "تمت الإضافة إلى قائمة التشغيل بنجاح"
    override val itemRemovedFromPlaylist = "تمت إزالة الملف من قائمة التشغيل"
    override val itemDeleted = "تم حذف الملف بنجاح"
    override val noCustomPlaylists = "لا توجد قوائم تشغيل مخصصة، انقر على زر الإنشاء لإنشاء أول قائمة!"
    override val trackActionTitle = "خيارات الوسائط"
    override val emptyPlaylist = "هذه القائمة فارغة حالياً"
    override val selectPlaylistToAdd = "اختر قائمة التشغيل"
    override val newPlaylistTitle = "قائمة جديدة"
    override val confirmDelete = "تأكيد الحذف"
    override val bulkDeleteConfirmMessage = "هل أنت متأكد من حذف العناصر المختارة؟"
    override val noPlaylistsFound = "لم يتم العثور على قوائم تشغيل"
    override val storageDrives = "وسائط التخزين"
    override val backToStorageDrives = "العودة لوسائط التخزين"
    override val tabletInternalStorage = "ذاكرة الجهاز الداخلية"
    override val sdCardStorage = "بطاقة الذاكرة"
    override val usbDriveLabel = "وحدة تخزين خارجية"
    override val allStorageSources = "جميع وسائط التخزين"
    override val aboutSectionTitle = "حول التطبيق والمعلومات"
    override val appDeveloperLabel = "المطور"
    override val developerName = "achour tech pro"
    override val appVersionLabel = "الإصدار"
    override val appVersionValue = "الإصدار 2.5 (نسخة شاشة السيارة)"
    override val appFeaturesSummary = "مشغل وسائط متطور مصمم خصيصاً لشاشات سيارات أندرويد مع أزرار لمس كبيرة ودعم وحدات التخزين ومفاتيح المقود ومؤثرات صوتية بصرية."
    override val autoOpenOnUsbTitle = "فتح التطبيق تلقائياً عند إدخال وحدة تخزين"
    override val autoOpenOnUsbSubtitle = "تشغيل التطبيق مباشرة على شاشة السيارة بمجرد توصيل وحدة تخزين أو بطاقة ذاكرة"
    override val autoOpenOnUsbBadge = "فتح تلقائي"
    override val visualizerModeCover = "غلاف المقطع"
    override val visualizerModeBars = "مؤثرات الصوت (الأعمدة)"
    override val toggleVisualizerTooltip = "التبديل بين الغلاف وأعمدة الصوت"
    override val screenSizeTitle = "حجم الشاشة ومقياس الواجهة"
    override val screenSizeSubtitle = "ضبط حجم ومقياس عناصر واجهة مشغل السيارة لتناسب الشاشة بدقة"
    override val fontSizeTitle = "حجم الخط"
    override val fontSizeSubtitle = "تكبير أو تصغير حجم النصوص في جميع قوائم وشاشات التطبيق"
    override val swcCustomSectionTitle = "تخصيص أزرار المقود"
    override val swcCustomSectionSubtitle = "إعداد أزرار المقود حسب رغبتك وسرعة التسريع المطول"
    override val swcCustomBtn1Label = "رمز زر التالي"
    override val swcCustomBtn2Label = "رمز زر السابق"
    override val swcCustomAccelLabel = "سرعة التقديم والإرجاع المطول"
    override val saveCustomProfileBtn = "حفظ التخصيص كإعداد نهائي"

    // Dashboard Widgets
    override val widgetsSectionTitle = "إضافات لوحة القيادة"
    override val widgetsSectionSubtitle = "تكوين المعلومات المعروضة في وضع الشاشة الكاملة"
    override val showClock = "عرض الساعة"
    override val showDate = "عرض التاريخ"
    override val showTemp = "عرض درجة الحرارة"
    override val showSpeed = "عرض سرعة السيارة"
    override val speedUnit = "كم/س"
    override val tempUnit = "°م"
    override val speedUnitMph = "ميل/س"
    override val tempUnitFahrenheit = "°ف"
    override val tempCalibrationLabel = "معايرة مقياس الحرارة"
    override val speedSourceGps = "نظام الملاحة GPS"
    override val speedSourceCan = "نظام CAN-Bus"
    override val gpsPermissionRequired = "يرجى منح إذن الموقع لتتبع السرعة بدقة"
    override val tempSensorUnavailable = "مستشعر الحرارة غير مدعوم في الجهاز"
    override val tempSourceCanBus = "مقياس CAN-Bus للسيارة"
    override val tempSourceSensor = "مستشعر الحرارة المدمج"
    override val sensorsSectionTitle = "إدارة الحساسات وتتبع المركبة"
    override val sensorsSectionSubtitle = "تكافؤ ذكي متعدد الموارد للحرارة والسرعة عبر مستشعرات التابلت والسيارة"
    override val tempSensorSourceTitle = "مصدر مقياس درجة الحرارة"
    override val tempSensorSourceSubtitle = "اختر الحساس المفصل لقراءة الحرارة (محيطي، بطارية التابلت، المعالج، أو CAN-Bus)"
    override val tempSourceAuto = "تلقائي (اختيار الأفضل المتاح)"
    override val tempSourceAmbientHardware = "مستشعر الحرارة المحيطي بالجهاز"
    override val tempSourceBattery = "مستشعر حرارة بطارية التابلت"
    override val tempSourceCpuSysfs = "مستشعر حرارة معالج الجهاز (SoC)"
    override val tempSourceManual = "ضبط يدوي ثابت"
    override val speedSensorSourceTitle = "مصدر مقياس السرعة"
    override val speedSensorSourceSubtitle = "اختر مصدر تتبع السرعة (أقمار GPS، شبكة التابلت، CAN-Bus، أو وضع تجريبي)"
    override val speedSourceGpsHighPrecision = "أقمار GPS (دقة عالية)"
    override val speedSourceNetworkCell = "شبكة التابلت (تقديري)"
    override val speedSourceSimulated = "محاكاة تجريبية"
    override val sensorDiagnosticsTitle = "تشخيص الحساسات المتوفرة للجهاز"
    override val sensorDiagnosticsSubtitle = "عرض مباشر لجميع المستشعرات المكتشفة في جهاز التابلت والسيارة"
    override val sensorStatusDetected = "نشط ومكتشف"
    override val sensorStatusNotFound = "غير مدعوم بالجهاز"
    override val sensorStatusActive = "قيد القراءة الحية"
    override val manualTempPresetTitle = "درجة الحرارة اليدوية المحفوظة"
    override val simulatedSpeedPresetTitle = "السرعة التجريبية المحددة"
    override val speedMultiplierLabel = "معامل معايرة السرعة"
    override val sensorsTabTitle = "الحساسات والسرعة"

    // Sensors Search & Discovery
    override val scanSensorsButton = "البحث عن المستشعرات وفحص العتاد"
    override val scanSensorsSubtitle = "فحص عميق وشامل لجميع مستشعرات الجهاز، حرارة المعالج، والـ GPS"
    override val sensorScannerDialogTitle = "مستكشف وفاحص مستشعرات السيارة"
    override val filterAll = "الكل"
    override val filterTemp = "مستشعرات الحرارة"
    override val filterSpeed = "السرعة والحركة"
    override val filterThermalSysfs = "حرارة المعالج (Sysfs)"
    override val filterGps = "الموقع و GPS"
    override val setAsTempSensor = "تعيين كمستشعر حرارة"
    override val setAsSpeedSensor = "تعيين كمستشعر سرعة"
    override val activeSensorBadge = "المستشعر النشط"
    override val grantLocationPermission = "منح إذن الموقع لتفعيل GPS"
    override val openLocationSettings = "فتح إعدادات GPS بالجهاز"
    override val gpsPermissionGranted = "إذن الموقع ممنوح"
    override val gpsPermissionDenied = "إذن الموقع غير ممنوح (مطلوب لسرعة GPS)"
    override val rescanSensors = "إعادة الفحص الآن"
    override val tempSourceCustomSensor = "مستشعر حرارة مخصص"
    override val tempSourceCustomSysfs = "ملف حرارة المعالج (Sysfs)"
    override val speedSourceCustomSensor = "مستشعر سرعة مخصص"
    override val liveSensorValues = "القراءة الحية"
    override val searchSensorsPlaceholder = "ابحث بالاسم (مثال: temp, speed, accel, thermal)..."
    override val noSensorsFoundForFilter = "لم يتم العثور على مستشعرات تطابق هذا الفلتر"
    override val sensorsFoundCountSummary = "تم اكتشاف %d مستشعر ومصدر عتادي"

    override val canBusSectionTitle = "تكامل نظام بيانات السيارة"
    override val canBusSectionSubtitle = "إعدادات متقدمة للاتصال المباشر بنظام السيارة الإلكتروني"
    override val canBusKeyRemapTitle = "إعادة تعيين أزرار نظام السيارة"
    override val canBusWheelSwitchTitle = "تبديل وضع عجلة المقود"
    override val canBusWheelSwitchSubtitle = "تغيير كيفية تفسير إشارات التدوير من نظام السيارة"
    override val canBusProtocolTitle = "بروتوكول نظام السيارة"
    override val canBusCarStatusTitle = "معلومات وحالة السيارة"
    override val canBusVoltage = "جهد البطارية"
    override val canBusFuel = "مستوى الوقود"
    override val canBusDoors = "حالة الأبواب"
    override val canBusCleanUpSwc = "تنظيف الإعدادات غير النشطة"
    override val canBusSection = "إعدادات نظام السيارة"
    override val carSpeedLabel = "سرعة السيارة"
    override val carTempLabel = "حرارة الجو"
    override val canBusWheelModeLabel = "وضع عجلة المقود"
    override val canBusWheelModeNormal = "عادي"
    override val canBusWheelModeReversed = "معكوس"
    override val canBusWheelModeSmart = "ذكي"
    override val canBusProtocolLabel = "بروتوكول نظام السيارة"
    override val canBusProtocolGeneric = "عام (نظام أندرويد)"
    override val peugeotModeBadge = "وضع مخصص لسيارة بيجو 207"
    override val peugeotModeHelp = "تفعيل الدعم المباشر لعجلة ذراع المقود وأزرار الصوت وزر المصدر"

    // Additional Localization fields
    override val resetToSystemLanguage = "استعادة لغة النظام الافتراضية"
    override val mediaSessionActiveTitle = "نظام جلسة الوسائط القياسي نشط ومفعل"
    override val mediaSessionActiveDesc = "تقوم شاشة السيارة بتحويل أزرار المقود وإشارات الاتصال بالسيارة تلقائياً إلى أحداث الوسائط القياسية لنظام أندرويد لضمان استقرار التشغيل والتحكم المباشر."
    override val supportedSwcButtonsTitle = "الأزرار المدعومة على عجلة القيادة:"
    override val btnPlayPause = "زر التشغيل والإيقاف المؤقت"
    override val btnNextTrack = "زر المسار التالي"
    override val btnPrevTrack = "زر المسار السابق"
    override val btnFastForward = "زر التقديم السريع"
    override val btnRewind = "زر الإرجاع السريع"
    override val btnStop = "زر إيقاف الوسائط"
    override val audioFocusTitle = "إدارة التركيز الصوتي وخفض الصوت التلقائي"
    override val audioFocusDesc = "عند تحدث الملاحة أو المساعد الصوتي، ينخفض صوت الوسائط بسلاسة إلى 20%، ويعود فور انتهاء التوجيهات دون قطع مفاجئ."
    override val canBusIntegrationTitle = "تكامل شبكة بيانات السيارة مع النظام"
    override val canBusIntegrationDesc = "يقوم معالج شاشة السيارة بفك تشفير إشارات نظام السيارة وتغذية أندرويد ببيانات السرعة ومفاتيح التحكم بالوسائط مباشرة لضمان استقرار كامل وموثوقية أثناء القيادة."
    override val systemOptimizationsTitle = "تحسينات شاشة السيارة المطبقة:"
    override val optImmersiveFullscreen = "وضع ملء الشاشة الشامل"
    override val optKeepScreenOn = "إبقاء الشاشة مضاءة أثناء القيادة"
    override val optBufferControl = "إدارة التخزين المؤقت للوسائط"
    override val statusActive = "مفعل"
    override val createVideoPlaylist = "إنشاء قائمة فيديو مخصصة"
    override val smartPlaylists = "قوائم التشغيل الذكية"
    override val multiSelect = "تحديد متعدد"
    override val cancel = "إلغاء"
    override val delete = "حذف"
    override val close = "إغلاق"
    override val createPlaylistSubtitleMusic = "إنشاء قائمة تشغيل مخصصة للمقاطع المختارة"
    override val createPlaylistSubtitleVideo = "إنشاء قائمة لتجميع مقاطع الفيديو المفضلة"
}

object EnglishStrings : AppStrings {
    override val appName = "Car Media Player"
    override val tabMusic = "Music"
    override val tabVideo = "Video"
    override val tabUsb = "USB Storage"
    override val tabSettings = "Settings"
    override val allTracks = "All Tracks"
    override val artists = "Artists"
    override val albums = "Albums"
    override val folders = "Folders"
    override val tracksCount = "tracks"
    override val searchPlaceholder = "Search by title or artist..."
    override val noTracksFound = "No music tracks found on USB"
    override val noVideosFound = "No videos found on USB"
    override val noUsbFound = "No connected USB drives found. Please insert a USB drive"
    override val unknownArtist = "Unknown Artist"
    override val unknownAlbum = "Unknown Album"
    override val selectArtistToView = "Select an artist to view their songs"
    override val selectAlbumToView = "Select an album to view its tracks"
    override val backToArtists = "Back to Artists"
    override val backToAlbums = "Back to Albums"
    override val backToFolders = "Back to Folders"
    override val musicPlaylists = "Playlists"
    override val backToMusicPlaylists = "Back to Playlists"
    override val nowPlaying = "Now Playing"
    override val tapToFullscreen = "Tap for Fullscreen"
    override val minimize = "Minimize Player"
    override val fullscreen = "Fullscreen"
    override val exitFullscreen = "Exit Fullscreen"
    override val play = "Play"
    override val pause = "Pause"
    override val next = "Next"
    override val previous = "Previous"
    override val selectMediaToPlay = "Select a track to play on car display"
    override val selectVideoToPlay = "Select a video to play on car display"
    override val videoPlayerHeader = "Smart Video Player"
    override val audioPlayerHeader = "Car Audio Player"
    override val storageTitle = "USB Storage Devices"
    override val storageScanButton = "Scan Devices"
    override val scanningStorage = "Scanning USB media..."
    override val storageMounted = "Mounted & Ready"
    override val storageUnmounted = "Unmounted"
    override val storageRoot = "Path"
    override val storageId = "ID"
    override val settingsTitle = "Car & Language Settings"
    override val languageSection = "App Language"
    override val systemDefaultRuleNotice = "Defaults to car system language (Arabic / French / English, and strictly English for any other language)"
    override val langArabic = "Arabic"
    override val langFrench = "French"
    override val langEnglish = "English"
    override val themeSection = "App Theme & Appearance"
    override val themeDark = "Night Mode (Dark)"
    override val themeLight = "Day Mode (Light)"
    override val themeAutoSensor = "Auto (Car Light Sensor)"
    override val themeAutoTime = "Auto by Time (Sunrise/Sunset)"
    override val themeAutoScreen = "Auto by Screen Brightness & Lights"
    override val lightSensorStatusAvailable = "Car light sensor active: auto-switches day & night"
    override val lightSensorStatusUnavailable = "Light sensor not detected: smart alternatives (time / screen brightness) active"
    override val lightSensorAlternativeNotice = "Smart alternatives for vehicles without sensor: Auto-switch by Time or by Screen Brightness & Headlights!"
    override val dynamicColorTitle = "Dynamic Colors from Album Art"
    override val dynamicColorSubtitle = "Adapts accent styling to the currently playing song"
    override val compactScreenTitle = "Compact Screen Mode"
    override val compactScreenSubtitle = "Optimize layouts, margins, and controls for small car displays (≤ 7 inches)"
    override val compactScreenBadge = "Compact Display Mode"
    override val keepScreenOnTitle = "Keep Screen On Always"
    override val keepScreenOnSubtitle = "Prevent the car display from sleeping or dimming while the app is open"
    override val keepScreenOnBadge = "Screen Always On"
    override val fullscreenTitle = "Immersive Fullscreen"
    override val fullscreenSubtitle = "Hide system navigation and status bars to use 100% of the car display"
    override val fullscreenBadge = "Fullscreen Active"
    override val videoSpeed = "Speed"
    override val videoAspect = "Aspect"
    override val forward10 = "+10s"
    override val replay10 = "-10s"
    override val swcSimultaneousToast = "Steering wheel simultaneous press detected: Paused"
    override val swcSimultaneousToggleToast = "Steering wheel simultaneous press: Play / Pause toggle"
    override val swcSettingsSectionTitle = "Steering Wheel Controls (SWC)"
    override val swcSettingsSectionSubtitle = "Customize button behaviors, timing windows, and seek speeds for your specific vehicle"
    override val swcPresetTitle = "Vehicle Preset Profile"
    override val swcPresetSmartAuto = "Smart Auto (Recommended)"
    override val swcPresetDedicatedPause = "Dedicated Pause Button"
    override val swcPresetSlowCanbus = "Slow CAN-Bus / Resistive Keys"
    override val swcPresetPeugeot = "Peugeot 207 / PSA (Stalk Control)"
    override val swcPresetCustom = "Custom Profile"
    override val swcDualPressModeTitle = "Track Skip Dual-Button Behavior (Next + Prev)"
    override val swcDualPressModeSubtitle = "Choose how dual key detection triggers Play / Pause toggle"
    override val swcDualModeBoth = "Simultaneous & Sequential (Smart)"
    override val swcDualModeStrictSimultaneous = "Simultaneous Only (Exact same time)"
    override val swcDualModeSequential = "Sequential Only (First then Second)"
    override val swcDualModeDisabled = "Disabled (Use dedicated button)"
    override val swcDualPressWindowLabel = "Dual Press Detection Window"
    override val swcDedicatedPauseTitle = "Vehicle has Dedicated Play/Pause/Mute Button"
    override val swcDedicatedPauseSubtitle = "If your steering wheel has a Play/Pause button, you can disable the dual-button combo to avoid accidental pauses"
    override val swcLongPressModeTitle = "Long-Press Function on Next / Previous"
    override val swcLongPressModeSubtitle = "Define what happens when holding down the Next or Previous button"
    override val swcLongPressModeContinuous = "Continuous Seeking (Fast-Forward >> / Rewind <<)"
    override val swcLongPressModeStep = "Single Jump Step (e.g. +10s / -10s)"
    override val swcLongPressModeFolder = "Skip Whole Folder / Album"
    override val swcLongPressModeDisabled = "Disabled (Single track skip only)"
    override val swcSeekStepLabel = "Seek Step Duration"
    override val swcHoldThresholdLabel = "Long-Press Hold Threshold"
    override val swcExtendedKeysTitle = "Accept Extended Keys (CAN-Bus & Head Units)"
    override val swcExtendedKeysSubtitle = "Recognize Channel Up/Down and D-Pad navigation codes as Next/Prev keys"
    override val swcDebounceTitle = "Anti-Double-Click Filter (Debounce)"
    override val swcDebounceSubtitle = "Suppresses rapid duplicate CAN-bus pulses to avoid skipping two tracks at once"
    override val swcTesterTitle = "Live Steering Wheel Key Monitor"
    override val swcTesterSubtitle = "Press any steering wheel button now to see the exact code received and how the app interprets it"
    override val swcTesterWaiting = "Waiting for steering wheel key press..."
    override val swcTesterKeyDetected = "Detected Key"
    override val swcTesterAction = "Event Action"
    override val swcTesterMappedFunction = "Mapped Action"
    override val swcCustomizationTabTitle = "Customization"
    override val swcMapFunctionsTitle = "Map Button Functions"
    override val swcMapNextLabel = "Next Button"
    override val swcMapPrevLabel = "Prev Button"
    override val swcMapPlayPauseLabel = "Play/Pause Button"
    override val swcMapSelectHint = "Press steering button to map"
    override val swcMapWaiting = "Waiting for press..."
    override val usbConnectedToast = "USB Storage Connected: %s"
    override val usbDisconnectedToast = "USB Storage Removed: %s"
    override val resumedPlayback = "Resuming playback from where you left off"
    override val fastForwarding = "Fast Forwarding >>"
    override val rewinding = "<< Rewinding"
    override val internalStorage = "Internal Storage"
    override val allVideos = "All Videos"
    override val videoFolders = "Folders"
    override val videoPlaylists = "Playlists"
    override val videoDrives = "Drives"
    override val backToVideoFolders = "Back to Folders"
    override val backToVideoPlaylists = "Back to Playlists"
    override val backToVideoDrives = "Back to Drives"
    override val playlistFavorites = "Favorites ⭐"
    override val playlistShortClips = "Short Clips (< 5m)"
    override val playlistMovies = "Movies & Episodes (> 10m)"
    override val playlistRecent = "Recently Added"
    override val searchVideosPlaceholder = "Search videos or folder..."
    override val videoCountLabel = "%d videos"
    override val addToFavorites = "Add to Favorites"
    override val removeFromFavorites = "Remove from Favorites"
    override val createPlaylist = "Create New Playlist"
    override val playlistNamePlaceholder = "Enter playlist name..."
    override val customPlaylists = "My Playlists"
    override val addToPlaylist = "Add to Playlist"
    override val removeFromPlaylist = "Remove from Playlist"
    override val deleteMediaItem = "Delete File"
    override val deleteMediaConfirmTitle = "Confirm Deletion"
    override val deleteMediaConfirmMessage = "Are you sure you want to permanently delete this file?"
    override val deletePlaylistConfirmTitle = "Delete Playlist"
    override val deletePlaylistConfirmMessage = "Are you sure you want to delete this playlist?"
    override val itemAddedToPlaylist = "Added to playlist successfully"
    override val itemRemovedFromPlaylist = "Removed from playlist"
    override val itemDeleted = "File deleted successfully"
    override val noCustomPlaylists = "No custom playlists yet. Tap Create to make your first playlist!"
    override val trackActionTitle = "Media Options"
    override val emptyPlaylist = "This playlist is currently empty"
    override val selectPlaylistToAdd = "Select Playlist"
    override val newPlaylistTitle = "New Playlist"
    override val confirmDelete = "Confirm Delete"
    override val bulkDeleteConfirmMessage = "Are you sure you want to delete the selected items?"
    override val noPlaylistsFound = "No playlists found"
    override val storageDrives = "Storage & USB"
    override val backToStorageDrives = "Back to Storage Drives"
    override val tabletInternalStorage = "Tablet Internal Storage"
    override val sdCardStorage = "SD Card Storage"
    override val usbDriveLabel = "USB Drive"
    override val allStorageSources = "All Storage Sources"
    override val aboutSectionTitle = "About App & System"
    override val appDeveloperLabel = "Developer"
    override val developerName = "achour tech pro"
    override val appVersionLabel = "Version"
    override val appVersionValue = "v2.5 (Car Head Unit Edition)"
    override val appFeaturesSummary = "Automotive-tailored media player designed for car Android head units with massive controls, USB OTG detection, steering wheel controls, and live audio visualizers."
    override val autoOpenOnUsbTitle = "Auto-launch on USB Insertion"
    override val autoOpenOnUsbSubtitle = "Automatically bring Car Media Player to the screen when a USB drive is connected"
    override val autoOpenOnUsbBadge = "Auto Launch"
    override val visualizerModeCover = "Album Cover"
    override val visualizerModeBars = "Audio Visualizer Bars"
    override val toggleVisualizerTooltip = "Toggle between Album Art and Dynamic Equalizer Bars"
    override val screenSizeTitle = "Screen Size & UI Scale"
    override val screenSizeSubtitle = "Adjust UI element dimensions and scaling to fit your car screen perfectly"
    override val fontSizeTitle = "Font Size Scale"
    override val fontSizeSubtitle = "Scale up or down text sizes across all lists and player screens"
    override val swcCustomSectionTitle = "Custom SWC Button Profile"
    override val swcCustomSectionSubtitle = "Configure dual-button pause combo (stop/brake), long-press acceleration, and save as final custom settings"
    override val swcCustomBtn1Label = "Steering Button 1 Key Code"
    override val swcCustomBtn2Label = "Steering Button 2 Key Code"
    override val swcCustomAccelLabel = "Long-Press Acceleration Speed Factor"
    override val saveCustomProfileBtn = "Save as Final Custom Setting"

    // Dashboard Widgets
    override val widgetsSectionTitle = "Dashboard Widgets"
    override val widgetsSectionSubtitle = "Configure information displayed in fullscreen mode"
    override val showClock = "Show Clock"
    override val showDate = "Show Date"
    override val showTemp = "Show Temperature"
    override val showSpeed = "Show Car Speed"
    override val speedUnit = "km/h"
    override val tempUnit = "°C"
    override val speedUnitMph = "mph"
    override val tempUnitFahrenheit = "°F"
    override val tempCalibrationLabel = "Thermometer Calibration"
    override val speedSourceGps = "GPS Navigation"
    override val speedSourceCan = "CAN-Bus Protocol"
    override val gpsPermissionRequired = "Location permission is required for accurate GPS speed tracking"
    override val tempSensorUnavailable = "Ambient temperature sensor not available on this device"
    override val tempSourceCanBus = "Vehicle CAN-Bus Thermometer"
    override val tempSourceSensor = "Built-in Temperature Sensor"
    override val sensorsSectionTitle = "Sensors & Telemetry Management"
    override val sensorsSectionSubtitle = "Smart multi-source temperature & speed fallback across tablet and car sensors"
    override val tempSensorSourceTitle = "Temperature Sensor Source"
    override val tempSensorSourceSubtitle = "Choose your preferred temperature sensor (Ambient, Tablet Battery, CPU, or CAN-Bus)"
    override val tempSourceAuto = "Auto (Select Best Available)"
    override val tempSourceAmbientHardware = "Ambient Temperature Sensor"
    override val tempSourceBattery = "Tablet Battery Sensor"
    override val tempSourceCpuSysfs = "CPU / System Thermal Sysfs"
    override val tempSourceManual = "Manual Preset"
    override val speedSensorSourceTitle = "Speed Sensor Source"
    override val speedSensorSourceSubtitle = "Choose speed calculation source (High Precision GPS, Network, CAN-Bus, or Demo Mode)"
    override val speedSourceGpsHighPrecision = "GPS Satellite (High Precision)"
    override val speedSourceNetworkCell = "Network / Cell Tower"
    override val speedSourceSimulated = "Demo / Simulated Speed"
    override val sensorDiagnosticsTitle = "Device Sensor Diagnostics"
    override val sensorDiagnosticsSubtitle = "Real-time readings for all detected tablet and car sensors"
    override val sensorStatusDetected = "Active & Detected"
    override val sensorStatusNotFound = "Not Supported on Device"
    override val sensorStatusActive = "Live Reading Active"
    override val manualTempPresetTitle = "Manual Temperature Preset"
    override val simulatedSpeedPresetTitle = "Simulated Speed Preset"
    override val speedMultiplierLabel = "Speed Calibration Multiplier"
    override val sensorsTabTitle = "Sensors & Speed"

    // Sensors Search & Discovery
    override val scanSensorsButton = "Scan & Discover All Sensors"
    override val scanSensorsSubtitle = "Deep hardware scan for sensors, CPU thermal, battery, and GPS"
    override val sensorScannerDialogTitle = "Vehicle Sensors Discovery & Diagnostics"
    override val filterAll = "All"
    override val filterTemp = "Temperature"
    override val filterSpeed = "Speed & Motion"
    override val filterThermalSysfs = "CPU Thermal (Sysfs)"
    override val filterGps = "GPS & Location"
    override val setAsTempSensor = "Set as Temp Sensor"
    override val setAsSpeedSensor = "Set as Speed Sensor"
    override val activeSensorBadge = "Active Sensor"
    override val grantLocationPermission = "Grant Location Permission for GPS"
    override val openLocationSettings = "Open GPS Settings"
    override val gpsPermissionGranted = "Location Permission Granted"
    override val gpsPermissionDenied = "Location Permission Denied (Required for GPS Speed)"
    override val rescanSensors = "Rescan Now"
    override val tempSourceCustomSensor = "Custom Hardware Temp Sensor"
    override val tempSourceCustomSysfs = "Custom CPU Thermal File (Sysfs)"
    override val speedSourceCustomSensor = "Custom Speed Sensor"
    override val liveSensorValues = "Live Value"
    override val searchSensorsPlaceholder = "Search by name (e.g. temp, speed, accel, thermal)..."
    override val noSensorsFoundForFilter = "No sensors match this filter"
    override val sensorsFoundCountSummary = "Discovered %d hardware sensors & sources"

    override val canBusSectionTitle = "CAN-Bus Integration"
    override val canBusSectionSubtitle = "Advanced settings for direct vehicle system communication"
    override val canBusKeyRemapTitle = "CAN-Bus Key Remapping"
    override val canBusWheelSwitchTitle = "Wheel Mode Switch"
    override val canBusWheelSwitchSubtitle = "Change how CAN-Bus rotary/wheel signals are interpreted"
    override val canBusProtocolTitle = "CAN Protocol"
    override val canBusCarStatusTitle = "Car Info"
    override val canBusVoltage = "Battery Voltage"
    override val canBusFuel = "Fuel Level"
    override val canBusDoors = "Door Status"
    override val canBusCleanUpSwc = "Clean Up Inactive Settings"
    override val canBusSection = "CAN-Bus Settings"
    override val carSpeedLabel = "Car Speed"
    override val carTempLabel = "Ambient Temp"
    override val canBusWheelModeLabel = "Steering Wheel Mode"
    override val canBusWheelModeNormal = "Normal"
    override val canBusWheelModeReversed = "Reversed"
    override val canBusWheelModeSmart = "Smart"
    override val canBusProtocolLabel = "CAN Protocol"
    override val canBusProtocolGeneric = "Generic (Android)"
    override val peugeotModeBadge = "Peugeot 207 / PSA Dedicated Mode"
    override val peugeotModeHelp = "Optimized for Peugeot 207 rotary stalk wheel, Source key, and Raise/SimpleSoft CAN boxes"

    // Additional Localization fields
    override val resetToSystemLanguage = "Reset to Car System Language"
    override val mediaSessionActiveTitle = "Native MediaSession Active & Synced"
    override val mediaSessionActiveDesc = "The head unit MCU routes physical steering wheel buttons and CAN-Bus signals to standard Android media events for direct, seamless playback control."
    override val supportedSwcButtonsTitle = "Supported Steering Wheel Buttons:"
    override val btnPlayPause = "Play / Pause Button"
    override val btnNextTrack = "Next Track Button"
    override val btnPrevTrack = "Previous Track Button"
    override val btnFastForward = "Fast-Forward Button"
    override val btnRewind = "Rewind Button"
    override val btnStop = "Stop Media Button"
    override val audioFocusTitle = "Audio Focus & Auto-Ducking Management"
    override val audioFocusDesc = "When navigation (Google Maps / Waze) or voice assistant speaks, media volume smoothly ducks to 20% and restores immediately after."
    override val canBusIntegrationTitle = "Vehicle CAN-Bus Integration"
    override val canBusIntegrationDesc = "The head unit MCU decodes CAN-Bus signals to supply Android with speed telemetry and steering wheel media controls for seamless stability."
    override val systemOptimizationsTitle = "Applied Head Unit Optimizations:"
    override val optImmersiveFullscreen = "Immersive Fullscreen Mode"
    override val optKeepScreenOn = "Keep Screen On While Driving"
    override val optBufferControl = "ExoPlayer Media Buffer Load Control"
    override val statusActive = "Active"
    override val createVideoPlaylist = "Create Custom Video Playlist"
    override val smartPlaylists = "Smart Playlists"
    override val multiSelect = "Multi-Select"
    override val cancel = "Cancel"
    override val delete = "Delete"
    override val close = "Close"
    override val createPlaylistSubtitleMusic = "Create a custom playlist for your selected music"
    override val createPlaylistSubtitleVideo = "Create a collection for your favorite videos"
}

object FrenchStrings : AppStrings {
    override val appName = "Lecteur Média Voiture"
    override val tabMusic = "Musique"
    override val tabVideo = "Vidéo"
    override val tabUsb = "Stockage USB"
    override val tabSettings = "Paramètres"
    override val allTracks = "Tous les titres"
    override val artists = "Artistes"
    override val albums = "Albums"
    override val folders = "Dossiers"
    override val tracksCount = "pistes"
    override val searchPlaceholder = "Rechercher par titre ou artiste..."
    override val noTracksFound = "Aucune piste musicale trouvée sur la clé USB"
    override val noVideosFound = "Aucune vidéo trouvée sur la clé USB"
    override val noUsbFound = "Aucun support USB détecté. Veuillez brancher une clé USB"
    override val unknownArtist = "Artiste Inconnu"
    override val unknownAlbum = "Album Inconnu"
    override val selectArtistToView = "Sélectionnez un artiste pour afficher ses titres"
    override val selectAlbumToView = "Sélectionnez un album pour afficher ses pistes"
    override val backToArtists = "Retour aux artistes"
    override val backToAlbums = "Retour aux albums"
    override val backToFolders = "Retour aux dossiers"
    override val musicPlaylists = "Listes de lecture"
    override val backToMusicPlaylists = "Retour aux listes"
    override val nowPlaying = "Lecture en cours"
    override val tapToFullscreen = "Appuyez pour plein écran"
    override val minimize = "Réduire le lecteur"
    override val fullscreen = "Plein écran"
    override val exitFullscreen = "Quitter plein écran"
    override val play = "Lecture"
    override val pause = "Pause"
    override val next = "Suivant"
    override val previous = "Précédent"
    override val selectMediaToPlay = "Sélectionnez un titre à lire sur l'écran du véhicule"
    override val selectVideoToPlay = "Sélectionnez une vidéo à lire sur l'écran du véhicule"
    override val videoPlayerHeader = "Lecteur Vidéo Intelligent"
    override val audioPlayerHeader = "Lecteur Audio Voiture"
    override val storageTitle = "Stockage USB"
    override val storageScanButton = "Analyser"
    override val scanningStorage = "Analyse des médias USB en cours..."
    override val storageMounted = "Monté et prêt"
    override val storageUnmounted = "Non monté"
    override val storageRoot = "Chemin"
    override val storageId = "Identifiant"
    override val settingsTitle = "Paramètres du véhicule et langue"
    override val languageSection = "Langue de l'application"
    override val systemDefaultRuleNotice = "Par défaut selon la langue du véhicule (Arabe / Français / Anglais, et strictement Anglais pour toute autre langue)"
    override val langArabic = "Arabe"
    override val langFrench = "Français"
    override val langEnglish = "Anglais"
    override val themeSection = "Thème de l'application"
    override val themeDark = "Mode Nuit (Sombre)"
    override val themeLight = "Mode Jour (Clair)"
    override val themeAutoSensor = "Auto (Capteur de lumière)"
    override val themeAutoTime = "Auto selon l'heure (Lever/Coucher)"
    override val themeAutoScreen = "Auto selon luminosité de l'écran"
    override val lightSensorStatusAvailable = "Capteur de luminosité actif : bascule auto jour/nuit"
    override val lightSensorStatusUnavailable = "Capteur non détecté : alternatives intelligentes (heure / luminosité) actives"
    override val lightSensorAlternativeNotice = "Alternatives pour véhicules sans capteur : bascule selon l'heure ou la luminosité de l'écran !"
    override val dynamicColorTitle = "Couleurs dynamiques selon la pochette"
    override val dynamicColorSubtitle = "Adapte les accents et touches selon la chanson en cours"
    override val compactScreenTitle = "Mode Petits Écrans"
    override val compactScreenSubtitle = "Optimiser les marges et les boutons pour petits écrans de voiture (≤ 7 pouces)"
    override val compactScreenBadge = "Mode Écran Compact"
    override val keepScreenOnTitle = "Garder l'écran allumé"
    override val keepScreenOnSubtitle = "Empêcher la mise en veille de l'écran pendant que l'application est ouverte"
    override val keepScreenOnBadge = "Écran toujours actif"
    override val fullscreenTitle = "Plein écran immersif"
    override val fullscreenSubtitle = "Masquer les barres système pour occuper 100% de l'écran de la voiture"
    override val fullscreenBadge = "Plein Écran Actif"
    override val videoSpeed = "Vitesse"
    override val videoAspect = "Format"
    override val forward10 = "+10s"
    override val replay10 = "-10s"
    override val swcSimultaneousToast = "Commandes au volant : touches simultanées détectées (Pause)"
    override val swcSimultaneousToggleToast = "Commandes au volant : touches simultanées (Lecture / Pause)"
    override val swcSettingsSectionTitle = "Commandes au Volant (SWC)"
    override val swcSettingsSectionSubtitle = "Personnalisez le comportement des touches et la vitesse de défilement selon votre véhicule"
    override val swcPresetTitle = "Profil prédéfini du véhicule"
    override val swcPresetSmartAuto = "Auto Intelligent (Recommandé)"
    override val swcPresetDedicatedPause = "Bouton Pause Dédié"
    override val swcPresetSlowCanbus = "CAN-Bus Lent / Résistif"
    override val swcPresetPeugeot = "Peugeot 207 / PSA (Commodo)"
    override val swcPresetCustom = "Personnalisé"
    override val swcDualPressModeTitle = "Comportement double touche (Suivant + Précédent)"
    override val swcDualPressModeSubtitle = "Choisissez comment la double pression active la Lecture / Pause"
    override val swcDualModeBoth = "Simultané et Séquentiel (Intelligent)"
    override val swcDualModeStrictSimultaneous = "Simultané uniquement (Même instant)"
    override val swcDualModeSequential = "Séquentiel uniquement (L'un après l'autre)"
    override val swcDualModeDisabled = "Désactivé (Boutons dédiés uniquement)"
    override val swcDualPressWindowLabel = "Fenêtre de détection de la combinaison"
    override val swcDedicatedPauseTitle = "Le véhicule a un bouton Lecture/Pause/Muet dédié"
    override val swcDedicatedPauseSubtitle = "Si votre volant dispose d'un bouton Pause, désactivez la combinaison double pour éviter les pauses accidentelles"
    override val swcLongPressModeTitle = "Action de pression longue sur Suivant / Précédent"
    override val swcLongPressModeSubtitle = "Définissez ce qui se produit en maintenant la touche enfoncée"
    override val swcLongPressModeContinuous = "Avance / Retour rapide continu (>> / <<)"
    override val swcLongPressModeStep = "Saut temporel unique (ex: +10s / -10s)"
    override val swcLongPressModeFolder = "Passer au dossier / album entier"
    override val swcLongPressModeDisabled = "Désactivé (Piste suivante uniquement)"
    override val swcSeekStepLabel = "Durée du saut en secondes"
    override val swcHoldThresholdLabel = "Délai d'appui long requis"
    override val swcExtendedKeysTitle = "Accepter touches étendues (CAN-Bus / Écrans Android)"
    override val swcExtendedKeysSubtitle = "Reconnaître Channel Up/Down et flèches D-Pad comme touches Suivant/Précédent"
    override val swcDebounceTitle = "Filtre anti-rebond (Debounce)"
    override val swcDebounceSubtitle = "Ignore les impulsions doubles rapides pour éviter de sauter 2 pistes d'un coup"
    override val swcTesterTitle = "Moniteur de test des touches au volant"
    override val swcTesterSubtitle = "Appuyez sur un bouton de votre volant pour vérifier le code reçu et l'action associée"
    override val swcTesterWaiting = "En attente d'une touche au volant..."
    override val swcTesterKeyDetected = "Touche détectée"
    override val swcTesterAction = "Action"
    override val swcTesterMappedFunction = "Fonction mappée"
    override val swcCustomizationTabTitle = "Customisation"
    override val swcMapFunctionsTitle = "Mapper les fonctions"
    override val swcMapNextLabel = "Bouton Suivant"
    override val swcMapPrevLabel = "Bouton Précédent"
    override val swcMapPlayPauseLabel = "Bouton Lecture/Pause"
    override val swcMapSelectHint = "Appuyez sur le bouton pour mapper"
    override val swcMapWaiting = "En attente d'appui..."
    override val usbConnectedToast = "Clé USB connectée : %s"
    override val usbDisconnectedToast = "Clé USB retirée : %s"
    override val resumedPlayback = "Reprise de la lecture là où vous vous étiez arrêté"
    override val fastForwarding = "Avance rapide >>"
    override val rewinding = "<< Retour rapide"
    override val internalStorage = "Stockage interne"
    override val allVideos = "Toutes les vidéos"
    override val videoFolders = "Dossiers"
    override val videoPlaylists = "Listes de lecture"
    override val videoDrives = "Supports"
    override val backToVideoFolders = "Retour aux dossiers"
    override val backToVideoPlaylists = "Retour aux listes"
    override val backToVideoDrives = "Retour aux supports"
    override val playlistFavorites = "Favoris ⭐"
    override val playlistShortClips = "Courts métrages (< 5m)"
    override val playlistMovies = "Films & Épisodes (> 10m)"
    override val playlistRecent = "Récemment ajoutées"
    override val searchVideosPlaceholder = "Rechercher des vidéos ou dossiers..."
    override val videoCountLabel = "%d vidéos"
    override val addToFavorites = "Ajouter aux favoris"
    override val removeFromFavorites = "Retirer des favoris"
    override val createPlaylist = "Créer une liste de lecture"
    override val playlistNamePlaceholder = "Nom de la liste..."
    override val customPlaylists = "Mes Listes"
    override val addToPlaylist = "Ajouter à la liste"
    override val removeFromPlaylist = "Retirer de la liste"
    override val deleteMediaItem = "Supprimer le fichier"
    override val deleteMediaConfirmTitle = "Confirmer la suppression"
    override val deleteMediaConfirmMessage = "Voulez-vous vraiment supprimer définitivement ce fichier ?"
    override val deletePlaylistConfirmTitle = "Supprimer la liste"
    override val deletePlaylistConfirmMessage = "Voulez-vous vraiment supprimer cette liste de lecture ?"
    override val itemAddedToPlaylist = "Ajouté à la liste avec succès"
    override val itemRemovedFromPlaylist = "Retiré de la liste de lecture"
    override val itemDeleted = "Fichier supprimé avec succès"
    override val noCustomPlaylists = "Aucune liste personnalisée. Cliquez sur Créer pour en créer une !"
    override val trackActionTitle = "Options du média"
    override val emptyPlaylist = "Cette liste est actuellement vide"
    override val selectPlaylistToAdd = "Sélectionner une liste"
    override val newPlaylistTitle = "Nouvelle liste"
    override val confirmDelete = "Confirmer la suppression"
    override val bulkDeleteConfirmMessage = "Êtes-vous sûr de vouloir supprimer les éléments sélectionnés ?"
    override val noPlaylistsFound = "Aucune liste trouvée"
    override val storageDrives = "Supports & USB"
    override val backToStorageDrives = "Retour aux supports"
    override val tabletInternalStorage = "Stockage interne tablette"
    override val sdCardStorage = "Carte mémoire SD"
    override val usbDriveLabel = "Clé USB"
    override val allStorageSources = "Tous les supports"
    override val aboutSectionTitle = "À propos de l'application"
    override val appDeveloperLabel = "Développeur"
    override val developerName = "achour tech pro"
    override val appVersionLabel = "Version"
    override val appVersionValue = "v2.5 (Car Head Unit Edition)"
    override val appFeaturesSummary = "Lecteur multimédia automobile optimisé pour écrans de voiture Android, commandes tactiles géantes, boutons au volant et visualiseur audio dynamique."
    override val autoOpenOnUsbTitle = "Ouverture automatique à l'insertion USB"
    override val autoOpenOnUsbSubtitle = "Lancer ou afficher l'application directement à la connexion d'une clé USB"
    override val autoOpenOnUsbBadge = "Lancement auto"
    override val visualizerModeCover = "Pochette d'album"
    override val visualizerModeBars = "Spectre audio (Barres)"
    override val toggleVisualizerTooltip = "Basculer entre pochette et barres d'égaliseur"
    override val screenSizeTitle = "Taille d'écran & Échelle UI"
    override val screenSizeSubtitle = "Ajuster l'échelle des éléments pour s'adapter parfaitement à l'écran de la voiture"
    override val fontSizeTitle = "Taille de police"
    override val fontSizeSubtitle = "Agrandir ou réduire la taille du texte dans toute l'application"
    override val swcCustomSectionTitle = "Profil de boutons SWC personnalisé"
    override val swcCustomSectionSubtitle = "Configurer la pause à deux boutons, l'accélération d'appui long et enregistrer comme personnalisé"
    override val swcCustomBtn1Label = "Code touche volant 1"
    override val swcCustomBtn2Label = "Code touche volant 2"
    override val swcCustomAccelLabel = "Facteur d'accélération d'appui long"
    override val saveCustomProfileBtn = "Enregistrer comme personnalisé"

    // Dashboard Widgets
    override val widgetsSectionTitle = "Widgets de Bord"
    override val widgetsSectionSubtitle = "Configurer les informations affichées en plein écran"
    override val showClock = "Afficher l'horloge"
    override val showDate = "Afficher la date"
    override val showTemp = "Afficher la température"
    override val showSpeed = "Afficher la vitesse"
    override val speedUnit = "km/h"
    override val tempUnit = "°C"
    override val speedUnitMph = "mph"
    override val tempUnitFahrenheit = "°F"
    override val tempCalibrationLabel = "Étalonnage du thermomètre"
    override val speedSourceGps = "Navigation GPS"
    override val speedSourceCan = "Protocole CAN-Bus"
    override val gpsPermissionRequired = "L'autorisation de localisation est requise pour le suivi précis de la vitesse GPS"
    override val tempSensorUnavailable = "Capteur de température ambiante non disponible sur cet appareil"
    override val tempSourceCanBus = "Thermomètre CAN-Bus du véhicule"
    override val tempSourceSensor = "Capteur de température intégré"
    override val sensorsSectionTitle = "Gestion des Capteurs & Télémétrie"
    override val sensorsSectionSubtitle = "Sélection intelligente multi-source de température et vitesse pour tablette et véhicule"
    override val tempSensorSourceTitle = "Source du capteur de température"
    override val tempSensorSourceSubtitle = "Choisissez la source de température (Ambiant, Batterie tablette, CPU, ou CAN-Bus)"
    override val tempSourceAuto = "Auto (Sélectionner le meilleur disponible)"
    override val tempSourceAmbientHardware = "Capteur de température ambiante"
    override val tempSourceBattery = "Capteur de batterie tablette"
    override val tempSourceCpuSysfs = "Processeur / Thermique CPU"
    override val tempSourceManual = "Réglage manuel fixe"
    override val speedSensorSourceTitle = "Source du capteur de vitesse"
    override val speedSensorSourceSubtitle = "Source de vitesse (GPS haute précision, Réseau, CAN-Bus, ou Mode Démo)"
    override val speedSourceGpsHighPrecision = "Satellite GPS (Haute Précision)"
    override val speedSourceNetworkCell = "Réseau / Antenne-relais"
    override val speedSourceSimulated = "Vitesse simulée (Démo)"
    override val sensorDiagnosticsTitle = "Diagnostic des Capteurs de l'Appareil"
    override val sensorDiagnosticsSubtitle = "Lecture en direct de tous les capteurs détectés sur la tablette et la voiture"
    override val sensorStatusDetected = "Actif & Détecté"
    override val sensorStatusNotFound = "Non supporté sur l'appareil"
    override val sensorStatusActive = "Lecture en direct active"
    override val manualTempPresetTitle = "Température manuelle enregistrée"
    override val simulatedSpeedPresetTitle = "Vitesse simulée définie"
    override val speedMultiplierLabel = "Coefficient d'étalonnage de vitesse"
    override val sensorsTabTitle = "Capteurs & Vitesse"

    // Sensors Search & Discovery
    override val scanSensorsButton = "Rechercher les capteurs & Matériel"
    override val scanSensorsSubtitle = "Analyse approfondie de tous les capteurs, température CPU, et GPS"
    override val sensorScannerDialogTitle = "Explorateur de capteurs du véhicule"
    override val filterAll = "Tous"
    override val filterTemp = "Température"
    override val filterSpeed = "Vitesse & Mouvement"
    override val filterThermalSysfs = "Thermique CPU (Sysfs)"
    override val filterGps = "GPS & Localisation"
    override val setAsTempSensor = "Définir comme capteur de temp."
    override val setAsSpeedSensor = "Définir comme capteur de vitesse"
    override val activeSensorBadge = "Capteur Actif"
    override val grantLocationPermission = "Accorder l'autorisation de localisation GPS"
    override val openLocationSettings = "Ouvrir les paramètres GPS"
    override val gpsPermissionGranted = "Autorisation de localisation accordée"
    override val gpsPermissionDenied = "Autorisation refusée (Requise pour la vitesse GPS)"
    override val rescanSensors = "Re-scanner maintenant"
    override val tempSourceCustomSensor = "Capteur de température matériel"
    override val tempSourceCustomSysfs = "Fichier thermique CPU personnalisé (Sysfs)"
    override val speedSourceCustomSensor = "Capteur de vitesse personnalisé"
    override val liveSensorValues = "Valeur en direct"
    override val searchSensorsPlaceholder = "Rechercher par nom (ex: temp, speed, accel, thermal)..."
    override val noSensorsFoundForFilter = "Aucun capteur ne correspond à ce filtre"
    override val sensorsFoundCountSummary = "%d capteurs et sources matérielles détectés"

    override val canBusSectionTitle = "Intégration CAN-Bus"
    override val canBusSectionSubtitle = "Paramètres avancés pour la communication directe avec le véhicule"
    override val canBusKeyRemapTitle = "Remappage des touches CAN-Bus"
    override val canBusWheelSwitchTitle = "Commutateur de mode volant"
    override val canBusWheelSwitchSubtitle = "Changer l'interprétation des signaux de rotation CAN-Bus"
    override val canBusProtocolTitle = "Protocole CAN"
    override val canBusCarStatusTitle = "Infos Véhicule"
    override val canBusVoltage = "Tension Batterie"
    override val canBusFuel = "Niveau Carburant"
    override val canBusDoors = "État des Portes"
    override val canBusCleanUpSwc = "Nettoyer les paramètres inactifs"
    override val canBusSection = "Paramètres CAN-Bus"
    override val carSpeedLabel = "Vitesse voiture"
    override val carTempLabel = "Temp. Ambiante"
    override val canBusWheelModeLabel = "Mode volant"
    override val canBusWheelModeNormal = "Normal"
    override val canBusWheelModeReversed = "Inversé"
    override val canBusWheelModeSmart = "Intelligent"
    override val canBusProtocolLabel = "Protocole CAN"
    override val canBusProtocolGeneric = "Générique (Android)"
    override val peugeotModeBadge = "Mode Dédié Peugeot 207 / PSA"
    override val peugeotModeHelp = "Optimisé pour la molette du commodo Peugeot 207, bouton Source et boîtiers Raise/SimpleSoft"

    // Additional Localization fields
    override val resetToSystemLanguage = "Réinitialiser selon la langue du véhicule"
    override val mediaSessionActiveTitle = "MediaSession standard actif et synchronisé"
    override val mediaSessionActiveDesc = "Le MCU de l'autoradio convertit les commandes physiques au volant et les signaux CAN-Bus en événements multimédias Android standards pour un contrôle direct et fluide."
    override val supportedSwcButtonsTitle = "Commandes au volant prises en charge :"
    override val btnPlayPause = "Bouton Lecture / Pause"
    override val btnNextTrack = "Bouton Piste Suivante"
    override val btnPrevTrack = "Bouton Piste Précédente"
    override val btnFastForward = "Bouton Avance Rapide"
    override val btnRewind = "Bouton Retour Rapide"
    override val btnStop = "Bouton Arrêt Média"
    override val audioFocusTitle = "Gestion du focus audio et atténuation (Ducking)"
    override val audioFocusDesc = "Lorsque la navigation (Google Maps / Waze) ou l'assistant vocal parle, le volume s'atténue à 20% puis reprend immédiatement à la fin des indications."
    override val canBusIntegrationTitle = "Intégration CAN-Bus avec le véhicule"
    override val canBusIntegrationDesc = "Le MCU de l'autoradio décode les signaux CAN-Bus pour transmettre directement la vitesse et les commandes multimédias à Android pour une stabilité maximale."
    override val systemOptimizationsTitle = "Optimisations appliquées à l'écran de bord :"
    override val optImmersiveFullscreen = "Mode plein écran immersif"
    override val optKeepScreenOn = "Garder l'écran allumé pendant la conduite"
    override val optBufferControl = "Gestion du tampon multimédia (LoadControl)"
    override val statusActive = "Actif"
    override val createVideoPlaylist = "Créer une liste vidéo personnalisée"
    override val smartPlaylists = "Listes de lecture intelligentes"
    override val multiSelect = "Sélection multiple"
    override val cancel = "Annuler"
    override val delete = "Supprimer"
    override val close = "Fermer"
    override val createPlaylistSubtitleMusic = "Créer une liste personnalisée pour vos morceaux choisis"
    override val createPlaylistSubtitleVideo = "Créer une collection pour vos vidéos favorites"
}

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

    val strings: AppStrings
        get() = getStrings(_currentLanguage.value)
}

val LocalAppStrings = compositionLocalOf<AppStrings> { EnglishStrings }
