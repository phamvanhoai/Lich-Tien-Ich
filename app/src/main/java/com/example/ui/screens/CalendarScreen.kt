package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.MainViewModel
import com.example.data.Event
import com.example.utils.DateTimeUtils
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val displayMode by viewModel.displayMode.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()

    var showAddEventDialog by remember { mutableStateOf(false) }
    var selectedEventForDetail by remember { mutableStateOf<Event?>(null) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Generate the full-month days around selectedDate
    val monthDays = remember(selectedDate) {
        DateTimeUtils.getMonthDays(selectedDate)
    }
    
    val currentCal = remember(selectedDate) {
        Calendar.getInstance().apply { timeInMillis = selectedDate }
    }
    val currentMonth = currentCal.get(Calendar.MONTH)
    val currentYear = currentCal.get(Calendar.YEAR)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("add_event_fab")
                    .padding(bottom = 48.dp) // elevate to avoid bottom navigation bar overlap
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm sự kiện")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Month Selector & Monthly Calendar Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = selectedDate
                                cal.add(Calendar.MONTH, -1)
                                viewModel.selectDate(cal.timeInMillis)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Tháng trước",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = DateTimeUtils.formatMonthYearVietnamese(selectedDate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            val canChiYear = com.example.utils.LunarUtils.getCanChiYear(currentYear)
                            Text(
                                text = "Năm $canChiYear",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val todayMillis = viewModel.getMidnightMillis(System.currentTimeMillis())
                            if (selectedDate != todayMillis) {
                                TextButton(
                                    onClick = { viewModel.selectDate(todayMillis) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = "Hôm nay",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    cal.timeInMillis = selectedDate
                                    cal.add(Calendar.MONTH, 1)
                                    viewModel.selectDate(cal.timeInMillis)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Tháng sau",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Weekdays header row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val dayHeaders = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                        dayHeaders.forEach { header ->
                            Text(
                                text = header,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (header == "CN") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Month Days Grid
                    val rows = remember(monthDays) { monthDays.chunked(7) }
                    rows.forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            week.forEach { dayMillis ->
                                val dayCal = Calendar.getInstance().apply { timeInMillis = dayMillis }
                                val isSelected = selectedDate == dayMillis
                                val isToday = viewModel.getMidnightMillis(System.currentTimeMillis()) == dayMillis
                                val isCurrentMonth = dayCal.get(Calendar.MONTH) == currentMonth && dayCal.get(Calendar.YEAR) == currentYear
                                
                                val (lDay, lMonth, _) = com.example.utils.LunarUtils.getLunarDate(dayMillis)
                                val lunarDayStr = if (lDay == 1) "$lDay/$lMonth" else lDay.toString()
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable {
                                            viewModel.selectDate(dayMillis)
                                        }
                                        .testTag("date_cell_${DateTimeUtils.getDayOfMonth(dayMillis)}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        // Solar day text
                                        Text(
                                            text = DateTimeUtils.getDayOfMonth(dayMillis),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                isToday -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        
                                        // Lunar day text
                                        Text(
                                            text = lunarDayStr,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontWeight = if (lDay == 1 || lDay == 15) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                                !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                                lDay == 1 || lDay == 15 -> MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            }
                                        )
                                        
                                        // Event indicator dots
                                        val dayEvents = allEvents.filter { it.dateMillis == dayMillis }
                                        if (dayEvents.isNotEmpty()) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                dayEvents.take(3).forEach { ev ->
                                                    val parsedColor = remember(ev.colorHex) {
                                                        try {
                                                            Color(android.graphics.Color.parseColor(ev.colorHex))
                                                        } catch (e: Exception) {
                                                            null
                                                        }
                                                    }
                                                    val dotColor = if (isSelected) {
                                                        MaterialTheme.colorScheme.onPrimary
                                                    } else {
                                                        parsedColor ?: MaterialTheme.colorScheme.primary
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(RoundedCornerShape(100.dp))
                                                            .background(dotColor)
                                                    )
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Header indicating currently selected starting date
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = DateTimeUtils.formatDateVietnamese(selectedDate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = selectedDate
                    val yearCanChi = com.example.utils.LunarUtils.getCanChiYear(cal.get(Calendar.YEAR))
                    val (lD, lM, lLeap) = com.example.utils.LunarUtils.getLunarDate(selectedDate)
                    val lunarText = "Âm lịch: Ngày ${com.example.utils.LunarUtils.getLunarDayName(lD)} tháng $lM${if (lLeap) " (Nhuận)" else ""}, năm $yearCanChi"
                    Text(
                        text = lunarText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                val isTodaySelected = viewModel.getMidnightMillis(System.currentTimeMillis()) == selectedDate
                if (isTodaySelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Hôm nay",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Dynamic schedule views (single day details as requested)
            val daysCount = 1
            val daysList = remember(selectedDate) {
                listOf(selectedDate)
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp), // extra spacer for FAB
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(daysList) { dayMillis ->
                    val eventsForThisDay = allEvents.filter { it.dateMillis == dayMillis }
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Day header if viewing multiple days
                        if (daysCount > 1) {
                            Text(
                                text = "${DateTimeUtils.formatDateVietnamese(dayMillis)} (${com.example.utils.LunarUtils.getLunarDateString(dayMillis)})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        if (eventsForThisDay.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Trống",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Không có sự kiện hoặc cuộc hẹn nào.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        } else {
                            eventsForThisDay.forEach { event ->
                                EventCard(
                                    event = event,
                                    onClick = { selectedEventForDetail = event }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // EVENT DETAIL DIALOG
    selectedEventForDetail?.let { event ->
        Dialog(onDismissRequest = { selectedEventForDetail = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("event_detail_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Category bar and color icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(android.graphics.Color.parseColor(event.colorHex)))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = event.category,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { selectedEventForDetail = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date row (Solar & Lunar)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Ngày",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val dateStr = DateTimeUtils.formatDateVietnamese(event.dateMillis)
                        val lunarStr = com.example.utils.LunarUtils.getLunarDateString(event.dateMillis)
                        Text(
                            text = "$dateStr ($lunarStr)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Time range
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Thời gian",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${event.startTime} - ${event.endTime}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Location if available
                    if (event.location.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(event.location))
                                    Toast.makeText(context, "Đã sao chép địa điểm!", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Địa điểm",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = event.location,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Sao chép",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // URL if available
                    if (event.url.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(event.url))
                                    Toast.makeText(context, "Đã sao chép liên kết!", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Liên kết",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = event.url,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Sao chép",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Reminder offset
                    if (event.hasReminder) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Nhắc nhở",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Báo thức nhắc nhở trước ${event.reminderMinutesBefore} phút",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Note text if any
                    if (event.note.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Ghi chú:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = event.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions block (Delete button)
                    Button(
                        onClick = {
                            viewModel.deleteEvent(event)
                            selectedEventForDetail = null
                            Toast.makeText(context, "Đã xóa sự kiện thành công!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_event_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Xóa Sự Kiện", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // ADD EVENT DIALOG
    if (showAddEventDialog) {
        Dialog(onDismissRequest = { showAddEventDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("add_event_dialog")
            ) {
                var title by remember { mutableStateOf("") }
                var note by remember { mutableStateOf("") }
                var category by remember { mutableStateOf("Công việc") }
                var colorHex by remember { mutableStateOf("#1976D2") }
                
                // Hours input helper
                var startHour by remember { mutableStateOf("09") }
                var startMinute by remember { mutableStateOf("00") }
                var endHour by remember { mutableStateOf("10") }
                var endMinute by remember { mutableStateOf("00") }
                
                var url by remember { mutableStateOf("") }
                var location by remember { mutableStateOf("") }
                var hasReminder by remember { mutableStateOf(false) }
                var reminderOffset by remember { mutableStateOf(15) }

                val categories = listOf(
                    Triple("Công việc", "#1976D2", Icons.Default.Work),
                    Triple("Học tập", "#9C27B0", Icons.Default.School),
                    Triple("Việc nhà", "#4CAF50", Icons.Default.Home),
                    Triple("Ngày lễ", "#E91E63", Icons.Default.Star),
                    Triple("Gia đình", "#FF9800", Icons.Default.Favorite),
                    Triple("Cuộc hẹn", "#009688", Icons.Default.Alarm)
                )

                LazyColumn(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column {
                            Text(
                                text = "Lập kế hoạch mới",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val dateStr = DateTimeUtils.formatDateShort(selectedDate)
                            val lunarStr = com.example.utils.LunarUtils.getLunarDateString(selectedDate)
                            Text(
                                text = "Cho ngày $dateStr ($lunarStr)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Tên sự kiện / hoạt động") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_event_title_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Category Selection Chips
                    item {
                        Text(
                            text = "Chọn danh mục & mã màu",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // First row of categories
                            categories.take(3).forEach { (catName, catColorHex, icon) ->
                                val isCatSelected = category == catName
                                FilterChip(
                                    selected = isCatSelected,
                                    onClick = { 
                                        category = catName
                                        colorHex = catColorHex
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = catName,
                                            tint = if (isCatSelected) MaterialTheme.colorScheme.onPrimary else Color(android.graphics.Color.parseColor(catColorHex)),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text(catName, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Second row of categories
                            categories.drop(3).forEach { (catName, catColorHex, icon) ->
                                val isCatSelected = category == catName
                                FilterChip(
                                    selected = isCatSelected,
                                    onClick = { 
                                        category = catName
                                        colorHex = catColorHex
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = catName,
                                            tint = if (isCatSelected) MaterialTheme.colorScheme.onPrimary else Color(android.graphics.Color.parseColor(catColorHex)),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text(catName, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Time settings
                    item {
                        Text(
                            text = "Thiết lập thời gian (24h)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Start Time
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = startHour,
                                    onValueChange = { if (it.length <= 2) startHour = it },
                                    modifier = Modifier.width(55.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Text(" : ", fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = startMinute,
                                    onValueChange = { if (it.length <= 2) startMinute = it },
                                    modifier = Modifier.width(55.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                            
                            Text("đến")
                            
                            // End Time
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = endHour,
                                    onValueChange = { if (it.length <= 2) endHour = it },
                                    modifier = Modifier.width(55.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Text(" : ", fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = endMinute,
                                    onValueChange = { if (it.length <= 2) endMinute = it },
                                    modifier = Modifier.width(55.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Địa điểm / Địa chỉ (Map)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("Liên kết URL (Zoom, Website,...)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Reminder Switcher
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Báo thức",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Đặt thông báo nhắc nhở", fontWeight = FontWeight.Medium)
                                    if (hasReminder) {
                                        Text("Báo trước $reminderOffset phút", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            Switch(
                                checked = hasReminder,
                                onCheckedChange = { hasReminder = it }
                            )
                        }
                        
                        if (hasReminder) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(5, 15, 30, 60).forEach { mins ->
                                    val isMinsSelected = reminderOffset == mins
                                    FilterChip(
                                        selected = isMinsSelected,
                                        onClick = { reminderOffset = mins },
                                        label = { Text("$mins phút", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Ghi chú bổ sung") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Save / Cancel Actions
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAddEventDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Hủy bỏ")
                            }
                            Button(
                                onClick = {
                                    if (title.isBlank()) {
                                        Toast.makeText(context, "Vui lòng nhập tên sự kiện!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val finalStart = "${startHour.padStart(2, '0')}:${startMinute.padStart(2, '0')}"
                                    val finalEnd = "${endHour.padStart(2, '0')}:${endMinute.padStart(2, '0')}"
                                    viewModel.addEvent(
                                        title = title,
                                        note = note,
                                        category = category,
                                        colorHex = colorHex,
                                        dateMillis = selectedDate,
                                        startTime = finalStart,
                                        endTime = finalEnd,
                                        url = url,
                                        location = location,
                                        hasReminder = hasReminder
                                    )
                                    showAddEventDialog = false
                                    Toast.makeText(context, "Đã thêm sự kiện thành công!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("save_event_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Lưu Lại", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = Color(android.graphics.Color.parseColor(event.colorHex))
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val containerBgColor = if (isDark) {
        cardColor.copy(alpha = 0.16f)
    } else {
        cardColor.copy(alpha = 0.08f)
    }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerBgColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .testTag("event_item_${event.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left color highlight bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(cardColor)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Category Indicator badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(cardColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = cardColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Clock
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Giờ",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${event.startTime} - ${event.endTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Title
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Location / note info snippet
                if (event.note.isNotEmpty() || event.location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (event.location.isNotEmpty()) "📍 ${event.location}" else event.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Right-side indicators for map, urls, reminders
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .fillMaxHeight()
            ) {
                if (event.hasReminder) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Có báo thức",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (event.location.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Có địa chỉ",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (event.url.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Có liên kết",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
