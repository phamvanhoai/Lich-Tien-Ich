package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.theme.FontSizeOptions
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen {
    Calendar,
    Checklist,
    Settings
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = CalendarRepository(db)

    // Screen state
    private val _currentScreen = MutableStateFlow(AppScreen.Calendar)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Selected date in millis (at midnight)
    private val _selectedDate = MutableStateFlow(getMidnightMillis(System.currentTimeMillis()))
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    // Display mode: 1, 3, 5, 7 days
    private val _displayMode = MutableStateFlow(1) // 1, 3, 5, 7
    val displayMode: StateFlow<Int> = _displayMode.asStateFlow()

    // Database events and chores
    val allEvents: StateFlow<List<Event>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChores: StateFlow<List<Chore>> = repository.allChores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings
    private val _themeIndex = MutableStateFlow(0)
    val themeIndex: StateFlow<Int> = _themeIndex.asStateFlow()

    private val _darkModeOption = MutableStateFlow("system") // "system", "light", "dark"
    val darkModeOption: StateFlow<String> = _darkModeOption.asStateFlow()

    private val _fontSizeIndex = MutableStateFlow(4) // standard (100%)
    val fontSizeIndex: StateFlow<Int> = _fontSizeIndex.asStateFlow()

    val fontScale: StateFlow<Float> = _fontSizeIndex.map { index ->
        FontSizeOptions.getOrNull(index)?.scaleFactor ?: 1.0f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    private val _pinCode = MutableStateFlow("")
    val pinCode: StateFlow<String> = _pinCode.asStateFlow()

    private val _userName = MutableStateFlow("Thành Viên")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userBio = MutableStateFlow("Người dùng đam mê tiện ích lịch trình")
    val userBio: StateFlow<String> = _userBio.asStateFlow()

    private val _isUnlocked = MutableStateFlow(true)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _adsRemoved = MutableStateFlow(false)
    val adsRemoved: StateFlow<Boolean> = _adsRemoved.asStateFlow()

    private val _sharedCalendarsEnabled = MutableStateFlow(false)
    val sharedCalendarsEnabled: StateFlow<Boolean> = _sharedCalendarsEnabled.asStateFlow()

    init {
        // Load settings from database
        viewModelScope.launch {
            repository.allSettings.collect { settings ->
                settings.forEach { setting ->
                    when (setting.key) {
                        "theme_index" -> _themeIndex.value = setting.value.toIntOrNull() ?: 0
                        "font_size_index" -> _fontSizeIndex.value = setting.value.toIntOrNull() ?: 4
                        "pin_code" -> {
                            _pinCode.value = setting.value
                            if (setting.value.isNotEmpty()) {
                                _isUnlocked.value = false // lock on app launch if PIN exists
                            }
                        }
                        "ads_removed" -> _adsRemoved.value = setting.value.toBoolean()
                        "shared_calendars_enabled" -> _sharedCalendarsEnabled.value = setting.value.toBoolean()
                        "user_name" -> _userName.value = setting.value
                        "user_bio" -> _userBio.value = setting.value
                        "dark_mode_option" -> _darkModeOption.value = setting.value
                    }
                }
            }
        }
        
        // Populate sample default events & chores on first launch if empty
        viewModelScope.launch {
            repository.allEvents.first().let { currentList ->
                if (currentList.isEmpty()) {
                    populateDefaultData()
                }
            }
        }
    }

    private suspend fun populateDefaultData() {
        val today = getMidnightMillis(System.currentTimeMillis())
        val oneDay = 24 * 60 * 60 * 1000L
        
        val samples = listOf(
            Event(
                title = "Họp nhóm thiết kế Lịch",
                note = "Bàn về 20 màu chủ đề và 10 kích thước phông chữ.",
                category = "Công việc",
                colorHex = "#1976D2",
                dateMillis = today,
                startTime = "09:00",
                endTime = "10:30",
                url = "https://ai.studio/build",
                location = "Hà Nội, Việt Nam",
                hasReminder = true
            ),
            Event(
                title = "Học lập trình Android",
                note = "Học Jetpack Compose nâng cao và Room Database.",
                category = "Học tập",
                colorHex = "#9C27B0",
                dateMillis = today,
                startTime = "14:00",
                endTime = "16:00",
                url = "",
                location = "Thư viện trường",
                hasReminder = false
            ),
            Event(
                title = "Tập thể dục chiều",
                note = "Chạy bộ 5km xung quanh công viên.",
                category = "Cuộc hẹn",
                colorHex = "#009688",
                dateMillis = today + oneDay,
                startTime = "17:30",
                endTime = "18:30",
                url = "",
                location = "Công viên Nghĩa Đô",
                hasReminder = true
            ),
            Event(
                title = "Bữa tối ấm cúng cùng gia đình",
                note = "Chuẩn bị lẩu thái và trái cây tráng miệng.",
                category = "Gia đình",
                colorHex = "#FF9800",
                dateMillis = today,
                startTime = "19:00",
                endTime = "21:30",
                url = "",
                location = "Nhà riêng",
                hasReminder = false
            ),
            Event(
                title = "Ngày Quốc Khánh Việt Nam",
                note = "Nghỉ lễ quốc gia toàn quốc.",
                category = "Ngày lễ",
                colorHex = "#E91E63",
                dateMillis = today + 3 * oneDay,
                startTime = "08:00",
                endTime = "23:00",
                url = "",
                location = "Toàn quốc",
                hasReminder = false
            )
        )
        
        samples.forEach { repository.insertEvent(it) }

        val sampleChores = listOf(
            Chore(title = "Dọn dẹp phòng làm việc", category = "Việc nhà", isCompleted = false, dueDateMillis = today),
            Chore(title = "Mua sữa và thực phẩm tuần mới", category = "Gia đình", isCompleted = true, dueDateMillis = today),
            Chore(title = "Nộp bài tập lập trình di động", category = "Học tập", isCompleted = false, dueDateMillis = today + oneDay),
            Chore(title = "Tưới hoa ngoài ban công", category = "Việc nhà", isCompleted = false, dueDateMillis = today + oneDay)
        )
        sampleChores.forEach { repository.insertChore(it) }
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectDate(millis: Long) {
        _selectedDate.value = getMidnightMillis(millis)
    }

    fun setDisplayMode(mode: Int) {
        if (mode in listOf(1, 3, 5, 7)) {
            _displayMode.value = mode
        }
    }

    // Event operations
    fun addEvent(
        title: String,
        note: String,
        category: String,
        colorHex: String,
        dateMillis: Long,
        startTime: String,
        endTime: String,
        url: String,
        location: String,
        hasReminder: Boolean
    ) {
        viewModelScope.launch {
            val event = Event(
                title = title,
                note = note,
                category = category,
                colorHex = colorHex,
                dateMillis = getMidnightMillis(dateMillis),
                startTime = startTime,
                endTime = endTime,
                url = url,
                location = location,
                hasReminder = hasReminder
            )
            repository.insertEvent(event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    // Chore operations
    fun addChore(title: String, category: String, dueDateMillis: Long) {
        viewModelScope.launch {
            val chore = Chore(
                title = title,
                category = category,
                isCompleted = false,
                dueDateMillis = getMidnightMillis(dueDateMillis)
            )
            repository.insertChore(chore)
        }
    }

    fun toggleChore(chore: Chore) {
        viewModelScope.launch {
            repository.updateChore(chore.copy(isCompleted = !chore.isCompleted))
        }
    }

    fun deleteChore(chore: Chore) {
        viewModelScope.launch {
            repository.deleteChore(chore)
        }
    }

    // Setting update operations
    fun updateThemeIndex(index: Int) {
        _themeIndex.value = index
        viewModelScope.launch {
            repository.saveSetting("theme_index", index.toString())
        }
    }

    fun updateFontSizeIndex(index: Int) {
        _fontSizeIndex.value = index
        viewModelScope.launch {
            repository.saveSetting("font_size_index", index.toString())
        }
    }

    fun updatePinCode(pin: String) {
        _pinCode.value = pin
        viewModelScope.launch {
            repository.saveSetting("pin_code", pin)
            if (pin.isEmpty()) {
                _isUnlocked.value = true
            }
        }
    }

    fun verifyPinCode(pin: String): Boolean {
        return if (pin == _pinCode.value) {
            _isUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun lockApp() {
        if (_pinCode.value.isNotEmpty()) {
            _isUnlocked.value = false
        }
    }

    fun purchaseRemoveAds() {
        _adsRemoved.value = true
        viewModelScope.launch {
            repository.saveSetting("ads_removed", "true")
        }
    }

    fun toggleSharedCalendars(enabled: Boolean) {
        _sharedCalendarsEnabled.value = enabled
        viewModelScope.launch {
            repository.saveSetting("shared_calendars_enabled", enabled.toString())
        }
    }

    fun updateUserName(name: String) {
        _userName.value = name
        viewModelScope.launch {
            repository.saveSetting("user_name", name)
        }
    }

    fun updateUserBio(bio: String) {
        _userBio.value = bio
        viewModelScope.launch {
            repository.saveSetting("user_bio", bio)
        }
    }

    fun updateDarkModeOption(option: String) {
        _darkModeOption.value = option
        viewModelScope.launch {
            repository.saveSetting("dark_mode_option", option)
        }
    }

    fun getMidnightMillis(timeMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
