package com.example.computer.feature.teacher.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.computer.data.model.Assignment

@Composable
fun AssignHomeworkDialog(
    onDismiss: () -> Unit,
    onSubmit: (Assignment) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("布置作业") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("作业标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("作业要求") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("截止日期（如 2025-03-01）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

//                Spacer(Modifier.height(8.dp))
//
//                Text(
//                    text = "支持图片 / 文件上传（后续扩展）",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isBlank()) return@TextButton
                    onSubmit(
                        Assignment(
                            id =title,
                            title = title,
                            description = content.ifBlank { "无" },
                            dueDate = dueDate.ifBlank { "未设置" },

                        )
                    )
                }
            ) {
                Text("发布作业")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}