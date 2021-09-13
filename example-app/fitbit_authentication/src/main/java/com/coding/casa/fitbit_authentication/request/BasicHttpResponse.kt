package com.coding.casa.fitbit_authentication.request

import java.io.UnsupportedEncodingException

class BasicHttpResponse {
    lateinit var body: ByteArray
    private var statusCode = 0

    constructor() {}
    constructor(statusCode: Int, body: ByteArray) {
        this.body = body
        this.statusCode = statusCode
    }

    @get:Throws(UnsupportedEncodingException::class)
    val bodyAsString: String
        get() = String(body, Charsets.UTF_8)

    val isSuccessful: Boolean
        get() = statusCode in 100..399
}