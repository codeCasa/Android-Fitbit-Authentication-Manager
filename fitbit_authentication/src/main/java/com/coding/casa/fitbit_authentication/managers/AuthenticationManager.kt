package com.coding.casa.fitbit_authentication.managers

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.coding.casa.fitbit_authentication.authentication.AccessToken
import com.coding.casa.fitbit_authentication.authentication.AuthenticationResult
import com.coding.casa.fitbit_authentication.configuration.AuthenticationConfiguration
import com.coding.casa.fitbit_authentication.security.SecurePreferences

/**
 * Manages authentication flow
 * @param context The current application context
 * @param secretKey Unique secret to encrypt access token
 * @param preferenceName Name of store to place secured access token
 */
abstract class AuthenticationManager(context: Context, secretKey: String, preferenceName: String) {
    private val preferences: SecurePreferences =
        SecurePreferences(context, preferenceName, secretKey, true)
    private var configured = false
    protected lateinit var configuration: AuthenticationConfiguration
    protected var loginRequestCode: Int = -1

    /**
     * @property accessToken The authentication token access token. Null if not logged in.
     * @throws IllegalArgumentException Must configure authentication manager first
     */
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

    /**
     * @property isLoggedIn True if user is logged in
     * @throws IllegalArgumentException Must configure authentication manager first
     */
    val isLoggedIn: Boolean
        get() {
            checkIsConfigured()
            val currentAccessToken = accessToken
            return currentAccessToken != null && !currentAccessToken.hasExpired()
        }

    /**
     * Configures the manager with the given authentication model
     * @param authenticationConfiguration The configuration model to use
     * @param loginRequestCode The unique request code for when the login flow finishes
     */
    fun configure(authenticationConfiguration: AuthenticationConfiguration, loginRequestCode: Int) {
        configuration = authenticationConfiguration
        this.loginRequestCode = loginRequestCode
        configured = true
    }

    protected fun checkIsConfigured() {
        require(configured) { "You must call `configure` on AuthenticationManager before using its methods!" }
    }

    /**
     * Attempts to log into service
     * @param activity The current activity
     */
    abstract fun login(activity: Activity)

    /**
     * Logouts out of the current session
     * @param activity The current activity
     * @param onSuccess Callback to invoke when logging out is successful
     * @param onError Callback to invoke whe logging out fails
     */
    abstract fun logout(activity: Activity, onSuccess: (() -> Unit)? = null, onError: (() -> Unit)? = null)

    /**
     * Consumes activity's result to determine if the authentication flow completed
     * @param requestCode The result's original request code
     * @param resultCode The result code
     * @param data The result sent from activity
     * @param onAuthenticationFinished Callback invoked if authentication flow finished
     */
    abstract fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, onAuthenticationFinished: (result: AuthenticationResult) -> Unit)

    companion object {

        @get:Synchronized
        @set:Synchronized
        internal var authenticationResult: AuthenticationResult? = null
        private const val AUTH_TOKEN_KEY = "AUTH_TOKEN"
    }
}
