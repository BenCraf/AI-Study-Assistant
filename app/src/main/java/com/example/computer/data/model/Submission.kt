package com.example.computer.data.model

data class Submission(
    val id: String,
    val assignmentId: String,
    val studentId: String,
    val studentName: String,
    val content: String,                 // 学生文字作业
    val attachments: List<String> = emptyList(), // 图片路径（以后用）
    val imageUris: List<String> = emptyList(),
    val submitTime: Long = System.currentTimeMillis(),

    // ===== 教师批改字段 =====
    val score: Int? = null,
    val comment: String? = null,
    val status: SubmissionStatus = SubmissionStatus.SUBMITTED
)

enum class SubmissionStatus {
    SUBMITTED,   // 已提交，未批改
    GRADED       // 已批改
}