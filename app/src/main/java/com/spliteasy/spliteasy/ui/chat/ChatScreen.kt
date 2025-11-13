package com.spliteasy.spliteasy.ui.chat

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ChatScreen() {
    val tidioUrl = "https://tu-sitio-web.com/pagina-del-chat"

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                loadUrl(tidioUrl)
            }
        },
        update = { webView ->
        }
    )
}