# Tính năng: Chế độ Sáng / Tối / Hệ thống

> **Mục tiêu:** Thêm mục "Giao diện" vào trang Cá nhân (`StaffProfileScreen`), cho phép người dùng chọn chế độ hiển thị. Lựa chọn được lưu vĩnh viễn qua DataStore và áp dụng toàn app ngay lập tức.

---

## 1. Cấu trúc file cần tạo / chỉnh sửa

```
app/src/main/java/com/example/dacs3/
├── data/
│   └── local/
│       └── datastore/
│           └── ThemeSettings.kt          ← TẠO MỚI
├── ui/
│   ├── theme/
│   │   ├── Color.kt                      ← CHỈNH SỬA (thêm màu dark)
│   │   ├── Theme.kt                      ← CHỈNH SỬA (nhận tham số themeMode)
│   │   └── DesignSystem.kt               ← TẠO MỚI (nếu cần token ngoài M3)
│   ├── viewmodel/
│   │   └── MainViewModel.kt              ← TẠO MỚI
│   └── screens/
│       └── staff/
│           └── StaffProfileScreen.kt     ← CHỈNH SỬA (thêm SectionCard mới)
└── MainActivity.kt                       ← CHỈNH SỬA (kết nối ViewModel → Theme)
```

---

## 2. Enum ThemeMode

```kotlin
// Dùng chung toàn dự án
enum class ThemeMode { LIGHT, DARK, SYSTEM }
```

---

## 3. ThemeSettings.kt — Lưu trữ DataStore

**Chức năng:** Lưu và đọc lựa chọn theme bằng `Preferences DataStore`.

```kotlin
// Dependency cần thêm vào build.gradle (app):
// implementation("androidx.datastore:datastore-preferences:1.0.0")

object ThemeSettings {
    private val Context.dataStore by preferencesDataStore(name = "theme_prefs")
    private val THEME_KEY = stringPreferencesKey("theme_mode")

    // Đọc: trả về Flow<ThemeMode>, mặc định SYSTEM
    fun getThemeMode(context: Context): Flow<ThemeMode> =
        context.dataStore.data.map { prefs ->
            ThemeMode.valueOf(prefs[THEME_KEY] ?: ThemeMode.SYSTEM.name)
        }

    // Ghi: lưu ThemeMode vào DataStore
    suspend fun saveThemeMode(context: Context, mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode.name
        }
    }
}
```

---

## 4. MainViewModel.kt — Quản lý State

**Chức năng:** Cầu nối giữa DataStore và UI. Cung cấp `StateFlow` để toàn app quan sát.

```kotlin
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // State hiện tại của theme — UI observe cái này
    val themeMode: StateFlow<ThemeMode> = ThemeSettings
        .getThemeMode(application)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM
        )

    // Gọi khi người dùng chọn mode mới
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            ThemeSettings.saveThemeMode(getApplication(), mode)
        }
    }
}
```

---

## 5. Color.kt — Bảng màu Sáng & Tối

**Nguyên tắc đặt tên:** `<Tên>Light` cho sáng, `<Tên>Dark` cho tối.

```kotlin
// === LIGHT ===
val PrimaryLight       = Color(0xFF1241AF)
val OnPrimaryLight     = Color(0xFFFFFFFF)
val BackgroundLight    = Color(0xFFF1F5F9)
val SurfaceLight       = Color(0xFFFFFFFF)
val OnSurfaceLight     = Color(0xFF0F172A)
val SecondaryTextLight = Color(0xFF64748B)

// === DARK ===
val PrimaryDark        = Color(0xFF3D6FD4)
val OnPrimaryDark      = Color(0xFFFFFFFF)
val BackgroundDark     = Color(0xFF0F172A)
val SurfaceDark        = Color(0xFF1E293B)
val OnSurfaceDark      = Color(0xFFF1F5F9)
val SecondaryTextDark  = Color(0xFF94A3B8)
```

---

## 6. Theme.kt — Áp dụng ColorScheme

**Chức năng:** Nhận `themeMode: ThemeMode` từ ngoài vào, chọn đúng bảng màu.

```kotlin
@Composable
fun DACS3Theme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    // Đổi màu Status Bar & Navigation Bar theo theme
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.primary.toArgb()
        WindowCompat.getInsetsController(window, view)
            .isAppearanceLightStatusBars = !isDark
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

private val LightColorScheme = lightColorScheme(
    primary          = PrimaryLight,
    onPrimary        = OnPrimaryLight,
    background       = BackgroundLight,
    surface          = SurfaceLight,
    onSurface        = OnSurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary          = PrimaryDark,
    onPrimary        = OnPrimaryDark,
    background       = BackgroundDark,
    surface          = SurfaceDark,
    onSurface        = OnSurfaceDark,
)
```

---

## 7. MainActivity.kt — Kết nối ViewModel

```kotlin
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Observe themeMode từ ViewModel
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()

            DACS3Theme(themeMode = themeMode) {
                // NavHost / Scaffold của app ở đây
            }
        }
    }
}
```

---

## 8. UI — Mục "Giao diện" trong StaffProfileScreen

**Vị trí chèn:** Thêm `SectionCard` mới **sau mục Kinh nghiệm**, trước `Spacer(40.dp)`.

```kotlin
// Nhận thêm tham số:
// mainViewModel: MainViewModel (truyền từ Activity/NavHost)

@Composable
fun StaffProfileScreen(
    userViewModel: UserViewModel,
    staffViewModel: StaffViewModel,
    mainViewModel: MainViewModel,       // ← THÊM
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val currentTheme by mainViewModel.themeMode.collectAsStateWithLifecycle()

    // ... code hiện tại ...

    // === THÊM VÀO SAU SectionCard("KINH NGHIỆM") ===
    Spacer(modifier = Modifier.height(16.dp))

    SectionCard(title = "GIAO DIỆN") {
        ThemeModeSelector(
            currentMode = currentTheme,
            onModeSelected = { mainViewModel.setThemeMode(it) }
        )
    }
}

// -------------------------------------------------------
// Composable con: 3 lựa chọn dạng nút toggle ngang
// -------------------------------------------------------
@Composable
fun ThemeModeSelector(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    val primaryColor = Color(0xFF1241AF)

    val options = listOf(
        Triple(ThemeMode.LIGHT,  "Sáng",    Icons.Default.LightMode),
        Triple(ThemeMode.DARK,   "Tối",     Icons.Default.DarkMode),
        Triple(ThemeMode.SYSTEM, "Hệ thống",Icons.Default.SettingsBrightness)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F5F9)),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        options.forEach { (mode, label, icon) ->
            val isSelected = currentMode == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) primaryColor else Color.Transparent
                    )
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color.White else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFF64748B)
                    )
                }
            }
        }
    }
}
```

---

## 9. res/values/themes.xml — Đồng bộ màu cửa sổ

Thêm file `res/values-night/themes.xml` để tránh "chớp trắng" khi app khởi động:

```xml
<!-- res/values/themes.xml (chế độ sáng) -->
<style name="Theme.DACS3" parent="android:Theme.Material.Light.NoActionBar">
    <item name="android:windowBackground">@color/background_light</item>
    <item name="android:statusBarColor">@color/primary_light</item>
</style>

<!-- res/values-night/themes.xml (chế độ tối) -->
<style name="Theme.DACS3" parent="android:Theme.Material.NoActionBar">
    <item name="android:windowBackground">@color/background_dark</item>
    <item name="android:statusBarColor">@color/primary_dark</item>
</style>
```

---

## 10. Luồng hoạt động tổng thể

```
Người dùng nhấn "Tối"
        ↓
ThemeModeSelector gọi mainViewModel.setThemeMode(DARK)
        ↓
MainViewModel gọi ThemeSettings.saveThemeMode(context, DARK)
        ↓
DataStore lưu "DARK" vào bộ nhớ vĩnh viễn
        ↓
StateFlow phát giá trị mới → MainActivity recompose
        ↓
DACS3Theme nhận themeMode = DARK → dùng DarkColorScheme
        ↓
Toàn bộ app đổi màu (Button, Text, Card, StatusBar)
```

---

## 11. Checklist tích hợp

- [ ] Thêm dependency `datastore-preferences` vào `build.gradle`
- [ ] Tạo `ThemeSettings.kt` với DataStore
- [ ] Tạo `MainViewModel.kt` với `StateFlow<ThemeMode>`
- [ ] Cập nhật `Color.kt` — thêm màu Dark
- [ ] Cập nhật `Theme.kt` — nhận `themeMode` tham số
- [ ] Cập nhật `MainActivity.kt` — observe ViewModel
- [ ] Thêm `ThemeModeSelector` vào `StaffProfileScreen`
- [ ] Thêm `res/values-night/themes.xml`
- [ ] Test: tắt app → mở lại → theme cũ vẫn được nhớ