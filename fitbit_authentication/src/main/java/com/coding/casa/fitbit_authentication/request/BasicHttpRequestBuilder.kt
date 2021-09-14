package com.coding.casa.fitbit_authentication.request

import android.text.TextUtils
import androidx.core.util.Pair
import java.io.UnsupportedEncodingException
import java.util.ArrayList

class BasicHttpRequestBuilder private constructor() {
    private val basicHttpRequest: BasicHttpRequest
    fun build(): BasicHttpRequest {
        require(!TextUtils.isEmpty(basicHttpRequest.url)) { "Url cannot be empty!" }
        return basicHttpRequest
    }

    fun setUrl(url: String?): BasicHttpRequestBuilder {
        basicHttpRequest.url = url
        return this
    }

    fun setAuthorization(authorization: String?): BasicHttpRequestBuilder {
        basicHttpRequest.setAuthorization(authorization)
        return this
    }

    fun setMethod(method: String?): BasicHttpRequestBuilder {
        basicHttpRequest.method = method
        return this
    }

    fun setContentType(contentType: String?): BasicHttpRequestBuilder {
        basicHttpRequest.contentType = contentType
        return this
    }

    @Throws(UnsupportedEncodingException::class)
    fun setContent(content: String): BasicHttpRequestBuilder {
        basicHttpRequest.setContent(content)
        return this
    }

    fun setContent(content: ByteArray?): BasicHttpRequestBuilder {
        basicHttpRequest.setContent(content)
        return this
    }

    fun setUseCaches(useCaches: Boolean): BasicHttpRequestBuilder {
        basicHttpRequest.setUseCaches(useCaches)
        return this
    }

    fun addQueryParam(name: String?, value: String?): BasicHttpRequestBuilder {
        if (basicHttpRequest.params == null) {
            basicHttpRequest.params = ArrayList()
        }
        basicHttpRequest.params!!
            .add(Pair(name, value))
        return this
    }

    companion object {
        @JvmStatic
        fun create(): BasicHttpRequestBuilder {
            return BasicHttpRequestBuilder()
        }
    }

    init {
        basicHttpRequest = BasicHttpRequest()
        basicHttpRequest.method = "GET"
    }
}
