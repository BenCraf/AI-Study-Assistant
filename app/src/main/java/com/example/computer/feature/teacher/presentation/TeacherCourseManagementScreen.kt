package com.example.computer.feature.teacher.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.computer.data.model.Course
import java.util.UUID

@Composable
fun TeacherCourseManagementScreen(
    modifier: Modifier = Modifier,
    viewModel: TeacherCourseViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val courses by viewModel.courses.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCourse by remember { mutableStateOf<Course?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Course?>(null) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }

    // 移除了 Scaffold，直接使用 Box 来管理 FAB
    Box(modifier = modifier.fillMaxSize()) {
        if (selectedCourse != null) {
            // 课程详情页面
            CourseDetailScreen(
                course = selectedCourse!!,
                modifier = Modifier.fillMaxSize(),
                onEdit = {
                    editingCourse = selectedCourse
                },
                onDelete = {
                    showDeleteDialog = selectedCourse
                }
            )
        } else {
            // 课程列表
            if (courses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "暂无课程",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "点击右下角按钮添加课程",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(courses) { course ->
                        CourseCard(
                            course = course,
                            onClick = { selectedCourse = course },
                            onEdit = { editingCourse = course },
                            onDelete = { showDeleteDialog = course }
                        )
                    }
                }
            }
        }

        // FAB 悬浮按钮
        if (selectedCourse == null) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加课程")
            }
        }
    }

    // 添加/编辑课程对话框
    if (showAddDialog || editingCourse != null) {
        CourseEditDialog(
            course = editingCourse,
            onDismiss = {
                showAddDialog = false
                editingCourse = null
            },
            onConfirm = { course ->
                if (editingCourse != null) {
                    viewModel.updateCourse(course)
                    Toast.makeText(context, "课程已更新", Toast.LENGTH_SHORT).show()
                    if (selectedCourse?.id == course.id) {
                        selectedCourse = course
                    }
                } else {
                    viewModel.addCourse(course)
                    Toast.makeText(context, "课程已添加", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
                editingCourse = null
            }
        )
    }

    // 删除确认对话框
    showDeleteDialog?.let { course ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除课程") },
            text = { Text("确定要删除课程《${course.name}》吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCourse(course.id)
                        Toast.makeText(context, "课程已删除", Toast.LENGTH_SHORT).show()
                        if (selectedCourse?.id == course.id) {
                            selectedCourse = null
                        }
                        showDeleteDialog = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = course.code,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CourseInfoChip(
                    icon = Icons.Default.Schedule,
                    text = "${course.credits}学分"
                )
                CourseInfoChip(
                    icon = Icons.Default.Person,
                    text = "${course.studentCount}人"
                )
            }

            if (course.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun CourseInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CourseDetailScreen(
    course: Course,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "课程代码: ${course.code}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "课程信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                CourseDetailRow("学分", "${course.credits}")
                Spacer(Modifier.height(8.dp))
                CourseDetailRow("学生人数", "${course.studentCount}")
                Spacer(Modifier.height(8.dp))
                CourseDetailRow("学期", course.semester)

                if (course.description.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "课程描述",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = course.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("编辑")
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("删除")
            }
        }
    }
}

@Composable
private fun CourseDetailRow(
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseEditDialog(
    course: Course?,
    onDismiss: () -> Unit,
    onConfirm: (Course) -> Unit
) {
    var name by remember { mutableStateOf(course?.name ?: "") }
    var code by remember { mutableStateOf(course?.code ?: "") }
    var credits by remember { mutableStateOf(course?.credits?.toString() ?: "") }
    var semester by remember { mutableStateOf(course?.semester ?: "") }
    var description by remember { mutableStateOf(course?.description ?: "") }

    val isValid = name.isNotBlank() && code.isNotBlank() &&
            credits.toIntOrNull() != null && semester.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (course != null) "编辑课程" else "添加课程",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("课程名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("课程代码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = credits,
                        onValueChange = { credits = it },
                        label = { Text("学分") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = semester,
                        onValueChange = { semester = it },
                        label = { Text("学期") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("如: 2024秋") }
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("课程描述") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newCourse = Course(
                                id = course?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                code = code.trim(),
                                credits = credits.toInt(),
                                semester = semester.trim(),
                                description = description.trim(),
                                teacherId = course?.teacherId ?: "current_teacher_id",
                                studentCount = course?.studentCount ?: 0
                            )
                            onConfirm(newCourse)
                        },
                        enabled = isValid
                    ) {
                        Text(if (course != null) "保存" else "添加")
                    }
                }
            }
        }
    }
}

// ViewModel
class TeacherCourseViewModel : androidx.lifecycle.ViewModel() {
    val courses = com.example.computer.data.repository.CourseRepository.courses

    fun addCourse(course: Course) {
        com.example.computer.data.repository.CourseRepository.addCourse(course)
    }

    fun updateCourse(course: Course) {
        com.example.computer.data.repository.CourseRepository.updateCourse(course)
    }

    fun deleteCourse(courseId: String) {
        com.example.computer.data.repository.CourseRepository.deleteCourse(courseId)
    }
}