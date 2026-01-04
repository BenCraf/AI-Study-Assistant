// ParentTeacherChatScreen.kt
package com.example.computer.feature.parent.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==================== 数据模型 ====================

data class TeacherContact(
    val id: String,
    val name: String,
    val subject: String,
    val role: String,
    val avatar: String,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val unreadCount: Int
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: String,
    val isFromParent: Boolean,
    val messageType: MessageType
)

enum class MessageType {
    TEXT,
    SYSTEM,
    NOTIFICATION
}

data class ChatConversation(
    val teacher: TeacherContact,
    val messages: List<ChatMessage>,
    val childName: String
)

// ==================== 数据仓库 ====================

object ParentTeacherChatRepository {

    fun getTeacherContacts(childName: String): List<TeacherContact> {
        return when (childName) {
            "小明" -> getTeachersForXiaoMing()
            "小红" -> getTeachersForXiaoHong()
            "小华" -> getTeachersForXiaoHua()
            else -> getTeachersForXiaoMing()
        }
    }

    private fun getTeachersForXiaoMing(): List<TeacherContact> {
        return listOf(
            TeacherContact(
                id = "t1",
                name = "王老师",
                subject = "语文",
                role = "班主任",
                avatar = "👩‍🏫",
                lastMessage = "小明这次作文写得很好，继续保持！",
                lastMessageTime = "今天 14:30",
                unreadCount = 0
            ),
            TeacherContact(
                id = "t2",
                name = "李老师",
                subject = "数学",
                role = "任课老师",
                avatar = "👨‍🏫",
                lastMessage = "家长您好，小明数学基础很扎实",
                lastMessageTime = "昨天 16:20",
                unreadCount = 0
            ),
            TeacherContact(
                id = "t3",
                name = "张老师",
                subject = "英语",
                role = "任课老师",
                avatar = "👩‍🏫",
                lastMessage = "建议多练习口语",
                lastMessageTime = "12-24 10:15",
                unreadCount = 1
            ),
            TeacherContact(
                id = "t4",
                name = "陈老师",
                subject = "科学",
                role = "任课老师",
                avatar = "👨‍🏫",
                lastMessage = null,
                lastMessageTime = null,
                unreadCount = 0
            )
        )
    }

    private fun getTeachersForXiaoHong(): List<TeacherContact> {
        return listOf(
            TeacherContact(
                id = "t5",
                name = "刘老师",
                subject = "语文",
                role = "班主任",
                avatar = "👨‍🏫",
                lastMessage = "小红最近学习状态不错",
                lastMessageTime = "今天 15:00",
                unreadCount = 2
            ),
            TeacherContact(
                id = "t6",
                name = "周老师",
                subject = "数学",
                role = "任课老师",
                avatar = "👩‍🏫",
                lastMessage = "需要加强计算能力训练",
                lastMessageTime = "昨天 11:30",
                unreadCount = 0
            ),
            TeacherContact(
                id = "t7",
                name = "吴老师",
                subject = "英语",
                role = "任课老师",
                avatar = "👨‍🏫",
                lastMessage = "词汇量有待提高",
                lastMessageTime = "12-23 14:00",
                unreadCount = 0
            )
        )
    }

    private fun getTeachersForXiaoHua(): List<TeacherContact> {
        return listOf(
            TeacherContact(
                id = "t8",
                name = "赵老师",
                subject = "语文",
                role = "班主任",
                avatar = "👩‍🏫",
                lastMessage = "小华表现优秀，值得表扬",
                lastMessageTime = "今天 16:45",
                unreadCount = 0
            ),
            TeacherContact(
                id = "t9",
                name = "孙老师",
                subject = "数学",
                role = "任课老师",
                avatar = "👨‍🏫",
                lastMessage = "建议参加数学竞赛",
                lastMessageTime = "今天 13:20",
                unreadCount = 1
            ),
            TeacherContact(
                id = "t10",
                name = "郑老师",
                subject = "英语",
                role = "任课老师",
                avatar = "👩‍🏫",
                lastMessage = "口语表达能力很强",
                lastMessageTime = "昨天 15:30",
                unreadCount = 0
            ),
            TeacherContact(
                id = "t11",
                name = "钱老师",
                subject = "物理",
                role = "任课老师",
                avatar = "👨‍🏫",
                lastMessage = "实验操作规范",
                lastMessageTime = "12-24 09:00",
                unreadCount = 0
            )
        )
    }

    fun getChatConversation(teacher: TeacherContact, childName: String): ChatConversation {
        val messages = when (teacher.id) {
            "t1" -> getMessagesForWangTeacher()
            "t2" -> getMessagesForLiTeacher()
            "t3" -> getMessagesForZhangTeacher()
            "t5" -> getMessagesForLiuTeacher()
            "t8" -> getMessagesForZhaoTeacher()
            "t9" -> getMessagesForSunTeacher()
            else -> emptyList()
        }

        return ChatConversation(
            teacher = teacher,
            messages = messages,
            childName = childName
        )
    }

    private fun getMessagesForWangTeacher(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "m1",
                senderId = "system",
                senderName = "系统",
                content = "已建立与王老师的沟通渠道",
                timestamp = "2024-12-20 09:00",
                isFromParent = false,
                messageType = MessageType.SYSTEM
            ),
            ChatMessage(
                id = "m2",
                senderId = "parent",
                senderName = "家长",
                content = "王老师您好，想了解一下孩子最近在学校的表现",
                timestamp = "2024-12-20 09:15",
                isFromParent = true,
                messageType = MessageType.TEXT
            ),
            ChatMessage(
                id = "m3",
                senderId = "t1",
                senderName = "王老师",
                content = "家长您好！小明同学最近表现很不错，上课认真听讲，作业完成质量高。",
                timestamp = "2024-12-20 10:30",
                isFromParent = false,
                messageType = MessageType.TEXT
            ),
            ChatMessage(
                id = "m4",
                senderId = "parent",
                senderName = "家长",
                content = "谢谢老师！孩子有什么需要改进的地方吗？",
                timestamp = "2024-12-20 10:35",
                isFromParent = true,
                messageType = MessageType.TEXT
            ),
            ChatMessage(
                id = "m5",
                senderId = "t1",
                senderName = "王老师",
                content = "主要是阅读理解方面可以再加强，建议多读一些课外书籍。另外，小明这次作文写得很好，继续保持！",
                timestamp = "今天 14:30",
                isFromParent = false,
                messageType = MessageType.TEXT
            )
        )
    }

    private fun getMessagesForLiTeacher(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "m6",
                senderId = "parent",
                senderName = "家长",
                content = "李老师好，孩子数学成绩怎么样？",
                timestamp = "昨天 15:00",
                isFromParent = true,
                messageType = MessageType.TEXT
            ),
            ChatMessage(
                id = "m7",
                senderId = "t2",
                senderName = "李老师",
                content = "家长您好，小明数学基础很扎实，解题思路清晰，是班上的数学小能手。",
                timestamp = "昨天 16:20",
                isFromParent = false,
                messageType = MessageType.TEXT
            )
        )
    }

    private fun getMessagesForZhangTeacher(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "m8",
                senderId = "t3",
                senderName = "张老师",
                content = "家长您好，小明英语综合能力不错，建议多练习口语，可以看一些英文动画片。",
                timestamp = "12-24 10:15",
                isFromParent = false,
                messageType = MessageType.TEXT
            )
        )
    }

    private fun getMessagesForLiuTeacher(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "m9",
                senderId = "parent",
                senderName = "家长",
                content = "刘老师您好，小红最近学习状态怎么样？",
                timestamp = "今天 14:30",
                isFromParent = true,
                messageType = MessageType.TEXT
            ),
            ChatMessage(
                id = "m10",
                senderId = "t5",
                senderName = "刘老师",
                content = "小红最近学习状态不错，但需要注意考勤，本月有几次迟到和缺勤。",
                timestamp = "今天 15:00",
                isFromParent = false,
                messageType = MessageType.TEXT
            ),
            ChatMessage(
                id = "m11",
                senderId = "parent",
                senderName = "家长",
                content = "好的老师，我会督促孩子按时到校",
                timestamp = "今天 15:05",
                isFromParent = true,
                messageType = MessageType.TEXT
            )
        )
    }

    private fun getMessagesForZhaoTeacher(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "m12",
                senderId = "t8",
                senderName = "赵老师",
                content = "家长您好，小华在期末考试中表现优异，各科成绩都很优秀，值得表扬！",
                timestamp = "今天 16:45",
                isFromParent = false,
                messageType = MessageType.TEXT
            )
        )
    }

    private fun getMessagesForSunTeacher(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "m13",
                senderId = "t9",
                senderName = "孙老师",
                content = "小华数学天赋出众，建议参加学校的数学竞赛，锻炼一下",
                timestamp = "今天 13:20",
                isFromParent = false,
                messageType = MessageType.TEXT
            )
        )
    }
}

// ==================== 主界面 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentTeacherChatScreen(
    childName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val teachers = remember(childName) {
        ParentTeacherChatRepository.getTeacherContacts(childName)
    }
    var selectedTeacher by remember { mutableStateOf<TeacherContact?>(null) }

    // 如果选中了老师，显示聊天界面
    selectedTeacher?.let { teacher ->
        ChatDetailScreen(
            conversation = remember(teacher) {
                ParentTeacherChatRepository.getChatConversation(teacher, childName)
            },
            onBack = { selectedTeacher = null }
        )
        return
    }

    // 老师列表界面
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("老师沟通")
                        Text(
                            text = "${childName}的任课老师",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "📧 与老师保持良好沟通，及时了解孩子动态",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(teachers) { teacher ->
                TeacherContactCard(
                    teacher = teacher,
                    onClick = { selectedTeacher = teacher }
                )
            }
        }
    }
}

// ==================== 组件 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherContactCard(
    teacher: TeacherContact,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = teacher.avatar,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = teacher.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (teacher.role == "班主任") {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text(
                                text = "班主任",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${teacher.subject}老师",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                teacher.lastMessage?.let { message ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                teacher.lastMessageTime?.let { time ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // 未读消息提示
            if (teacher.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF44336)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = teacher.unreadCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversation: ChatConversation,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = conversation.teacher.avatar,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = conversation.teacher.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${conversation.teacher.subject} · ${conversation.teacher.role}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("输入消息...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledIconButton(
                        onClick = {
                            // TODO: 发送消息逻辑
                            if (messageText.isNotBlank()) {
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank(),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "发送"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(conversation.messages) { message ->
                when (message.messageType) {
                    MessageType.SYSTEM -> SystemMessageBubble(message)
                    else -> {
                        if (message.isFromParent) {
                            ParentMessageBubble(message)
                        } else {
                            TeacherMessageBubble(message, conversation.teacher.avatar)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemMessageBubble(message: ChatMessage) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun TeacherMessageBubble(message: ChatMessage, avatar: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = avatar, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f, fill = false)) {
            Surface(
                shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Text(
                text = message.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun ParentMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Text(
                text = message.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                modifier = Modifier.padding(end = 12.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
