package com.example.computer.feature.teacher.presentation

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.computer.data.model.Assignment
import com.example.computer.data.model.Submission
import com.example.computer.data.model.SubmissionStatus
import androidx.compose.foundation.lazy.grid.items

/* ================== 主页面 ================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeHomeworkScreen(
    assignment: Assignment,
    viewModel: TeacherViewModel,
    onBack: () -> Unit
) {
    val submissions by viewModel.submissions.collectAsState()
    val assignmentSubmissions = submissions.filter { it.assignmentId == assignment.id }

    var selectedSubmission by remember { mutableStateOf<Submission?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("批改作业") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
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

            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            if (assignmentSubmissions.isEmpty()) {
                Text("暂无学生提交")
            } else {
                assignmentSubmissions.forEach {
                    SubmissionItem(
                        submission = it,
                        onClick = { selectedSubmission = it }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    selectedSubmission?.let {
        GradeSubmissionDialog(
            submission = it,
            onDismiss = { selectedSubmission = null },
            onSubmit = { isCorrect, comment ->
                viewModel.gradeSubmission(
                    submissionId = it.id,
                    score = if (isCorrect) 1 else 0,
                    comment = comment
                )
                selectedSubmission = null
            }
        )
    }
}

/* ================== 批改对话框 ================== */

@Composable
fun GradeSubmissionDialog(
    submission: Submission,
    onDismiss: () -> Unit,
    onSubmit: (isCorrect: Boolean, comment: String) -> Unit
) {
    var isCorrect by remember { mutableStateOf(submission.score?.let { it == 1 }) }
    var comment by remember { mutableStateOf(submission.comment ?: "") }
    var previewImagePath by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("批改作业") },
        text = {
            Column {

                Text("学生：${submission.studentName}")
                Spacer(Modifier.height(8.dp))

                Text("作业内容：")
                Text(submission.content)

                Spacer(Modifier.height(12.dp))
                Text("提交的图片：")

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.heightIn(max = 240.dp)
                ) {
                    items(submission.imageUris) { path ->
                        SubmissionImageThumbnail(
                            path = path,
                            onClick = { previewImagePath = path }
                        )
                    }

                }

                Spacer(Modifier.height(12.dp))

                Text("是否正确：")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isCorrect == true,
                        onClick = { isCorrect = true }
                    )
                    Text("正确")

                    Spacer(Modifier.width(16.dp))

                    RadioButton(
                        selected = isCorrect == false,
                        onClick = { isCorrect = false }
                    )
                    Text("错误")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("评语") },
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val result = isCorrect ?: return@TextButton
                    onSubmit(result, comment)
                }
            ) {
                Text("提交批改")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    previewImagePath?.let {
        FullscreenImageDialog(
            imagePath = it,
            onDismiss = { previewImagePath = null }
        )
    }
}

/* ================== 列表项 ================== */

@Composable
fun SubmissionItem(
    submission: Submission,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(submission.studentName, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))

            Text(
                text = when (submission.status) {
                    SubmissionStatus.SUBMITTED -> "未批改"
                    SubmissionStatus.GRADED ->
                        if (submission.score == 1) "已批改（正确）" else "已批改（错误）"
                },
                color = if (submission.status == SubmissionStatus.GRADED)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}

/* ================== 缩略图 ================== */

@Composable
fun SubmissionImageThumbnail(
    path: String,
    onClick: () -> Unit
) {
    val bitmap = remember(path) { BitmapFactory.decodeFile(path) }

    Box(
        modifier = Modifier
            .size(96.dp)
            .padding(4.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text("加载失败", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/* ================== 全屏预览 ================== */

@Composable
fun FullscreenImageDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    val bitmap = remember(imagePath) { BitmapFactory.decodeFile(imagePath) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}