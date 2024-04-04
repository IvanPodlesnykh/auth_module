package com.novometgroup.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.novometgroup.androidiscool.MotorDetails
import com.novometgroup.androidiscool.getApiService
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
import java.util.concurrent.Callable
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var sharedPrefHandler: SharedPrefHandler

    val login = "Ivan.Zolotarev@novometgroup.com"
    val password = "12345678"
    var token = ""
    var cookies = ""
    var session = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefHandler = SharedPrefHandler(getSharedPreferences("sharedPref", MODE_PRIVATE))

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
                    cookies = ""
                    val headerMapList = response.headers().toMultimap()
                    val cookiesResponse = headerMapList.get("set-cookie")
                    for (item in cookiesResponse!!.iterator()) {
                        if (item.contains("XSRF-TOKEN")) {
                            token = item.substringAfter("XSRF-TOKEN=").substringBefore(";")
                            sharedPrefHandler.saveString("token", token)
                            cookies += item.substringBefore(";")
                        }
                        if (item.contains("difa_php_session")) {
                            session = item.substringAfter("difa_php_session=").substringBefore(";")
                            sharedPrefHandler.saveString("session", session)
                            cookies += item.substringBefore(";")
                        }
                    }
                    binding.tokenText.text = "token: $token\nsesson: $session"

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
                    cookies = ""
                    val headerMapList = response.headers().toMultimap()
                    val cookiesResponse = headerMapList.get("set-cookie")
                    for (item in cookiesResponse!!.iterator()) {
                        if (item.contains("XSRF-TOKEN")) {
                            cookies += item.substringBefore(";")
                            token = item.substringAfter("XSRF-TOKEN=").substringBefore(";")
                            sharedPrefHandler.saveString("token", token)
                        }
                        if (item.contains("difa_php_session")) {
                            cookies += item.substringBefore(";")
                            session = item.substringAfter("difa_php_session=").substringBefore(";")
                            sharedPrefHandler.saveString("session", session)
                        }
                    }

                    binding.authText.text = "token: $token\nsesion: $session"
                    binding.progressBar.isVisible = false
                }

                override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                    binding.authText.text = t.toString()
                    binding.progressBar.isVisible = false
                }
            })
        }

        binding.getInfo.setOnClickListener {
            binding.progressBar.isVisible = true
            getApiService().getMotorDetails(cookies, token).enqueue(
                object : Callback<ArrayList<MotorDetails>> {
                    override fun onResponse(
                        call: Call<ArrayList<MotorDetails>>,
                        response: Response<ArrayList<MotorDetails>>
                    ) {
                        val motorDetails = response.body()
                        println("CODE ${response.code()}")
                        if (motorDetails != null) {
                            binding.infoText.text =
                                "${motorDetails[2].code} : ${motorDetails[2].name}"
                        }
                        binding.progressBar.isVisible = false
                    }

                    override fun onFailure(call: Call<ArrayList<MotorDetails>>, t: Throwable) {
                        binding.infoText.text = t.toString()
                        binding.progressBar.isVisible = false
                    }
                }
            )
        }
    }

    companion object {
        //const val AUTH_BASE_URL = "http://10.0.2.2:8000/"
        const val AUTH_BASE_URL = "http://172.16.30.36:8000/"
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
//            builder.cookieJar(MyCookieJar())
//            builder.addInterceptor(MyInterceptor())
            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}