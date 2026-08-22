package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppLanguage(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val flagEmoji: String
)

object LocalizationManager {
    val supportedLanguages = listOf(
        AppLanguage("en", "English", "English", "🇺🇸"),
        AppLanguage("ru", "Russian", "Русский", "🇷🇺"),
        AppLanguage("es", "Spanish", "Español", "🇪🇸"),
        AppLanguage("fr", "French", "Français", "🇫🇷"),
        AppLanguage("de", "German", "Deutsch", "🇩🇪"),
        AppLanguage("zh", "Chinese (Simplified)", "简体中文", "🇨🇳"),
        AppLanguage("ja", "Japanese", "日本語", "🇯🇵"),
        AppLanguage("ko", "Korean", "한국어", "🇰🇷"),
        AppLanguage("ar", "Arabic", "العربية", "🇸🇦"),
        AppLanguage("pt", "Portuguese", "Português", "🇧🇷"),
        AppLanguage("tr", "Turkish", "Türkçe", "🇹🇷"),
        AppLanguage("it", "Italian", "Italiano", "🇮🇹"),
        AppLanguage("uk", "Ukrainian", "Українська", "🇺🇦"),
        AppLanguage("pl", "Polish", "Polski", "🇵🇱"),
        AppLanguage("hi", "Hindi", "हिन्दी", "🇮🇳"),
        AppLanguage("id", "Indonesian", "Bahasa Indonesia", "🇮🇩"),
        AppLanguage("vi", "Vietnamese", "Tiếng Việt", "🇻🇳"),
        AppLanguage("nl", "Dutch", "Nederlands", "🇳🇱"),
        AppLanguage("el", "Greek", "Ελληνικά", "🇬🇷"),
        AppLanguage("cs", "Czech", "Čeština", "🇨🇿"),
        AppLanguage("sv", "Swedish", "Svenska", "🇸🇪"),
        AppLanguage("ro", "Romanian", "Română", "🇷🇴"),
        AppLanguage("hu", "Hungarian", "Magyar", "🇭🇺"),
        AppLanguage("he", "Hebrew", "עברית", "🇮🇱"),
        AppLanguage("th", "Thai", "ไทย", "🇹🇭"),
        AppLanguage("fa", "Persian", "فارسی", "🇮🇷"),
        AppLanguage("ms", "Malay", "Bahasa Melayu", "🇲🇾"),
        AppLanguage("fi", "Finnish", "Suomi", "🇫🇮"),
        AppLanguage("no", "Norwegian", "Norsk", "🇳🇴"),
        AppLanguage("da", "Danish", "Dansk", "🇩🇰"),
        AppLanguage("bg", "Bulgarian", "Български", "🇧🇬"),
        AppLanguage("sr", "Serbian", "Српски", "🇷🇸"),
        AppLanguage("hr", "Croatian", "Hrvatski", "🇭🇷"),
        AppLanguage("sk", "Slovak", "Slovenčina", "🇸🇰"),
        AppLanguage("lt", "Lithuanian", "Lietuvių", "🇱🇹"),
        AppLanguage("lv", "Latvian", "Latviešu", "🇱🇻"),
        AppLanguage("et", "Estonian", "Eesti", "🇪🇪"),
        AppLanguage("az", "Azerbaijani", "Azərbaycan", "🇦🇿"),
        AppLanguage("kk", "Kazakh", "Қазақша", "🇰🇿"),
        AppLanguage("uz", "Uzbek", "Oʻzbekcha", "🇺🇿"),
        AppLanguage("ka", "Georgian", "ქართული", "🇬🇪"),
        AppLanguage("hy", "Armenian", "Հայերեն", "🇦🇲"),
        AppLanguage("be", "Belarusian", "Беларуская", "🇧🇾"),
        AppLanguage("bn", "Bengali", "বাংলা", "🇧🇩"),
        AppLanguage("ta", "Tamil", "தமிழ்", "🇮🇳"),
        AppLanguage("ur", "Urdu", "اردو", "🇵🇰"),
        AppLanguage("tl", "Tagalog", "Filipino", "🇵🇭"),
        AppLanguage("sw", "Swahili", "Kiswahili", "🇰🇪"),
        AppLanguage("af", "Afrikaans", "Afrikaans", "🇿🇦"),
        AppLanguage("is", "Icelandic", "Íslenska", "🇮🇸")
    )

    // Default language is English ("en") on new login/entry
    private val _currentLanguage = MutableStateFlow(supportedLanguages.first { it.code == "en" })
    val currentLanguage = _currentLanguage.asStateFlow()

    fun setLanguage(code: String) {
        val lang = supportedLanguages.find { it.code == code } ?: supportedLanguages.first()
        _currentLanguage.value = lang
    }

    fun getCurrentCode(): String = _currentLanguage.value.code

    // Comprehensive multi-language dictionary
    private val translations = mapOf(
        "settings" to mapOf(
            "en" to "Settings", "ru" to "Настройки", "es" to "Ajustes", "fr" to "Paramètres", "de" to "Einstellungen",
            "zh" to "设置", "ja" to "設定", "ko" to "설정", "ar" to "الإعدادات", "pt" to "Configurações", "tr" to "Ayarlar",
            "it" to "Impostazioni", "uk" to "Налаштування", "pl" to "Ustawienia"
        ),
        "language" to mapOf(
            "en" to "Language", "ru" to "Язык", "es" to "Idioma", "fr" to "Langue", "de" to "Sprache",
            "zh" to "语言", "ja" to "言語", "ko" to "언어", "ar" to "اللغة", "pt" to "Idioma", "tr" to "Dil",
            "it" to "Lingua", "uk" to "Мова", "pl" to "Język"
        ),
        "search_language" to mapOf(
            "en" to "Search language...", "ru" to "Поиск языка...", "es" to "Buscar idioma...", "fr" to "Rechercher une langue...",
            "de" to "Sprache suchen...", "zh" to "搜索语言...", "ja" to "言語を検索...", "ko" to "언어 검색...", "ar" to "البحث عن لغة..."
        ),
        "edit_profile" to mapOf(
            "en" to "Edit Profile", "ru" to "Изменить профиль", "es" to "Editar perfil", "fr" to "Modifier le profil",
            "de" to "Profil bearbeiten", "zh" to "编辑个人资料", "ja" to "プロフィール編集", "ko" to "프로필 수정", "ar" to "تعديل الملف الشخصي"
        ),
        "account" to mapOf(
            "en" to "Account", "ru" to "Аккаунт", "es" to "Cuenta", "fr" to "Compte", "de" to "Konto",
            "zh" to "账户", "ja" to "アカウント", "ko" to "계정", "ar" to "الحساب", "uk" to "Акаунт"
        ),
        "privacy_security" to mapOf(
            "en" to "Privacy and Security", "ru" to "Конфиденциальность", "es" to "Privacidad y seguridad",
            "fr" to "Confidentialité et sécurité", "de" to "Privatsphäre & Sicherheit", "zh" to "隐私与安全",
            "ja" to "プライバシーとセキュリティ", "ko" to "개인정보 및 보안", "ar" to "الخصوصية والأمان"
        ),
        "privacy_subtitle" to mapOf(
            "en" to "Phone number, online status, 2FA passcode",
            "ru" to "Номер телефона, статус сети, 2FA пароль",
            "es" to "Número de teléfono, estado en línea, código 2FA",
            "fr" to "Numéro de téléphone, statut en ligne, code 2FA"
        ),
        "chat_appearance" to mapOf(
            "en" to "Chat Settings & Appearance", "ru" to "Настройки чатов и Внешний вид",
            "es" to "Ajustes de chat y apariencia", "fr" to "Paramètres de discussion et apparence",
            "de" to "Chat-Einstellungen & Design"
        ),
        "devices" to mapOf(
            "en" to "Devices", "ru" to "Устройства", "es" to "Dispositivos", "fr" to "Appareils", "de" to "Geräte",
            "zh" to "设备", "ja" to "デバイス", "ko" to "기기", "ar" to "الأجهزة"
        ),
        "data_storage" to mapOf(
            "en" to "Data and Storage", "ru" to "Данные и память", "es" to "Datos y almacenamiento",
            "fr" to "Données et stockage", "de" to "Daten und Speicher", "zh" to "数据与存储"
        ),
        "notifications_sounds" to mapOf(
            "en" to "Notifications and Sounds", "ru" to "Уведомления и звуки", "es" to "Notificaciones y sonidos",
            "fr" to "Notifications et sons", "de" to "Benachrichtigungen & Töne"
        ),
        "developer_panel" to mapOf(
            "en" to "Developer Tools (Creator Panel)", "ru" to "Инструменты разработчика (Creator Panel)",
            "es" to "Herramientas de desarrollador (Panel de Creador)", "fr" to "Outils de développeur"
        ),
        "logout" to mapOf(
            "en" to "Log Out", "ru" to "Выйти", "es" to "Cerrar sesión", "fr" to "Se déconnecter", "de" to "Abmelden",
            "zh" to "退出登录", "ja" to "ログアウト", "ko" to "로그아웃", "ar" to "تسجيل الخروج"
        ),
        "chat" to mapOf(
            "en" to "Chat", "ru" to "Чат", "es" to "Chat", "fr" to "Discussion", "de" to "Chat",
            "zh" to "聊天", "ja" to "チャット", "ko" to "채팅", "ar" to "دردشة"
        ),
        "mute" to mapOf(
            "en" to "Mute", "ru" to "Заглушить", "es" to "Silenciar", "fr" to "Couper le son", "de" to "Stummschalten",
            "zh" to "静音", "ja" to "ミュート", "ko" to "음소거", "ar" to "كتم الصوت"
        ),
        "unmute" to mapOf(
            "en" to "Unmute", "ru" to "Включить звук", "es" to "Reactivar sonido", "fr" to "Rétablir le son",
            "de" to "Laut schalten", "zh" to "取消静音", "ja" to "ミュート解除", "ko" to "음소거 해제", "ar" to "إلغاء كتم الصوت"
        ),
        "online" to mapOf(
            "en" to "online", "ru" to "в сети", "es" to "en línea", "fr" to "en ligne", "de" to "online",
            "zh" to "在线", "ja" to "オンライン", "ko" to "온라인", "ar" to "متصل"
        ),
        "last_seen" to mapOf(
            "en" to "last seen recently", "ru" to "был(а) недавно", "es" to "última vez recientemente",
            "fr" to "vu récemment", "de" to "zuletzt kürzlich gesehen", "zh" to "最近上线", "ja" to "最近オンライン"
        ),
        "search" to mapOf(
            "en" to "Search", "ru" to "Поиск", "es" to "Buscar", "fr" to "Rechercher", "de" to "Suche",
            "zh" to "搜索", "ja" to "検索", "ko" to "검색", "ar" to "بحث"
        ),
        "new_group" to mapOf(
            "en" to "New Group", "ru" to "Создать группу", "es" to "Nuevo grupo", "fr" to "Nouveau groupe",
            "de" to "Neue Gruppe", "zh" to "新建群组", "ja" to "新しいグループ"
        ),
        "saved_messages" to mapOf(
            "en" to "Saved Messages", "ru" to "Избранное", "es" to "Mensajes guardados", "fr" to "Messages enregistrés",
            "de" to "Gespeicherte Nachrichten", "zh" to "收藏夹", "ja" to "保存済みメッセージ"
        )
    )

    fun tr(key: String, default: String? = null): String {
        val code = _currentLanguage.value.code
        val langMap = translations[key] ?: return default ?: key
        return langMap[code] ?: langMap["en"] ?: default ?: key
    }
}
