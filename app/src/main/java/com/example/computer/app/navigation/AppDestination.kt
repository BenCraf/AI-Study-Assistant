package com.example.computer.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(
    val label: String,
    val icon: ImageVector
) {
    HOME("首页", Icons.Filled.Home),
    STUDENT("学生", Icons.Filled.Person),
    TEACHER("教师", Icons.Filled.Person),
    PARENT("家长", Icons.Filled.Person)
}