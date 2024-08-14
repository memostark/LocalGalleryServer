package com.guillermonegrete.gallery.config

import org.springframework.context.annotation.Configuration
import java.net.DatagramSocket
import java.net.InetAddress

@Configuration
class NetworkConfig {

    fun getLocalIpAddress(): String {
        return System.getenv("OUTBOUND_IP_ADDRESS")
            ?: DatagramSocket().use { datagramSocket ->
                datagramSocket.connect(InetAddress.getByName("8.8.8.8"), 12345)
                return datagramSocket.localAddress.hostAddress
            }
    }
}