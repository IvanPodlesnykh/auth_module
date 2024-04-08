package com.novometgroup.auth

import com.google.gson.annotations.SerializedName

data class RegistrationBody(val email: String, val password: String,@SerializedName("device_name") val deviceName: String)