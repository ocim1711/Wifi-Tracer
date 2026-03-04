package com.example.wifitracer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val scanner = NetworkScanner()
    private val spoofer = ArpSpoofer()
    private val disruptor = NoRootDisruptor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WifiTracerTheme {
                MainScreen(scanner, spoofer, disruptor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(scanner: NetworkScanner, spoofer: ArpSpoofer, disruptor: NoRootDisruptor) {
    var devices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var isRooted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isRooted = spoofer.isRooted()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Wifi Tracer") })
        },
        floatingActionButton = {
            val context = androidx.compose.ui.platform.LocalContext.current
            FloatingActionButton(onClick = {
                val subnet = NetworkUtils.getSubnet(context)
                if (subnet != null) {
                    isScanning = true
                    coroutineScope.launch {
                        scanner.scan(subnet) { foundDevices ->
                            devices = foundDevices
                            isScanning = false
                        }
                    }
                }
            }) {
                Text(if (isScanning) "..." else "Scan")
            }
        }
    ) { padding ->
        val context = androidx.compose.ui.platform.LocalContext.current
        val gatewayIp = NetworkUtils.getGatewayIp(context) ?: "192.168.1.1"

        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (!isRooted) {
                Text(
                    "Mode Tanpa Root: Fitur Netcut akan menggunakan UDP Flood untuk mengganggu koneksi.",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            LazyColumn {
                items(devices) { device ->
                    DeviceItem(device, isRooted, spoofer, disruptor, gatewayIp)
                }
            }
        }
    }
}

@Composable
fun DeviceItem(device: Device, isRooted: Boolean, spoofer: ArpSpoofer, disruptor: NoRootDisruptor, gatewayIp: String) {
    var isCut by remember { mutableStateOf(device.isCut) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "IP: ${device.ip}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "MAC: ${device.mac}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Vendor: ${device.vendor}", style = MaterialTheme.typography.bodySmall)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isRooted) "ARP Cut" else "UDP Disruption",
                    style = MaterialTheme.typography.labelSmall
                )
                Switch(
                    checked = isCut,
                    onCheckedChange = { checked ->
                        isCut = checked
                        if (checked) {
                            if (isRooted) {
                                spoofer.startNetcut(device.ip, gatewayIp, "wlan0")
                            } else {
                                disruptor.startDisruption(device.ip)
                            }
                        } else {
                            if (isRooted) {
                                spoofer.stopNetcut(device.ip)
                            } else {
                                disruptor.stopDisruption(device.ip)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun WifiTracerTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
