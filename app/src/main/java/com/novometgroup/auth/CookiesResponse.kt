package com.novometgroup.auth

import com.google.gson.annotations.SerializedName

data class CookiesResponse(@SerializedName("Set-Cookie") val cookies: String)