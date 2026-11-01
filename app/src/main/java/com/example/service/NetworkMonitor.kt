package com.example.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.model.NetworkStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun observeNetworkStatus(): Flow<NetworkStatus> = callbackFlow {
        fun computeCurrentStatus(): NetworkStatus {
            return try {
                val activeNetwork = connectivityManager.activeNetwork ?: return NetworkStatus.Disconnected
                val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
                    ?: return NetworkStatus.Disconnected

                val hasTransport = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)

                if (!hasTransport) {
                    NetworkStatus.Disconnected
                } else {
                    val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    val isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    when {
                        hasInternet && isValidated -> NetworkStatus.ConnectedValidated
                        hasInternet -> NetworkStatus.ConnectedNoInternet
                        else -> NetworkStatus.Disconnected
                    }
                }
            } catch (_: Exception) {
                NetworkStatus.Disconnected
            }
        }

        // Emit immediate state on subscription
        trySend(computeCurrentStatus())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(computeCurrentStatus())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(computeCurrentStatus())
            }

            override fun onLost(network: Network) {
                trySend(computeCurrentStatus())
            }

            override fun onUnavailable() {
                trySend(NetworkStatus.Disconnected)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        runCatching {
            connectivityManager.registerNetworkCallback(request, callback)
        }

        awaitClose {
            runCatching {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
    }.distinctUntilChanged()
}
