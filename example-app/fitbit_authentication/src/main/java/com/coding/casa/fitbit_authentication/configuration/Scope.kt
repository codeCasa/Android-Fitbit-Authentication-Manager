package com.coding.casa.fitbit_authentication.configuration

import android.os.Parcel
import android.os.Parcelable
import java.util.HashSet

enum class Scope() : Parcelable {
    activity, heartrate, location, nutrition, profile, settings, sleep, social, weight;

    constructor(parcel: Parcel) : this() {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
    }

    companion object CREATOR : Parcelable.Creator<Scope> {
        override fun createFromParcel(parcel: Parcel): Scope {
            return fromString(parcel.readString())
        }

        override fun newArray(size: Int): Array<Scope?> {
            return arrayOfNulls(size)
        }

        fun all(): Set<Scope> {
            return HashSet(
                listOf(
                    *values()
                )
            )
        }

        fun fromString(name: String?): Scope {
            for (scope in values()) {
                if (scope.name == name) {
                    return scope
                }
            }
            return activity
        }
    }
}