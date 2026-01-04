package com.example.computer.feature.student.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.computer.data.model.Assignment
import com.example.computer.data.model.LearningData
import com.example.computer.data.repository.SubmissionRepository
import com.example.computer.feature.common.domain.requestLearningSuggestions
import com.example.computer.feature.common.presentation.LatexText
import com.example.computer.feature.common.presentation.LearningDashboard
import com.example.newapp.presentation.student.NoteAssistantScreen
import kotlinx.coroutines.launch
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.painterResource
import com.example.computer.R
import com.example.computer.data.model.Submission
import java.io.File

fun copyImageToAppDir(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val fileName = "submission_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, fileName)

    file.outputStream().use { output ->
        inputStream.copyTo(output)
    }

    return file.absolutePath   // 👈 关键：返回真实文件路径
}
data class StudentFeatureUiModel(
    val id: String,
    val title: String,
    val description: String,
    @DrawableRes val iconResId: Int
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    viewModel: StudentViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val learningData = remember { getSampleLearningData() }
    val scrollState = rememberScrollState()

    // 子页面导航
    var showNoteAssistant by remember { mutableStateOf(false) }
    var showErrorBook by remember { mutableStateOf(false) }
    var showTimetable by remember { mutableStateOf(false) }
    var showScores by remember { mutableStateOf(false) }

    // AI 建议状态
    var aiSuggestion by rememberSaveable { mutableStateOf<String?>(null) }
    var aiLoading by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var showAssignments by remember { mutableStateOf(false) }



    var showSubmitDialog by remember { mutableStateOf(false) }
    var submittingAssignment by remember { mutableStateOf<Assignment?>(null) }
    var submissionText by remember { mutableStateOf("") }


    // ========= 子页面优先渲染 =========
    if (showNoteAssistant) {
        // 使用独立的 NoteAssistantScreen
        NoteAssistantScreen(
            onNavigateBack = { showNoteAssistant = false }
        )
        return
    }
    if (showErrorBook) {
        ErrorBookMainScreen(onBack = { showErrorBook = false })
        return
    }
    if (showTimetable) {
        TimetableScreen(onBack = { showTimetable = false })
        return
    }
    if (showAssignments) {
        StudentAssignmentScreen(
            viewModel = viewModel,
            onBack = { showAssignments = false }
        )
        return
    }
    // 在其他子页面检查之后添加：
    if (showScores) {
        ScoreScreen(onBack = { showScores = false })
        return
    }

    // ========= 主学生页面 =========
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("学生") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回"
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
            // 学习仪表盘 + AI 建议
            LearningDashboard(
                learningData = learningData,
                onAiSuggest = { data ->
                    coroutineScope.launch {
                        aiLoading = true
                        try {
                            val result = requestLearningSuggestions(data)
                            aiSuggestion = result
                            Toast.makeText(context, "AI 建议已生成", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            aiSuggestion = "生成 AI 建议时出错：${e.message}"
                            Toast.makeText(context, "AI 调用失败", Toast.LENGTH_SHORT).show()
                        } finally {
                            aiLoading = false
                        }
                    }
                }
            )

            if (aiLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AI 正在分析学习情况，请稍候…",
                    style = MaterialTheme.typography.bodySmall
                )
            }

// 在 StudentScreen.kt 中，找到显示 AI 建议的 Card，替换为：

            aiSuggestion?.let { suggestionText ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme
                            .primaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "💡 AI 学习建议",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // 使用 LaTeX 渲染组件替代普通 Text
                        LatexText(
                            text = suggestionText,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))


            // ====== 功能菜单 ======
            Text(
                text = "功能菜单",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            val studentFeatures = listOf(
                StudentFeatureUiModel(
                    id = "note_assistant",
                    title = "笔记助手",
                    description = "录像并转换语音为文本",
                    iconResId = R.drawable.ic_note
                ),
                StudentFeatureUiModel(
                    id = "error_book",
                    title = "错题本管理",
                    description = "拍照/上传错题，智能解析与归类",
                    iconResId = R.drawable.ic_error_book
                ),
                StudentFeatureUiModel(
                    id = "assignments",
                    title = "作业提交",
                    description = "提交和查看作业",
                    iconResId = R.drawable.ic_assignment
                ),
                StudentFeatureUiModel(
                    id = "scores",
                    title = "成绩查询",
                    description = "查看各科成绩",
                    iconResId = R.drawable.ic_score
                ),
//                StudentFeatureUiModel(
//                    id = "materials",
//                    title = "学习资料",
//                    description = "下载学习资料",
//                    iconResId = R.drawable.ic_material
//                ),
                StudentFeatureUiModel(
                    id = "timetable",
                    title = "课程表",
                    description = "查看每周课程安排",
                    iconResId = R.drawable.ic_timetable
                )
            )

            Column {
                studentFeatures.forEach { feature ->
                    StudentFeatureItem(
                        feature = feature,
                        onClick = {
                            when (feature.id) {
                                "assignments" -> showAssignments = true
                                "note_assistant" -> showNoteAssistant = true
                                "error_book" -> showErrorBook = true
                                "timetable" -> showTimetable = true
                                "scores" -> showScores = true
                                else -> {
                                    Toast.makeText(
                                        context,
                                        "打开${feature.title}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

     // ====== 提交作业对话框 ======


    if (showSubmitDialog && submittingAssignment != null) {
        SubmitHomeworkDialog(
            assignment = submittingAssignment!!,
            initialText = submissionText,
            initialImages = emptyList(),
            onDismiss = { showSubmitDialog = false },
//            onSubmit = { text, images ->
//                viewModel.submitAssignment(
//                    assignment = submittingAssignment!!,
//                    text = text,
//                    imageUris = images
//                )
//                Toast.makeText(context, "作业提交成功", Toast.LENGTH_SHORT).show()
//                showSubmitDialog = false
//            }
            onSubmit = { text, images ->
                val localImagePaths = images.map {
                    copyImageToAppDir(context, Uri.parse(it))
                }

                viewModel.submitAssignment(
                    assignment = submittingAssignment!!,
                    text = text,
                    imageUris = localImagePaths   // ✅ 现在是文件路径
                )

                localImagePaths.forEach {
                    Log.d("SUBMIT_IMAGE_PATH", it)
                }
            }
        )
    }



}
// ================= 功能项 =================
@Composable
fun StudentFeatureItem(
    feature: StudentFeatureUiModel,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(feature.iconResId),
                    contentDescription = feature.title,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(feature.title, fontWeight = FontWeight.Bold)
                Text(
                    feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun AssignmentForStudentItem(
    assignment: Assignment,
    submittedContent: String?,
    onSubmitClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "截止日期：${assignment.dueDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = assignment.description,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (submittedContent != null) {
                Text(
                    text = "✅ 已提交",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            OutlinedButton(
                onClick = onSubmitClick
            ) {
                Text(if (submittedContent == null) "提交作业" else "查看/修改提交")
            }
        }
    }
}

@Composable
fun rememberImagePicker(
    onImagesPicked: (List<String>) -> Unit
): () -> Unit {
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents()
        ) { uris ->
            onImagesPicked(uris.map { it.toString() })
        }

    return {
        launcher.launch("image/*")
    }
}
private fun getSampleLearningData(): LearningData {
    return LearningData(
        totalLearningTime = "36小时",
        skillLevel = "中级",
        reviewProgress = 0.75f,
        dailyGoal = "2小时",
        streakDays = 7,
        completedCourses = 10,
        averageScore = "85%"
    )
}

@Composable
fun SubmitHomeworkDialog(
    assignment: Assignment,
    initialText: String,
    initialImages: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (String, List<String>) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    var images by remember { mutableStateOf(initialImages) }

    val pickImages = rememberImagePicker {
        images = images + it
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("提交作业：${assignment.title}") },
        text = {
            Column {

                Text(
                    text = "作业要求：${assignment.description}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("作业内容") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "已上传图片（${images.size}）",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(4.dp))

                images.forEach { uri ->
                    Text(
                        text = "📷 $uri",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(onClick = pickImages) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("上传图片")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isBlank() && images.isEmpty()) return@TextButton
                    onSubmit(text, images)
                }
            ) {
                Text("提交")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAssignmentScreen(
    viewModel: StudentViewModel,
    onBack: () -> Unit
) {
    val assignments by viewModel.assignments.collectAsState()
    val submissions by SubmissionRepository.submissions.collectAsState()

    val currentStudentId = "student_001"

    val pendingAssignments = assignments.filter { a ->
        submissions.none { it.assignmentId == a.id && it.studentId == currentStudentId }
    }

    val submittedAssignments = assignments.filter { a ->
        submissions.any { it.assignmentId == a.id && it.studentId == currentStudentId }
    }

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("作业提交") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("待提交 (${pendingAssignments.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("已提交 (${submittedAssignments.size})") }
                )
            }

            Spacer(Modifier.height(8.dp))

            when (selectedTab) {
                0 -> AssignmentList(
                    assignments = pendingAssignments,
                    submissions = submissions,
                    viewModel = viewModel
                )
                1 -> AssignmentList(
                    assignments = submittedAssignments,
                    submissions = submissions,
                    viewModel = viewModel
                )
            }
        }
    }
}


@Composable
fun AssignmentList(
    assignments: List<Assignment>,
    submissions: List<Submission>,
    viewModel: StudentViewModel
) {
    val context = LocalContext.current
    var showSubmitDialog by remember { mutableStateOf(false) }
    var submittingAssignment by remember { mutableStateOf<Assignment?>(null) }
    var submissionText by remember { mutableStateOf("") }

    val currentStudentId = "student_001"

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        assignments.forEach { hw ->
            val submission = submissions.find {
                it.assignmentId == hw.id && it.studentId == currentStudentId
            }

            AssignmentForStudentItem(
                assignment = hw,
                submittedContent = submission?.content,
                onSubmitClick = {
                    submittingAssignment = hw
                    submissionText = submission?.content ?: ""
                    showSubmitDialog = true
                }
            )

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showSubmitDialog && submittingAssignment != null) {
        SubmitHomeworkDialog(
            assignment = submittingAssignment!!,
            initialText = submissionText,
            initialImages = emptyList(),
            onDismiss = { showSubmitDialog = false },
//            onSubmit = { text, images ->
//                viewModel.submitAssignment(
//                    assignment = submittingAssignment!!,
//                    text = text,
//                    imageUris = images
//                )
//                Toast.makeText(context, "作业提交成功", Toast.LENGTH_SHORT).show()
//                showSubmitDialog = false
//            }
            onSubmit = { text, images ->
                val localImagePaths = images.map {
                    copyImageToAppDir(context, Uri.parse(it))
                }

                viewModel.submitAssignment(
                    assignment = submittingAssignment!!,
                    text = text,
                    imageUris = localImagePaths   // ✅ 现在是文件路径
                )

                Toast.makeText(context, "作业提交成功", Toast.LENGTH_SHORT).show()
                showSubmitDialog = false

                localImagePaths.forEach {
                    Log.d("SUBMIT_IMAGE_PATH", it)
                }
            }
        )
    }
}