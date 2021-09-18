# Android-Fitbit-Authentication-Manager
An Android module for authenticating your app with the Fitbit Web API.

[![](https://jitci.com/gh/codeCasa/Android-Fitbit-Authentication-Manager/svg)](https://jitci.com/gh/codeCasa/Android-Fitbit-Authentication-Manager)

## Installation
---
### *Gradle*

1. In your projects root build.gradle
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
2. In the project's build.gradle
```
dependencies {
	implementation 'com.github.codeCasa:Android-Fitbit-Authentication-Manager:Tag'
}
```

### *Maven*
1. Add repo
```
<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
</repositories>
```
2. Add dependency
```
<dependency>
	    <groupId>com.github.codeCasa</groupId>
	    <artifactId>Android-Fitbit-Authentication-Manager</artifactId>
	    <version>Tag</version>
</dependency>
```

## Fitbit App Setup
---
1. Log into [Fitbit Developer portal](https://dev.fitbit.com/login)
![fitbit developer portal](./documentation/login.png)
2. Register Your application
![Registration application](./documentation/register_app.png)
3. Filled out application
![Registration application](./documentation/filled_registration.png)
4. Submit your application
5. Save your client secret and client id in safe place
![Managed app](./documentation/app_details.png)

## Android Setup
---
### *AndroidManifest.xml*
```
 <uses-permission android:name="android.permission.INTERNET"/>
    <application
        ...
        <activity
            android:name="com.coding.casa.fitbit_authentication.ui.LoginActivity"
            android:exported="true"
            android:theme="@style/Theme.Fitbit.Teal">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data
                    android:host="fitbit"
                    android:scheme="strongfoundation" />
            </intent-filter>
        </activity>
    </application>
```
### *SomeActivity*
```
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
```