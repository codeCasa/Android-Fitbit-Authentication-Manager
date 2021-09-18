package com.coding.casa.fitbit_authentication.configuration

import android.content.Intent

/**
 * The configuration for the authentication flow
 */
class AuthenticationConfiguration internal constructor() {
    /**
     * @property clientCredentials The Fitbit credentials needed to progress through auth flow
     */
    var clientCredentials: ClientCredentials? = null

    /**
     * @property requiredScopes The scopes required by application
     */
    var requiredScopes: MutableSet<Scope>? =
        null

    /**
     * @property optionalScopes The scopes not required by application but desired
     */
    var optionalScopes: MutableSet<Scope>? =
        null

    /**
     * @property beforeLoginActivity The activity intent to start after logging out
     */
    var beforeLoginActivity: Intent? = null

    /**
     * @property isLogoutOnAuthFailure Set to true if wished to logout on authentication failure
     */
    var isLogoutOnAuthFailure = false

    /**
     * @property tokenExpiresIn The time in seconds before token expires (max: 31536000L - 365 days)
     */
    var tokenExpiresIn: Long? = null
}
