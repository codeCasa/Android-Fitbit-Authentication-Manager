package com.coding.casa.fitbit_authentication.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.coding.casa.fitbit_authentication.R
import com.coding.casa.fitbit_authentication.authentication.AuthenticationChangedHandler
import com.coding.casa.fitbit_authentication.authentication.AuthenticationResult
import com.coding.casa.fitbit_authentication.configuration.ClientCredentials
import com.coding.casa.fitbit_authentication.configuration.Scope
import com.coding.casa.fitbit_authentication.managers.AuthenticationManager

internal class LoginActivity : AppCompatActivity() {
    private lateinit var loginWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginWebView = findViewById(R.id.login_webview)
        val uri = checkIntentForAuth()
        val authorizationController: AuthenticationChangedHandler
        if (uri == null) {
            val clientCredentials: ClientCredentials =
                intent.getParcelableExtra(CLIENT_CREDENTIALS_KEY) ?: return
            val expiresIn =
                intent.getLongExtra(EXPIRES_IN_KEY, 604800)
            val scopes =
                intent.getParcelableArrayExtra(SCOPES_KEY) ?: return
            val scopesSet: MutableSet<Scope> =
                HashSet()
            for (scope in scopes) {
                scopesSet.add(scope as Scope)
            }
            authorizationController = AuthenticationChangedHandler(
                loginWebView,
                clientCredentials,
                this::onAuthFinished,
                uri
            )
            authorizationController.authenticate(expiresIn, scopesSet, redirectUrl)
        } else {
            AuthenticationChangedHandler.authenticate(uri.toString(), redirectUrl, this::onAuthFinished)
        }
    }

    private fun checkIntentForAuth(): Uri? {
        return intent.data
    }

    private fun onAuthFinished(result: AuthenticationResult) {
        loginWebView.visibility = View.GONE
        val resultIntent = Intent()
        if (result.isSuccessful) {
            AuthenticationManager.authenticationResult = result
        }
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        resultIntent.putExtra(AUTHENTICATION_RESULT_KEY, result)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        const val AUTHENTICATION_RESULT_KEY = "AUTHENTICATION_RESULT_KEY"
        private const val CLIENT_CREDENTIALS_KEY = "CLIENT_CREDENTIALS_KEY"
        private const val EXPIRES_IN_KEY = "EXPIRES_IN_KEY"
        private const val SCOPES_KEY = "SCOPES_KEY"

        private var redirectUrl: String = ""

        fun createIntent(
            context: Context?,
            @NonNull clientCredentials: ClientCredentials?,
            @Nullable expiresIn: Long?,
            scopes: Set<Scope>,
            redirectUrl: String,
        ): Intent {
            this.redirectUrl = redirectUrl
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            intent.putExtra(CLIENT_CREDENTIALS_KEY, clientCredentials)
            intent.putExtra(EXPIRES_IN_KEY, expiresIn)
            intent.putExtra(SCOPES_KEY, scopes.toTypedArray())
            return intent
        }
    }
}
