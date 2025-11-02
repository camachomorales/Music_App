package com.example.music.network.utility

import android.app.Activity
import com.google.gson.Gson
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class RequestNetworkController private constructor() {

    private var client: OkHttpClient? = null

    companion object {
        const val GET = "GET"
        const val POST = "POST"
        const val PUT = "PUT"
        const val DELETE = "DELETE"

        const val REQUEST_PARAM = 0
        const val REQUEST_BODY = 1

        private const val SOCKET_TIMEOUT = 15000L
        private const val READ_TIMEOUT = 25000L

        @Volatile
        private var instance: RequestNetworkController? = null

        fun getInstance(): RequestNetworkController {
            return instance ?: synchronized(this) {
                instance ?: RequestNetworkController().also { instance = it }
            }
        }
    }

    private fun getClient(): OkHttpClient {
        if (client == null) {
            val builder = OkHttpClient.Builder()

            try {
                val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

                        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    }
                )

                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAllCerts, SecureRandom())
                val sslSocketFactory = sslContext.socketFactory

                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.connectTimeout(SOCKET_TIMEOUT, TimeUnit.MILLISECONDS)
                builder.readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                builder.writeTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                builder.hostnameVerifier { _, _ -> true }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            client = builder.build()
        }

        return client!!
    }

    fun execute(
        requestNetwork: RequestNetwork,
        method: String,
        url: String,
        tag: String,
        requestListener: RequestNetwork.RequestListener
    ) {
        val reqBuilder = Request.Builder()
        val headerBuilder = Headers.Builder()

        if (requestNetwork.getHeaders().isNotEmpty()) {
            val headers = requestNetwork.getHeaders()
            for ((key, value) in headers) {
                headerBuilder.add(key, value.toString())
            }
        }

        try {
            if (requestNetwork.getRequestType() == REQUEST_PARAM) {
                if (method == GET) {
                    val httpUrlBuilder = try {
                        url.toHttpUrl().newBuilder()
                    } catch (e: IllegalArgumentException) {
                        throw NullPointerException("unexpected url: $url")
                    }

                    if (requestNetwork.getParams().isNotEmpty()) {
                        val params = requestNetwork.getParams()
                        for ((key, value) in params) {
                            httpUrlBuilder.addQueryParameter(key, value.toString())
                        }
                    }

                    reqBuilder.url(httpUrlBuilder.build()).headers(headerBuilder.build()).get()
                } else {
                    val formBuilder = FormBody.Builder()
                    if (requestNetwork.getParams().isNotEmpty()) {
                        val params = requestNetwork.getParams()
                        for ((key, value) in params) {
                            formBuilder.add(key, value.toString())
                        }
                    }

                    val reqBody = formBuilder.build()
                    reqBuilder.url(url).headers(headerBuilder.build()).method(method, reqBody)
                }
            } else {
                val reqBody = Gson().toJson(requestNetwork.getParams())
                    .toRequestBody("application/json".toMediaType())

                if (method == GET) {
                    reqBuilder.url(url).headers(headerBuilder.build()).get()
                } else {
                    reqBuilder.url(url).headers(headerBuilder.build()).method(method, reqBody)
                }
            }

            val req = reqBuilder.build()

            getClient().newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    requestNetwork.getActivity().runOnUiThread {
                        requestListener.onErrorResponse(tag, e.message ?: "Unknown error")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()?.trim() ?: ""
                    val activity = requestNetwork.getActivity()

                    if (!activity.isFinishing) {
                        activity.runOnUiThread {
                            val responseHeaders = response.headers
                            val map = HashMap<String, Any>()
                            for (name in responseHeaders.names()) {
                                map[name] = responseHeaders[name] ?: "null"
                            }
                            requestListener.onResponse(tag, responseBody, map)
                        }
                    }
                }
            })
        } catch (e: Exception) {
            requestListener.onErrorResponse(tag, e.message ?: "Unknown error")
        }
    }
}