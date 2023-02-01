package com.omar.pcconnector.network.exceptions




abstract class NetworkException(msg: String = "Network error"): RuntimeException(msg) {}

object NoWifiNetworkException: NetworkException("No Wifi Connection Available")
object NoInternetConnectionException: NetworkException("No Internet Connection Available")