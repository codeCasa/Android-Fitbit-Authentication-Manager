package com.coding.casa.fitbit_authentication.configuration

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils

/**
 * The credentials for the Fitbit application
 */
class ClientCredentials : Parcelable {
    /**
     * @property clientId The Fitbit client id
     */
    var clientId: String?

    /**
     * @property clientSecret The Fitbit client secret
     */
    var clientSecret: String?

    /**
     * @property redirectUrl The Fitbit redirect url
     */
    var redirectUrl: String?

    /**
     * Creates a instance of client credentials
     * @param clientId The Fitbit client id
     * @param clientSecret The Fitbit client secret
     * @param redirectUrl The Fitbit redirect url
     */
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
