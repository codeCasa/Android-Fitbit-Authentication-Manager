package com.coding.casa.fitbit_authentication.ui

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent

internal class WebViewEventHandler(private val urlChangeHandler: UrlChangedHandler, private val redirectUrl: String, private val successCallbackUrl: String) :
    WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView,
        url: String
    ): Boolean {
        return loadUrl(view, url)
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest
    ): Boolean {
        return loadUrl(view, request.url.toString())
    }

    private fun loadUrl(view: WebView, url: String): Boolean {
        if (url.contains(successCallbackUrl) || url.contains(redirectUrl)) {
            view.loadUrl(url)
            urlChangeHandler.onUrlChanged(url)
            return false
        }
        val customTabsIntent = CustomTabsIntent.Builder()
            .build()
        customTabsIntent.launchUrl(view.context, Uri.parse(url))
        return false
    }

    override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        urlChangeHandler.onLoadError(errorCode, description)
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        super.onReceivedError(view, request, error)
        urlChangeHandler.onLoadError(error.errorCode, error.description)
    }
}
