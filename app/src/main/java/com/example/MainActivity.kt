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
import kotlinx.coroutines.delay

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

            MyApplicationTheme(
                themeIndex = themeIndex,
                fontScale = fontScale
            ) {
                if (!isUnlocked && pinCode.isNotEmpty()) {
                    LockScreen(viewModel = viewModel)
                } else {
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val adsRemoved by viewModel.adsRemoved.collectAsState()

                    var showAdDetailsCheckout by remember { mutableStateOf(false) }

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
                                            ),
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

                                    // Initials profile avatar in modern square-round box
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "TV",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        },
                        bottomBar = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Dynamic simulated ad banner if not purchased premium
                                if (!adsRemoved) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showAdDetailsCheckout = true }
                                            .testTag("ad_banner_bar")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Campaign,
                                                    contentDescription = "QC",
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Tắt quảng cáo & Mở khóa 20 giao diện màu sắc",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Text(
                                                text = "XÓA QUẢNG CÁO",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier
                                                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

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

                    // AD CHECKOUT GATEWAY OVERLAY DIALOG
                    if (showAdDetailsCheckout) {
                        Dialog(onDismissRequest = { showAdDetailsCheckout = false }) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .testTag("ad_checkout_dialog")
                            ) {
                                var payStep by remember { mutableStateOf(0) } // 0: options, 1: loading, 2: success

                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (payStep == 0) {
                                        Text(
                                            text = "Gỡ quảng cáo trọn đời",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Sở hữu giao diện tối giản không quảng cáo, mở khóa toàn vẹn 20 màu giao diện nghệ thuật chỉ với 1 lần mua duy nhất.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        
                                        val methods = listOf(
                                            "Thẻ tín dụng / Ghi nợ (Visa/Mastercard)",
                                            "Ví điện tử MoMo",
                                            "Thanh toán ShopeePay",
                                            "Chuyển khoản Ngân hàng (QR Pay)"
                                        )
                                        methods.forEach { method ->
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { payStep = 1 }
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Payment,
                                                        contentDescription = "Pay icon",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(method, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                }
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        TextButton(onClick = { showAdDetailsCheckout = false }) {
                                            Text("Hủy", color = MaterialTheme.colorScheme.error)
                                        }
                                    } else if (payStep == 1) {
                                        Text("Đang kết nối cổng MoMo/Visa...", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(24.dp))
                                        CircularProgressIndicator(modifier = Modifier.size(50.dp))
                                        Spacer(modifier = Modifier.height(24.dp))
                                        
                                        LaunchedEffect(Unit) {
                                            delay(1500)
                                            viewModel.purchaseRemoveAds()
                                            payStep = 2
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Celebration,
                                            contentDescription = "Thành công",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("Kích hoạt Premium thành công!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Biểu ngữ quảng cáo đã bị gỡ vĩnh viễn. Đã mở khóa 15 màu sắc chủ đề nghệ thuật mới!", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(
                                            onClick = { showAdDetailsCheckout = false },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Bắt đầu khám phá", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
