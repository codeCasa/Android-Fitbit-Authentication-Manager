package com.coding.casa.fitbit_authentication.authentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.coding.casa.fitbit_authentication.configuration.Scope
import com.coding.casa.fitbit_authentication.managers.AuthenticationManager
import com.coding.casa.fitbit_authentication.ui.LoginActivity
import java.util.HashSet

class FitbitAuthenticationManager(
    context: Context,
    secretKey: String,
    preferenceName: String,
    private val redirectUrl: String,
    private val successCallbackUrl: String
) :
    AuthenticationManager(context, secretKey, preferenceName) {

    override fun login(activity: Activity) {
        val scopes: MutableSet<Scope> =
            HashSet()
        scopes.addAll(configuration.requiredScopes!!)
        scopes.addAll(configuration.optionalScopes!!)
        val intent = LoginActivity.createIntent(
            activity,
            configuration.clientCredentials!!,
            configuration.tokenExpiresIn,
            scopes,
            redirectUrl,
            successCallbackUrl
        )
        activity.startActivityForResult(intent, loginRequestCode)
    }

    override fun logout(activity: Activity, onSuccess: (() -> Unit)?, onError: (() -> Unit)?) {
        checkIsConfigured()
        if (!isLoggedIn) {
            return
        }
        configuration.clientCredentials?.let { _credentials ->
            TaskRunner().executeAsync(
                LogoutTask(_credentials, accessToken),
                object : TaskRunner.Callback<String> {
                    override fun onComplete(result: String) {
                        val beforeLoginActivity =
                            configuration.beforeLoginActivity
                        if (beforeLoginActivity != null) {
                            activity.startActivity(beforeLoginActivity)
                        }
                        if (result.isEmpty()) {
                            onSuccess?.invoke()
                            return
                        }
                        onError?.invoke()
                    }
                }
            )
        }
        authenticationResult = null
        accessToken = null
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onAuthenticationFinished: (result: AuthenticationResult) -> Unit
    ) {
        checkIsConfigured()
        if (data == null && authenticationResult == null) {
            onAuthenticationFinished(AuthenticationResult.dismissed())
            return
        }
        when (requestCode) {
            loginRequestCode -> {
                if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
                    var authenticationResult: AuthenticationResult? =
                        data?.getParcelableExtra(LoginActivity.AUTHENTICATION_RESULT_KEY) ?: AuthenticationManager.authenticationResult
                    if (authenticationResult == null) {
                        onAuthenticationFinished(AuthenticationResult.dismissed())
                        return
                    }
                    if (authenticationResult.isSuccessful) {
                        val grantedScopes: Set<Scope> =
                            HashSet(
                                authenticationResult.accessToken?.scopes ?: listOf()
                            )
                        val requiredScopes: MutableSet<Scope> =
                            HashSet(
                                configuration.requiredScopes ?: listOf()
                            )
                        requiredScopes.removeAll(grantedScopes)
                        if (requiredScopes.size > 0) {
                            authenticationResult =
                                AuthenticationResult.missingRequiredScopes(
                                    requiredScopes
                                )
                        } else {
                            accessToken = authenticationResult.accessToken
                        }
                    }
                    onAuthenticationFinished(authenticationResult)
                } else {
                    onAuthenticationFinished(AuthenticationResult.dismissed())
                }
            }
        }
    }
}
