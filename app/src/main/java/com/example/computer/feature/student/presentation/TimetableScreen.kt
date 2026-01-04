// TimetableScreen.kt
package com.example.computer.feature.student.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==================== 数据模型 ====================

data class Course(
    val id: String,
    val name: String,
    val teacher: String,
    val location: String,
    val dayOfWeek: Int, // 1=周一, 2=周二, ..., 7=周日
    val startPeriod: Int, // 开始节次 (1-12)
    val endPeriod: Int, // 结束节次 (1-12)
    val color: Color = Color(0xFF6200EE)
)

data class Semester(
    val id: String,
    val name: String,
    val courses: List<Course>
)

// ==================== 课程表数据仓库 ====================

object TimetableRepository {

    private val sampleCourses = listOf(
        // 周一
        Course(
            id = "1",
            name = "高等数学",
            teacher = "王教授",
            location = "教学楼A101",
            dayOfWeek = 1,
            startPeriod = 1,
            endPeriod = 2,
            color = Color(0xFF6200EE)
        ),
        Course(
            id = "2",
            name = "大学物理",
            teacher = "李老师",
            location = "教学楼B202",
            dayOfWeek = 1,
            startPeriod = 3,
            endPeriod = 4,
            color = Color(0xFF03DAC5)
        ),

        // 周二
        Course(
            id = "3",
            name = "数据结构",
            teacher = "张教授",
            location = "实验楼C301",
            dayOfWeek = 2,
            startPeriod = 1,
            endPeriod = 2,
            color = Color(0xFFFF6B6B)
        ),
        Course(
            id = "4",
            name = "英语",
            teacher = "陈老师",
            location = "教学楼A205",
            dayOfWeek = 2,
            startPeriod = 5,
            endPeriod = 6,
            color = Color(0xFF4ECDC4)
        ),

        // 周三
        Course(
            id = "5",
            name = "计算机网络",
            teacher = "刘教授",
            location = "实验楼C402",
            dayOfWeek = 3,
            startPeriod = 1,
            endPeriod = 3,
            color = Color(0xFFFFBE0B)
        ),
        Course(
            id = "6",
            name = "操作系统",
            teacher = "赵老师",
            location = "教学楼B103",
            dayOfWeek = 3,
            startPeriod = 7,
            endPeriod = 8,
            color = Color(0xFF9B59B6)
        ),

        // 周四
        Course(
            id = "7",
            name = "软件工程",
            teacher = "孙教授",
            location = "教学楼A302",
            dayOfWeek = 4,
            startPeriod = 3,
            endPeriod = 4,
            color = Color(0xFF3498DB)
        ),
        Course(
            id = "8",
            name = "数据库原理",
            teacher = "周老师",
            location = "实验楼C201",
            dayOfWeek = 4,
            startPeriod = 5,
            endPeriod = 6,
            color = Color(0xFFE74C3C)
        ),

        // 周五
        Course(
            id = "9",
            name = "算法设计",
            teacher = "吴教授",
            location = "教学楼B305",
            dayOfWeek = 5,
            startPeriod = 1,
            endPeriod = 2,
            color = Color(0xFF2ECC71)
        ),
        Course(
            id = "10",
            name = "体育",
            teacher = "郑老师",
            location = "体育馆",
            dayOfWeek = 5,
            startPeriod = 9,
            endPeriod = 10,
            color = Color(0xFFE67E22)
        )
    )

    val currentSemester = Semester(
        id = "2024-2025-1",
        name = "2024-2025学年第一学期",
        courses = sampleCourses
    )

    // 获取时间段描述
    fun getPeriodTime(period: Int): String {
        return when (period) {
            1 -> "08:00-08:45"
            2 -> "08:55-09:40"
            3 -> "10:00-10:45"
            4 -> "10:55-11:40"
            5 -> "14:00-14:45"
            6 -> "14:55-15:40"
            7 -> "16:00-16:45"
            8 -> "16:55-17:40"
            9 -> "19:00-19:45"
            10 -> "19:55-20:40"
            11 -> "20:50-21:35"
            12 -> "21:45-22:30"
            else -> ""
        }
    }
}

// ==================== 课程表主界面 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semester = remember { TimetableRepository.currentSemester }
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val maxPeriods = 12

    var selectedCourse by remember { mutableStateOf<Course?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("课程表")
                        Text(
                            text = semester.name,
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 统计信息卡片
                TimetableStatisticsCard(
                    totalCourses = semester.courses.size,
                    totalHours = semester.courses.sumOf { it.endPeriod - it.startPeriod + 1 } * 0.75
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 课程表网格
                TimetableGrid(
                    courses = semester.courses,
                    weekDays = weekDays,
                    maxPeriods = maxPeriods,
                    onCourseClick = { course ->
                        selectedCourse = course
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 课程详情对话框
            selectedCourse?.let { course ->
                CourseDetailDialog(
                    course = course,
                    onDismiss = { selectedCourse = null }
                )
            }
        }
    }
}

// ==================== 统计信息卡片 ====================

@Composable
fun TimetableStatisticsCard(
    totalCourses: Int,
    totalHours: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatisticItem(
                label = "总课程数",
                value = "$totalCourses 门"
            )

            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            StatisticItem(
                label = "周学时数",
                value = String.format("%.1f 小时", totalHours)
            )
        }
    }
}

@Composable
fun StatisticItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== 课程表网格 ====================

@Composable
fun TimetableGrid(
    courses: List<Course>,
    weekDays: List<String>,
    maxPeriods: Int,
    onCourseClick: (Course) -> Unit
) {
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // 表头：星期
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
        ) {
            // 左上角空白（节次列）
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "节次",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // 星期列
            weekDays.forEachIndexed { index, day ->
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp)
                        .background(
                            if (index < 5) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 课程表主体
        for (period in 1..maxPeriods) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
            ) {
                // 节次列
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$period",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = TimetableRepository.getPeriodTime(period),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 每天的课程格子
                for (day in 1..7) {
                    val coursesInSlot = courses.filter { course ->
                        course.dayOfWeek == day &&
                                period >= course.startPeriod &&
                                period <= course.endPeriod
                    }

                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(80.dp)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        coursesInSlot.firstOrNull()?.let { course ->
                            // 只在课程开始节次显示课程卡片
                            if (period == course.startPeriod) {
                                CourseCell(
                                    course = course,
                                    onClick = { onCourseClick(course) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 课程单元格 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCell(
    course: Course,
    onClick: () -> Unit
) {
    val height = ((course.endPeriod - course.startPeriod + 1) * 80).dp

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = course.color.copy(alpha = 0.15f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.5.dp,
            brush = androidx.compose.ui.graphics.SolidColor(course.color)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp,
                lineHeight = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = course.location,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (height > 100.dp) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course.teacher,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ==================== 课程详情对话框 ====================

@Composable
fun CourseDetailDialog(
    course: Course,
    onDismiss: () -> Unit
) {
    val weekDay = listOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")[course.dayOfWeek]

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(course.color.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = course.name.take(2),
                    style = MaterialTheme.typography.titleLarge,
                    color = course.color,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        title = {
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CourseDetailItem(
                    label = "授课教师",
                    value = course.teacher
                )

                CourseDetailItem(
                    label = "上课地点",
                    value = course.location
                )

                CourseDetailItem(
                    label = "上课时间",
                    value = "$weekDay 第${course.startPeriod}-${course.endPeriod}节"
                )

                CourseDetailItem(
                    label = "时间段",
                    value = "${TimetableRepository.getPeriodTime(course.startPeriod)} - " +
                            TimetableRepository.getPeriodTime(course.endPeriod).split("-")[1]
                )
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
fun CourseDetailItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}