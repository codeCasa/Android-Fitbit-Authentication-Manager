package com.coding.casa.fitbit_authentication.authentication

import android.os.Parcel
import android.os.Parcelable
import com.coding.casa.fitbit_authentication.configuration.Scope
import java.util.ArrayList

/**
 * Result model returned after user attempts to authenticate with Fitbit
 */
class AuthenticationResult : Parcelable {
    /**
     * @property status The result status
     */
    val status: Status?

    /**
     * @property errorMessage The error message if authentication failed, otherwise null
     */
    val errorMessage: String?

    /**
     * @property accessToken The resulting access token if authentication was successful, otherwise null
     */
    val accessToken: AccessToken?

    /**
     * @property missingScopes Scopes that were not approved by the user
     */
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

        /**
         * Creates an Authentication Result with an error message
         * @param message The error message to provide to result
         *
         * @return The error AuthenticationResult
         */
        fun error(message: String?): AuthenticationResult {
            return AuthenticationResult(
                Status.Error,
                null,
                message,
                null
            )
        }

        /**
         * Creates an Authentication Result when the authentication flow was dismissed
         *
         * @return The dismissed AuthenticationResult
         */
        fun dismissed(): AuthenticationResult {
            return AuthenticationResult(
                Status.Dismissed,
                null,
                null,
                null
            )
        }

        /**
         * Creates an Authentication Result when the authentication flow was successful
         * @param accessToken The resulting token from the authentication flow
         *
         * @return The dismissed AuthenticationResult
         */
        fun success(accessToken: AccessToken?): AuthenticationResult {
            return AuthenticationResult(
                Status.Successful,
                accessToken,
                null,
                null
            )
        }

        /**
         * Creates an Authentication Result when the authentication flow did not accept all the scopes
         * @param scopes The missing/non-approved scopes
         *
         * @return The dismissed AuthenticationResult
         */
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
