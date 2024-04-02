package com.novometgroup.auth

import okhttp3.Interceptor
import okhttp3.Response

class MyInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val request = original.newBuilder()
            .header("Accept", "application/json")
            .header("Uses-Agent", "Android application")
            .method(original.method(), original.body())
            .build()
        val response = chain.proceed(request)

        return response
    }
}