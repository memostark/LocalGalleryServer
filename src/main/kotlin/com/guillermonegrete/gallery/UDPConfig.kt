package com.guillermonegrete.gallery

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.ip.udp.UnicastReceivingChannelAdapter
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.stereotype.Service

@Configuration
class UDPConfig {

    @Bean
    fun processUniCastUdpMessage(): IntegrationFlow {
        return IntegrationFlows
                .from(UnicastReceivingChannelAdapter(80).apply {
                    receiveBufferSize = 1024
                    setLookupHost(false)
                })
                .handle("UDPServer", "handleMessage")
                .get()
    }
}


@Service
class UDPServer {
    fun handleMessage(message: Message<Any>) {
        val data = String((message.payload as ByteArray))
        if(data == EXPECTED_MSG){
            val address = message.headers["ip_address"] ?: return
            val portBlob = message.headers["ip_port"] ?: return
            val port = portBlob.toString().toInt()

            val handler = UnicastSendingMessageHandler(address.toString(), port)
            handler.handleMessage(MessageBuilder.withPayload(RESPONSE_MSG).build())
        }
    }

    companion object{
        const val EXPECTED_MSG = "DISCOVER_FUIFSERVER_REQUEST"
        const val RESPONSE_MSG = "DISCOVER_FUIFSERVER_RESPONSE"
    }
}
