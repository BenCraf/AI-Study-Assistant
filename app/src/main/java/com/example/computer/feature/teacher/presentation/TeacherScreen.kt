package com.example.computer.feature.teacher.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.computer.R
import com.example.computer.data.model.Assignment
import com.example.computer.data.repository.AssignmentRepository
import com.example.computer.feature.teacher.presentation.model.TeacherFeatureUiModel

enum class TeacherPage {
    HOME,
    GRADE_LIST,
    COURSE_MANAGEMENT,
    STUDENT_MANAGEMENT,
    GRADE_ENTRY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScreen(
    modifier: Modifier = Modifier,
    viewModel: TeacherViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val assignments by viewModel.assignments.collectAsState()

    var currentPage by remember { mutableStateOf(TeacherPage.HOME) }
    var gradingAssignment by remember { mutableStateOf<Assignment?>(null) }
    var showAssignDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            gradingAssignment != null -> "批改作业"
                            currentPage == TeacherPage.GRADE_LIST -> "选择作业"
                            currentPage == TeacherPage.COURSE_MANAGEMENT -> "课程管理"
                            currentPage == TeacherPage.STUDENT_MANAGEMENT -> "学生管理"
                            currentPage == TeacherPage.GRADE_ENTRY -> "成绩录入"
                            else -> "教师"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            gradingAssignment != null -> gradingAssignment = null
                            currentPage == TeacherPage.GRADE_LIST -> currentPage = TeacherPage.HOME
                            currentPage == TeacherPage.COURSE_MANAGEMENT -> currentPage = TeacherPage.HOME
                            currentPage == TeacherPage.STUDENT_MANAGEMENT -> currentPage = TeacherPage.HOME  // 新增
                            currentPage == TeacherPage.GRADE_ENTRY -> currentPage = TeacherPage.HOME
                            else -> onBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->

        // ======= 页面内容切换 =======
        when {

            // ================== 批改学生作业页面 ==================
            gradingAssignment != null -> {
                GradeHomeworkScreen(
                    assignment = gradingAssignment!!,
                    viewModel = viewModel,
                    onBack = { gradingAssignment = null }
                )
            }

            // ================== 成绩录入页面 ==================
            currentPage == TeacherPage.GRADE_ENTRY -> {
                TeacherGradeEntryScreen(
                    modifier = Modifier.padding(innerPadding),
                    onBack = { currentPage = TeacherPage.HOME }
                )
            }

            // ================== 批改作业：选择作业列表 ==================
            currentPage == TeacherPage.GRADE_LIST -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "选择要批改的作业",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    if (assignments.isEmpty()) {
                        Text("暂无可批改作业")
                    } else {
                        assignments.forEach { assignment ->
                            AssignmentItem(
                                assignment = assignment,
                                onClick = { gradingAssignment = assignment }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            // ================== 课程管理页面 ==================
            currentPage == TeacherPage.COURSE_MANAGEMENT -> {
                TeacherCourseManagementScreen(
                    modifier = Modifier.padding(innerPadding),
                    onBack = { currentPage = TeacherPage.HOME }
                )
            }

            // ================== 学生管理页面 ==================
            currentPage == TeacherPage.STUDENT_MANAGEMENT -> {
                TeacherStudentManagementScreen(
                    modifier = Modifier.padding(innerPadding),
                    onBack = { currentPage = TeacherPage.HOME }
                )
            }

            // ================== 教师首页 ==================
            else -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {

                    Text(
                        text = "教师界面",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(24.dp))

                    val features = listOf(
                        TeacherFeatureUiModel(
                            "course",
                            "课程管理",
                            "管理所教课程",
                            R.drawable.ic_course
                        ),
                        TeacherFeatureUiModel(
                            "assign",
                            "布置作业",
                            "发布新作业",
                            R.drawable.ic_assign
                        ),
                        TeacherFeatureUiModel(
                            "grade",
                            "批改作业",
                            "查看并批改学生作业",
                            R.drawable.ic_grade
                        ),
                        TeacherFeatureUiModel(
                            "student",
                            "学生管理",
                            "管理班级学生",
                            R.drawable.ic_student_t
                        ),
                        TeacherFeatureUiModel(
                            "score",
                            "成绩录入",
                            "录入学生成绩",
                            R.drawable.ic_score
                        )
                    )

                    features.forEach {
                        TeacherFeatureItem(
                            feature = it,
                            onClick = {
                                when (it.id) {
                                    "course" -> {
                                        currentPage = TeacherPage.COURSE_MANAGEMENT
                                    }

                                    "assign" -> showAssignDialog = true

                                    "student" -> {
                                        currentPage = TeacherPage.STUDENT_MANAGEMENT  // 新增
                                    }

                                    "score" -> {
                                        currentPage = TeacherPage.GRADE_ENTRY  // 新增
                                    }

                                    "grade" -> {
                                        if (assignments.isEmpty()) {
                                            Toast.makeText(
                                                context,
                                                "暂无可批改作业",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            currentPage = TeacherPage.GRADE_LIST
                                        }
                                    }

                                    else -> Toast.makeText(
                                        context,
                                        "点击了：${it.title}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // ================== 布置作业弹窗 ==================
    if (showAssignDialog) {
        AssignHomeworkDialog(
            onDismiss = { showAssignDialog = false },
            onSubmit = { assignment ->
                AssignmentRepository.publishAssignment(assignment)
                Toast.makeText(context, "作业已发布", Toast.LENGTH_SHORT).show()
                showAssignDialog = false
            }
        )
    }
}