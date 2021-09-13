package com.coding.casa.fitbit_authentication.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.io.UnsupportedEncodingException
import java.security.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/*
Copyright (C) 2012 Sveinung Kval Bakken, sveinung.bakken@gmail.com

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */
class SecurePreferences(
    context: Context,
    preferenceName: String?,
    secureKey: String,
    encryptKeys: Boolean
) {
    class SecurePreferencesException(e: Throwable?) :
        RuntimeException(e)

    private var encryptKeys = false
    private var writer: Cipher? = null
    private var reader: Cipher? = null
    private var keyWriter: Cipher? = null
    private var preferences: SharedPreferences? = null

    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class
    )
    protected fun initCiphers(secureKey: String) {
        val ivSpec = iv
        val secretKey = getSecretKey(secureKey)
        writer!!.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        reader!!.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        keyWriter!!.init(Cipher.ENCRYPT_MODE, secretKey)
    }

    protected val iv: IvParameterSpec
        protected get() {
            val iv = ByteArray(writer!!.blockSize)
            System.arraycopy(
                "fldsjfodasjifudslfjdsaofshaufihadsf".toByteArray(),
                0,
                iv,
                0,
                writer!!.blockSize
            )
            return IvParameterSpec(iv)
        }

    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class
    )
    protected fun getSecretKey(key: String): SecretKeySpec {
        val keyBytes = createKeyBytes(key)
        return SecretKeySpec(keyBytes, TRANSFORMATION)
    }

    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class
    )
    protected fun createKeyBytes(key: String): ByteArray {
        val md =
            MessageDigest.getInstance(SECRET_KEY_HASH_TRANSFORMATION)
        md.reset()
        return md.digest(key.toByteArray(charset(CHARSET)))
    }

    fun put(key: String, value: String?) {
        if (value == null) {
            preferences!!.edit().remove(toKey(key)).apply()
        } else {
            putValue(toKey(key), value)
        }
    }

    fun containsKey(key: String): Boolean {
        return preferences!!.contains(toKey(key))
    }

    fun removeValue(key: String) {
        preferences!!.edit().remove(toKey(key)).apply()
    }

    @Throws(SecurePreferencesException::class)
    fun getString(key: String): String? {
        if (preferences!!.contains(toKey(key))) {
            val securedEncodedValue = preferences!!.getString(toKey(key), "")
            return decrypt(securedEncodedValue)
        }
        return null
    }

    fun clear() {
        preferences!!.edit().clear().apply()
    }

    private fun toKey(key: String): String {
        return if (encryptKeys) encrypt(key, keyWriter) else key
    }

    @Throws(SecurePreferencesException::class)
    private fun putValue(key: String, value: String) {
        val secureValueEncoded = encrypt(value, writer)
        preferences!!.edit().putString(key, secureValueEncoded).apply()
    }

    @Throws(SecurePreferencesException::class)
    protected fun encrypt(value: String, writer: Cipher?): String {
        val secureValue: ByteArray
        secureValue = try {
            convert(
                writer,
                value.toByteArray(charset(CHARSET))
            )
        } catch (e: UnsupportedEncodingException) {
            throw SecurePreferencesException(e)
        }
        return Base64.encodeToString(secureValue, Base64.NO_WRAP)
    }

    protected fun decrypt(securedEncodedValue: String?): String {
        val securedValue =
            Base64.decode(securedEncodedValue, Base64.NO_WRAP)
        val value = convert(reader, securedValue)
        return try {
            String(value, Charsets.UTF_8)
        } catch (e: UnsupportedEncodingException) {
            throw SecurePreferencesException(e)
        }
    }

    companion object {
        private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
        private const val KEY_TRANSFORMATION = "AES/ECB/PKCS5Padding"
        private const val SECRET_KEY_HASH_TRANSFORMATION = "SHA-256"
        private const val CHARSET = "UTF-8"

        @Throws(SecurePreferencesException::class)
        private fun convert(cipher: Cipher?, bs: ByteArray): ByteArray {
            return try {
                cipher!!.doFinal(bs)
            } catch (e: Exception) {
                throw SecurePreferencesException(e)
            }
        }
    }

    /**
     * This will initialize an instance of the SecurePreferences class
     * @param context your current context.
     * @param preferenceName name of preferences file (preferenceName.xml)
     * @param secureKey the key used for encryption, finding a good key scheme is hard.
     * Hardcoding your key in the application is bad, but better than plaintext preferences. Having the user enter the key upon application launch is a safe(r) alternative, but annoying to the user.
     * @param encryptKeys settings this to false will only encrypt the values,
     * true will encrypt both values and keys. Keys can contain a lot of information about
     * the plaintext value of the value which can be used to decipher the value.
     * @throws SecurePreferencesException
     */
    init {
        try {
            writer =
                Cipher.getInstance(TRANSFORMATION)
            reader =
                Cipher.getInstance(TRANSFORMATION)
            keyWriter =
                Cipher.getInstance(KEY_TRANSFORMATION)
            initCiphers(secureKey)
            preferences =
                context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            this.encryptKeys = encryptKeys
        } catch (e: GeneralSecurityException) {
            throw SecurePreferencesException(e)
        } catch (e: UnsupportedEncodingException) {
            throw SecurePreferencesException(e)
        }
    }
}
