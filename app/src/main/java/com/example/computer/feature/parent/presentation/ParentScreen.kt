package com.example.computer.feature.parent.presentation

import android.content.Context
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.computer.data.model.ChildInfo
import com.example.computer.data.model.LearningData
import com.example.computer.feature.common.domain.requestLearningSuggestions
import com.example.computer.feature.common.presentation.LearningDashboard
import com.example.computer.feature.common.presentation.LatexText
//import com.example.computer.feature.parent.presentation.model.ParentFeatureUiModel
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.res.painterResource
import com.example.computer.R

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*

// 模拟父母功能项
data class ParentFeatureUiModel(
    val id: String,
    val title: String,
    val description: String,
    @DrawableRes val iconResId: Int
)
// 模拟孩子作业数据
data class ChildHomework(
    val childName: String,
    val date: String,
    val homeworkList: List<HomeworkStatus>
)




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentScreen(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    onBack: () -> Unit = {}
) {
    // 模拟多个孩子的学习数据
    val childrenData = remember { getSampleChildrenData() }
    val scrollState = rememberScrollState()
    var selectedChildIndex by remember { mutableStateOf(0) }

    // AI 建议内容 & loading 状态
    var aiSuggestion by rememberSaveable { mutableStateOf<String?>(null) }
    var aiLoading by rememberSaveable { mutableStateOf(false) }
    var showHomeworkProgress by remember { mutableStateOf(false) }
    // ✅ 新增：导航状态变量
    var showChildScores by remember { mutableStateOf(false) }
    var showChildSchedule by remember { mutableStateOf(false) }
    var selectedChildName by remember { mutableStateOf("") }
    var showChildAttendance by remember { mutableStateOf(false) }
    var showTeacherChat by remember { mutableStateOf(false) }

    // 协程作用域，用来调 requestLearningSuggestions
    val coroutineScope = rememberCoroutineScope()

    // ✅ 如果显示成绩页面，则返回成绩页面
    if (showChildScores) {
        ParentChildScoreScreen(
            childName = selectedChildName,
            onBack = { showChildScores = false }
        )
        return
    }

    // ✅ 如果显示课程表页面，则返回课程表页面
    if (showChildSchedule) {
        ParentChildScheduleScreen(
            childName = selectedChildName,
            onBack = { showChildSchedule = false }
        )
        return
    }

    if (showChildAttendance) {
        ParentChildAttendanceScreen(
            childName = selectedChildName,
            onBack = { showChildAttendance = false }
        )
        return
    }

    if (showTeacherChat) {
        ParentTeacherChatScreen(
            childName = selectedChildName,
            onBack = { showTeacherChat = false }
        )
        return
    }

    // 如果显示作业完成界面
    if (showHomeworkProgress) {
        ParentChildHomeworkScreen(
            children = childrenData.map { it.name },
            onBack = { showHomeworkProgress = false }
        )
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("家长") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back to home"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "家长界面",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 孩子选择器
            ChildSelector(
                childrenData = childrenData,
                selectedIndex = selectedChildIndex,
                onChildSelected = { index ->
                    selectedChildIndex = index
                    // 换孩子时清空之前的 AI 建议
                    aiSuggestion = null
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val currentChild = childrenData[selectedChildIndex]

            // 显示选中孩子的学习仪表盘 + AI 建议按钮
            LearningDashboard(
                learningData = currentChild.learningData,
                showChildInfo = true,
                childName = currentChild.name,
                onAiSuggest = { data ->
                    coroutineScope.launch {
                        aiLoading = true
                        try {
                            val result = requestLearningSuggestions(data)
                            aiSuggestion = result
                            Toast.makeText(
                                context,
                                "已为 ${currentChild.name} 生成 AI 建议",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            aiSuggestion = "生成 AI 建议时出错：${e.message}"
                            Toast.makeText(
                                context,
                                "AI 调用失败",
                                Toast.LENGTH_SHORT
                            ).show()
                        } finally {
                            aiLoading = false
                        }
                    }
                }
            )

            // Loading 提示
            if (aiLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AI 正在分析 ${currentChild.name} 的学习情况,请稍候…",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // AI 建议卡片 - 使用 LatexText 支持 LaTeX 格式
            aiSuggestion?.let { suggestionText ->
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🤖 AI 对 ${currentChild.name} 的学习建议",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // 使用 LatexText 替代普通 Text，支持 LaTeX 渲染
                        LatexText(
                            text = suggestionText,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "家长功能",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))


            val parentFeatures = listOf(
                ParentFeatureUiModel(
                    "child_scores",
                    "孩子成绩",
                    "查看孩子各科成绩",
                    R.drawable.ic_child_score
                ),
                ParentFeatureUiModel(
                    "attendance",
                    "考勤情况",
                    "查看孩子出勤情况",
                    R.drawable.ic_attendance
                ),
                ParentFeatureUiModel(
                    "homework_progress",
                    "作业完成",
                    "检查作业完成情况",
                    R.drawable.ic_homework
                ),
                ParentFeatureUiModel(
                    "teacher_chat",
                    "老师沟通",
                    "与任课老师沟通",
                    R.drawable.ic_teacher_chat
                ),
                ParentFeatureUiModel(
                    "schedule",
                    "课程安排",
                    "查看孩子课程表",
                    R.drawable.ic_schedule
                )
            )

            Column {
                parentFeatures.forEach { feature ->
                    ParentFeatureItem(
                        feature = feature,
                        context = context,
                        onNavigate = { featureId ->
                            // ✅ 修改：处理导航逻辑
                            when (featureId) {
                                "child_scores" -> {
                                    selectedChildName = childrenData[selectedChildIndex].name
                                    showChildScores = true
                                }
                                "schedule" -> {
                                    selectedChildName = childrenData[selectedChildIndex].name
                                    showChildSchedule = true
                                }
                                "attendance" -> {
                                    selectedChildName = childrenData[selectedChildIndex].name
                                    showChildAttendance = true
                                }
//                                "homework_progress" -> {
//                                    Toast.makeText(context, "查看作业完成情况", Toast.LENGTH_SHORT).show()
//                                }

                                "homework_progress" -> showHomeworkProgress = true
                                "teacher_chat" -> {
                                    selectedChildName = childrenData[selectedChildIndex].name
                                    showTeacherChat = true
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ChildSelector(
    childrenData: List<ChildInfo>,
    selectedIndex: Int,
    onChildSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "选择孩子",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                childrenData.forEachIndexed { index, child ->
                    ChildChip(
                        childName = child.name,
                        grade = child.grade,
                        isSelected = index == selectedIndex,
                        onClick = { onChildSelected(index) }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildChip(
    childName: String,
    grade: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = childName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = grade,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentFeatureItem(
    feature: ParentFeatureUiModel,
    context: Context,
    onNavigate: (String) -> Unit  // ✅ 新增回调参数
) {
    Card(
        onClick = {
            onNavigate(feature.id)  // ✅ 调用回调而不是直接 Toast
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 图标背景圆
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = feature.iconResId),
                    contentDescription = feature.title,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.padding(horizontal = 16.dp))
            Column {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// 模拟多个孩子的数据
private fun getSampleChildrenData(): List<ChildInfo> {
    return listOf(
        ChildInfo(
            name = "小明",
            grade = "五年级",
            learningData = LearningData(
                totalLearningTime = "42小时",
                skillLevel = "高级",
                reviewProgress = 0.85f,
                dailyGoal = "2.5小时",
                streakDays = 14,
                completedCourses = 15,
                averageScore = "92%"
            )
        ),
        ChildInfo(
            name = "小红",
            grade = "四年级",
            learningData = LearningData(
                totalLearningTime = "28小时",
                skillLevel = "中级",
                reviewProgress = 0.65f,
                dailyGoal = "1.5小时",
                streakDays = 5,
                completedCourses = 8,
                averageScore = "78%"
            )
        ),
        ChildInfo(
            name = "小华",
            grade = "六年级",
            learningData = LearningData(
                totalLearningTime = "56小时",
                skillLevel = "高级",
                reviewProgress = 0.95f,
                dailyGoal = "3小时",
                streakDays = 21,
                completedCourses = 18,
                averageScore = "95%"
            )
        )
    )
}

data class HomeworkStatus(
    val title: String,
    val isCompleted: Boolean,
    val progress: Float // 0~1
)

@Composable
fun HomeworkProgressSection(
    homeworkList: List<HomeworkStatus>
) {
    Column {
        Text(
            text = "作业完成情况",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        homeworkList.forEach { homework ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = homework.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // 显示进度百分比
                        Text(
                            text = "完成度：${(homework.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    // 简单标记是否完成
                    Text(
                        text = if (homework.isCompleted) "✅ 已完成" else "❌ 未完成",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// 作业完成界面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentChildHomeworkScreen(
    children: List<String>,
    onBack: () -> Unit
) {
    var selectedChild by remember { mutableStateOf(children[0]) }
    var selectedDay by remember { mutableStateOf("今天") }

    // 模拟作业数据
    val sampleData = remember {
        listOf(
            ChildHomework("小明", "今天", listOf(
                HomeworkStatus("语文作业", true, 1f),
                HomeworkStatus("数学作业", false, 0.6f),
                HomeworkStatus("英语作业", true, 1f)
            )),
            ChildHomework("小明", "昨天", listOf(
                HomeworkStatus("语文作业", true, 1f),
                HomeworkStatus("数学作业", true, 1f),
                HomeworkStatus("英语作业", false, 0.4f)
            )),
            ChildHomework("小红", "今天", listOf(
                HomeworkStatus("语文作业", true, 1f),
                HomeworkStatus("数学作业", true, 1f),
                HomeworkStatus("英语作业", true, 1f)
            ))
        )
    }

    val filteredData = sampleData.filter { it.childName == selectedChild && it.date == selectedDay }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("作业完成情况") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 选择孩子
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                children.forEach { child ->
                    FilterChip(
                        selected = selectedChild == child,
                        onClick = { selectedChild = child },
                        label = { Text(child) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 选择日期
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("今天", "昨天", "前天").forEach { day ->
                    FilterChip(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        label = { Text(day) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 作业列表
            filteredData.forEach { childHomework ->
                Text("${childHomework.childName} - ${childHomework.date}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(childHomework.homeworkList) { hw ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(hw.title, fontWeight = FontWeight.Bold)
                                    Text("完成度：${(hw.progress*100).toInt()}%")
                                }
                                Text(if (hw.isCompleted) "✅ 已完成" else "❌ 未完成",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (filteredData.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("暂无作业数据", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}