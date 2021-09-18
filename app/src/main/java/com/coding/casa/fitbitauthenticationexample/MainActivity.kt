package com.coding.casa.fitbitauthenticationexample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.coding.casa.fitbit_authentication.authentication.AuthenticationResult
import com.coding.casa.fitbit_authentication.managers.FitbitAuthenticationManager
import com.coding.casa.fitbit_authentication.configuration.AuthenticationConfiguration
import com.coding.casa.fitbit_authentication.configuration.AuthenticationConfigurationBuilder
import com.coding.casa.fitbit_authentication.configuration.ClientCredentials
import com.coding.casa.fitbit_authentication.configuration.Scope
import com.coding.casa.fitbit_authentication.managers.AuthenticationManager
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var loginBtn: Button
    private lateinit var authenticationManager: AuthenticationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        authenticationManager = FitbitAuthenticationManager(this, "SOME_SECRET_KEY", "PREF_NAME", "strongfoundation://fitbit")
        authenticationManager.configure(generateAuthenticationConfiguration(), 12)
        loginBtn = findViewById(R.id.loginBtn)
        loginBtn.setOnClickListener {
            if(!authenticationManager.isLoggedIn) {
                authenticationManager.login(this)
            }else {
                authenticationManager.logout(this, {
                    Snackbar.make(loginBtn, "Successfully logged out", Snackbar.LENGTH_SHORT).show()}, {
                    Snackbar.make(loginBtn, "Failed to logout, please try again", Snackbar.LENGTH_SHORT).show()
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authenticationManager.onActivityResult(requestCode, resultCode, data) {
            when(it.status) {
                AuthenticationResult.Status.Successful -> {
                    Snackbar.make(loginBtn, "Successfully logged in", Snackbar.LENGTH_SHORT).show()
                    loginBtn.text = "Logout"
                }
                AuthenticationResult.Status.Dismissed -> {

                }
                AuthenticationResult.Status.Error -> {
                    Snackbar.make(loginBtn, "Failed to login, please try again", Snackbar.LENGTH_SHORT).show()
                }
                AuthenticationResult.Status.MissingRequiredScopes -> {
                    Snackbar.make(loginBtn, "Failed to logout, missing required scopes, please try again", Snackbar.LENGTH_SHORT).show()
                }
                null -> {

                }
            }
        }
    }

    private fun generateAuthenticationConfiguration(): AuthenticationConfiguration {
        val clientId = ""
        val redirectUrl = ""
        val clientSecret = ""
        val clientCredentials = ClientCredentials(
            clientId,
            clientSecret,
            redirectUrl
        )

        return AuthenticationConfigurationBuilder()
            .setClientCredentials(clientCredentials)
            .setTokenExpiresIn(31536000L) // 365 days
            .setBeforeLoginActivity(Intent(this, MainActivity::class.java))
            .addRequiredScopes(
                Scope.profile,
                Scope.activity,
                Scope.nutrition,
                Scope.weight,
                Scope.heartrate
            )
            .setLogoutOnAuthFailure(true)
            .build()
    }
}