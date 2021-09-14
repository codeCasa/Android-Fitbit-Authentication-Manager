package com.coding.casa.fitbit_authentication.ui

interface UrlChangedHandler {
    fun onUrlChanged(newUrl: String)
    fun onLoadError(errorCode: Int, description: CharSequence)
}
