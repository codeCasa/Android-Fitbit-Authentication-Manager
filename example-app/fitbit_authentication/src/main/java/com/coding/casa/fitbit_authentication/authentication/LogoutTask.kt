package com.coding.casa.fitbit_authentication.authentication

import android.os.Handler
import android.util.Base64
import com.coding.casa.fitbit_authentication.configuration.ClientCredentials
import java.io.IOException
import java.nio.charset.Charset
import android.os.Looper
import com.coding.casa.fitbit_authentication.request.BasicHttpRequestBuilder.Companion.create
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors


internal class TaskRunner {
    private val executor: Executor =
        Executors.newSingleThreadExecutor() // change according to your requirements
    private val handler = Handler(Looper.getMainLooper())

    interface Callback<R> {
        fun onComplete(result: R)
    }

    fun <R> executeAsync(callable: Callable<R>, callback: Callback<R>) {
        executor.execute {
            val result: R = callable.call()
            handler.post { callback.onComplete(result) }
        }
    }
}
internal class LogoutTask(
    private val clientCredentials: ClientCredentials,
    private val accessToken: AccessToken?,
): Callable<String> {
    override fun call(): String {
        val tokenString = String.format(
            "%s:%s",
            clientCredentials.clientId,
            clientCredentials.clientSecret
        )
        val token = Base64.encodeToString(
            tokenString.toByteArray(
                Charset.forName("UTF-8")
            ),
            0
        )

        val request = create()
            .setUrl(REVOKE_URL)
            .setContentType("application/json")
            .setAuthorization(String.format("Basic %s", token).trim { it <= ' ' })
            .setMethod("POST")
            .addQueryParam("token", accessToken!!.accessToken)
            .build()
        return try {
            val response = request.execute()
            if(response.isSuccessful){""}else{response.bodyAsString}
        } catch (e: IOException) {
            e.message ?: ""
        }
    }

    companion object {
        private const val REVOKE_URL = "https://api.fitbit.com/oauth2/revoke"
    }
}