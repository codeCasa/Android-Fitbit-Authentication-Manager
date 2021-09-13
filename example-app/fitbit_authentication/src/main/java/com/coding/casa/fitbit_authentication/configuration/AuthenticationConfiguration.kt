package com.coding.casa.fitbit_authentication.configuration

import android.content.Intent


class AuthenticationConfiguration internal constructor() {
    var clientCredentials: ClientCredentials? = null
    var requiredScopes: MutableSet<Scope>? =
        null
    var optionalScopes: MutableSet<Scope>? =
        null
    var beforeLoginActivity: Intent? = null
    var isLogoutOnAuthFailure = false
    var tokenExpiresIn: Long? = null
}
