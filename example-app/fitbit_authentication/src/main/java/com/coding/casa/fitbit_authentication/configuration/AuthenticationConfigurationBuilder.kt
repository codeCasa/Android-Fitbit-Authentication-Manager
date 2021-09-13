package com.coding.casa.fitbit_authentication.configuration

import android.content.Intent
import java.util.HashSet

class AuthenticationConfigurationBuilder {
    private val authenticationConfiguration: AuthenticationConfiguration =
        AuthenticationConfiguration()
    private var hasSetClientCredentials = false
    fun setClientCredentials(clientCredentials: ClientCredentials?): AuthenticationConfigurationBuilder {
        authenticationConfiguration.clientCredentials = clientCredentials
        hasSetClientCredentials = clientCredentials != null && clientCredentials.isComplete
        return this
    }

    fun addRequiredScopes(vararg requiredScopes: Scope): AuthenticationConfigurationBuilder {
        for (scope in requiredScopes) {
            authenticationConfiguration.requiredScopes?.add(scope)
        }
        return this
    }

    fun addOptionalScopes(vararg optionalScopes: Scope): AuthenticationConfigurationBuilder {
        for (scope in optionalScopes) {
            authenticationConfiguration.optionalScopes?.add(scope)
        }
        return this
    }

    fun setBeforeLoginActivity(beforeLoginActivity: Intent): AuthenticationConfigurationBuilder {
        authenticationConfiguration.beforeLoginActivity = beforeLoginActivity
        beforeLoginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        beforeLoginActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        beforeLoginActivity.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        return this
    }

    fun setLogoutOnAuthFailure(loginOnAuthFailure: Boolean): AuthenticationConfigurationBuilder {
        authenticationConfiguration.isLogoutOnAuthFailure = loginOnAuthFailure
        return this
    }

    fun setTokenExpiresIn(tokenExpiresIn: Long?): AuthenticationConfigurationBuilder {
        authenticationConfiguration.tokenExpiresIn = tokenExpiresIn
        return this
    }

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

        // Set default values
        authenticationConfiguration.requiredScopes = HashSet()
        authenticationConfiguration.optionalScopes = HashSet()
        authenticationConfiguration.isLogoutOnAuthFailure = false
    }
}