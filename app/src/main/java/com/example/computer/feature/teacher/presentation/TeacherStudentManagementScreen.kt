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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.computer.data.model.Student
import com.example.computer.data.repository.StudentRepository
import java.util.UUID

@Composable
fun TeacherStudentManagementScreen(
    modifier: Modifier = Modifier,
    viewModel: TeacherStudentViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Student?>(null) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // 过滤学生列表
    val filteredStudents = remember(students, searchQuery) {
        if (searchQuery.isBlank()) {
            students
        } else {
            students.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.studentId.contains(searchQuery, ignoreCase = true) ||
                        it.className.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 搜索框
            if (selectedStudent == null) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
            }

            if (selectedStudent != null) {
                // 学生详情页面
                StudentDetailScreen(
                    student = selectedStudent!!,
                    onEdit = {
                        editingStudent = selectedStudent
                    },
                    onDelete = {
                        showDeleteDialog = selectedStudent
                    }
                )
            } else {
                // 学生列表
                if (filteredStudents.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "未找到匹配的学生" else "暂无学生",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (searchQuery.isEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "点击右下角按钮添加学生",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 统计信息卡片
                        item {
                            StudentStatisticsCard(
                                totalCount = students.size,
                                filteredCount = filteredStudents.size,
                                isFiltered = searchQuery.isNotEmpty()
                            )
                        }

                        items(filteredStudents) { student ->
                            StudentCard(
                                student = student,
                                onClick = { selectedStudent = student },
                                onEdit = { editingStudent = student },
                                onDelete = { showDeleteDialog = student }
                            )
                        }
                    }
                }
            }
        }

        // FAB 悬浮按钮
        if (selectedStudent == null) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加学生")
            }
        }
    }

    // 添加/编辑学生对话框
    if (showAddDialog || editingStudent != null) {
        StudentEditDialog(
            student = editingStudent,
            onDismiss = {
                showAddDialog = false
                editingStudent = null
            },
            onConfirm = { student ->
                if (editingStudent != null) {
                    viewModel.updateStudent(student)
                    Toast.makeText(context, "学生信息已更新", Toast.LENGTH_SHORT).show()
                    if (selectedStudent?.id == student.id) {
                        selectedStudent = student
                    }
                } else {
                    viewModel.addStudent(student)
                    Toast.makeText(context, "学生已添加", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
                editingStudent = null
            }
        )
    }

    // 删除确认对话框
    showDeleteDialog?.let { student ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除学生") },
            text = { Text("确定要删除学生《${student.name}》(学号: ${student.studentId})吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteStudent(student.id)
                        Toast.makeText(context, "学生已删除", Toast.LENGTH_SHORT).show()
                        if (selectedStudent?.id == student.id) {
                            selectedStudent = null
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
private fun StudentStatisticsCard(
    totalCount: Int,
    filteredCount: Int,
    isFiltered: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Column {
                    Text(
                        text = if (isFiltered) "搜索结果" else "学生总数",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (isFiltered) "$filteredCount / $totalCount 人" else "$totalCount 人",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentCard(
    student: Student,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 头像
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.name.take(1),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StudentInfoChip(
                    icon = Icons.Default.School,
                    text = student.className
                )
                if (student.email.isNotBlank()) {
                    StudentInfoChip(
                        icon = Icons.Default.Email,
                        text = student.email
                    )
                }
            }

            if (student.phone.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                StudentInfoChip(
                    icon = Icons.Default.Phone,
                    text = student.phone
                )
            }
        }
    }
}

@Composable
private fun StudentInfoChip(
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
private fun StudentDetailScreen(
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 头部卡片
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.take(1),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = student.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "学号: ${student.studentId}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 详细信息卡片
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "详细信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                StudentDetailRow(
                    icon = Icons.Default.School,
                    label = "班级",
                    value = student.className
                )

                if (student.email.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    StudentDetailRow(
                        icon = Icons.Default.Email,
                        label = "邮箱",
                        value = student.email
                    )
                }

                if (student.phone.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    StudentDetailRow(
                        icon = Icons.Default.Phone,
                        label = "电话",
                        value = student.phone
                    )
                }

                Spacer(Modifier.height(8.dp))
                StudentDetailRow(
                    icon = Icons.Default.Person,
                    label = "性别",
                    value = student.gender
                )

                if (student.major.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    StudentDetailRow(
                        icon = Icons.Default.MenuBook,
                        label = "专业",
                        value = student.major
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 操作按钮
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
private fun StudentDetailRow(
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
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentEditDialog(
    student: Student?,
    onDismiss: () -> Unit,
    onConfirm: (Student) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var studentId by remember { mutableStateOf(student?.studentId ?: "") }
    var className by remember { mutableStateOf(student?.className ?: "") }
    var email by remember { mutableStateOf(student?.email ?: "") }
    var phone by remember { mutableStateOf(student?.phone ?: "") }
    var gender by remember { mutableStateOf(student?.gender ?: "男") }
    var major by remember { mutableStateOf(student?.major ?: "") }

    var showGenderMenu by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank() && studentId.isNotBlank() && className.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (student != null) "编辑学生" else "添加学生",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("姓名 *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            }
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = studentId,
                            onValueChange = { studentId = it },
                            label = { Text("学号 *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null)
                            }
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = className,
                            onValueChange = { className = it },
                            label = { Text("班级 *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.School, contentDescription = null)
                            }
                        )
                    }

                    item {
                        Box {
                            OutlinedTextField(
                                value = gender,
                                onValueChange = {},
                                label = { Text("性别") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showGenderMenu = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                }
                            )

                            DropdownMenu(
                                expanded = showGenderMenu,
                                onDismissRequest = { showGenderMenu = false }
                            ) {
                                listOf("男", "女").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            gender = option
                                            showGenderMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = major,
                            onValueChange = { major = it },
                            label = { Text("专业") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.MenuBook, contentDescription = null)
                            }
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("邮箱") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            }
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("电话") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null)
                            }
                        )
                    }
                }

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
                            val newStudent = Student(
                                id = student?.id ?: UUID.randomUUID().toString(),
                                studentId = studentId.trim(),
                                name = name.trim(),
                                className = className.trim(),
                                email = email.trim(),
                                phone = phone.trim(),
                                gender = gender,
                                major = major.trim()
                            )
                            onConfirm(newStudent)
                        },
                        enabled = isValid
                    ) {
                        Text(if (student != null) "保存" else "添加")
                    }
                }
            }
        }
    }
}

// ViewModel
class TeacherStudentViewModel : ViewModel() {
    val students = StudentRepository.students

    fun addStudent(student: Student) {
        StudentRepository.addStudent(student)
    }

    fun updateStudent(student: Student) {
        StudentRepository.updateStudent(student)
    }

    fun deleteStudent(studentId: String) {
        StudentRepository.deleteStudent(studentId)
    }
}