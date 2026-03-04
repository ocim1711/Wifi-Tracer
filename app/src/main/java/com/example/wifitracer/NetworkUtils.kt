package com.example.wifitracer

import android.content.Context
import android.net.DhcpInfo
import android.net.wifi.WifiManager
import java.net.InetAddress
import java.util.*

object NetworkUtils {

    fun getSubnet(context: Context): String? {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcpInfo: DhcpInfo = wifiManager.dhcpInfo
        val ipAddress = dhcpInfo.ipAddress
        
        if (ipAddress == 0) return null
        
        val ip = String.format(
            Locale.US,
            "%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff
        )
        return ip
    }

    fun getGatewayIp(context: Context): String? {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcpInfo: DhcpInfo = wifiManager.dhcpInfo
        val gateway = dhcpInfo.gateway
        
        if (gateway == 0) return null
        
        return String.format(
            Locale.US,
            "%d.%d.%d.%d",
            gateway and 0xff,
            gateway shr 8 and 0xff,
            gateway shr 16 and 0xff,
            gateway shr 24 and 0xff
        )
    }
}
