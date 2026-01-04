package com.example.computer.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import android.util.Log
import org.json.JSONObject

private const val TAG = "🧮KaTeXView"

@Composable
fun KaTeXMarkdownView(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface

    Log.e(TAG, "========================================")
    Log.e(TAG, "📝 渲染内容长度: ${markdown.length}")
    Log.e(TAG, "📝 前200字符: ${markdown.take(200)}")

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor),
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.e(TAG, "✅ WebView 页面加载完成")
                    }
                }
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = false
                    builtInZoomControls = false
                }
                setBackgroundColor(Color.Transparent.toArgb())
            }
        },
        update = { webView ->
            val html = generateKaTeXHTML(markdown, backgroundColor, textColor)

            Log.e(TAG, "📄 生成的 HTML 长度: ${html.length}")

            webView.loadDataWithBaseURL(
                "https://katex.org/",  // 设置 baseURL 以支持 CORS
                html,
                "text/html",
                "UTF-8",
                null
            )
        }
    )
}

/**
 * ✅ 使用 JSON 安全传递数据，避免转义问题
 */
private fun generateKaTeXHTML(
    markdown: String,
    bgColor: Color,
    textColor: Color
): String {
    val bgHex = String.format("#%06X", 0xFFFFFF and bgColor.toArgb())
    val textHex = String.format("#%06X", 0xFFFFFF and textColor.toArgb())

    val jsonData = JSONObject().apply {
        put("markdown", markdown)
    }.toString()

    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css" crossorigin="anonymous">
    
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            background-color: $bgHex;
            color: $textHex;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            font-size: 15px;
            line-height: 1.7;
            padding: 12px;
            overflow-x: hidden;
        }
        
        h1, h2, h3, h4, h5, h6 { 
            color: $textHex; 
            margin: 16px 0 10px 0;
            font-weight: 600;
        }
        
        h3 {
            font-size: 1.15em;
            border-bottom: 2px solid ${textHex}25;
            padding-bottom: 6px;
        }
        
        p { 
            margin: 10px 0; 
            word-wrap: break-word;
        }
        
        ul, ol { 
            margin: 10px 0; 
            padding-left: 20px;
        }
        
        li { 
            margin: 6px 0;
        }
        
        .katex-display {
            margin: 18px 0 !important;
            padding: 10px 0;
            overflow-x: auto;
            overflow-y: hidden;
        }
        
        .katex-display > .katex {
            text-align: center;
            display: inline-block;
        }
        
        .katex {
            font-size: 1.08em;
        }
        
        code {
            background-color: ${textHex}12;
            padding: 2px 5px;
            border-radius: 3px;
            font-family: 'Courier New', monospace;
            font-size: 0.92em;
        }
        
        pre {
            background-color: ${textHex}10;
            padding: 10px;
            border-radius: 5px;
            overflow-x: auto;
            margin: 10px 0;
        }
        
        pre code {
            background: none;
            padding: 0;
        }
        
        strong { 
            font-weight: 600; 
        }
        
        .error {
            background-color: #ffebee;
            color: #c62828;
            padding: 12px;
            border-radius: 5px;
            margin: 10px 0;
            border-left: 4px solid #c62828;
        }
    </style>
</head>
<body>
    <div id="content">加载中...</div>
    
    <script src="https://cdn.jsdelivr.net/npm/marked@11.1.1/marked.min.js" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js" crossorigin="anonymous"></script>
    
    <script>
        (function() {
            try {
                const jsonData = $jsonData;
                let markdownText = jsonData.markdown;
                
                console.log('📄 原始文本长度:', markdownText.length);
                
                // ✅ 步骤1: 提取并保护所有公式
                const mathPlaceholders = [];
                let placeholderIndex = 0;
                
                // 提取块级公式 \$\$...\$\$
                markdownText = markdownText.replace(/\$\$([^$]+?)\$\$/g, function(match, formula) {
                    const placeholder = `MATH_BLOCK_${'$'}{placeholderIndex}`;
                    mathPlaceholders.push({
                        placeholder: placeholder,
                        formula: formula.trim(),
                        isBlock: true
                    });
                    placeholderIndex++;
                    return placeholder;
                });
                
                // 提取行内公式 \$...\$
                markdownText = markdownText.replace(/\$([^$\n]+?)\$/g, function(match, formula) {
                    const placeholder = `MATH_INLINE_${'$'}{placeholderIndex}`;
                    mathPlaceholders.push({
                        placeholder: placeholder,
                        formula: formula.trim(),
                        isBlock: false
                    });
                    placeholderIndex++;
                    return placeholder;
                });
                
                console.log('💡 提取公式数量:', mathPlaceholders.length);
                
                // ✅ 步骤2: 转换 Markdown 为 HTML
                if (typeof marked !== 'undefined') {
                    marked.setOptions({
                        breaks: true,
                        gfm: true,
                        headerIds: false,
                        mangle: false
                    });
                    
                    let htmlContent = marked.parse(markdownText);
                    
                    // ✅ 步骤3: 渲染公式并替换占位符
                    mathPlaceholders.forEach(item => {
                        try {
                            const renderedMath = katex.renderToString(item.formula, {
                                displayMode: item.isBlock,
                                throwOnError: false,
                                strict: false,
                                trust: true,
                                output: 'html'
                            });
                            
                            // 清理占位符周围的 <br> 标签（仅对块级公式）
                            if (item.isBlock) {
                                const pattern = new RegExp(
                                    `(<br\\s*/?>\s*)*${'$'}{item.placeholder}(\s*<br\\s*/?>)*`,
                                    'g'
                                );
                                htmlContent = htmlContent.replace(pattern, renderedMath);
                            } else {
                                htmlContent = htmlContent.replace(
                                    new RegExp(item.placeholder, 'g'),
                                    renderedMath
                                );
                            }
                            
                            console.log('✅ 渲染公式:', item.formula.substring(0, 30));
                        } catch (e) {
                            console.error('❌ 公式渲染失败:', item.formula, e);
                            htmlContent = htmlContent.replace(
                                new RegExp(item.placeholder, 'g'),
                                `<span class="error">公式错误: ${'$'}{item.formula}</span>`
                            );
                        }
                    });
                    
                    // ✅ 步骤4: 清理多余的空行
                    htmlContent = htmlContent.replace(/(<br\s*\/?>\s*){3,}/g, '<br><br>');
                    
                    document.getElementById('content').innerHTML = htmlContent;
                    
                    console.log('✅ 渲染完成');
                } else {
                    throw new Error('marked.js 未加载');
                }
                
            } catch (error) {
                console.error('❌ 渲染错误:', error);
                document.getElementById('content').innerHTML = 
                    '<div class="error"><strong>渲染失败</strong><br>' + 
                    error.message + '</div>';
            }
        })();
    </script>
</body>
</html>
    """.trimIndent()
}
