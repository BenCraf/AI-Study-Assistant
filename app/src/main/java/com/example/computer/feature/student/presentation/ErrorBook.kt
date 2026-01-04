package com.example.computer.feature.student.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.computer.feature.common.presentation.LatexText
import com.example.computer.ui.components.KaTeXMarkdownView

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ========== 数据模型 ==========
data class ErrorQuestion(
    val id: String,
    val title: String,
    val createTime: Long,
    val questionImagePath: String,
    val wrongAnswerImagePath: String?,
    val errorAnalysis: String?,
    val correctAnswer: String?
)

// ========== 错题本管理器 ==========
class ErrorBookManager(private val context: Context) {
    private val rootDir = File(context.filesDir, "tmprepository")
    private val indexFile = File(rootDir, "index.txt")

    init {
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }
        if (!indexFile.exists()) {
            indexFile.writeText("")
        }
    }

    suspend fun getAllQuestions(): List<ErrorQuestion> = withContext(Dispatchers.IO) {
        val questions = mutableListOf<ErrorQuestion>()

        if (!indexFile.exists()) return@withContext questions

        indexFile.readLines().forEach { line ->
            if (line.isBlank()) return@forEach

            val parts = line.split("|")
            if (parts.size >= 3) {
                val id = parts[0]
                val title = parts[1]
                val createTime = parts[2].toLongOrNull() ?: System.currentTimeMillis()

                val questionDir = File(rootDir, id)
                if (questionDir.exists()) {
                    val questionImg = File(questionDir, "question.jpg")
                    val wrongAnswerImg = File(questionDir, "wrong_answer.jpg")
                    val errorAnalysisFile = File(questionDir, "error_analysis.txt")
                    val correctAnswerFile = File(questionDir, "correct_answer.txt")

                    questions.add(
                        ErrorQuestion(
                            id = id,
                            title = title,
                            createTime = createTime,
                            questionImagePath = questionImg.absolutePath,
                            wrongAnswerImagePath = if (wrongAnswerImg.exists()) wrongAnswerImg.absolutePath else null,
                            errorAnalysis = if (errorAnalysisFile.exists()) {
                                val text = errorAnalysisFile.readText()
                                if (text.isBlank()) null else text
                            } else null,
                            correctAnswer = if (correctAnswerFile.exists()) {
                                val text = correctAnswerFile.readText()
                                if (text.isBlank()) null else text
                            } else null
                        )
                    )
                }
            }
        }

        questions.sortedByDescending { it.createTime }
    }

    suspend fun addQuestion(
        title: String,
        questionImageUri: Uri,
        wrongAnswerImageUri: Uri?,
        onAnalyzing: () -> Unit = {}
    ): ErrorQuestion = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val createTime = System.currentTimeMillis()

        val questionDir = File(rootDir, id)
        questionDir.mkdirs()

        // 保存标题
        File(questionDir, "title.txt").writeText(title)

        // 保存题目图片
        val questionImg = File(questionDir, "question.jpg")
        context.contentResolver.openInputStream(questionImageUri)?.use { input ->
            questionImg.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // 保存错误解答图片（如果有）
        var wrongAnswerPath: String? = null
        if (wrongAnswerImageUri != null) {
            val wrongAnswerImg = File(questionDir, "wrong_answer.jpg")
            context.contentResolver.openInputStream(wrongAnswerImageUri)?.use { input ->
                wrongAnswerImg.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            wrongAnswerPath = wrongAnswerImg.absolutePath
        }

        // 更新索引文件
        val indexLine = "$id|$title|$createTime\n"
        indexFile.appendText(indexLine)

        // 初始化变量
        var errorAnalysisText: String? = null
        var correctAnswerText: String? = null

        // 通知开始分析
        withContext(Dispatchers.Main) {
            onAnalyzing()
        }

        try {
            if (wrongAnswerImageUri != null) {
                // 情况1：有错误解答 - 调用 analyzeErrorQuestion
                // 先给出错误分析，然后给出正确解答
                val analysisResult = analyzeErrorQuestion(
                    context = context,
                    questionImageUri = questionImageUri,
                    wrongAnswerImageUri = wrongAnswerImageUri
                )

                errorAnalysisText = analysisResult.errorAnalysis
                correctAnswerText = analysisResult.correctAnswer

                // 保存分析结果
                File(questionDir, "error_analysis.txt").writeText(errorAnalysisText ?: "")
                File(questionDir, "correct_answer.txt").writeText(correctAnswerText)
            } else {
                // 情况2：没有错误解答 - 调用 analyzeQuestionOnly
                // 只给出正确解答
                val analysisResult = analyzeQuestionOnly(
                    context = context,
                    questionImageUri = questionImageUri
                )

                // 没有错误分析
                errorAnalysisText = null
                correctAnswerText = analysisResult.correctAnswer

                // 保存分析结果
                File(questionDir, "error_analysis.txt").writeText("")  // 空文件
                File(questionDir, "correct_answer.txt").writeText(correctAnswerText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 分析失败，保存错误信息
            val errorMsg = "AI 分析失败：${e.message}\n\n请点击右上角的重新分析按钮重试。"

            if (wrongAnswerImageUri != null) {
                // 有错误解答的情况，错误信息保存到错误分析中
                File(questionDir, "error_analysis.txt").writeText(errorMsg)
                File(questionDir, "correct_answer.txt").writeText("")
                errorAnalysisText = errorMsg
                correctAnswerText = null
            } else {
                // 没有错误解答的情况，错误信息保存到正确解答中
                File(questionDir, "error_analysis.txt").writeText("")
                File(questionDir, "correct_answer.txt").writeText(errorMsg)
                errorAnalysisText = null
                correctAnswerText = errorMsg
            }
        }

        ErrorQuestion(
            id = id,
            title = title,
            createTime = createTime,
            questionImagePath = questionImg.absolutePath,
            wrongAnswerImagePath = wrongAnswerPath,
            errorAnalysis = errorAnalysisText,
            correctAnswer = correctAnswerText
        )
    }

    fun deleteQuestion(id: String) {
        val questionDir = File(rootDir, id)
        if (questionDir.exists()) {
            questionDir.deleteRecursively()
        }

        // 更新索引文件
        val lines = indexFile.readLines().filter { !it.startsWith("$id|") }
        indexFile.writeText(lines.joinToString("\n") + "\n")
    }

    suspend fun reanalyzeQuestion(question: ErrorQuestion): ErrorQuestion = withContext(Dispatchers.IO) {
        val questionUri = Uri.fromFile(File(question.questionImagePath))

        val result = if (question.wrongAnswerImagePath != null) {
            val wrongAnswerUri = Uri.fromFile(File(question.wrongAnswerImagePath!!))
            analyzeErrorQuestion(
                context = context,
                questionImageUri = questionUri,
                wrongAnswerImageUri = wrongAnswerUri
            )
        } else {
            analyzeQuestionOnly(
                context = context,
                questionImageUri = questionUri
            )
        }

        // 保存新的分析结果
        val questionDir = File(question.questionImagePath).parentFile
        if (questionDir != null) {
            File(questionDir, "error_analysis.txt").writeText(result.errorAnalysis ?: "")
            File(questionDir, "correct_answer.txt").writeText(result.correctAnswer)
        }

        question.copy(
            errorAnalysis = result.errorAnalysis,
            correctAnswer = result.correctAnswer
        )
    }
}

// ========== 主错题本界面 ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorBookMainScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val errorBookManager = remember { ErrorBookManager(context) }
    val scope = rememberCoroutineScope()

    var questions by remember { mutableStateOf<List<ErrorQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedQuestion by remember { mutableStateOf<ErrorQuestion?>(null) }

    // 加载错题列表
    LaunchedEffect(Unit) {
        scope.launch {
            questions = errorBookManager.getAllQuestions()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("错题本") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加错题",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                questions.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "还没有错题",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击右下角的 + 号添加错题",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(questions) { question ->
                            ErrorQuestionListItem(
                                question = question,
                                onClick = { selectedQuestion = question }
                            )
                        }
                    }
                }
            }
        }
    }

    // 添加错题对话框
    if (showAddDialog) {
        AddErrorQuestionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, questionUri, wrongAnswerUri ->
                scope.launch {
                    isLoading = true
                    try {
                        val hasWrongAnswer = wrongAnswerUri != null

                        // 显示分析提示
                        withContext(Dispatchers.Main) {
                            val message = if (hasWrongAnswer) {
                                "正在添加错题并进行 AI 错误分析，请稍候..."
                            } else {
                                "正在添加题目并生成 AI 解答，请稍候..."
                            }
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }

                        val newQuestion = errorBookManager.addQuestion(
                            title = title,
                            questionImageUri = questionUri,
                            wrongAnswerImageUri = wrongAnswerUri,
                            onAnalyzing = {
                                // 这个回调在开始分析时调用
                            }
                        )

                        questions = errorBookManager.getAllQuestions()
                        showAddDialog = false
                        selectedQuestion = newQuestion // 直接跳转到错题详情

                        val message = if (hasWrongAnswer) {
                            "错题添加成功，AI 错误分析已完成"
                        } else {
                            "题目添加成功，AI 解答已生成"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "添加失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                    }
                }
            }
        )
    }

    // 错题详情界面
    selectedQuestion?.let { question ->
        ErrorQuestionDetailScreen(
            question = question,
            onBack = {
                selectedQuestion = null
                // 刷新列表
                scope.launch {
                    questions = errorBookManager.getAllQuestions()
                }
            },
            onDelete = {
                errorBookManager.deleteQuestion(question.id)
                selectedQuestion = null
                scope.launch {
                    questions = errorBookManager.getAllQuestions()
                }
                Toast.makeText(context, "错题已删除", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// ========== 错题列表项 ==========
@Composable
fun ErrorQuestionListItem(
    question: ErrorQuestion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 题目缩略图
            AsyncImage(
                model = File(question.questionImagePath),
                contentDescription = "题目图片",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = question.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(question.createTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ========== 添加错题对话框 ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddErrorQuestionDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, questionUri: Uri, wrongAnswerUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var questionImageUri by remember { mutableStateOf<Uri?>(null) }
    var wrongAnswerImageUri by remember { mutableStateOf<Uri?>(null) }

    // 拍照相关
    var pendingPhotoType by remember { mutableStateOf<String?>(null) }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingPhotoUri != null) {
            when (pendingPhotoType) {
                "question" -> questionImageUri = pendingPhotoUri
                "wrong_answer" -> wrongAnswerImageUri = pendingPhotoUri
            }
        }
        pendingPhotoType = null
        pendingPhotoUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingPhotoType != null) {
            val uri = createTempImageUri(context)
            pendingPhotoUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, "需要相机权限", Toast.LENGTH_SHORT).show()
            pendingPhotoType = null
        }
    }

    // 相册选择
    val pickQuestionImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { questionImageUri = it } }

    val pickWrongAnswerImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { wrongAnswerImageUri = it } }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "添加错题",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("错题标题") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 题目图片 (必须)
                Text(
                    text = "题目图片 *",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                ImageUploadSection(
                    imageUri = questionImageUri,
                    onTakePhoto = {
                        pendingPhotoType = "question"
                        val hasCameraPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasCameraPermission) {
                            val uri = createTempImageUri(context)
                            pendingPhotoUri = uri
                            takePictureLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onPickFromGallery = { pickQuestionImageLauncher.launch("image/*") },
                    onRemove = { questionImageUri = null }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 错误解答图片 (可选)
                Text(
                    text = "错误解答 (可选)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "如果上传错误解答，AI 会分析错误原因；否则只生成正确答案",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                ImageUploadSection(
                    imageUri = wrongAnswerImageUri,
                    onTakePhoto = {
                        pendingPhotoType = "wrong_answer"
                        val hasCameraPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasCameraPermission) {
                            val uri = createTempImageUri(context)
                            pendingPhotoUri = uri
                            takePictureLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onPickFromGallery = { pickWrongAnswerImageLauncher.launch("image/*") },
                    onRemove = { wrongAnswerImageUri = null }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "请输入标题", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (questionImageUri == null) {
                                Toast.makeText(context, "请上传题目图片", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onConfirm(title, questionImageUri!!, wrongAnswerImageUri)
                        }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

// ========== 图片上传组件 ==========
@Composable
fun ImageUploadSection(
    imageUri: Uri?,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onRemove: () -> Unit
) {
    if (imageUri == null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onTakePhoto,
                modifier = Modifier.weight(1f)
            ) {
                Text("📷 拍照")
            }
            OutlinedButton(
                onClick = onPickFromGallery,
                modifier = Modifier.weight(1f)
            ) {
                Text("🖼 相册")
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "已选择的图片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除",
                    tint = Color.White
                )
            }
        }
    }
}

// ========== 错题详情界面 ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorQuestionDetailScreen(
    question: ErrorQuestion,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val errorBookManager = remember { ErrorBookManager(context) }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isReanalyzing by remember { mutableStateOf(false) }
    var updatedQuestion by remember { mutableStateOf(question) }

    // ✅ 打印日志
    Log.e("📘ERROR_DETAIL", "========================================")
    Log.e("📘ERROR_DETAIL", "错题ID: ${updatedQuestion.id}")
    Log.e("📘ERROR_DETAIL", "正确解答内容:")
    Log.e("📘ERROR_DETAIL", updatedQuestion.correctAnswer ?: "null")
    Log.e("📘ERROR_DETAIL", "========================================")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(updatedQuestion.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                isReanalyzing = true
                                try {
                                    Log.e("📘REANALYZE", "开始重新分析...")
                                    val newQuestion = errorBookManager.reanalyzeQuestion(updatedQuestion)
                                    updatedQuestion = newQuestion
                                    Toast.makeText(context, "重新分析完成", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("📘REANALYZE", "重新分析失败", e)
                                    Toast.makeText(context, "分析失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isReanalyzing = false
                                }
                            }
                        },
                        enabled = !isReanalyzing
                    ) {
                        Text(if (isReanalyzing) "分析中..." else "重新分析")
                    }

                    TextButton(onClick = { showDeleteDialog = true }) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 题目
                DetailSection(
                    title = "题目",
                    imagePath = updatedQuestion.questionImagePath
                )

                // 错误解答
                updatedQuestion.wrongAnswerImagePath?.let { path ->
                    Spacer(modifier = Modifier.height(24.dp))
                    DetailSection(
                        title = "错误解答",
                        imagePath = path
                    )
                }

                // 错误分析
                updatedQuestion.errorAnalysis?.takeIf { it.isNotBlank() }?.let { analysis ->
                    Spacer(modifier = Modifier.height(24.dp))
                    DetailSection(
                        title = "错误分析",
                        text = analysis
                    )
                }

//                // ✅ 始终显示原始 API 返回（用于调试）
//                updatedQuestion.correctAnswer?.let { answer ->
//                    Spacer(modifier = Modifier.height(24.dp))
//                    DebugSection(text = answer)
//                }

                // 正确解答（渲染后）
                updatedQuestion.correctAnswer?.takeIf { it.isNotBlank() }?.let { answer ->
                    Spacer(modifier = Modifier.height(24.dp))
                    DetailSection(
                        title = "正确解答",
                        text = answer
                    )
                }
            }

            if (isReanalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("AI 正在重新分析...")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这道错题吗?此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun DetailSection(
    title: String,
    imagePath: String? = null,
    text: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        imagePath?.let { path ->
            AsyncImage(
                model = File(path),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.FillWidth
            )
        }

        text?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                // ✅ 使用 KaTeXMarkdownView 替代 LatexText
                KaTeXMarkdownView(
                    markdown = it,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
@Composable
fun DebugSection(text: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "📋 API 原始返回（调试用）",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            SelectionContainer {
                Text(
                    text = text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
//                        .verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}



//@Composable
//fun DetailSection(
//    title: String,
//    imagePath: String? = null,
//    text: String? = null
//) {
//    Column(modifier = Modifier.fillMaxWidth()) {
//        Text(
//            text = title,
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold,
//            color = MaterialTheme.colorScheme.primary
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        imagePath?.let { path ->
//            AsyncImage(
//                model = File(path),
//                contentDescription = title,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(MaterialTheme.colorScheme.surfaceVariant),
//                contentScale = ContentScale.FillWidth
//            )
//        }
//
//        text?.let { rawText ->
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
//                )
//            ) {
//                // --- 修改开始 ---
//                // 如果你想强制把所有的 $$ 变成行内公式（$），可以使用这个处理逻辑
//                // 或者是为了修复 AI 混用格式的问题
//                val processedText = rawText
//                // 策略A: 如果你希望 $$ 也显示为行内，将其替换为 $
//                // .replace("$$", "$")
//
//                // 策略B (推荐): 保持原样，让 LatexText 里的 WebView 去渲染
//                // 大多数情况下，上面的 LatexText 实现已经能完美处理 $$ 了
//
//                LatexText(
//                    text = processedText, // 使用处理后的文本
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(12.dp)
//                )
//                // --- 修改结束 ---
//            }
//        }
//    }
//}
// ========== 工具函数 ==========
private fun createTempImageUri(context: Context): Uri {
    val imageFile = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "temp_${System.currentTimeMillis()}.jpg"
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}