package com.novometgroup.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.novometgroup.auth.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateException
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var sharedPrefHandler: SharedPrefHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefHandler = SharedPrefHandler(getSharedPreferences("sharedPref", MODE_PRIVATE))

        val login = ""
        val password = "12345678"
        var token = ""
        var cookies = ""

        val authService = Retrofit.Builder()
                .baseUrl(AUTH_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getUnsafeOkHttpClient())
                .build()
                .create(AuthApi::class.java)

        binding.tokenButton.setOnClickListener {
            binding.progressBar.isVisible = true

            authService.getCookies().enqueue(object : Callback<MyResponse>{
                override fun onResponse(
                    call: Call<MyResponse>,
                    response: Response<MyResponse>
                ) {
                    val cookiesResponse = response.headers()["Set-Cookie"]

                    val cookiesArr = cookiesResponse!!.split(";")

                    token = cookiesArr[0].substringAfterLast("=")

                    binding.tokenText.text = token
                    sharedPrefHandler.saveString("token", token)
                    binding.progressBar.isVisible = false
                }

                override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                    binding.tokenText.text = t.toString()
                    binding.progressBar.isVisible = false
                }

            })
        }

        binding.authButton.setOnClickListener {
            binding.progressBar.isVisible = true

            authService.authenticate(login, password, cookies, token).enqueue(object : Callback<MyResponse> {
                override fun onResponse(
                    call: Call<MyResponse>,
                    response: Response<MyResponse>
                ) {
                    binding.authText.text = response.headers()["Set-Cookie"]
                    binding.progressBar.isVisible = false
                }

                override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                    binding.authText.text = t.toString()
                    binding.progressBar.isVisible = false
                }

            })
        }

    }

    companion object {
        const val AUTH_BASE_URL = "https://some.adress/"
    }

    private fun getInterceptedHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(MyInterceptor())
            .build()
    }


    // Custom OkHttp client to bypass ssl certificate checks
    private fun getUnsafeOkHttpClient(): OkHttpClient? {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<out java.security.cert.X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<out java.security.cert.X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate>? {
                        return arrayOf()
                    }
                }
            )

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory
            val trustManagerFactory: TrustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers: Array<TrustManager> =
                trustManagerFactory.trustManagers
            check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                "Unexpected default trust managers:" + trustManagers.contentToString()
            }

            val trustManager =
                trustManagers[0] as X509TrustManager


            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustManager)
            builder.hostnameVerifier{ _, _ -> true}
            builder.addInterceptor(MyInterceptor())
            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}