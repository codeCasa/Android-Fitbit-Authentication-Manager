package com.coding.casa.fitbit_authentication.configuration

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils

class ClientCredentials : Parcelable {
    var clientId: String?
    var clientSecret: String?
    var redirectUrl: String?

    constructor(
        clientId: String?,
        clientSecret: String?,
        redirectUrl: String?
    ) {
        this.clientId = clientId
        this.clientSecret = clientSecret
        this.redirectUrl = redirectUrl
    }

    protected constructor(`in`: Parcel) {
        clientId = `in`.readString()
        clientSecret = `in`.readString()
        redirectUrl = `in`.readString()
    }

    val isComplete: Boolean
        get() = (
                !TextUtils.isEmpty(clientId) &&
                        !TextUtils.isEmpty(clientSecret) &&
                        !TextUtils.isEmpty(redirectUrl)
                )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(clientId)
        parcel.writeString(clientSecret)
        parcel.writeString(redirectUrl)
    }

    companion object CREATOR : Parcelable.Creator<ClientCredentials> {
        override fun createFromParcel(parcel: Parcel): ClientCredentials {
            return ClientCredentials(parcel)
        }

        override fun newArray(size: Int): Array<ClientCredentials?> {
            return arrayOfNulls(size)
        }
    }
}