// ParentChildScoreScreen.kt
package com.example.computer.feature.parent.presentation

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
import com.example.computer.data.model.ChildInfo

// ==================== 数据模型 ====================

data class ChildCourseScore(
    val id: String,
    val courseName: String,
    val teacher: String,
    val recentScores: List<ScoreRecord>,
    val averageScore: Double,
    val trend: ScoreTrend,
    val teacherComment: String?
)

data class ScoreRecord(
    val date: String,
    val type: String, // 作业、测验、考试等
    val score: Double,
    val fullScore: Double,
    val comment: String?
)

enum class ScoreTrend {
    UP,      // 上升
    DOWN,    // 下降
    STABLE   // 稳定
}

data class ChildScoreSummary(
    val childName: String,
    val grade: String,
    val semester: String,
    val courses: List<ChildCourseScore>,
    val overallAverage: Double,
    val classRanking: Int?,
    val totalStudents: Int?,
    val strengths: List<String>,
    val weaknesses: List<String>
)

// ==================== 数据仓库 ====================

object ParentScoreRepository {

    fun getChildScores(childName: String): ChildScoreSummary {
        val courses = when (childName) {
            "小明" -> getSampleCoursesForXiaoMing()
            "小红" -> getSampleCoursesForXiaoHong()
            "小华" -> getSampleCoursesForXiaoHua()
            else -> getSampleCoursesForXiaoMing()
        }

        val overallAverage = courses.map { it.averageScore }.average()

        return ChildScoreSummary(
            childName = childName,
            grade = when (childName) {
                "小明" -> "五年级"
                "小红" -> "四年级"
                "小华" -> "六年级"
                else -> "五年级"
            },
            semester = "2024-2025学年第一学期",
            courses = courses,
            overallAverage = overallAverage,
            classRanking = when (childName) {
                "小明" -> 8
                "小红" -> 15
                "小华" -> 3
                else -> 10
            },
            totalStudents = 45,
            strengths = getStrengths(courses),
            weaknesses = getWeaknesses(courses)
        )
    }

    private fun getSampleCoursesForXiaoMing(): List<ChildCourseScore> {
        return listOf(
            ChildCourseScore(
                id = "1",
                courseName = "语文",
                teacher = "王老师",
                recentScores = listOf(
                    ScoreRecord("2024-12-20", "期末考试", 92.0, 100.0, "作文优秀"),
                    ScoreRecord("2024-12-10", "作文", 88.0, 100.0, "构思新颖"),
                    ScoreRecord("2024-12-01", "月考", 90.0, 100.0, null),
                    ScoreRecord("2024-11-20", "测验", 85.0, 100.0, null)
                ),
                averageScore = 88.75,
                trend = ScoreTrend.UP,
                teacherComment = "小明同学语文基础扎实，作文能力突出，建议继续保持阅读习惯。"
            ),
            ChildCourseScore(
                id = "2",
                courseName = "数学",
                teacher = "李老师",
                recentScores = listOf(
                    ScoreRecord("2024-12-22", "期末考试", 95.0, 100.0, "满分"),
                    ScoreRecord("2024-12-12", "单元测试", 98.0, 100.0, "计算准确"),
                    ScoreRecord("2024-12-03", "月考", 92.0, 100.0, null),
                    ScoreRecord("2024-11-22", "测验", 90.0, 100.0, null)
                ),
                averageScore = 93.75,
                trend = ScoreTrend.UP,
                teacherComment = "数学思维清晰，解题方法灵活，是班上的数学小能手。"
            ),
            ChildCourseScore(
                id = "3",
                courseName = "英语",
                teacher = "张老师",
                recentScores = listOf(
                    ScoreRecord("2024-12-18", "期末考试", 89.0, 100.0, null),
                    ScoreRecord("2024-12-08", "口语测试", 92.0, 100.0, "发音标准"),
                    ScoreRecord("2024-11-28", "月考", 87.0, 100.0, null),
                    ScoreRecord("2024-11-18", "听力测试", 85.0, 100.0, null)
                ),
                averageScore = 88.25,
                trend = ScoreTrend.STABLE,
                teacherComment = "英语综合能力良好，口语表达能力尤为突出。"
            ),
            ChildCourseScore(
                id = "4",
                courseName = "科学",
                teacher = "陈老师",
                recentScores = listOf(
                    ScoreRecord("2024-12-25", "期末考试", 91.0, 100.0, null),
                    ScoreRecord("2024-12-15", "实验报告", 94.0, 100.0, "观察细致"),
                    ScoreRecord("2024-12-05", "月考", 88.0, 100.0, null)
                ),
                averageScore = 91.0,
                trend = ScoreTrend.UP,
                teacherComment = "对科学实验充满兴趣，动手能力强。"
            )
        )
    }

    private fun getSampleCoursesForXiaoHong(): List<ChildCourseScore> {
        return listOf(
            ChildCourseScore(
                id = "1",
                courseName = "语文",
                teacher = "刘老师",
                recentScores = listOf(
                    ScoreRecord("2024-12-20", "期末考试", 78.0, 100.0, null),
                    ScoreRecord("2024-12-10", "作文", 75.0, 100.0, "需加强"),
                    ScoreRecord("2024-12-01", "月考", 80.0, 100.0, null)
                ),
                averageScore = 77.67,
                trend = ScoreTrend.STABLE,
                teacherComment = "基础知识掌握尚可，阅读理解能力需要提升。"
            ),
            ChildCourseScore(
                id = "2",
                courseName = "数学",
                teacher = "周老师",
                recentScores = listOf(
                    ScoreRecord("2024-12-22", "期末考试", 82.0, 100.0, null),
                    ScoreRecord("2024-12-12", "单元测试", 85.0, 100.0, null),
                    ScoreRecord("2024-12-03", "月考", 79.0, 100.0, "计算失误")
                ),
                averageScore = 82.0,
                trend = ScoreTrend.UP,
                teacherComment = "计算能力有所提高，应用题理解还需加强。"
            ),
            ChildCourseScore(
                id = "3",
                courseName = "英语",
                teacher = "吴老师",
                recentScores = listOf(
                    ScoreRecord("2024-12-18", "期末考试", 76.0, 100.0, null),
                    ScoreRecord("2024-12-08", "口语测试", 72.0, 100.0, "需练习"),
                    ScoreRecord("2024-11-28", "月考", 74.0, 100.0, null)
                ),
                averageScore = 74.0,
                trend = ScoreTrend.STABLE,
                teacherComment = "词汇量需要扩充，建议多读英文绘本。"
            )
        )
    }

    private fun getSampleCoursesForXiaoHua(): List<ChildCourseScore> {
        return listOf(
            ChildCourseScore(
                id = "1",
                courseName = "语文",
                teacher = "赵老师",
                recentScores = listOf(
                    ScoreRecord("2024-12-20", "期末考试", 96.0, 100.0, "优秀"),
                    ScoreRecord("2024-12-10", "作文", 95.0, 100.0, "文笔流畅"),
                    ScoreRecord("2024-12-01", "月考", 94.0, 100.0, null)
                ),
                averageScore = 95.0,
                trend = ScoreTrend.STABLE,
                teacherComment = "语文功底深厚，阅读面广，值得表扬。"
            ),
            ChildCourseScore(
                id = "2",
                courseName = "数学",
                teacher = "孙老师",
                recentScores = listOf(
                    ScoreRecord("2024-12-22", "期末考试", 98.0, 100.0, "满分"),
                    ScoreRecord("2024-12-12", "竞赛", 100.0, 100.0, "第一名"),
                    ScoreRecord("2024-12-03", "月考", 97.0, 100.0, null)
                ),
                averageScore = 98.33,
                trend = ScoreTrend.UP,
                teacherComment = "数学天赋出众，逻辑思维能力极强，建议参加数学竞赛。"
            ),
            ChildCourseScore(
                id = "3",
                courseName = "英语",
                teacher = "郑老师",
                recentScores = listOf(
                    ScoreRecord("2024-12-18", "期末考试", 94.0, 100.0, null),
                    ScoreRecord("2024-12-08", "演讲比赛", 96.0, 100.0, "二等奖"),
                    ScoreRecord("2024-11-28", "月考", 93.0, 100.0, null)
                ),
                averageScore = 94.33,
                trend = ScoreTrend.STABLE,
                teacherComment = "英语综合能力优秀，口语表达流利自然。"
            )
        )
    }

    private fun getStrengths(courses: List<ChildCourseScore>): List<String> {
        return courses
            .filter { it.averageScore >= 85 }
            .map { it.courseName }
    }

    private fun getWeaknesses(courses: List<ChildCourseScore>): List<String> {
        return courses
            .filter { it.averageScore < 80 }
            .map { it.courseName }
    }

    fun getScoreColor(score: Double): Color {
        return when {
            score >= 90 -> Color(0xFF4CAF50)
            score >= 80 -> Color(0xFF2196F3)
            score >= 70 -> Color(0xFFFF9800)
            score >= 60 -> Color(0xFFFFC107)
            else -> Color(0xFFF44336)
        }
    }

    fun getTrendIcon(trend: ScoreTrend) = when (trend) {
        ScoreTrend.UP -> Icons.Filled.ArrowUpward
        ScoreTrend.DOWN -> Icons.Filled.ArrowDownward
        ScoreTrend.STABLE -> Icons.Filled.Remove
    }

    fun getTrendColor(trend: ScoreTrend) = when (trend) {
        ScoreTrend.UP -> Color(0xFF4CAF50)
        ScoreTrend.DOWN -> Color(0xFFF44336)
        ScoreTrend.STABLE -> Color(0xFF9E9E9E)
    }
}

// ==================== 主界面 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentChildScoreScreen(
    childName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scoreSummary = remember(childName) {
        ParentScoreRepository.getChildScores(childName)
    }
    var selectedCourse by remember { mutableStateOf<ChildCourseScore?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("${childName}的成绩")
                        Text(
                            text = scoreSummary.semester,
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
            // 总体成绩概览
            OverallScoreCard(scoreSummary)

            Spacer(modifier = Modifier.height(8.dp))

            // 优势与薄弱科目
            StrengthWeaknessCard(scoreSummary)

            Spacer(modifier = Modifier.height(16.dp))

            // 各科成绩详情
            Text(
                text = "各科成绩详情",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                scoreSummary.courses.forEach { course ->
                    CourseScoreCard(
                        course = course,
                        onClick = { selectedCourse = course }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 课程详情对话框
        selectedCourse?.let { course ->
            CourseDetailDialog(
                course = course,
                childName = childName,
                onDismiss = { selectedCourse = null }
            )
        }
    }
}

// ==================== 组件 ====================

@Composable
fun OverallScoreCard(scoreSummary: ChildScoreSummary) {
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
                        text = "总体平均分",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format("%.2f", scoreSummary.overallAverage),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                scoreSummary.classRanking?.let { rank ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "班级排名",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$rank / ${scoreSummary.totalStudents}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(
                    label = "科目总数",
                    value = "${scoreSummary.courses.size}"
                )
                StatItem(
                    label = "优秀科目",
                    value = "${scoreSummary.strengths.size}"
                )
                StatItem(
                    label = "需提升",
                    value = "${scoreSummary.weaknesses.size}"
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
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

@Composable
fun StrengthWeaknessCard(scoreSummary: ChildScoreSummary) {
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
            if (scoreSummary.strengths.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "优势科目：${scoreSummary.strengths.joinToString("、")}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (scoreSummary.strengths.isNotEmpty() && scoreSummary.weaknesses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (scoreSummary.weaknesses.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "需提升科目：${scoreSummary.weaknesses.joinToString("、")}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (scoreSummary.strengths.isEmpty() && scoreSummary.weaknesses.isEmpty()) {
                Text(
                    text = "各科发展均衡",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScoreCard(
    course: ChildCourseScore,
    onClick: () -> Unit
) {
    val scoreColor = ParentScoreRepository.getScoreColor(course.averageScore)
    val trendIcon = ParentScoreRepository.getTrendIcon(course.trend)
    val trendColor = ParentScoreRepository.getTrendColor(course.trend)

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "任课老师：${course.teacher}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (course.trend) {
                            ScoreTrend.UP -> "成绩上升"
                            ScoreTrend.DOWN -> "需要关注"
                            ScoreTrend.STABLE -> "稳定发挥"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = trendColor,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(scoreColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", course.averageScore),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                    Text(
                        text = "平均分",
                        style = MaterialTheme.typography.bodySmall,
                        color = scoreColor,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CourseDetailDialog(
    course: ChildCourseScore,
    childName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "任课老师：${course.teacher}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 平均分
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "平均分",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("%.2f", course.averageScore),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ParentScoreRepository.getScoreColor(course.averageScore)
                    )
                }

                Divider()

                // 近期成绩记录
                Text(
                    text = "📊 近期成绩记录",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                course.recentScores.forEach { record ->
                    ScoreRecordItem(record)
                }

                // 老师评语
                course.teacherComment?.let { comment ->
                    Divider()
                    Text(
                        text = "👨‍🏫 老师评语",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = comment,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
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
fun ScoreRecordItem(record: ScoreRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.type,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = record.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                record.comment?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = "${record.score.toInt()}/${record.fullScore.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ParentScoreRepository.getScoreColor(
                    record.score / record.fullScore * 100
                )
            )
        }
    }
}
