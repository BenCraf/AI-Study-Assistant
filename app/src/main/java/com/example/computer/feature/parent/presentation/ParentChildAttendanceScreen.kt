// ParentChildAttendanceScreen.kt
package com.example.computer.feature.parent.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ==================== 数据模型 ====================

data class AttendanceRecord(
    val date: String,
    val status: AttendanceStatus,
    val checkInTime: String?,
    val checkOutTime: String?,
    val reason: String?,
    val approvedBy: String?
)

enum class AttendanceStatus {
    PRESENT,      // 出勤
    LATE,         // 迟到
    LEAVE,        // 请假
    ABSENT,       // 缺勤
    SICK_LEAVE    // 病假
}

data class AttendanceStatistics(
    val totalDays: Int,
    val presentDays: Int,
    val lateDays: Int,
    val leaveDays: Int,
    val absentDays: Int,
    val attendanceRate: Double
)

data class MonthlyAttendance(
    val childName: String,
    val grade: String,
    val month: String,
    val records: List<AttendanceRecord>,
    val statistics: AttendanceStatistics,
    val warnings: List<String>
)

// ==================== 数据仓库 ====================

object ParentAttendanceRepository {

    fun getChildAttendance(childName: String): MonthlyAttendance {
        val records = when (childName) {
            "小明" -> getAttendanceForXiaoMing()
            "小红" -> getAttendanceForXiaoHong()
            "小华" -> getAttendanceForXiaoHua()
            else -> getAttendanceForXiaoMing()
        }

        val statistics = calculateStatistics(records)
        val warnings = generateWarnings(statistics, records)

        return MonthlyAttendance(
            childName = childName,
            grade = when (childName) {
                "小明" -> "五年级"
                "小红" -> "四年级"
                "小华" -> "六年级"
                else -> "五年级"
            },
            month = "2024年12月",
            records = records,
            statistics = statistics,
            warnings = warnings
        )
    }

    private fun getAttendanceForXiaoMing(): List<AttendanceRecord> {
        return listOf(
            AttendanceRecord("2024-12-02", AttendanceStatus.PRESENT, "07:45", "16:30", null, null),
            AttendanceRecord("2024-12-03", AttendanceStatus.PRESENT, "07:50", "16:30", null, null),
            AttendanceRecord("2024-12-04", AttendanceStatus.LATE, "08:15", "16:30", "交通堵塞", null),
            AttendanceRecord("2024-12-05", AttendanceStatus.PRESENT, "07:40", "16:30", null, null),
            AttendanceRecord("2024-12-06", AttendanceStatus.PRESENT, "07:55", "16:30", null, null),
            AttendanceRecord("2024-12-09", AttendanceStatus.PRESENT, "07:45", "16:30", null, null),
            AttendanceRecord("2024-12-10", AttendanceStatus.PRESENT, "07:50", "16:30", null, null),
            AttendanceRecord("2024-12-11", AttendanceStatus.PRESENT, "07:42", "16:30", null, null),
            AttendanceRecord("2024-12-12", AttendanceStatus.SICK_LEAVE, null, null, "感冒发烧", "王老师"),
            AttendanceRecord("2024-12-13", AttendanceStatus.PRESENT, "07:48", "16:30", null, null),
            AttendanceRecord("2024-12-16", AttendanceStatus.PRESENT, "07:52", "16:30", null, null),
            AttendanceRecord("2024-12-17", AttendanceStatus.PRESENT, "07:44", "16:30", null, null),
            AttendanceRecord("2024-12-18", AttendanceStatus.PRESENT, "07:46", "16:30", null, null),
            AttendanceRecord("2024-12-19", AttendanceStatus.PRESENT, "07:50", "16:30", null, null),
            AttendanceRecord("2024-12-20", AttendanceStatus.PRESENT, "07:43", "16:30", null, null),
            AttendanceRecord("2024-12-23", AttendanceStatus.PRESENT, "07:47", "16:30", null, null),
            AttendanceRecord("2024-12-24", AttendanceStatus.PRESENT, "07:51", "16:30", null, null),
            AttendanceRecord("2024-12-25", AttendanceStatus.PRESENT, "07:49", "16:30", null, null),
            AttendanceRecord("2024-12-26", AttendanceStatus.PRESENT, "07:45", "16:30", null, null)
        )
    }

    private fun getAttendanceForXiaoHong(): List<AttendanceRecord> {
        return listOf(
            AttendanceRecord("2024-12-02", AttendanceStatus.PRESENT, "07:55", "16:30", null, null),
            AttendanceRecord("2024-12-03", AttendanceStatus.LATE, "08:10", "16:30", "起床晚了", null),
            AttendanceRecord("2024-12-04", AttendanceStatus.PRESENT, "07:58", "16:30", null, null),
            AttendanceRecord("2024-12-05", AttendanceStatus.LEAVE, null, null, "家庭事务", "刘老师"),
            AttendanceRecord("2024-12-06", AttendanceStatus.PRESENT, "07:52", "16:30", null, null),
            AttendanceRecord("2024-12-09", AttendanceStatus.LATE, "08:12", "16:30", "交通原因", null),
            AttendanceRecord("2024-12-10", AttendanceStatus.PRESENT, "07:50", "16:30", null, null),
            AttendanceRecord("2024-12-11", AttendanceStatus.PRESENT, "07:54", "16:30", null, null),
            AttendanceRecord("2024-12-12", AttendanceStatus.PRESENT, "07:56", "16:30", null, null),
            AttendanceRecord("2024-12-13", AttendanceStatus.ABSENT, null, null, "未说明原因", null),
            AttendanceRecord("2024-12-16", AttendanceStatus.PRESENT, "07:48", "16:30", null, null),
            AttendanceRecord("2024-12-17", AttendanceStatus.LATE, "08:08", "16:30", null, null),
            AttendanceRecord("2024-12-18", AttendanceStatus.PRESENT, "07:52", "16:30", null, null),
            AttendanceRecord("2024-12-19", AttendanceStatus.PRESENT, "07:55", "16:30", null, null),
            AttendanceRecord("2024-12-20", AttendanceStatus.PRESENT, "07:50", "16:30", null, null),
            AttendanceRecord("2024-12-23", AttendanceStatus.SICK_LEAVE, null, null, "肚子疼", "刘老师"),
            AttendanceRecord("2024-12-24", AttendanceStatus.PRESENT, "07:53", "16:30", null, null),
            AttendanceRecord("2024-12-25", AttendanceStatus.PRESENT, "07:57", "16:30", null, null),
            AttendanceRecord("2024-12-26", AttendanceStatus.PRESENT, "07:51", "16:30", null, null)
        )
    }

    private fun getAttendanceForXiaoHua(): List<AttendanceRecord> {
        return listOf(
            AttendanceRecord("2024-12-02", AttendanceStatus.PRESENT, "07:35", "17:00", null, null),
            AttendanceRecord("2024-12-03", AttendanceStatus.PRESENT, "07:40", "17:00", null, null),
            AttendanceRecord("2024-12-04", AttendanceStatus.PRESENT, "07:38", "17:00", null, null),
            AttendanceRecord("2024-12-05", AttendanceStatus.PRESENT, "07:42", "17:00", null, null),
            AttendanceRecord("2024-12-06", AttendanceStatus.PRESENT, "07:36", "17:00", null, null),
            AttendanceRecord("2024-12-09", AttendanceStatus.PRESENT, "07:39", "17:00", null, null),
            AttendanceRecord("2024-12-10", AttendanceStatus.PRESENT, "07:37", "17:00", null, null),
            AttendanceRecord("2024-12-11", AttendanceStatus.PRESENT, "07:41", "17:00", null, null),
            AttendanceRecord("2024-12-12", AttendanceStatus.PRESENT, "07:40", "17:00", null, null),
            AttendanceRecord("2024-12-13", AttendanceStatus.PRESENT, "07:38", "17:00", null, null),
            AttendanceRecord("2024-12-16", AttendanceStatus.PRESENT, "07:35", "17:00", null, null),
            AttendanceRecord("2024-12-17", AttendanceStatus.PRESENT, "07:39", "17:00", null, null),
            AttendanceRecord("2024-12-18", AttendanceStatus.PRESENT, "07:36", "17:00", null, null),
            AttendanceRecord("2024-12-19", AttendanceStatus.PRESENT, "07:40", "17:00", null, null),
            AttendanceRecord("2024-12-20", AttendanceStatus.PRESENT, "07:37", "17:00", null, null),
            AttendanceRecord("2024-12-23", AttendanceStatus.PRESENT, "07:38", "17:00", null, null),
            AttendanceRecord("2024-12-24", AttendanceStatus.PRESENT, "07:41", "17:00", null, null),
            AttendanceRecord("2024-12-25", AttendanceStatus.PRESENT, "07:39", "17:00", null, null),
            AttendanceRecord("2024-12-26", AttendanceStatus.PRESENT, "07:35", "17:00", null, null)
        )
    }

    private fun calculateStatistics(records: List<AttendanceRecord>): AttendanceStatistics {
        val totalDays = records.size
        val presentDays = records.count { it.status == AttendanceStatus.PRESENT }
        val lateDays = records.count { it.status == AttendanceStatus.LATE }
        val leaveDays = records.count {
            it.status == AttendanceStatus.LEAVE || it.status == AttendanceStatus.SICK_LEAVE
        }
        val absentDays = records.count { it.status == AttendanceStatus.ABSENT }

        val attendanceRate = ((presentDays + lateDays).toDouble() / totalDays * 100)

        return AttendanceStatistics(
            totalDays = totalDays,
            presentDays = presentDays,
            lateDays = lateDays,
            leaveDays = leaveDays,
            absentDays = absentDays,
            attendanceRate = attendanceRate
        )
    }

    private fun generateWarnings(
        statistics: AttendanceStatistics,
        records: List<AttendanceRecord>
    ): List<String> {
        val warnings = mutableListOf<String>()

        if (statistics.attendanceRate < 95) {
            warnings.add("⚠️ 出勤率低于95%，请注意保持良好的出勤记录")
        }

        if (statistics.lateDays >= 3) {
            warnings.add("⚠️ 本月迟到次数较多，请注意调整作息时间")
        }

        if (statistics.absentDays > 0) {
            warnings.add("⚠️ 存在未请假缺勤记录，请及时与班主任沟通")
        }

        // 检查连续迟到
        val recentLate = records.takeLast(5).count { it.status == AttendanceStatus.LATE }
        if (recentLate >= 3) {
            warnings.add("⚠️ 近期连续迟到，建议提早出门")
        }

        return warnings
    }

    fun getStatusColor(status: AttendanceStatus): Color {
        return when (status) {
            AttendanceStatus.PRESENT -> Color(0xFF4CAF50)
            AttendanceStatus.LATE -> Color(0xFFFF9800)
            AttendanceStatus.LEAVE -> Color(0xFF2196F3)
            AttendanceStatus.SICK_LEAVE -> Color(0xFF9C27B0)
            AttendanceStatus.ABSENT -> Color(0xFFF44336)
        }
    }

    fun getStatusIcon(status: AttendanceStatus) = when (status) {
        AttendanceStatus.PRESENT -> Icons.Filled.CheckCircle
        AttendanceStatus.LATE -> Icons.Filled.AccessTime
        AttendanceStatus.LEAVE -> Icons.Filled.EventBusy
        AttendanceStatus.SICK_LEAVE -> Icons.Filled.LocalHospital
        AttendanceStatus.ABSENT -> Icons.Filled.Cancel
    }

    fun getStatusText(status: AttendanceStatus): String {
        return when (status) {
            AttendanceStatus.PRESENT -> "出勤"
            AttendanceStatus.LATE -> "迟到"
            AttendanceStatus.LEAVE -> "请假"
            AttendanceStatus.SICK_LEAVE -> "病假"
            AttendanceStatus.ABSENT -> "缺勤"
        }
    }
}

// ==================== 主界面 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentChildAttendanceScreen(
    childName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val attendance = remember(childName) {
        ParentAttendanceRepository.getChildAttendance(childName)
    }
    var selectedRecord by remember { mutableStateOf<AttendanceRecord?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("${childName}的考勤记录")
                        Text(
                            text = attendance.month,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 统计概览
            AttendanceStatisticsCard(attendance.statistics)

            Spacer(modifier = Modifier.height(16.dp))

            // 警告提示
            if (attendance.warnings.isNotEmpty()) {
                WarningsCard(attendance.warnings)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 考勤记录列表
            Text(
                text = "考勤详细记录",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                attendance.records.sortedByDescending { it.date }.forEach { record ->
                    AttendanceRecordCard(
                        record = record,
                        onClick = { selectedRecord = record }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 记录详情对话框
        selectedRecord?.let { record ->
            AttendanceDetailDialog(
                record = record,
                childName = childName,
                onDismiss = { selectedRecord = null }
            )
        }
    }
}

// ==================== 组件 ====================

@Composable
fun AttendanceStatisticsCard(statistics: AttendanceStatistics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "出勤率",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format("%.1f%%", statistics.attendanceRate),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (statistics.attendanceRate >= 95)
                            Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "累计天数",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${statistics.totalDays}天",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Divider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatisticItem(
                    icon = Icons.Filled.CheckCircle,
                    label = "出勤",
                    value = "${statistics.presentDays}天",
                    color = Color(0xFF4CAF50)
                )
                StatisticItem(
                    icon = Icons.Filled.AccessTime,
                    label = "迟到",
                    value = "${statistics.lateDays}次",
                    color = Color(0xFFFF9800)
                )
                StatisticItem(
                    icon = Icons.Filled.EventBusy,
                    label = "请假",
                    value = "${statistics.leaveDays}天",
                    color = Color(0xFF2196F3)
                )
                StatisticItem(
                    icon = Icons.Filled.Cancel,
                    label = "缺勤",
                    value = "${statistics.absentDays}天",
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WarningsCard(warnings: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "考勤提醒",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            warnings.forEach { warning ->
                Text(
                    text = warning,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceRecordCard(
    record: AttendanceRecord,
    onClick: () -> Unit
) {
    val statusColor = ParentAttendanceRepository.getStatusColor(record.status)
    val statusIcon = ParentAttendanceRepository.getStatusIcon(record.status)
    val statusText = ParentAttendanceRepository.getStatusText(record.status)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = record.date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                        if (record.checkInTime != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "签到: ${record.checkInTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                    record.reason?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AttendanceDetailDialog(
    record: AttendanceRecord,
    childName: String,
    onDismiss: () -> Unit
) {
    val statusColor = ParentAttendanceRepository.getStatusColor(record.status)
    val statusIcon = ParentAttendanceRepository.getStatusIcon(record.status)
    val statusText = ParentAttendanceRepository.getStatusText(record.status)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = record.date,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                record.checkInTime?.let {
                    AttendanceInfoRow(
                        icon = Icons.Filled.Login,
                        label = "签到时间",
                        value = it
                    )
                }

                record.checkOutTime?.let {
                    AttendanceInfoRow(
                        icon = Icons.Filled.Logout,
                        label = "签退时间",
                        value = it
                    )
                }

                record.reason?.let {
                    Divider()
                    AttendanceInfoRow(
                        icon = Icons.Filled.Description,
                        label = "原因说明",
                        value = it
                    )
                }

                record.approvedBy?.let {
                    AttendanceInfoRow(
                        icon = Icons.Filled.VerifiedUser,
                        label = "审批人",
                        value = it
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
fun AttendanceInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}