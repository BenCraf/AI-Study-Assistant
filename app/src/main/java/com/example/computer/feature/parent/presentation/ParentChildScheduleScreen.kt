// ParentChildScheduleScreen.kt
package com.example.computer.feature.parent.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==================== 数据模型 ====================

data class CourseSchedule(
    val courseName: String,
    val teacher: String,
    val classroom: String,
    val dayOfWeek: Int,  // 1-7 代表周一到周日
    val period: Int,      // 第几节课 (1-8)
    val courseType: String, // 课程类型：主课、副课、活动课等
    val color: Color
)

data class WeekSchedule(
    val childName: String,
    val grade: String,
    val weekNumber: Int,
    val courses: List<CourseSchedule>,
    val specialEvents: List<SpecialEvent>
)

data class SpecialEvent(
    val date: String,
    val dayOfWeek: Int,
    val title: String,
    val description: String,
    val type: EventType
)

enum class EventType {
    EXAM,        // 考试
    ACTIVITY,    // 活动
    HOLIDAY,     // 假期
    PARENT_MEETING // 家长会
}

// ==================== 数据仓库 ====================

object ParentScheduleRepository {

    private val courseColors = listOf(
        Color(0xFF64B5F6), // 蓝色
        Color(0xFF81C784), // 绿色
        Color(0xFFFFB74D), // 橙色
        Color(0xFFBA68C8), // 紫色
        Color(0xFFFF8A65), // 深橙
        Color(0xFF4DB6AC), // 青色
        Color(0xFFFFD54F), // 黄色
        Color(0xFF9575CD)  // 深紫
    )

    fun getChildSchedule(childName: String): WeekSchedule {
        val courses = when (childName) {
            "小明" -> getScheduleForXiaoMing()
            "小红" -> getScheduleForXiaoHong()
            "小华" -> getScheduleForXiaoHua()
            else -> getScheduleForXiaoMing()
        }

        val specialEvents = listOf(
            SpecialEvent(
                date = "2024-12-27",
                dayOfWeek = 5,
                title = "英语口语测试",
                description = "全班进行英语口语能力测试",
                type = EventType.EXAM
            ),
            SpecialEvent(
                date = "2024-12-28",
                dayOfWeek = 6,
                title = "冬季运动会",
                description = "学校年度冬季运动会",
                type = EventType.ACTIVITY
            )
        )

        return WeekSchedule(
            childName = childName,
            grade = when (childName) {
                "小明" -> "五年级"
                "小红" -> "四年级"
                "小华" -> "六年级"
                else -> "五年级"
            },
            weekNumber = 18,
            courses = courses,
            specialEvents = specialEvents
        )
    }

    private fun getScheduleForXiaoMing(): List<CourseSchedule> {
        return listOf(
            // 周一
            CourseSchedule("语文", "王老师", "301", 1, 1, "主课", courseColors[0]),
            CourseSchedule("数学", "李老师", "301", 1, 2, "主课", courseColors[1]),
            CourseSchedule("英语", "张老师", "301", 1, 3, "主课", courseColors[2]),
            CourseSchedule("体育", "刘老师", "操场", 1, 4, "副课", courseColors[3]),
            CourseSchedule("科学", "陈老师", "实验室", 1, 5, "主课", courseColors[4]),
            CourseSchedule("音乐", "赵老师", "音乐室", 1, 6, "副课", courseColors[5]),

            // 周二
            CourseSchedule("数学", "李老师", "301", 2, 1, "主课", courseColors[1]),
            CourseSchedule("语文", "王老师", "301", 2, 2, "主课", courseColors[0]),
            CourseSchedule("英语", "张老师", "301", 2, 3, "主课", courseColors[2]),
            CourseSchedule("美术", "周老师", "美术室", 2, 4, "副课", courseColors[6]),
            CourseSchedule("信息技术", "吴老师", "机房", 2, 5, "副课", courseColors[7]),
            CourseSchedule("阅读", "王老师", "图书馆", 2, 6, "活动", courseColors[0]),

            // 周三
            CourseSchedule("英语", "张老师", "301", 3, 1, "主课", courseColors[2]),
            CourseSchedule("数学", "李老师", "301", 3, 2, "主课", courseColors[1]),
            CourseSchedule("语文", "王老师", "301", 3, 3, "主课", courseColors[0]),
            CourseSchedule("科学", "陈老师", "实验室", 3, 4, "主课", courseColors[4]),
            CourseSchedule("体育", "刘老师", "操场", 3, 5, "副课", courseColors[3]),
            CourseSchedule("班会", "王老师", "301", 3, 6, "活动", courseColors[0]),

            // 周四
            CourseSchedule("语文", "王老师", "301", 4, 1, "主课", courseColors[0]),
            CourseSchedule("数学", "李老师", "301", 4, 2, "主课", courseColors[1]),
            CourseSchedule("英语", "张老师", "301", 4, 3, "主课", courseColors[2]),
            CourseSchedule("音乐", "赵老师", "音乐室", 4, 4, "副课", courseColors[5]),
            CourseSchedule("美术", "周老师", "美术室", 4, 5, "副课", courseColors[6]),
            CourseSchedule("科学", "陈老师", "实验室", 4, 6, "主课", courseColors[4]),

            // 周五
            CourseSchedule("数学", "李老师", "301", 5, 1, "主课", courseColors[1]),
            CourseSchedule("语文", "王老师", "301", 5, 2, "主课", courseColors[0]),
            CourseSchedule("英语", "张老师", "301", 5, 3, "主课", courseColors[2]),
            CourseSchedule("体育", "刘老师", "操场", 5, 4, "副课", courseColors[3]),
            CourseSchedule("信息技术", "吴老师", "机房", 5, 5, "副课", courseColors[7]),
            CourseSchedule("社团活动", "各老师", "各活动室", 5, 6, "活动", courseColors[6])
        )
    }

    private fun getScheduleForXiaoHong(): List<CourseSchedule> {
        return listOf(
            // 周一
            CourseSchedule("语文", "刘老师", "201", 1, 1, "主课", courseColors[0]),
            CourseSchedule("数学", "周老师", "201", 1, 2, "主课", courseColors[1]),
            CourseSchedule("英语", "吴老师", "201", 1, 3, "主课", courseColors[2]),
            CourseSchedule("体育", "孙老师", "操场", 1, 4, "副课", courseColors[3]),
            CourseSchedule("美术", "郑老师", "美术室", 1, 5, "副课", courseColors[6]),

            // 周二
            CourseSchedule("数学", "周老师", "201", 2, 1, "主课", courseColors[1]),
            CourseSchedule("语文", "刘老师", "201", 2, 2, "主课", courseColors[0]),
            CourseSchedule("音乐", "钱老师", "音乐室", 2, 3, "副课", courseColors[5]),
            CourseSchedule("科学", "冯老师", "实验室", 2, 4, "主课", courseColors[4]),
            CourseSchedule("英语", "吴老师", "201", 2, 5, "主课", courseColors[2]),

            // 周三
            CourseSchedule("语文", "刘老师", "201", 3, 1, "主课", courseColors[0]),
            CourseSchedule("数学", "周老师", "201", 3, 2, "主课", courseColors[1]),
            CourseSchedule("体育", "孙老师", "操场", 3, 3, "副课", courseColors[3]),
            CourseSchedule("英语", "吴老师", "201", 3, 4, "主课", courseColors[2]),
            CourseSchedule("信息技术", "卫老师", "机房", 3, 5, "副课", courseColors[7]),

            // 周四
            CourseSchedule("数学", "周老师", "201", 4, 1, "主课", courseColors[1]),
            CourseSchedule("语文", "刘老师", "201", 4, 2, "主课", courseColors[0]),
            CourseSchedule("科学", "冯老师", "实验室", 4, 3, "主课", courseColors[4]),
            CourseSchedule("美术", "郑老师", "美术室", 4, 4, "副课", courseColors[6]),
            CourseSchedule("阅读", "刘老师", "图书馆", 4, 5, "活动", courseColors[0]),

            // 周五
            CourseSchedule("语文", "刘老师", "201", 5, 1, "主课", courseColors[0]),
            CourseSchedule("数学", "周老师", "201", 5, 2, "主课", courseColors[1]),
            CourseSchedule("英语", "吴老师", "201", 5, 3, "主课", courseColors[2]),
            CourseSchedule("音乐", "钱老师", "音乐室", 5, 4, "副课", courseColors[5]),
            CourseSchedule("班会", "刘老师", "201", 5, 5, "活动", courseColors[0])
        )
    }

    private fun getScheduleForXiaoHua(): List<CourseSchedule> {
        return listOf(
            // 周一
            CourseSchedule("语文", "赵老师", "401", 1, 1, "主课", courseColors[0]),
            CourseSchedule("数学", "孙老师", "401", 1, 2, "主课", courseColors[1]),
            CourseSchedule("英语", "郑老师", "401", 1, 3, "主课", courseColors[2]),
            CourseSchedule("物理", "钱老师", "实验室", 1, 4, "主课", courseColors[4]),
            CourseSchedule("体育", "冯老师", "操场", 1, 5, "副课", courseColors[3]),
            CourseSchedule("音乐", "卫老师", "音乐室", 1, 6, "副课", courseColors[5]),

            // 周二
            CourseSchedule("数学", "孙老师", "401", 2, 1, "主课", courseColors[1]),
            CourseSchedule("语文", "赵老师", "401", 2, 2, "主课", courseColors[0]),
            CourseSchedule("英语", "郑老师", "401", 2, 3, "主课", courseColors[2]),
            CourseSchedule("化学", "蒋老师", "实验室", 2, 4, "主课", courseColors[7]),
            CourseSchedule("信息技术", "沈老师", "机房", 2, 5, "副课", courseColors[7]),
            CourseSchedule("生物", "韩老师", "实验室", 2, 6, "主课", courseColors[4]),

            // 周三
            CourseSchedule("英语", "郑老师", "401", 3, 1, "主课", courseColors[2]),
            CourseSchedule("数学", "孙老师", "401", 3, 2, "主课", courseColors[1]),
            CourseSchedule("语文", "赵老师", "401", 3, 3, "主课", courseColors[0]),
            CourseSchedule("物理", "钱老师", "实验室", 3, 4, "主课", courseColors[4]),
            CourseSchedule("美术", "杨老师", "美术室", 3, 5, "副课", courseColors[6]),
            CourseSchedule("班会", "赵老师", "401", 3, 6, "活动", courseColors[0]),

            // 周四
            CourseSchedule("语文", "赵老师", "401", 4, 1, "主课", courseColors[0]),
            CourseSchedule("数学", "孙老师", "401", 4, 2, "主课", courseColors[1]),
            CourseSchedule("英语", "郑老师", "401", 4, 3, "主课", courseColors[2]),
            CourseSchedule("化学", "蒋老师", "实验室", 4, 4, "主课", courseColors[7]),
            CourseSchedule("体育", "冯老师", "操场", 4, 5, "副课", courseColors[3]),
            CourseSchedule("历史", "朱老师", "401", 4, 6, "主课", courseColors[5]),

            // 周五
            CourseSchedule("数学", "孙老师", "401", 5, 1, "主课", courseColors[1]),
            CourseSchedule("语文", "赵老师", "401", 5, 2, "主课", courseColors[0]),
            CourseSchedule("英语", "郑老师", "401", 5, 3, "主课", courseColors[2]),
            CourseSchedule("生物", "韩老师", "实验室", 5, 4, "主课", courseColors[4]),
            CourseSchedule("地理", "秦老师", "401", 5, 5, "主课", courseColors[6]),
            CourseSchedule("社团活动", "各老师", "各活动室", 5, 6, "活动", courseColors[6])
        )
    }

    fun getEventIcon(type: EventType) = when (type) {
        EventType.EXAM -> Icons.Filled.School
        EventType.ACTIVITY -> Icons.Filled.Event
        EventType.HOLIDAY -> Icons.Filled.WbSunny
        EventType.PARENT_MEETING -> Icons.Filled.People
    }

    fun getEventColor(type: EventType) = when (type) {
        EventType.EXAM -> Color(0xFFF44336)
        EventType.ACTIVITY -> Color(0xFF4CAF50)
        EventType.HOLIDAY -> Color(0xFFFF9800)
        EventType.PARENT_MEETING -> Color(0xFF2196F3)
    }
}

// ==================== 主界面 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentChildScheduleScreen(
    childName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weekSchedule = remember(childName) {
        ParentScheduleRepository.getChildSchedule(childName)
    }
    var selectedCourse by remember { mutableStateOf<CourseSchedule?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("${childName}的课程表")
                        Text(
                            text = "${weekSchedule.grade} · 第${weekSchedule.weekNumber}周",
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
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
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

            // 特殊事件提醒
            if (weekSchedule.specialEvents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                SpecialEventsSection(weekSchedule.specialEvents)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 课程表
            Text(
                text = "本周课程安排",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 课程表格
            ScheduleTable(
                weekSchedule = weekSchedule,
                onCourseClick = { selectedCourse = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 课程统计
            CourseStatisticsSection(weekSchedule.courses)

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 课程详情对话框
        selectedCourse?.let { course ->
            CourseInfoDialog(
                course = course,
                onDismiss = { selectedCourse = null }
            )
        }
    }
}

// ==================== 组件 ====================

@Composable
fun SpecialEventsSection(events: List<SpecialEvent>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "📅 本周特别提醒",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        events.forEach { event ->
            SpecialEventCard(event)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SpecialEventCard(event: SpecialEvent) {
    val eventColor = ParentScheduleRepository.getEventColor(event.type)
    val eventIcon = ParentScheduleRepository.getEventIcon(event.type)
    val dayNames = listOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = eventColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(eventColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = eventIcon,
                    contentDescription = null,
                    tint = eventColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = eventColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${event.date} ${dayNames.getOrNull(event.dayOfWeek) ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ScheduleTable(
    weekSchedule: WeekSchedule,
    onCourseClick: (CourseSchedule) -> Unit
) {
    val dayNames = listOf("周一", "周二", "周三", "周四", "周五")
    val periods = (1..8).toList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // 表头
        Row(modifier = Modifier.fillMaxWidth()) {
            // 节次列
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(40.dp)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "节次",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // 星期列
            dayNames.forEach { day ->
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(40.dp)
                        .border(0.5.dp, MaterialTheme.colorScheme.outline)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 课程行
        periods.forEach { period ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // 节次
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(70.dp)
                        .border(0.5.dp, MaterialTheme.colorScheme.outline)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "第${period}节",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }

                // 各天的课程
                (1..5).forEach { day ->
                    val course = weekSchedule.courses.find {
                        it.dayOfWeek == day && it.period == period
                    }

                    CourseCell(
                        course = course,
                        onClick = { course?.let { onCourseClick(it) } }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCell(
    course: CourseSchedule?,
    onClick: () -> Unit
) {
    if (course != null) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .width(90.dp)
                .height(70.dp)
                .border(0.5.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(
                containerColor = course.color.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course.teacher,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = course.classroom,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(70.dp)
                .border(0.5.dp, MaterialTheme.colorScheme.outline)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Composable
fun CourseStatisticsSection(courses: List<CourseSchedule>) {
    val courseStats = courses.groupBy { it.courseName }.mapValues { it.value.size }
    val sortedStats = courseStats.entries.sortedByDescending { it.value }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📊 每周课时统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            sortedStats.forEach { (courseName, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = courseName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 进度条
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(count / 8f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${count}节",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "总课时",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${courses.size}节/周",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CourseInfoDialog(
    course: CourseSchedule,
    onDismiss: () -> Unit
) {
    val dayNames = listOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(course.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = course.color,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = course.courseName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CourseInfoRow(
                    icon = Icons.Filled.Person,
                    label = "任课老师",
                    value = course.teacher
                )
                CourseInfoRow(
                    icon = Icons.Filled.LocationOn,
                    label = "上课地点",
                    value = course.classroom
                )
                CourseInfoRow(
                    icon = Icons.Filled.DateRange,
                    label = "上课时间",
                    value = "${dayNames.getOrNull(course.dayOfWeek) ?: ""} 第${course.period}节"
                )
                CourseInfoRow(
                    icon = Icons.Filled.Category,
                    label = "课程类型",
                    value = course.courseType
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
fun CourseInfoRow(
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