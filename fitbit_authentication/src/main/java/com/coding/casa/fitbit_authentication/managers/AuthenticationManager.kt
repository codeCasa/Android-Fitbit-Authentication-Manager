package com.coding.casa.fitbit_authentication.managers

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.coding.casa.fitbit_authentication.authentication.AccessToken
import com.coding.casa.fitbit_authentication.authentication.AuthenticationResult
import com.coding.casa.fitbit_authentication.configuration.AuthenticationConfiguration
import com.coding.casa.fitbit_authentication.security.SecurePreferences

abstract class AuthenticationManager(context: Context, secretKey: String, preferenceName: String) {
    private val preferences: SecurePreferences =
        SecurePreferences(context, preferenceName, secretKey, true)
    private var configured = false
    protected lateinit var configuration: AuthenticationConfiguration
    protected var loginRequestCode: Int = -1
    private val AUTH_TOKEN_KEY = "AUTH_TOKEN"

    @get:Synchronized
    @set:Synchronized
    var accessToken: AccessToken? = null
        get() {
            checkIsConfigured()
            if (field == null) {
                field = AccessToken.fromBase64(preferences.getString(AUTH_TOKEN_KEY))
            }
            return field
        }
        protected set(currentAccessToken) {
            checkIsConfigured()
            field = currentAccessToken
            if (currentAccessToken != null) {
                preferences.put(
                    AUTH_TOKEN_KEY,
                    currentAccessToken.toBase64String()
                )
            } else {
                preferences.removeValue(AUTH_TOKEN_KEY)
            }
        }

    val isLoggedIn: Boolean
        get() {
            checkIsConfigured()
            val currentAccessToken = accessToken
            return currentAccessToken != null && !currentAccessToken.hasExpired()
        }

    fun configure(authenticationConfiguration: AuthenticationConfiguration, loginRequestCode: Int) {
        configuration = authenticationConfiguration
        this.loginRequestCode = loginRequestCode
        configured = true
    }

    protected fun checkIsConfigured() {
        require(configured) { "You must call `configure` on AuthenticationManager before using its methods!" }
    }

    abstract fun login(activity: Activity)
    abstract fun logout(activity: Activity, onSuccess: (() -> Unit)? = null, onError: (() -> Unit)? = null)
    abstract fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, onAuthenticationFinished: (result: AuthenticationResult) -> Unit)

    companion object {

        @get:Synchronized
        @set:Synchronized
        internal var authenticationResult: AuthenticationResult? = null
    }
}
