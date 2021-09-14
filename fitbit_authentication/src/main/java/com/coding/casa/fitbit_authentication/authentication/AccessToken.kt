package com.coding.casa.fitbit_authentication.authentication

import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import com.coding.casa.fitbit_authentication.configuration.Scope
import java.util.ArrayList

class AccessToken : Parcelable {
    var accessToken: String?
    var userId: String?
    var expirationDate: Long
    var scopes: List<Scope>? =
        ArrayList()

    constructor(
        accessToken: String?,
        userId: String?,
        expirationDate: Long,
        scopes: List<Scope>?
    ) {
        this.userId = userId
        this.accessToken = accessToken
        this.expirationDate = expirationDate
        this.scopes = scopes
    }

    protected constructor(`in`: Parcel) {
        accessToken = `in`.readString()
        userId = `in`.readString()
        expirationDate = `in`.readLong()
        val parcelScopes = mutableListOf<Scope>()
        `in`.readTypedList(parcelScopes, Scope.CREATOR)
        scopes = parcelScopes
    }

    fun hasExpired(): Boolean {
        return expirationDate < System.currentTimeMillis() / 1000
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(accessToken)
        parcel.writeString(userId)
        parcel.writeLong(expirationDate)
        parcel.writeTypedList(scopes?.toMutableList() ?: mutableListOf())
    }

    fun toBase64String(): String {
        val parcel = Parcel.obtain()
        writeToParcel(parcel, 0)
        val serializedMe = parcel.marshall()
        parcel.recycle()
        return Base64.encodeToString(serializedMe, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AccessToken?> = object : Parcelable.Creator<AccessToken?> {
            override fun createFromParcel(`in`: Parcel): AccessToken {
                return AccessToken(`in`)
            }

            override fun newArray(size: Int): Array<AccessToken?> {
                return arrayOfNulls(size)
            }
        }

        fun fromBase64(base64String: String?): AccessToken? {
            if (base64String == null) {
                return null
            }
            val serializedMe = Base64.decode(base64String, 0)
            val parcel = Parcel.obtain()
            parcel.unmarshall(serializedMe, 0, serializedMe.size)
            parcel.setDataPosition(0) // This is extremely important!
            val accessToken = AccessToken(parcel)
            parcel.recycle()
            return accessToken
        }
    }
}
