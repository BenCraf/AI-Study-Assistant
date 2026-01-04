package com.example.computer

import com.example.computer.feature.parent.presentation.ParentScreen
import com.example.computer.feature.teacher.presentation.TeacherScreen
import com.example.computer.feature.student.presentation.StudentScreen
import com.example.computer.app.navigation.AppDestination
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.example.computer.ui.theme.ComputerTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.util.Base64
import java.net.URLEncoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.Image



// IAT 语音听写配置
private const val IAT_HOST = "iat.xf-yun.com"
private const val IAT_PATH = "/v1"
private const val IAT_SCHEMA = "wss"

// 这些从你的讯飞控制台拿
private const val IAT_APP_ID = "0929b2a8"
private const val IAT_API_KEY = "ZDQ1MGJmNzYwNDM1ZGI0M2I3OGVlZDky"
private const val IAT_API_SECRET = "1c16939dc1ad97cc6e67e966221869ec"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComputerTheme {
                ComputerApp()
            }
        }
    }
}


@PreviewScreenSizes
@Composable
fun ComputerApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.HOME) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (currentDestination) {
            AppDestination.HOME -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                onSelectDestination = { dest -> currentDestination = dest }
            )

            AppDestination.STUDENT -> StudentScreen(
                modifier = Modifier.padding(innerPadding),
                onBack = { currentDestination = AppDestination.HOME }
            )

            AppDestination.TEACHER -> TeacherScreen(
                modifier = Modifier.padding(innerPadding),
                onBack = { currentDestination = AppDestination.HOME }
            )

            AppDestination.PARENT -> ParentScreen(
                modifier = Modifier.padding(innerPadding),
                onBack = { currentDestination = AppDestination.HOME }
            )
        }
    }
}
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onSelectDestination: (AppDestination) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {

        // ===== 顶部标题 =====
        Text(
            text = "AI 学习助手",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
//            text = "Smart learning tools for students, teachers and parents.",
            text = "为学生、教师和家长打造的智能学习工具",
            style = MaterialTheme.typography.bodyLarge,

            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ===== Hero 区块 =====
        HomeHeroCard()

        Spacer(modifier = Modifier.height(32.dp))


        // ===== 角色选择标题 =====
        Text(
//            text = "Choose your role",
            text = "选择你的身份",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        RoleEntryCard(
            iconResId = R.drawable.ic_student,
            title = "学生",
            subtitle = "AI 笔记、复习辅助与学习面板",
            chipText = "",
            onClick = { onSelectDestination(AppDestination.STUDENT) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        RoleEntryCard(
            iconResId = R.drawable.ic_teacher,
            title = "教师",
            subtitle = "课程管理、作业布置与成绩评定",
            chipText = "",
            onClick = { onSelectDestination(AppDestination.TEACHER) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        RoleEntryCard(
            iconResId = R.drawable.ic_parent,
            title = "家长",
//            subtitle = "Progress tracking and AI tips",
            subtitle = "学习进度追踪与 AI 建议",
            chipText = "",
            onClick = { onSelectDestination(AppDestination.PARENT) }
        )

        Spacer(modifier = Modifier.height(40.dp)) // ✅ 防止底部空旷 & 手势条重叠
    }
}



// 顶部大卡片
@Composable
private fun HomeHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
//                    text = "Learning made simpler",
                    text = "让学习更简单",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
//                    text = "Record notes, manage tasks and get AI guidance in one place.",
                    text = "记录笔记、管理任务，一站式获取 AI 学习指导",
                    style = MaterialTheme.typography.bodyMedium,

                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier
                        .size(72.dp)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// 小圆角徽章：填补中间那一块空白
@Composable
private fun HomeBadge(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                shape = CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleEntryCard(
    iconResId: Int,          // ✅ 用资源 id
    title: String,
    subtitle: String,
    chipText: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ==== 左侧圆形图标（使用你自己的图片） ====
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,   // ✅ 字变大
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }

            TextButton(onClick = onClick) {
                Text(
                    text = chipText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ComputerTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun StudentScreenPreview() {
    ComputerTheme {
        StudentScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherScreenPreview() {
    ComputerTheme {
        TeacherScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ParentScreenPreview() {
    ComputerTheme {
        ParentScreen()
    }
}

