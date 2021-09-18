package com.coding.casa.fitbit_authentication.configuration

import android.content.Intent
import java.util.HashSet

/**
 * A builder to construct an authentication configuration
 */
class AuthenticationConfigurationBuilder {
    private val authenticationConfiguration: AuthenticationConfiguration =
        AuthenticationConfiguration()
    private var hasSetClientCredentials = false

    /**
     * Sets the client credentials on the configuration
     * @param clientCredentials The client credentials for the current configuration
     * @return The current builder instance
     */
    fun setClientCredentials(clientCredentials: ClientCredentials?): AuthenticationConfigurationBuilder {
        authenticationConfiguration.clientCredentials = clientCredentials
        hasSetClientCredentials = clientCredentials != null && clientCredentials.isComplete
        return this
    }

    /**
     * Adds required scopes to the authentication request
     * @param requiredScopes The scopes to request
     * @return The current builder instance
     */
    fun addRequiredScopes(vararg requiredScopes: Scope): AuthenticationConfigurationBuilder {
        for (scope in requiredScopes) {
            authenticationConfiguration.requiredScopes?.add(scope)
        }
        return this
    }

    /**
     * Adds optional scopes to the authentication request
     * @param optionalScopes The scopes to request
     * @return The current builder instance
     */
    fun addOptionalScopes(vararg optionalScopes: Scope): AuthenticationConfigurationBuilder {
        for (scope in optionalScopes) {
            authenticationConfiguration.optionalScopes?.add(scope)
        }
        return this
    }

    /**
     * Sets the activity to launch when logging out
     * @param beforeLoginActivity The activity intent to launch
     * @return The current builder instance
     */
    fun setBeforeLoginActivity(beforeLoginActivity: Intent): AuthenticationConfigurationBuilder {
        authenticationConfiguration.beforeLoginActivity = beforeLoginActivity
        beforeLoginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        beforeLoginActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        beforeLoginActivity.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        return this
    }

    /**
     * If true will logout/revoke existing access token, otherwise if false no action will be taken
     * @param logoutOnAuthFailure
     * @return The current builder instance
     */
    fun setLogoutOnAuthFailure(logoutOnAuthFailure: Boolean): AuthenticationConfigurationBuilder {
        authenticationConfiguration.isLogoutOnAuthFailure = logoutOnAuthFailure
        return this
    }

    /**
     * Sets the time the token is due to expire in seconds
     * @param tokenExpiresIn The time in seconds before token expires (max: 31536000L - 365 days)
     * @return The current builder instance
     */
    fun setTokenExpiresIn(tokenExpiresIn: Long?): AuthenticationConfigurationBuilder {
        authenticationConfiguration.tokenExpiresIn = tokenExpiresIn
        return this
    }

    /**
     * Builds the current configuration for the authentication flow
     * @return The build authentication configuration
     * @throws IllegalArgumentException If client credentials are not set or both required and optional scopes are empty
     */
    fun build(): AuthenticationConfiguration {
        require(hasSetClientCredentials) { "Error: client credentials not set! You must set client credentials with valid client id, client secret, and redirect url" }
        require(
            (
                authenticationConfiguration.requiredScopes!!.size +
                    authenticationConfiguration.optionalScopes!!.size
                ) != 0
        ) { "You must specify at least one oauth2 scope in `requiredScopes` or `optionalScopes`" }
        return authenticationConfiguration
    }

    init {
        authenticationConfiguration.requiredScopes = HashSet()
        authenticationConfiguration.optionalScopes = HashSet()
        authenticationConfiguration.isLogoutOnAuthFailure = false
    }
}
