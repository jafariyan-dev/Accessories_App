package com.example.accessories_app.Activities

import android.graphics.BlurMaskFilter.Blur
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.accessories_app.databinding.ActivityAboutUsBinding

class About_Us : AppCompatActivity() {
    lateinit var b: ActivityAboutUsBinding
    private var nextUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(b.root)

        val webView = b.myWebView

        // تعریف یک بار WebViewClient
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // وقتی about:blank لود شد، URL اصلی رو لود کن
                if (url == "about:blank" && nextUrl != null) {
                    webView.clearHistory()
                    webView.loadUrl(nextUrl!!)
                    nextUrl = null
                }
            }
        }

        // هندل کلیک‌ها
        b.googleImg.setOnClickListener {
            hideAll()
            showWeb("https://manashimjewelry.ir/")
        }

        b.whatsappImg.setOnClickListener {
            hideAll()
            showWeb("https://api.whatsapp.com/send/?phone=989212846231&text&type=phone_number&app_absent=0&utm_source=ig")
        }

        b.threadsImg.setOnClickListener {
            hideAll()
            showWeb("https://www.threads.com/@manashiiim.jewelry")
        }

        b.telegramImg.setOnClickListener {
            hideAll()
            showWeb("https://t.me/manashiiim_jewelry")
        }

        b.instagramImg.setOnClickListener {
            hideAll()
            showWeb("https://www.instagram.com/manashiiim.jewelry")
        }
    }

    override fun onBackPressed() {
        val webView = b.myWebView
        if (webView.visibility == View.VISIBLE && webView.canGoBack()) {
            webView.goBack()
        } else if (webView.visibility == View.VISIBLE) {
            // برگشت به صفحه درباره ما
            b.myWebView.visibility = View.GONE
            b.myCardView.visibility = View.VISIBLE
            b.threads.visibility = View.VISIBLE
            b.instagram.visibility = View.VISIBLE
            b.telegram.visibility = View.VISIBLE
            b.google.visibility = View.VISIBLE
            b.whatsapp.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }

    private fun hideAll() {
        b.threads.visibility = View.GONE
        b.instagram.visibility = View.GONE
        b.whatsapp.visibility = View.GONE
        b.telegram.visibility = View.GONE
        b.google.visibility = View.GONE
        b.myCardView.visibility = View.GONE
    }

    private fun showWeb(url: String) {
        val webView = b.myWebView
        webView.visibility = View.VISIBLE
        nextUrl = url
        webView.loadUrl("about:blank") // ابتدا صفحه خالی برای پاک کردن history
    }
}