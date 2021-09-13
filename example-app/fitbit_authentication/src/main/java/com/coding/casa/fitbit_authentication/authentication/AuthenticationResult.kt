package com.coding.casa.fitbit_authentication.authentication

import android.os.Parcel
import android.os.Parcelable
import com.coding.casa.fitbit_authentication.configuration.Scope
import java.util.ArrayList

class AuthenticationResult : Parcelable {
    val status: Status?
    val errorMessage: String?
    val accessToken: AccessToken?
    val missingScopes: Set<Scope>?

    private constructor(
        status: Status,
        accessToken: AccessToken?,
        errorMessage: String?,
        missingScopes: Set<Scope>?
    ) {
        this.status = status
        this.errorMessage = errorMessage
        this.accessToken = accessToken
        this.missingScopes = missingScopes
    }

    protected constructor(`in`: Parcel) {
        status =
            Status.fromString(
                `in`.readString()
            )
        errorMessage = `in`.readString()
        accessToken = if (status == Status.Successful) {
            `in`.readParcelable(AccessToken::class.java.classLoader)
        } else {
            null
        }
        missingScopes = if (status == Status.MissingRequiredScopes) {
            val scopes: List<Scope> =
                `in`.createTypedArrayList(Scope.CREATOR)!!
            HashSet(scopes)
        } else {
            null
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(status!!.name)
        dest.writeString(errorMessage)
        if (status == Status.Successful) {
            dest.writeParcelable(accessToken, 0)
        } else if (status == Status.MissingRequiredScopes) {
            val scopeList: MutableList<Scope?> =
                ArrayList()
            scopeList.addAll(missingScopes!!)
            dest.writeTypedList(scopeList)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    val isSuccessful: Boolean
        get() = status == Status.Successful

    enum class Status {
        Successful, Dismissed, Error, MissingRequiredScopes;

        companion object {
            fun fromString(string: String?): Status? {
                for (status in values()) {
                    if (status.name == string) {
                        return status
                    }
                }
                return null
            }
        }
    }

    companion object {
        @JvmField
        val CREATOR =
            object :
                Parcelable.Creator<AuthenticationResult?> {
                override fun createFromParcel(`in`: Parcel): AuthenticationResult? {
                    return AuthenticationResult(`in`)
                }

                override fun newArray(size: Int): Array<AuthenticationResult?> {
                    return arrayOfNulls(
                        size
                    )
                }
            }

        fun error(message: String?): AuthenticationResult {
            return AuthenticationResult(
                Status.Error,
                null,
                message,
                null
            )
        }

        fun dismissed(): AuthenticationResult {
            return AuthenticationResult(
                Status.Dismissed,
                null,
                null,
                null
            )
        }

        fun success(accessToken: AccessToken?): AuthenticationResult {
            return AuthenticationResult(
                Status.Successful,
                accessToken,
                null,
                null
            )
        }

        fun missingRequiredScopes(scopes: Set<Scope>?): AuthenticationResult {
            return AuthenticationResult(
                Status.MissingRequiredScopes,
                null,
                null,
                scopes
            )
        }
    }
}