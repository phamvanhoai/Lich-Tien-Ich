package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.MainViewModel
import com.example.ui.theme.FontSizeOptions
import com.example.ui.theme.ThemeOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val themeIndex by viewModel.themeIndex.collectAsState()
    val fontSizeIndex by viewModel.fontSizeIndex.collectAsState()
    val pinCode by viewModel.pinCode.collectAsState()
    val adsRemoved by viewModel.adsRemoved.collectAsState()
    val sharedCalendarsEnabled by viewModel.sharedCalendarsEnabled.collectAsState()

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var isSyncingSimulation by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // PREMIUM UNLOCK SECTION (ADS REMOVE)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (adsRemoved) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                     else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (adsRemoved) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("premium_ad_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Premium Icon",
                            tint = if (adsRemoved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (adsRemoved) "Bản Premium Đã Kích Hoạt ✨" else "Mở Khóa Premium (Lifetime)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (adsRemoved) "Cảm ơn bạn đã ủng hộ nhà phát triển! Ứng dụng của bạn hiện đã sạch bóng quảng cáo, hỗ trợ đầy đủ 20 chủ đề màu sắc cao cấp và đồng bộ hóa thông minh trọn đời."
                               else "Sử dụng lịch trình không giới hạn: Gỡ bỏ toàn bộ biểu ngữ quảng cáo, mở khóa toàn vẹn 20 bộ giao diện màu sắc tùy chỉnh, và đồng bộ hóa đám mây Google Calendar / Outlook.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (!adsRemoved) {
                        Button(
                            onClick = { showCheckoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("purchase_premium_btn")
                        ) {
                            Text("Mua Bản Premium - Chỉ 49.000đ", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Đã mua",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bản quyền Pro Trọn Đời đang hoạt động ổn định.",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // FONT SIZING (10 LEVELS)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Font size",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Kích thước phông chữ (10 Cấp độ)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Đang chọn: " + (FontSizeOptions.getOrNull(fontSizeIndex)?.name ?: "Chuẩn"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Slider(
                        value = fontSizeIndex.toFloat(),
                        onValueChange = { viewModel.updateFontSizeIndex(it.toInt()) },
                        valueRange = 0f..9f,
                        steps = 8,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("font_size_slider")
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Live preview container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Chữ mẫu: Lịch Tiện Ích giúp việc sắp xếp thời gian của bạn trở nên đơn giản như ABC!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 20 THEMING COLORS SELECTION
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Themes",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Giao diện chủ đề (20 Màu sắc)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Lựa chọn mã màu đặc trưng để đồng bộ với thời gian biểu của bạn.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Theme grid
                    ThemeGrid(
                        selectedThemeIndex = themeIndex,
                        adsRemoved = adsRemoved,
                        onThemeSelect = { index ->
                            if (index > 4 && !adsRemoved) {
                                showCheckoutDialog = true
                                Toast.makeText(context, "Vui lòng mở khóa Premium để chọn 15 chủ đề nâng cao còn lại!", Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.updateThemeIndex(index)
                                Toast.makeText(context, "Đã áp dụng màu chủ đề mới!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }

        // PASSCODE LOCK PRIVACY
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Khóa bảo vệ riêng tư",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (pinCode.isEmpty()) {
                        Text(
                            text = "Mã PIN bảo mật hiện đang tắt. Bật khóa bảo vệ để yêu cầu mật khẩu mỗi khi mở ứng dụng Lịch Tiện Ích.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showPinSetupDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("enable_pin_btn")
                        ) {
                            Text("Thiết Lập Mã PIN Khóa App")
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Bảo vệ mã PIN đang hoạt động", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                Text("Đã lưu mã bảo mật của bạn", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                            Button(
                                onClick = {
                                    viewModel.updatePinCode("")
                                    Toast.makeText(context, "Đã tắt mã khóa bảo mật!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("disable_pin_btn")
                            ) {
                                Text("Hủy mã PIN")
                            }
                        }
                    }
                }
            }
        }

        // GOOGLE CALENDAR / EXTERNAL SYNC SIMULATOR
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud Sync",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Đồng bộ hóa đám mây",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Đồng bộ hóa các sự kiện và lịch làm việc của bạn với Google Calendar, Outlook, iCloud, Exchange để chia sẻ thời gian biểu cho đồng nghiệp hoặc gia đình của mình.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Đồng bộ Google Calendar & Outlook", fontWeight = FontWeight.Medium)
                            Text("Cho phép liên kết và cập nhật lịch dùng chung", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }
                        Switch(
                            checked = sharedCalendarsEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.toggleSharedCalendars(enabled)
                                if (enabled) {
                                    isSyncingSimulation = true
                                    scope.launch {
                                        syncMessage = "Đang kết nối API bảo mật Google & Outlook..."
                                        delay(1500)
                                        syncMessage = "Đang kéo dữ liệu Lịch làm việc dùng chung..."
                                        delay(1500)
                                        syncMessage = "Đang đối soát danh sách to-do với đồng nghiệp..."
                                        delay(1500)
                                        isSyncingSimulation = false
                                        Toast.makeText(context, "Đồng bộ hóa đám mây thành công!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.testTag("sync_google_switch")
                        )
                    }

                    AnimatedVisibility(
                        visible = isSyncingSimulation,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = syncMessage,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // CHECKOUT PREMIUM OVERLAY DIALOG (MOCK IN-APP PURCHASE)
    if (showCheckoutDialog) {
        Dialog(onDismissRequest = { showCheckoutDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("premium_checkout_dialog")
            ) {
                var paymentStep by remember { mutableStateOf(0) } // 0: options, 1: processing, 2: success

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (paymentStep == 0) {
                        Text(
                            text = "Thanh toán Premium",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Mở khóa vĩnh viễn toàn bộ tính năng Lịch Tiện Ích Pro, xóa sạch biểu ngữ quảng cáo.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Payment methods options
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
                                    .clickable { paymentStep = 1 }
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
                        
                        TextButton(onClick = { showCheckoutDialog = false }) {
                            Text("Hủy giao dịch", color = MaterialTheme.colorScheme.error)
                        }
                    } else if (paymentStep == 1) {
                        Text("Đang xử lý giao dịch...", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))
                        CircularProgressIndicator(modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Vui lòng không tắt ứng dụng Lịch Tiện Ích. Cổng bảo mật đang đối chiếu số dư.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        
                        LaunchedEffect(Unit) {
                            delay(2000)
                            viewModel.purchaseRemoveAds()
                            paymentStep = 2
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Thanh toán thành công!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Bạn hiện đang là thành viên Premium trọn đời của Lịch Tiện Ích. 20 màu giao diện đã được mở khóa hoàn tất!", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { 
                                showCheckoutDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Bắt đầu trải nghiệm ngay!", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // PIN SETUP MODAL
    if (showPinSetupDialog) {
        Dialog(onDismissRequest = { showPinSetupDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("pin_setup_dialog")
            ) {
                var newPinInput by remember { mutableStateOf("") }
                
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Thiết lập mã PIN mới",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nhập mã PIN gồm 4 chữ số để bảo mật lịch hẹn và to-do gia đình của bạn.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinInput = it },
                        label = { Text("Nhập 4 số PIN") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_pin_input"),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { showPinSetupDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hủy")
                        }
                        Button(
                            onClick = {
                                if (newPinInput.length != 4) {
                                    Toast.makeText(context, "Mã PIN phải có đúng 4 chữ số!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.updatePinCode(newPinInput)
                                showPinSetupDialog = false
                                Toast.makeText(context, "Mã bảo vệ PIN đã kích hoạt thành công!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("save_pin_btn")
                        ) {
                            Text("Lưu mã PIN")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeGrid(
    selectedThemeIndex: Int,
    adsRemoved: Boolean,
    onThemeSelect: (Int) -> Unit
) {
    // 20 custom color themes
    val themes = ThemeOptions

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Gợi ý miễn phí:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            themes.take(5).forEach { theme ->
                val isSelected = selectedThemeIndex == theme.id
                ThemeCircle(
                    theme = theme,
                    isSelected = isSelected,
                    isLocked = false,
                    onClick = { onThemeSelect(theme.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bản Premium Pro (Mở khóa 15 màu sắc đặc sắc):",
            style = MaterialTheme.typography.labelSmall,
            color = if (adsRemoved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Large Premium grid (15 themes)
        val premiumThemes = themes.drop(5)
        
        // Group premium themes in rows of 5 for elegant horizontal blocks
        for (row in 0 until 3) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                premiumThemes.slice(row * 5 until (row + 1) * 5).forEach { theme ->
                    val isSelected = selectedThemeIndex == theme.id
                    ThemeCircle(
                        theme = theme,
                        isSelected = isSelected,
                        isLocked = !adsRemoved,
                        onClick = { onThemeSelect(theme.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeCircle(
    theme: com.example.ui.theme.ThemeOption,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(58.dp)
            .clickable { onClick() }
            .testTag("theme_circle_${theme.id}")
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(theme.primary)
                .border(
                    2.dp,
                    if (isSelected) MaterialTheme.colorScheme.onBackground 
                    else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Label with translated short name
        val shortName = theme.name.substringBefore(" (")
        Text(
            text = shortName,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
