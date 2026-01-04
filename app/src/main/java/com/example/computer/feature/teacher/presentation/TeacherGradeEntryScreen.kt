package com.example.computer.feature.teacher.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.computer.data.model.Course
import com.example.computer.data.model.GradeEntry
import com.example.computer.data.model.Student
import com.example.computer.data.repository.CourseRepository
import com.example.computer.data.repository.StudentRepository
import com.example.computer.data.repository.GradeRepository

@Composable
fun TeacherGradeEntryScreen(
    modifier: Modifier = Modifier,
    viewModel: TeacherGradeEntryViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val courses by CourseRepository.courses.collectAsState()
    val students by StudentRepository.students.collectAsState()
    val grades by viewModel.grades.collectAsState()

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var showCourseSelector by remember { mutableStateOf(false) }
    var editingGrade by remember { mutableStateOf<GradeEntry?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // 过滤学生列表
    val filteredStudents = remember(students, searchQuery, selectedCourse) {
        if (selectedCourse == null) return@remember emptyList()

        val baseList = students
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.studentId.contains(searchQuery, ignoreCase = true) ||
                        it.className.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // 课程选择卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = { showCourseSelector = true }
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
                        text = "选择课程",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = selectedCourse?.name ?: "请选择要录入成绩的课程",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (selectedCourse != null) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedCourse != null)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedCourse != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "课程代码: ${selectedCourse?.code}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (selectedCourse != null) {
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("搜索学生姓名、学号或班级") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            // 统计信息
            val courseGrades = grades.filter { it.courseId == selectedCourse?.id }
            val gradedCount = courseGrades.size
            val totalCount = students.size
            val averageScore = if (courseGrades.isNotEmpty()) {
                courseGrades.mapNotNull { it.score }.average()
            } else 0.0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GradeStatItem(
                        label = "已录入",
                        value = "$gradedCount / $totalCount"
                    )
                    GradeStatItem(
                        label = "平均分",
                        value = if (courseGrades.isNotEmpty())
                            String.format("%.1f", averageScore)
                        else "—"
                    )
                    GradeStatItem(
                        label = "及格率",
                        value = if (courseGrades.isNotEmpty()) {
                            val passCount = courseGrades.count { (it.score ?: 0) >= 60 }
                            String.format("%.1f%%", passCount * 100.0 / courseGrades.size)
                        } else "—"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 学生成绩列表
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "未找到匹配的学生",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredStudents) { student ->
                        val grade = grades.find {
                            it.studentId == student.id && it.courseId == selectedCourse?.id
                        }

                        StudentGradeCard(
                            student = student,
                            grade = grade,
                            onEdit = {
                                editingGrade = grade ?: GradeEntry(
                                    id = "",
                                    studentId = student.id,
                                    courseId = selectedCourse?.id ?: "",
                                    score = null,
                                    remarks = ""
                                )
                            }
                        )
                    }
                }
            }
        } else {
            // 未选择课程时的提示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "请先选择课程",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "点击上方卡片选择要录入成绩的课程",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }

    // 课程选择对话框
    if (showCourseSelector) {
        CourseSelectionDialog(
            courses = courses,
            onDismiss = { showCourseSelector = false },
            onSelect = { course ->
                selectedCourse = course
                showCourseSelector = false
                searchQuery = ""
            }
        )
    }

    // 成绩录入/编辑对话框
    editingGrade?.let { grade ->
        GradeEditDialog(
            student = students.find { it.id == grade.studentId },
            course = selectedCourse,
            grade = grade,
            onDismiss = { editingGrade = null },
            onConfirm = { updatedGrade ->
                viewModel.saveGrade(updatedGrade)
                Toast.makeText(
                    context,
                    if (grade.id.isEmpty()) "成绩已录入" else "成绩已更新",
                    Toast.LENGTH_SHORT
                ).show()
                editingGrade = null
            }
        )
    }
}

@Composable
private fun GradeStatItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun StudentGradeCard(
    student: Student,
    grade: GradeEntry?,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 头像
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = if (grade?.score != null) {
                        when {
                            grade.score!! >= 90 -> MaterialTheme.colorScheme.primaryContainer
                            grade.score!! >= 60 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = student.name.take(1),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "学号: ${student.studentId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = student.className,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 成绩显示
            if (grade?.score != null) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = when {
                        grade.score!! >= 90 -> MaterialTheme.colorScheme.primaryContainer
                        grade.score!! >= 60 -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${grade.score}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "分",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                OutlinedButton(onClick = onEdit) {
                    Text("录入")
                }
            }
        }
    }
}

@Composable
private fun CourseSelectionDialog(
    courses: List<Course>,
    onDismiss: () -> Unit,
    onSelect: (Course) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "选择课程",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                if (courses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无课程",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(courses) { course ->
                            Card(
                                onClick = { onSelect(course) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = course.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "代码: ${course.code}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "学分: ${course.credits}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradeEditDialog(
    student: Student?,
    course: Course?,
    grade: GradeEntry,
    onDismiss: () -> Unit,
    onConfirm: (GradeEntry) -> Unit
) {
    var score by remember { mutableStateOf(grade.score?.toString() ?: "") }
    var remarks by remember { mutableStateOf(grade.remarks) }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "录入成绩",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                // 学生信息
                Card(
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
                            text = student?.name ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "学号: ${student?.studentId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "课程: ${course?.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 分数输入
                OutlinedTextField(
                    value = score,
                    onValueChange = {
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            score = it
                            showError = false
                        }
                    },
                    label = { Text("分数 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = showError,
                    supportingText = {
                        if (showError) {
                            Text("请输入0-100之间的分数")
                        } else {
                            Text("请输入0-100之间的整数")
                        }
                    },
                    trailingIcon = {
                        if (score.isNotEmpty()) {
                            val scoreInt = score.toIntOrNull()
                            if (scoreInt != null && scoreInt in 0..100) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))

                // 备注输入
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    placeholder = { Text("选填，如：平时成绩、考勤情况等") }
                )

                Spacer(Modifier.height(16.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val scoreInt = score.toIntOrNull()
                            if (scoreInt == null || scoreInt !in 0..100) {
                                showError = true
                                return@Button
                            }

                            onConfirm(
                                grade.copy(
                                    id = if (grade.id.isEmpty())
                                        java.util.UUID.randomUUID().toString()
                                    else grade.id,
                                    score = scoreInt,
                                    remarks = remarks.trim()
                                )
                            )
                        }
                    ) {
                        Text(if (grade.id.isEmpty()) "录入" else "保存")
                    }
                }
            }
        }
    }
}

// ViewModel
class TeacherGradeEntryViewModel : ViewModel() {
    val grades = GradeRepository.grades

    fun saveGrade(grade: GradeEntry) {
        GradeRepository.saveGrade(grade)
    }
}