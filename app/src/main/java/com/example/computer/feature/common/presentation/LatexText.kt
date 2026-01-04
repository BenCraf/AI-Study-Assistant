package com.example.computer.feature.common.presentation

import android.graphics.Typeface
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin

@Composable
fun LatexText(
    text: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = modifier,
        factory = {
            TextView(context).apply {
                setTextColor(textColor)
                textSize = 14f
                typeface = Typeface.DEFAULT
            }
        },
        update = { textView ->
            val markwon = Markwon.builder(context)
                .usePlugin(MarkwonInlineParserPlugin.create())  // 添加这个插件
                .usePlugin(JLatexMathPlugin.create(textView.textSize) { builder ->
                    builder.inlinesEnabled(true)
                })
                .build()

            markwon.setMarkdown(textView, text)
        }
    )
}