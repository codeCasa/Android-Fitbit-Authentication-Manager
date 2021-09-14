package com.coding.casa.fitbit_authentication.authentication

import android.annotation.SuppressLint
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.WebView
import com.coding.casa.fitbit_authentication.configuration.ClientCredentials
import com.coding.casa.fitbit_authentication.configuration.Scope
import com.coding.casa.fitbit_authentication.ui.UrlChangedHandler
import com.coding.casa.fitbit_authentication.ui.WebViewEventHandler
import java.util.Locale
import java.util.regex.Pattern

class AuthenticationChangedHandler(
    private val webView: WebView,
    private val clientCredentials: ClientCredentials,
    private val authenticationHandler: (result: AuthenticationResult) -> Unit,
    private val uri: Uri?
) : UrlChangedHandler {
    @SuppressLint("SetJavaScriptEnabled")
    fun authenticate(
        expiresIn: Long,
        scopes: Set<Scope>?,
        redirectUrl: String,
        successCallbackUrl: String
    ) {
        val webSettings = webView.settings
        webSettings.domStorageEnabled = true
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewEventHandler(this, redirectUrl, successCallbackUrl)
        val url = String.format(
            Locale.ENGLISH,
            AUTHORIZE_URL_FORMAT,
            clientCredentials.clientId,
            clientCredentials.redirectUrl,
            TextUtils.join("%20", scopes!!),
            expiresIn
        )
        if (uri == null) {
            webView.loadUrl(url)
        } else {
            onUrlChanged(uri.toString())
        }
    }

    private fun parseScopes(scopes: String?): List<Scope> {
        if (scopes.isNullOrBlank()) {
            return emptyList()
        }
        val scopesArray = scopes.split(" ").toTypedArray()
        val scopesList: MutableList<Scope> =
            ArrayList()
        for (scopeStr in scopesArray) {
            val scope: Scope =
                Scope.fromString(scopeStr)
            scopesList.add(scope)
        }
        return scopesList
    }

    private fun parseSuccess(url: String) {
        var urlString = url
        urlString = urlString.replaceFirst("#".toRegex(), "?")
        val uri = Uri.parse(urlString)

        // Save Auth Token
        val accessToken = uri.getQueryParameter("access_token")
        val userId = uri.getQueryParameter("user_id")
        val expiresOn = (
            uri.getQueryParameter("expires_in")
                ?.toLong() ?: 0
            ) + System.currentTimeMillis() / 1000
        val scopes =
            parseScopes(uri.getQueryParameter("scope"))
        val accessTokenObject = AccessToken(accessToken, userId, expiresOn, scopes)
        authenticationHandler(
            AuthenticationResult.success(
                accessTokenObject
            )
        )
    }

    override fun onUrlChanged(newUrl: String) {
        if (clientCredentials.redirectUrl?.let { newUrl.startsWith(it) }!!) {
            webView.visibility = View.GONE
            val successMatcher =
                TOKEN_MATCH_PATTERN.matcher(newUrl)
            val dismissedMatcher =
                DISMISSED_PATTERN.matcher(newUrl)
            when {
                successMatcher.find() -> {
                    parseSuccess(newUrl)
                }
                dismissedMatcher.find() -> {
                    authenticationHandler(AuthenticationResult.dismissed())
                }
                else -> {
                    Log.e(
                        TAG,
                        "Error getting access code from url"
                    )
                    authenticationHandler(
                        AuthenticationResult.error(
                            String.format("Url missing access code: %s", newUrl)
                        )
                    )
                }
            }
        }
    }

    override fun onLoadError(errorCode: Int, description: CharSequence) {
        authenticationHandler(
            AuthenticationResult.error(
                description.toString()
            )
        )
    }

    companion object {
        private const val TAG = "AuthorizationController"
        private const val AUTHORIZE_URL_FORMAT =
            "https://www.fitbit.com/oauth2/authorize?response_type=token&client_id=%s&redirect_uri=%s&scope=%s&expires_in=%d"
        private val TOKEN_MATCH_PATTERN =
            Pattern.compile("#access_token=(.+)&")
        private val DISMISSED_PATTERN =
            Pattern.compile("error_description=The\\+user\\+denied\\+the\\+request")

        fun authenticate(newUrl: String, redirectUrl: String, onAuthenticationFinished: (result: AuthenticationResult) -> Unit) {
            if (newUrl.contains(redirectUrl)) {
                val successMatcher =
                    TOKEN_MATCH_PATTERN.matcher(newUrl)
                val dismissedMatcher =
                    DISMISSED_PATTERN.matcher(newUrl)
                when {
                    successMatcher.find() -> {
                        parseSuccess(newUrl, onAuthenticationFinished)
                    }
                    dismissedMatcher.find() -> {
                        onAuthenticationFinished(AuthenticationResult.dismissed())
                    }
                    else -> {
                        Log.e(
                            TAG,
                            "Error getting access code from url"
                        )
                        onAuthenticationFinished(
                            AuthenticationResult.error(
                                String.format("Url missing access code: %s", newUrl)
                            )
                        )
                    }
                }
            }
        }

        private fun parseScopes(scopes: String?): List<Scope> {
            if (scopes.isNullOrBlank()) {
                return emptyList()
            }
            val scopesArray = scopes.split(" ").toTypedArray()
            val scopesList: MutableList<Scope> =
                ArrayList()
            for (scopeStr in scopesArray) {
                val scope: Scope =
                    Scope.fromString(scopeStr)
                scopesList.add(scope)
            }
            return scopesList
        }

        private fun parseSuccess(url: String, onAuthenticationFinished: (result: AuthenticationResult) -> Unit) {
            var urlString = url
            urlString = urlString.replaceFirst("#".toRegex(), "?")
            val uri = Uri.parse(urlString)

            // Save Auth Token
            val accessToken = uri.getQueryParameter("access_token")
            val userId = uri.getQueryParameter("user_id")
            val expiresOn = (
                uri.getQueryParameter("expires_in")
                    ?.toLong() ?: 0
                ) + System.currentTimeMillis() / 1000
            val scopes =
                parseScopes(uri.getQueryParameter("scope"))
            val accessTokenObject = AccessToken(accessToken, userId, expiresOn, scopes)
            onAuthenticationFinished(
                AuthenticationResult.success(
                    accessTokenObject
                )
            )
        }
    }
}
