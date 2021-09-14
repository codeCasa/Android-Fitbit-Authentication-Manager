package com.coding.casa.fitbit_authentication.request

import android.text.TextUtils
import androidx.core.util.Pair
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.ArrayList

class BasicHttpRequest internal constructor() {
    var url: String? = null
    private var authorization: String? = null
    var method: String? = null
    var contentType: String? = null
    private var content: ByteArray? = null
    private var useCaches = false
    var params: MutableList<Pair<String, String>>? =
        null

    fun setAuthorization(authorization: String?) {
        this.authorization = authorization
    }

    val contentLength: Int
        get() = if (content != null) content!!.size else 0

    fun setContent(content: ByteArray?) {
        this.content = content
    }

    @Throws(UnsupportedEncodingException::class)
    fun setContent(content: String) {
        setContent(content.toByteArray(charset("UTF-8")))
    }

    fun useCaches(): Boolean {
        return useCaches
    }

    fun setUseCaches(useCaches: Boolean) {
        this.useCaches = useCaches
    }

    @Synchronized
    @Throws(IOException::class)
    private fun fillInConnectionInfo(connection: HttpURLConnection?) {
        connection!!.requestMethod = method
        if (!TextUtils.isEmpty(authorization)) {
            connection.setRequestProperty("Authorization", authorization)
        }
        if (!TextUtils.isEmpty(contentType)) {
            connection.setRequestProperty("Content-Type", contentType)
        }
        if (content == null || content!!.size == 0) {
            connection.setRequestProperty("Content-Length", "0")
        } else {
            connection.setRequestProperty(
                "Content-Length",
                Integer.toString(content!!.size)
            )
            var outputStream: OutputStream? = null
            try {
                outputStream = connection.outputStream
                outputStream.write(content)
            } finally {
                outputStream?.close()
            }
        }
        connection.useCaches = useCaches
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getQuery(params: List<Pair<String, String>>?): String {
        val keyValues: MutableList<String?> =
            ArrayList()
        for (pair in params!!) {
            keyValues.add(
                URLEncoder.encode(pair.first, "UTF-8") +
                    "=" +
                    URLEncoder.encode(pair.second, "UTF-8")
            )
        }
        return TextUtils.join("&", keyValues)
    }

    @Throws(IOException::class)
    fun readBytes(inputStream: InputStream): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    @Synchronized
    @Throws(IOException::class)
    fun execute(): BasicHttpResponse {
        var connection: HttpURLConnection? = null
        val urlString =
            url + if (params != null) "?" + getQuery(params) else ""
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            fillInConnectionInfo(connection)
            connection.connect()
            BasicHttpResponse(
                connection.responseCode,
                readBytes(connection.inputStream)
            )
        } finally {
            connection?.disconnect()
        }
    }
}
