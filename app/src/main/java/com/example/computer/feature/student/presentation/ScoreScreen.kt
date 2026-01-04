// ScoreScreen.kt
package com.example.computer.feature.student.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

// ==================== 数据模型 ====================

data class CourseScore(
    val id: String,
    val courseName: String,
    val courseCode: String,
    val teacher: String,
    val credit: Double,
    val regularScore: Double?,      // 平时成绩
    val midtermScore: Double?,      // 期中成绩
    val finalScore: Double?,        // 期末成绩
    val totalScore: Double,         // 总评成绩
    val gradePoint: Double,         // 绩点
    val rank: Int?,                 // 班级排名
    val totalStudents: Int?,        // 班级总人数
    val examDate: String?,          // 考试日期
    val category: CourseCategory    // 课程类别
)

enum class CourseCategory(val displayName: String) {
    REQUIRED("必修课"),
    ELECTIVE("选修课"),
    PUBLIC("公共课"),
    PRACTICE("实践课")
}

data class SemesterScoreSummary(
    val semesterId: String,
    val semesterName: String,
    val courses: List<CourseScore>,
    val averageScore: Double,
    val averageGradePoint: Double,
    val totalCredits: Double,
    val passedCredits: Double,
    val ranking: Int?,
    val totalStudentsInClass: Int?
)

// ==================== 成绩数据仓库 ====================

object ScoreRepository {

    private val sampleCourses = listOf(
        CourseScore(
            id = "1",
            courseName = "高等数学A(1)",
            courseCode = "MATH101",
            teacher = "王教授",
            credit = 5.0,
            regularScore = 88.0,
            midtermScore = 85.0,
            finalScore = 92.0,
            totalScore = 89.5,
            gradePoint = 3.9,
            rank = 8,
            totalStudents = 120,
            examDate = "2024-12-20",
            category = CourseCategory.REQUIRED
        ),
        CourseScore(
            id = "2",
            courseName = "大学物理(1)",
            courseCode = "PHYS101",
            teacher = "李老师",
            credit = 4.0,
            regularScore = 85.0,
            midtermScore = 82.0,
            finalScore = 88.0,
            totalScore = 85.6,
            gradePoint = 3.6,
            rank = 15,
            totalStudents = 120,
            examDate = "2024-12-18",
            category = CourseCategory.REQUIRED
        ),
        CourseScore(
            id = "3",
            courseName = "数据结构",
            courseCode = "CS201",
            teacher = "张教授",
            credit = 4.5,
            regularScore = 92.0,
            midtermScore = 90.0,
            finalScore = 95.0,
            totalScore = 93.2,
            gradePoint = 4.3,
            rank = 3,
            totalStudents = 80,
            examDate = "2024-12-22",
            category = CourseCategory.REQUIRED
        ),
        CourseScore(
            id = "4",
            courseName = "大学英语(3)",
            courseCode = "ENG301",
            teacher = "陈老师",
            credit = 3.0,
            regularScore = 86.0,
            midtermScore = 84.0,
            finalScore = 87.0,
            totalScore = 86.0,
            gradePoint = 3.6,
            rank = 25,
            totalStudents = 100,
            examDate = "2024-12-15",
            category = CourseCategory.PUBLIC
        ),
        CourseScore(
            id = "5",
            courseName = "计算机网络",
            courseCode = "CS301",
            teacher = "刘教授",
            credit = 4.0,
            regularScore = 90.0,
            midtermScore = 88.0,
            finalScore = 91.0,
            totalScore = 89.8,
            gradePoint = 3.9,
            rank = 6,
            totalStudents = 75,
            examDate = "2024-12-25",
            category = CourseCategory.REQUIRED
        ),
        CourseScore(
            id = "6",
            courseName = "操作系统",
            courseCode = "CS302",
            teacher = "赵老师",
            credit = 4.0,
            regularScore = 87.0,
            midtermScore = 85.0,
            finalScore = 89.0,
            totalScore = 87.4,
            gradePoint = 3.7,
            rank = 12,
            totalStudents = 75,
            examDate = "2024-12-28",
            category = CourseCategory.REQUIRED
        ),
        CourseScore(
            id = "7",
            courseName = "Web开发技术",
            courseCode = "CS401",
            teacher = "孙老师",
            credit = 3.0,
            regularScore = 94.0,
            midtermScore = null,
            finalScore = 96.0,
            totalScore = 95.2,
            gradePoint = 4.5,
            rank = 2,
            totalStudents = 60,
            examDate = "2024-12-16",
            category = CourseCategory.ELECTIVE
        ),
        CourseScore(
            id = "8",
            courseName = "人工智能导论",
            courseCode = "CS501",
            teacher = "周教授",
            credit = 3.5,
            regularScore = 91.0,
            midtermScore = 89.0,
            finalScore = 93.0,
            totalScore = 91.6,
            gradePoint = 4.1,
            rank = 5,
            totalStudents = 70,
            examDate = "2024-12-30",
            category = CourseCategory.ELECTIVE
        ),
        CourseScore(
            id = "9",
            courseName = "软件工程实践",
            courseCode = "CS601",
            teacher = "吴老师",
            credit = 2.0,
            regularScore = 95.0,
            midtermScore = null,
            finalScore = null,
            totalScore = 95.0,
            gradePoint = 4.5,
            rank = 1,
            totalStudents = 50,
            examDate = null,
            category = CourseCategory.PRACTICE
        ),
        CourseScore(
            id = "10",
            courseName = "体育(3)",
            courseCode = "PE301",
            teacher = "郑老师",
            credit = 1.0,
            regularScore = 88.0,
            midtermScore = null,
            finalScore = 90.0,
            totalScore = 89.0,
            gradePoint = 3.9,
            rank = null,
            totalStudents = null,
            examDate = null,
            category = CourseCategory.PUBLIC
        )
    )

    fun getCurrentSemesterScores(): SemesterScoreSummary {
        val averageScore = sampleCourses.map { it.totalScore }.average()
        val averageGradePoint = sampleCourses.sumOf { it.gradePoint * it.credit } /
                sampleCourses.sumOf { it.credit }
        val totalCredits = sampleCourses.sumOf { it.credit }
        val passedCredits = sampleCourses.filter { it.totalScore >= 60 }.sumOf { it.credit }

        return SemesterScoreSummary(
            semesterId = "2024-2025-1",
            semesterName = "2024-2025学年第一学期",
            courses = sampleCourses,
            averageScore = averageScore,
            averageGradePoint = averageGradePoint,
            totalCredits = totalCredits,
            passedCredits = passedCredits,
            ranking = 15,
            totalStudentsInClass = 120
        )
    }

    // 获取成绩等级
    fun getScoreGrade(score: Double): String {
        return when {
            score >= 95 -> "A+"
            score >= 90 -> "A"
            score >= 85 -> "A-"
            score >= 82 -> "B+"
            score >= 78 -> "B"
            score >= 75 -> "B-"
            score >= 72 -> "C+"
            score >= 68 -> "C"
            score >= 64 -> "C-"
            score >= 60 -> "D"
            else -> "F"
        }
    }

    // 获取成绩颜色
    fun getScoreColor(score: Double): Color {
        return when {
            score >= 90 -> Color(0xFF4CAF50) // 绿色
            score >= 80 -> Color(0xFF2196F3) // 蓝色
            score >= 70 -> Color(0xFFFF9800) // 橙色
            score >= 60 -> Color(0xFFFFC107) // 黄色
            else -> Color(0xFFF44336) // 红色
        }
    }
}

// ==================== 成绩查询主界面 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semesterScore = remember { ScoreRepository.getCurrentSemesterScores() }
    var selectedCourse by remember { mutableStateOf<CourseScore?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("全部", "必修课", "选修课", "公共课", "实践课")

    val filteredCourses = when (selectedTab) {
        0 -> semesterScore.courses
        1 -> semesterScore.courses.filter { it.category == CourseCategory.REQUIRED }
        2 -> semesterScore.courses.filter { it.category == CourseCategory.ELECTIVE }
        3 -> semesterScore.courses.filter { it.category == CourseCategory.PUBLIC }
        4 -> semesterScore.courses.filter { it.category == CourseCategory.PRACTICE }
        else -> semesterScore.courses
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("成绩查询")
                        Text(
                            text = semesterScore.semesterName,
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 成绩概览卡片
            ScoreSummaryCard(semesterScore)

            Spacer(modifier = Modifier.height(8.dp))

            // 学期统计
            SemesterStatisticsCard(semesterScore)

            Spacer(modifier = Modifier.height(16.dp))

            // 分类选项卡
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 成绩列表
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (filteredCourses.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "暂无该类别课程成绩",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    filteredCourses.forEach { course ->
                        CourseScoreItem(
                            course = course,
                            onClick = { selectedCourse = course }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 课程详情对话框
        selectedCourse?.let { course ->
            CourseScoreDetailDialog(
                course = course,
                onDismiss = { selectedCourse = null }
            )
        }
    }
}

// ==================== 成绩概览卡片 ====================

@Composable
fun ScoreSummaryCard(semesterScore: SemesterScoreSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                        text = "平均分",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format("%.2f", semesterScore.averageScore),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "平均绩点",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format("%.2f", semesterScore.averageGradePoint),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                semesterScore.ranking?.let { rank ->
                    SummaryItem(
                        label = "班级排名",
                        value = "$rank/${semesterScore.totalStudentsInClass ?: 0}"
                    )
                }

                SummaryItem(
                    label = "总学分",
                    value = String.format("%.1f", semesterScore.totalCredits)
                )

                SummaryItem(
                    label = "获得学分",
                    value = String.format("%.1f", semesterScore.passedCredits)
                )
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

// ==================== 学期统计卡片 ====================

@Composable
fun SemesterStatisticsCard(semesterScore: SemesterScoreSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📊 成绩分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val gradeDistribution = semesterScore.courses.groupBy {
                ScoreRepository.getScoreGrade(it.totalScore)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("A+", "A", "A-", "B+", "B").forEach { grade ->
                    val count = gradeDistribution[grade]?.size ?: 0
                    GradeDistributionItem(grade = grade, count = count)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val excellentCount = semesterScore.courses.count { it.totalScore >= 90 }
                val passedCount = semesterScore.courses.count { it.totalScore >= 60 }
                val totalCount = semesterScore.courses.size

                StatisticsItem(
                    icon = Icons.Filled.Star,
                    label = "优秀",
                    value = "$excellentCount/$totalCount",
                    color = Color(0xFF4CAF50)
                )

                StatisticsItem(
                    icon = Icons.Filled.Check,
                    label = "及格",
                    value = "$passedCount/$totalCount",
                    color = Color(0xFF2196F3)
                )

                StatisticsItem(
                    icon = Icons.Filled.List,
                    label = "课程数",
                    value = "$totalCount",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GradeDistributionItem(grade: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = grade,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatisticsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== 课程成绩项 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScoreItem(
    course: CourseScore,
    onClick: () -> Unit
) {
    val scoreColor = ScoreRepository.getScoreColor(course.totalScore)
    val scoreGrade = ScoreRepository.getScoreGrade(course.totalScore)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = course.courseCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = course.teacher,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = course.category.displayName,
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "${course.credit}学分",
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(scoreColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.1f", course.totalScore),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Text(
                            text = scoreGrade,
                            style = MaterialTheme.typography.bodySmall,
                            color = scoreColor,
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "绩点 ${String.format("%.1f", course.gradePoint)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ==================== 课程成绩详情对话框 ====================

@Composable
fun CourseScoreDetailDialog(
    course: CourseScore,
    onDismiss: () -> Unit
) {
    val scoreColor = ScoreRepository.getScoreColor(course.totalScore)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(scoreColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", course.totalScore),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                    Text(
                        text = ScoreRepository.getScoreGrade(course.totalScore),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scoreColor
                    )
                }
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = course.courseCode,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 基本信息
                ScoreDetailSection(title = "📚 课程信息") {
                    ScoreDetailItem(label = "授课教师", value = course.teacher)
                    ScoreDetailItem(label = "课程类别", value = course.category.displayName)
                    ScoreDetailItem(label = "学分", value = "${course.credit}")
                    course.examDate?.let {
                        ScoreDetailItem(label = "考试日期", value = it)
                    }
                }

                Divider()

                // 成绩详情
                ScoreDetailSection(title = "📈 成绩详情") {
                    course.regularScore?.let {
                        ScoreDetailItem(
                            label = "平时成绩",
                            value = String.format("%.1f", it),
                            valueColor = ScoreRepository.getScoreColor(it)
                        )
                    }
                    course.midtermScore?.let {
                        ScoreDetailItem(
                            label = "期中成绩",
                            value = String.format("%.1f", it),
                            valueColor = ScoreRepository.getScoreColor(it)
                        )
                    }
                    course.finalScore?.let {
                        ScoreDetailItem(
                            label = "期末成绩",
                            value = String.format("%.1f", it),
                            valueColor = ScoreRepository.getScoreColor(it)
                        )
                    }
                    ScoreDetailItem(
                        label = "总评成绩",
                        value = String.format("%.1f", course.totalScore),
                        valueColor = scoreColor,
                        isBold = true
                    )
                    ScoreDetailItem(
                        label = "绩点",
                        value = String.format("%.1f", course.gradePoint),
                        isBold = true
                    )
                }

                course.rank?.let { rank ->
                    Divider()
                    ScoreDetailSection(title = "🏆 排名信息") {
                        ScoreDetailItem(
                            label = "班级排名",
                            value = "$rank / ${course.totalStudents ?: 0}"
                        )
                        val percentage = if (course.totalStudents != null && course.totalStudents > 0) {
                            (rank.toDouble() / course.totalStudents * 100)
                        } else 0.0
                        ScoreDetailItem(
                            label = "百分位",
                            value = String.format("前 %.1f%%", percentage)
                        )
                    }
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
fun ScoreDetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun ScoreDetailItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor,
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.End
        )
    }
}