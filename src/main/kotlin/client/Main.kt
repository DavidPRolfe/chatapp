package client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import shared.*
import java.nio.ByteBuffer
import java.net.Socket
import java.lang.System.exit
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*

private const val IP = "10.112.152.46"
private val USERNAME = ""

fun main() {

    val client = Client(IP)
    client.username = USERNAME
    if (!client.connect()) {
        println("Failed to establish connection with chat server")
        exit(0)
    }

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            client.close()
        }
    })

    GlobalScope.launch { while(true){client.receiveMessages()} }

    val scanner = Scanner(System.`in`)
    while (true) {
        print("> ")
        val message = scanner.nextLine()
        if (message == "/exit") {
            client.close()
            exit(0)
        }
        if (!message.isEmpty() && message.length < MAX_CHAR) {
            try {
                client.sendMessage(message)
            } catch (e: SendMessageException) {
                println(e.message)
                if (!client.connect()) {
                    println("Cannot connect to server right now")
                }
                client.sendMessage(message)
            }
        }
    }
}

