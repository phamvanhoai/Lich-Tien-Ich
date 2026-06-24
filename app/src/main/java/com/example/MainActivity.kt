package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ChecklistScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.DateTimeUtils
import com.example.utils.LunarUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeIndex by viewModel.themeIndex.collectAsState()
            val fontScale by viewModel.fontScale.collectAsState()
            val isUnlocked by viewModel.isUnlocked.collectAsState()
            val pinCode by viewModel.pinCode.collectAsState()
            val darkModeOption by viewModel.darkModeOption.collectAsState()

            val darkTheme = when (darkModeOption) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MyApplicationTheme(
                darkTheme = darkTheme,
                themeIndex = themeIndex,
                fontScale = fontScale
            ) {
                if (!isUnlocked && pinCode.isNotEmpty()) {
                    LockScreen(viewModel = viewModel)
                } else {
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val adsRemoved by viewModel.adsRemoved.collectAsState()
                    val allEvents by viewModel.allEvents.collectAsState()
                    val allChores by viewModel.allChores.collectAsState()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                modifier = Modifier.width(320.dp),
                                drawerContainerColor = MaterialTheme.colorScheme.background
                            ) {
                                DrawerContent(
                                    allEvents = allEvents,
                                    allChores = allChores,
                                    currentScreen = currentScreen,
                                    adsRemoved = adsRemoved,
                                    onScreenSelect = { screen ->
                                        viewModel.setScreen(screen)
                                        scope.launch { drawerState.close() }
                                    },
                                    onPurchasePremium = {
                                        scope.launch { drawerState.close() }
                                    },
                                    onLockApp = {
                                        viewModel.lockApp()
                                        scope.launch { drawerState.close() }
                                    },
                                    onSelectToday = {
                                        viewModel.selectDate(viewModel.getMidnightMillis(System.currentTimeMillis()))
                                        viewModel.setScreen(AppScreen.Calendar)
                                        scope.launch { drawerState.close() }
                                    },
                                    pinCode = pinCode,
                                    onClose = {
                                        scope.launch { drawerState.close() }
                                    }
                                )
                            }
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    navigationIcon = {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 12.dp, end = 4.dp)
                                                .size(38.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    scope.launch { drawerState.open() }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "Menu chính",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                title = {
                                    Text(
                                        text = "Lịch Tiện Ích",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                actions = {
                                    // Decorative Search icon in a circle-like modern square-round box
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { /* decorative */ }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Tìm kiếm",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    if (pinCode.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surface,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { viewModel.lockApp() }
                                                .padding(8.dp)
                                                .testTag("lock_action_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Khóa ứng dụng",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        },
                        bottomBar = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.testTag("app_navigation_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.Calendar,
                                        onClick = { viewModel.setScreen(AppScreen.Calendar) },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "Lịch Trình"
                                            )
                                        },
                                        label = { Text("Lịch Trình", fontWeight = FontWeight.Bold) },
                                        modifier = Modifier.testTag("nav_calendar")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.Checklist,
                                        onClick = { viewModel.setScreen(AppScreen.Checklist) },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Checklist,
                                                contentDescription = "Danh sách"
                                            )
                                        },
                                        label = { Text("Danh Sách", fontWeight = FontWeight.Bold) },
                                        modifier = Modifier.testTag("nav_checklist")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.Settings,
                                        onClick = { viewModel.setScreen(AppScreen.Settings) },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = "Cài Đặt"
                                            )
                                        },
                                        label = { Text("Cài Đặt", fontWeight = FontWeight.Bold) },
                                        modifier = Modifier.testTag("nav_settings")
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            when (currentScreen) {
                                AppScreen.Calendar -> CalendarScreen(viewModel = viewModel)
                                AppScreen.Checklist -> ChecklistScreen(viewModel = viewModel)
                                AppScreen.Settings -> SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                    }


                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    allEvents: List<com.example.data.Event>,
    allChores: List<com.example.data.Chore>,
    currentScreen: AppScreen,
    adsRemoved: Boolean,
    onScreenSelect: (AppScreen) -> Unit,
    onPurchasePremium: () -> Unit,
    onLockApp: () -> Unit,
    onSelectToday: () -> Unit,
    pinCode: String,
    onClose: () -> Unit
) {
    var showHelpGuide by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Close Drawer button & App Logo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Lịch Tiện Ích",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Phiên bản v1.2.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng menu",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Solar / Lunar Calendar info block
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Hôm nay",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = DateTimeUtils.formatDateVietnamese(System.currentTimeMillis()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = LunarUtils.getLunarDateString(System.currentTimeMillis()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Application statistics counters
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = allEvents.size.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sự kiện lịch",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val pendingChores = allChores.count { !it.isCompleted }
                    Text(
                        text = pendingChores.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Việc cần làm",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Navigation Sections
        Text(
            text = "CHỨC NĂNG",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Drawer Item - Calendar Screen
        NavigationDrawerItem(
            label = { Text("Lịch trình chính", fontWeight = FontWeight.Bold) },
            selected = currentScreen == AppScreen.Calendar,
            onClick = { onScreenSelect(AppScreen.Calendar) },
            icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent
            )
        )

        // Navigation Drawer Item - Checklist Screen
        NavigationDrawerItem(
            label = { Text("Danh sách công việc", fontWeight = FontWeight.Bold) },
            selected = currentScreen == AppScreen.Checklist,
            onClick = { onScreenSelect(AppScreen.Checklist) },
            icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent
            )
        )

        // Navigation Drawer Item - Settings Screen
        NavigationDrawerItem(
            label = { Text("Cấu hình cài đặt", fontWeight = FontWeight.Bold) },
            selected = currentScreen == AppScreen.Settings,
            onClick = { onScreenSelect(AppScreen.Settings) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent
            )
        )

        // Select Today quick action
        NavigationDrawerItem(
            label = { Text("Xem hôm nay", fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = onSelectToday,
            icon = { Icon(Icons.Default.Today, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        // User guide expandable block
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .clickable { showHelpGuide = !showHelpGuide }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Help",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hướng dẫn sử dụng nhanh",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = if (showHelpGuide) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (showHelpGuide) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Bấm nút tròn '+' góc dưới để thêm sự kiện mới hoặc công việc mới một cách nhanh chóng.\n" +
                               "• Vuốt lịch tháng sang trái/phải hoặc chạm mũi tên để đổi tháng.\n" +
                               "• Đặt mật khẩu PIN trong phần Cài đặt để bảo mật ứng dụng an toàn.\n" +
                               "• Tùy chọn 20 giao diện sắc màu đa dạng cho phong cách riêng của bạn.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Action Buttons at bottom
        if (pinCode.isNotEmpty()) {
            OutlinedButton(
                onClick = onLockApp,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Khóa App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
