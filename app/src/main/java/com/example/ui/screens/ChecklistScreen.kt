package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.Chore
import com.example.utils.DateTimeUtils
import java.util.*

@Composable
fun ChecklistScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allChores by viewModel.allChores.collectAsState()

    var newTaskTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Việc nhà") }
    var selectedDueDaysOffset by remember { mutableStateOf(0) } // 0: today, 1: tomorrow, 7: next week

    var showCompletedSection by remember { mutableStateOf(true) }

    val context = LocalContext.current

    // Categorization list
    val categories = listOf(
        Pair("Việc nhà", "#4CAF50"),
        Pair("Gia đình", "#FF9800"),
        Pair("Học tập", "#9C27B0"),
        Pair("Công việc", "#1976D2"),
        Pair("Thể thao", "#009688")
    )

    val outstandingChores = remember(allChores) { allChores.filter { !it.isCompleted } }
    val completedChores = remember(allChores) { allChores.filter { it.isCompleted } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Quick input text box
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Lên danh sách việc cần làm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = { Text("Quét nhà, mua sắm thực phẩm,...", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chore_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (newTaskTitle.isBlank()) {
                                Toast.makeText(context, "Vui lòng nhập công việc!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val offsetMillis = selectedDueDaysOffset * 24 * 60 * 60 * 1000L
                            val dueTime = System.currentTimeMillis() + offsetMillis
                            
                            viewModel.addChore(
                                title = newTaskTitle,
                                category = selectedCategory,
                                dueDateMillis = dueTime
                            )
                            newTaskTitle = ""
                            Toast.makeText(context, "Đã thêm công việc mới!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("add_chore_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fast Category Selector Chips
                Text("Danh mục:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { (catName, catColor) ->
                        val isSelected = selectedCategory == catName
                        val chipColor = Color(android.graphics.Color.parseColor(catColor))
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = catName },
                            label = { Text(catName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("category_chip_$catName")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Due date selector chips
                Text("Thời hạn hoàn thành:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val dueOffsets = listOf(
                        0 to "Hôm nay",
                        1 to "Ngày mai",
                        7 to "Tuần sau"
                    )
                    dueOffsets.forEach { (offset, label) ->
                        val isSelected = selectedDueDaysOffset == offset
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDueDaysOffset = offset },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.testTag("due_offset_chip_$offset")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lists section
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (outstandingChores.isEmpty() && completedChores.isEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Trống",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Danh sách trống!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hãy nhập các việc cần dọn dẹp, mua sắm hoặc chuẩn bị bài học ở trên để luôn ngăn nắp.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }

            // Outstanding list
            if (outstandingChores.isNotEmpty()) {
                item {
                    Text(
                        text = "Việc cần làm (${outstandingChores.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(outstandingChores) { chore ->
                    ChoreRow(
                        chore = chore,
                        onToggle = { viewModel.toggleChore(chore) },
                        onDelete = {
                            viewModel.deleteChore(chore)
                            Toast.makeText(context, "Đã xóa công việc!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Completed list (collapsible)
            if (completedChores.isNotEmpty()) {
                item {
                    val arrowRotation by animateFloatAsState(targetValue = if (showCompletedSection) 180f else 0f)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCompletedSection = !showCompletedSection }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Đã hoàn thành (${completedChores.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Mở rộng",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }
                }

                if (showCompletedSection) {
                    items(completedChores) { chore ->
                        ChoreRow(
                            chore = chore,
                            onToggle = { viewModel.toggleChore(chore) },
                            onDelete = {
                                viewModel.deleteChore(chore)
                                Toast.makeText(context, "Đã xóa công việc!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChoreRow(
    chore: Chore,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (chore.category) {
        "Việc nhà" -> Color(0xFF4CAF50)
        "Gia đình" -> Color(0xFFFF9800)
        "Học tập" -> Color(0xFF9C27B0)
        "Công việc" -> Color(0xFF1976D2)
        "Thể thao" -> Color(0xFF009688)
        else -> MaterialTheme.colorScheme.primary
    }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val containerBgColor = if (chore.isCompleted) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    } else {
        if (isDark) {
            categoryColor.copy(alpha = 0.16f)
        } else {
            categoryColor.copy(alpha = 0.08f)
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerBgColor
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .testTag("chore_item_${chore.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Checkbox
            Checkbox(
                checked = chore.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("chore_check_${chore.id}")
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Text Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = chore.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Time marker
                    val (ld, lm, _) = com.example.utils.LunarUtils.getLunarDate(chore.dueDateMillis)
                    Text(
                        text = "Hạn: " + DateTimeUtils.formatDateShort(chore.dueDateMillis) + " (${ld}/${lm} âm)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = chore.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (chore.isCompleted) FontWeight.Normal else FontWeight.Medium,
                    color = if (chore.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) 
                            else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (chore.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Trash Icon Button to Delete
            IconButton(
                onClick = { onDelete() },
                modifier = Modifier.testTag("chore_delete_${chore.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Xóa công việc",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}
