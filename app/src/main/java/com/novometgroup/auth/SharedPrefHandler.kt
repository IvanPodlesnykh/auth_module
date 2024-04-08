package com.novometgroup.auth

import android.content.SharedPreferences

class SharedPrefHandler (
    private val sharedPreferences: SharedPreferences
){
   fun saveString(key: String, value: String) {
       sharedPreferences.edit()
           .putString(key, value)
           .apply()
   }

   fun getString(key: String): String? {
       return sharedPreferences.getString(key, "")
   }

    fun deleteString(key: String) {
        sharedPreferences.edit()
            .putString(key, "")
            .apply()
    }
}