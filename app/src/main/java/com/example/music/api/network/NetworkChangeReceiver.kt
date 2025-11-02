package com.example.music.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkChangeReceiver(
    private val listener: NetworkStatusListener
) : BroadcastReceiver() {

    interface NetworkStatusListener {
        fun onNetworkConnected()
        fun onNetworkDisconnected()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        val isConnected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            val activeNetwork = connectivityManager?.activeNetworkInfo
            @Suppress("DEPRECATION")
            activeNetwork?.isConnected == true
        }

        if (isConnected) {
            listener.onNetworkConnected()
        } else {
            listener.onNetworkDisconnected()
        }
    }

    companion object {
        fun registerReceiver(context: Context, receiver: NetworkChangeReceiver) {
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            context.registerReceiver(receiver, filter)
        }

        fun unregisterReceiver(context: Context, receiver: NetworkChangeReceiver) {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered
            }
        }
    }
}