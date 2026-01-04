package com.example.computer.feature.student.presentation

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.newapp.presentation.student.NoteAssistantViewModel
import com.example.newapp.presentation.student.MindMapData
import kotlin.math.max

// 数据结构 - 增加尺寸信息
data class MindMapNode(
    val id: String,
    val label: String,
    val children: List<MindMapNode> = emptyList(),
    var position: Offset = Offset.Zero,  // 矩形左上角位置
    var size: Size = Size.Zero  // 矩形大小
)

// 树形布局配置
private object TreeLayoutConfig {
    const val HORIZONTAL_SPACING = 250f  // 层级之间的水平间距
    const val VERTICAL_SPACING = 40f     // 同层节点之间的垂直间距
    const val NODE_PADDING_HORIZONTAL = 24f  // 节点内部水平padding
    const val NODE_PADDING_VERTICAL = 16f    // 节点内部垂直padding
    const val MIN_NODE_WIDTH = 160f      // 最小节点宽度
    const val MIN_NODE_HEIGHT = 60f      // 最小节点高度
    const val CORNER_RADIUS = 12f        // 圆角半径
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindMapDetailScreen(
    mindMapData: MindMapData,
    onNavigateBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // 缩放、平移状态
    var scale by remember { mutableStateOf(0.8f) }  // 初始缩放稍小，便于查看全局
    var offset by remember { mutableStateOf(Offset(100f, 100f)) }  // 初始偏移

    // 计算节点布局
    LaunchedEffect(mindMapData.rootNode) {
        android.util.Log.d("MindMap", "开始计算树形布局")
        calculateTreeLayout(mindMapData.rootNode)
        android.util.Log.d("MindMap", "布局计算完成")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mindMapData.title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    // 重置视图
                    IconButton(onClick = {
                        scale = 0.8f
                        offset = Offset(100f, 100f)
                    }) {
                        Icon(Icons.Default.CenterFocusStrong, "重置")
                    }
                    // 放大
                    IconButton(onClick = {
                        scale = (scale * 1.2f).coerceAtMost(2f)
                    }) {
                        Icon(Icons.Default.ZoomIn, "放大")
                    }
                    // 缩小
                    IconButton(onClick = {
                        scale = (scale * 0.8f).coerceAtLeast(0.3f)
                    }) {
                        Icon(Icons.Default.ZoomOut, "缩小")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.3f, 2f)
                            offset += pan
                        }
                    }
            ) {
                drawIntoCanvas { canvas ->
                    canvas.save()
                    canvas.translate(offset.x, offset.y)
                    canvas.scale(scale, scale)

                    // 绘制树形思维导图
                    drawTreeNode(
                        canvas = canvas,
                        node = mindMapData.rootNode,
                        level = 0
                    )

                    canvas.restore()
                }
            }

            // 操作提示
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = "缩放: ${String.format("%.0f", scale * 100)}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = "双指缩放 | 拖动平移",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * 计算树形布局（从左到右，从上到下）
 */
private fun calculateTreeLayout(
    node: MindMapNode,
    x: Float = 50f,  // 起始X坐标
    y: Float = 50f,  // 起始Y坐标
    level: Int = 0
): Float {
    // 1. 计算当前节点的尺寸
    val textPaint = android.graphics.Paint().apply {
        textSize = getTextSize(level)
        isAntiAlias = true
    }

    val lines = splitTextToLines(node.label, getMaxTextWidth(level))
    val textWidth = lines.maxOfOrNull { textPaint.measureText(it) } ?: 0f
    val textHeight = lines.size * textPaint.textSize * 1.3f

    node.size = Size(
        width = max(
            textWidth + TreeLayoutConfig.NODE_PADDING_HORIZONTAL * 2,
            TreeLayoutConfig.MIN_NODE_WIDTH
        ),
        height = max(
            textHeight + TreeLayoutConfig.NODE_PADDING_VERTICAL * 2,
            TreeLayoutConfig.MIN_NODE_HEIGHT
        )
    )

    // 2. 如果有子节点，递归计算子节点位置
    if (node.children.isNotEmpty()) {
        val childX = x + node.size.width + TreeLayoutConfig.HORIZONTAL_SPACING
        var currentY = y

        // 计算所有子节点的总高度
        val childrenHeights = mutableListOf<Float>()
        node.children.forEach { child ->
            val childHeight = calculateTreeLayout(child, childX, currentY, level + 1)
            childrenHeights.add(childHeight)
            currentY += childHeight + TreeLayoutConfig.VERTICAL_SPACING
        }

        val totalChildrenHeight = childrenHeights.sum() +
                TreeLayoutConfig.VERTICAL_SPACING * (node.children.size - 1)

        // 3. 根据子节点总高度，居中对齐当前节点
        node.position = Offset(
            x = x,
            y = y + (totalChildrenHeight - node.size.height) / 2
        )

        return totalChildrenHeight
    } else {
        // 叶子节点
        node.position = Offset(x, y)
        return node.size.height
    }
}

/**
 * 绘制树形节点
 */
private fun DrawScope.drawTreeNode(
    canvas: androidx.compose.ui.graphics.Canvas,
    node: MindMapNode,
    level: Int
) {
    val nodePos = node.position
    val nodeSize = node.size

    // 1. 绘制连接线（连接到子节点）
    if (node.children.isNotEmpty()) {
        val lineColor = getLineColor(level)
        val startX = nodePos.x + nodeSize.width
        val startY = nodePos.y + nodeSize.height / 2

        node.children.forEach { child ->
            val childPos = child.position
            val childSize = child.size
            val endX = childPos.x
            val endY = childPos.y + childSize.height / 2

            // 绘制折线（先水平，再垂直，再水平）
            val midX = startX + TreeLayoutConfig.HORIZONTAL_SPACING / 2

            // 水平线1
            drawLine(
                color = lineColor,
                start = Offset(startX, startY),
                end = Offset(midX, startY),
                strokeWidth = 2f
            )

            // 垂直线
            drawLine(
                color = lineColor,
                start = Offset(midX, startY),
                end = Offset(midX, endY),
                strokeWidth = 2f
            )

            // 水平线2
            drawLine(
                color = lineColor,
                start = Offset(midX, endY),
                end = Offset(endX, endY),
                strokeWidth = 2f
            )
        }
    }

    // 2. 绘制节点矩形
    val bgColor = getNodeColor(level)
    val borderColor = getBorderColor(level)

    // 绘制阴影
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.1f),
        topLeft = Offset(nodePos.x + 3, nodePos.y + 3),
        size = nodeSize,
        cornerRadius = CornerRadius(TreeLayoutConfig.CORNER_RADIUS)
    )

    // 绘制背景
    drawRoundRect(
        color = bgColor,
        topLeft = nodePos,
        size = nodeSize,
        cornerRadius = CornerRadius(TreeLayoutConfig.CORNER_RADIUS)
    )

    // 绘制边框
    drawRoundRect(
        color = borderColor,
        topLeft = nodePos,
        size = nodeSize,
        cornerRadius = CornerRadius(TreeLayoutConfig.CORNER_RADIUS),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
    )

    // 3. 绘制文字
    canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = getTextColor(level)
            textSize = getTextSize(level)
            isAntiAlias = true
            isFakeBoldText = level == 0
        }

        val lines = splitTextToLines(node.label, getMaxTextWidth(level))
        val lineHeight = paint.textSize * 1.3f
        val totalHeight = lines.size * lineHeight
        var currentY = nodePos.y + (nodeSize.height - totalHeight) / 2 + paint.textSize

        lines.forEach { line ->
            val textWidth = paint.measureText(line)
            val textX = nodePos.x + (nodeSize.width - textWidth) / 2
            drawText(line, textX, currentY, paint)
            currentY += lineHeight
        }
    }

    // 4. 递归绘制子节点
    node.children.forEach { child ->
        drawTreeNode(canvas, child, level + 1)
    }
}

/**
 * 根据层级获取节点颜色
 */
private fun getNodeColor(level: Int): Color = when (level) {
    0 -> Color(0xFFE3F2FD)  // 浅蓝色 - 根节点
    1 -> Color(0xFFF1F8E9)  // 浅绿色 - 一级节点
    2 -> Color(0xFFFFF3E0)  // 浅橙色 - 二级节点
    else -> Color(0xFFF5F5F5)  // 浅灰色
}

/**
 * 根据层级获取边框颜色
 */
private fun getBorderColor(level: Int): Color = when (level) {
    0 -> Color(0xFF1976D2)  // 深蓝色
    1 -> Color(0xFF388E3C)  // 深绿色
    2 -> Color(0xFFF57C00)  // 深橙色
    else -> Color(0xFF757575)  // 深灰色
}

/**
 * 根据层级获取连线颜色
 */
private fun getLineColor(level: Int): Color = when (level) {
    0 -> Color(0xFF42A5F5)  // 蓝色
    1 -> Color(0xFF66BB6A)  // 绿色
    2 -> Color(0xFFFF9800)  // 橙色
    else -> Color(0xFF9E9E9E)  // 灰色
}

/**
 * 根据层级获取文字颜色
 */
private fun getTextColor(level: Int): Int = when (level) {
    0 -> android.graphics.Color.rgb(13, 71, 161)  // 深蓝色
    1 -> android.graphics.Color.rgb(27, 94, 32)   // 深绿色
    2 -> android.graphics.Color.rgb(230, 81, 0)   // 深橙色
    else -> android.graphics.Color.rgb(66, 66, 66) // 深灰色
}

/**
 * 根据层级获取文字大小
 */
private fun getTextSize(level: Int): Float = when (level) {
    0 -> 36f
    1 -> 30f
    2 -> 26f
    else -> 22f
}

/**
 * 根据层级获取最大文字宽度
 */
private fun getMaxTextWidth(level: Int): Float = when (level) {
    0 -> 200f
    1 -> 160f
    2 -> 140f
    else -> 120f
}

/**
 * 智能文字分行
 */
private fun splitTextToLines(text: String, maxWidth: Float): List<String> {
    if (text.isEmpty()) return listOf("")

    val lines = mutableListOf<String>()
    var currentLine = ""

    val paint = android.graphics.Paint().apply {
        textSize = 30f  // 使用平均字体大小计算
    }

    text.forEach { char ->
        val testLine = currentLine + char
        val width = paint.measureText(testLine)

        if (width > maxWidth && currentLine.isNotEmpty()) {
            lines.add(currentLine)
            currentLine = char.toString()
        } else {
            currentLine = testLine
        }
    }

    if (currentLine.isNotEmpty()) {
        lines.add(currentLine)
    }

    // 限制最多3行，超出部分用省略号
    return if (lines.size > 3) {
        lines.take(2) + listOf(lines.drop(2).joinToString("").take(10) + "...")
    } else {
        lines
    }
}

