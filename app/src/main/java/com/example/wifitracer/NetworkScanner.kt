package com.example.wifitracer

import java.net.InetAddress
import java.io.BufferedReader
import java.io.FileReader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NetworkScanner {

    fun scan(subnet: String, callback: (List<Device>) -> Unit) {
        val devices = mutableListOf<Device>()
        val executor = Executors.newFixedThreadPool(30)

        for (i in 1..254) {
            val ip = "$subnet.$i"
            executor.execute {
                try {
                    val address = InetAddress.getByName(ip)
                    if (address.isReachable(1000)) {
                        val mac = getMacFromArpTable(ip)
                        synchronized(devices) {
                            devices.add(Device(ip, mac))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        executor.shutdown()
        executor.awaitTermination(30, TimeUnit.SECONDS)
        callback(devices)
    }

    private fun getMacFromArpTable(ip: String): String {
        try {
            val reader = BufferedReader(FileReader("/proc/net/arp"))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line?.contains(ip) == true) {
                    val parts = line!!.split("\\s+".toRegex())
                    if (parts.size >= 4) {
                        return parts[3]
                    }
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "Unknown"
    }
}
