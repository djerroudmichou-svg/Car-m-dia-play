# 🚗 Car Media Player (Android Automotive / Head Unit)

مشغل وسائط متطور وخفيف الوزن مصمم خصيصاً لشاشات أندرويد للسيارات (Android Head Units)، يدعم قراءة فلاشات USB OTG تلقائياً، مع تكامل مباشر وعالي الدقة لأزرار المقود (SWC)، دعم بروتوكولات CAN-Bus، ووضع مخصص لسيارات بيجو وستروين (Peugeot / PSA).

An advanced, lightweight offline media player tailored for Android Automotive & Head Units. Features automatic USB OTG disk scanning, robust Steering Wheel Control (SWC) integration, CAN-Bus sensor telemetry, and specialized tuning for Peugeot / Citroën (PSA) stalk controls.

---

## 👨‍💻 معلومات المطور (Developer Info)

### 🇸🇦 العربية
- **المطور**: عاشور تك برو (Achour Tech Pro)
- **البريد الإلكتروني**: [technology1082@gmail.com](mailto:technology1082@gmail.com)
- **المشروع**: مشغل وسائط السيارات الذكي (Car Media Player for Android Head Units)
- **الهدف**: توفير تجربة تشغيل وسائط سلسة، آمنة، ومتكاملة تماماً مع أزرار المقود وعلب الكان باص لشاشات السيارات دون الحاجة للاتصال بالإنترنت.
- **المساهمات والاقتراحات**: نرحب بالاقتراحات، التقارير عن الأخطاء (Issues)، وطلبات السحب (Pull Requests) لتحسين دعم مختلف طرازات السيارات وعلب الكان باص.

### 🇬🇧 English
- **Developer**: Achour Tech Pro
- **Email**: [technology1082@gmail.com](mailto:technology1082@gmail.com)
- **Project**: Smart Car Media Player for Android Head Units
- **Mission**: Providing a seamless, driver-safe offline media experience fully integrated with steering wheel controls (SWC) and CAN-Bus decoders.
- **Feedback & Contributions**: Contributions, bug reports, and feature requests for various car models and CAN-Bus boxes are welcome!

---

## ✨ المميزات الرئيسية (Key Features)

### 🎵 تشغيل الوسائط وإدارة الملفات (Media & Storage)
- **فحص سريع وتلقائي لـ USB OTG**: اكتشاف فوري للفلاشة مع ميزة الفحص التدريجي (Incremental Scanning).
  * *Automatic USB OTG Detection with instant incremental file scanner.*
- **قاعدة بيانات محلية فائقة السرعة (Room Database)**: حفظ وفهرسة جميع الملفات الصوتية محلياً للتشغيل الفوري حتى دون إعادة الفحص.
  * *High-performance local Room SQLite database for immediate playback.*
- **تصفح حسب المجلدات والقوائم**: فرز حسب الألبومات، الفنانين، المجلدات، وقوائم التشغيل.
  * *Multi-category browsing: Albums, Artists, Folders, and Playlists.*
- **واجهة مخصصة لشاشات القيادة (Car UI/UX)**: عناصر تحكم واضحة وكبيرة لتوفير أقصى درجات الأمان وسهولة الاستخدام أثناء القيادة.
  * *Ergonomic automotive interface with large touch targets for driver safety.*

### 🎮 دعم متقدم لأزرار المقود (Steering Wheel Controls - SWC)
- **التعرف الذكي على المفاتيح**: دعم كامل لأزرار التحكم السلكية والمقاومة وعلب CAN-Bus.
  * *Full compatibility with resistive keys, key-study learning, and digital CAN-Bus keys.*
- **مانع الارتداد (Anti-Debounce)**: تجنب القفز المزدوج غير المرغوب فيه بين المسارات.
  * *Custom anti-bounce filtering preventing accidental double-skips.*
- **فاحص حي للأزرار (Live Key Monitor)**: شاشة مدمجة تكتشف وتعرض الأكواد فور الضغط على أي زر في المقود لتسهيل التعيين اليدوي.
  * *Real-time key monitor displaying raw keycodes and dispatched actions.*

### 🚙 وضع مخصص لسيارة بيجو 207 ومجموعة PSA (Peugeot / PSA Stalk Preset)
- حل مشكلة عجلة التدوير الخلفية (**Rotary Thumbwheel**) في ذراع المقود (Commodo).
  * *Full resolution for Peugeot rotary thumbwheel scroll pulses.*
- دعم مباشر لأكواد `DPAD_UP` / `DPAD_DOWN` / `CHANNEL_UP` / `CHANNEL_DOWN`.
  * *Native support for directional DPAD & Channel keycodes issued by PSA CAN boxes.*
- دعم زر رأس الذراع (**Source / Mode**) للتشغيل والإيقاف المؤقت (`Play / Pause`).
  * *Source/Mode end-stalk button mapped to Play/Pause toggle.*
- خيار تبديل اتجاه التدوير برمجياً (**Reversed Wheel Mode**) لتصحيح اتجاه التالي والسابق.
  * *Configurable reverse rotation direction for track skip navigation.*
- ضبط تلقائي لعلب الكان باص الشائعة: **Raise (RZC)**، **SimpleSoft (XP)**، و **HiWorld**.
  * *Tested with top PSA CAN decoders: Raise (RZC), SimpleSoft, and HiWorld.*

### 📊 تكامل مستشعرات السيارة (CAN-Bus Integration)
- عرض قراءات سرعة السيارة الحقيقية (**Car Speed in km/h**).
- عرض درجة حرارة الجو الخارجية (**Ambient Outdoor Temperature in °C**).

### 🌍 تعدد اللغات (Multilingual)
- دعم كامل ومتناسق لثلاث لغات: **العربية (Arabic)**، **الإنجليزية (English)**، و **الفرنسية (French)**.

---

## 🛠️ التقنيات المستخدمة (Tech Stack)

| التقنية / المكتبة | الغرض والاستخدام |
|-------------------|------------------|
| **Kotlin** | اللغة الأساسية لكتابة الكود بالكامل |
| **Jetpack Compose** | بناء واجهة المستخدم الحديثة بنظام Material 3 Automotive |
| **Room Database** | التخزين المحلي الآمن والسريع لملفات الصوت وقوائم التشغيل |
| **AndroidX Media3 / ExoPlayer** | محرك تشغيل الصوت الاحترافي وإدارة جلسات الوسائط |
| **Coroutines & StateFlow** | إدارة الحالة والتزامن في الخلفية بدون تجميد الواجهة |
| **DocumentFile & SAF** | التعامل المباشر مع فلاشات USB OTG ووحدات التخزين الخارجية |

---

## 🚀 كيفية التثبيت والتشغيل (Getting Started)

### متطلبات التطوير (Prerequisites):
1. **Android Studio**: الإصدار الحديث (Iguana / Jellyfish أو أحدث).
2. **JDK**: Java 17.
3. **Android SDK**: الحد الأدنى Android 8.0 (API 26)، والمستهدف Android 14 (API 34).

### البناء والتثبيت (Build & Install):
```bash
# 1. استنساخ المستودع (Clone the repository)
git clone https://github.com/technology1082/car-media-player.git

# 2. الانتقال إلى مجلد المشروع (Navigate to project)
cd car-media-player

# 3. بناء حزمة التثبيت (Build Debug APK)
./gradlew assembleDebug
```
الملف الناتج سيكون في المسار:
`app/build/outputs/apk/debug/app-debug.apk`

قم بنقل الملف إلى فلاشة USB وثبّته مباشرة على شاشة الأندرويد في سيارتك.

---

## ⚙️ خطوات إعداد المقود لسيارة بيجو 207 (Peugeot 207 Setup Guide)

1. افتح تطبيق **Car Media** على شاشة سيارتك.
2. اذهب إلى **الإعدادات (Settings)** ⬅️ تبويب **أزرار المقود (SWC)**.
3. اضغط على خيار **"بيجو 207 / PSA (ذراع المقود)"**:
   - سيتم تفعيل وضع **Reversed** للعجلة لضبط اتجاه التدوير.
   - سيتم ضبط زمن التهدئة على **120ms** لمنع القفز المزدوج عند لف العجلة بسرعة.
   - سيتم تفعيل الاستجابة لزر رأس الذراع (Source).
4. استخدم لوحة **Live Key Monitor** لاختبار حركة العجلة وأزرار الذراع والتأكد من استجابة النظام.

---

## 📄 الترخيص (License)
هذا المشروع مرخص تحت رخصة **MIT License** - يحق لك استخدام وتطوير الكود بحرية.
