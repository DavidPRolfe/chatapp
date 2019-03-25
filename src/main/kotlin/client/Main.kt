package client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import shared.Closed
import shared.Message
import java.nio.ByteBuffer
import java.net.Socket
import java.lang.System.exit
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*

import shared.PORT
import shared.decodeMessage
import java.net.SocketException

fun main() {
    var ip = "10.112.152.46"

    var client = Client(ip)
    if (!client.connect()) {
        println("Failed to establish connection with chat server")
        exit(0)
    }

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            client.close()
        }
    })

    GlobalScope.launch { while(true){client.recieveMessages()} }

    val scanner = Scanner(System.`in`)
    println("what's your username friend?")
    print("> ")
    val username = scanner.nextLine()
    client.username = username
    while (true) {
        print("> ")
        val message = scanner.nextLine()
        if (message == "/exit") {
            client.close()
            exit(0)
        }
        if (message == "/changeusername") {
            println("new username?")
            print("> ")
            client.username = scanner.nextLine()
            continue
        }
        if (!message.isEmpty()) {
            client.sendMessage(message)
        }
    }
}

class Client(val address: String){

    var username: String = "user"
    private lateinit var serverConnection: Socket

    fun generateHeader(): String {
        return username
    }

    fun connect(): Boolean {
        if (!this::serverConnection.isInitialized || !serverConnection.isConnected) {
            try {
                serverConnection = Socket(address, PORT)
            } catch (e: ConnectException) {
                return false
            } catch (e: UnknownHostException) {
                return false
            }
            return true
        }
        return true
    }

    fun close() {
        if (this::serverConnection.isInitialized && serverConnection.isConnected) {
            serverConnection.getOutputStream().close()
            serverConnection.close()
        }
    }

    fun sendMessage(message: String) {
        if (connect()) {
            try {
                val out = serverConnection.getOutputStream()
                val header = generateHeader()
                val headerSize = ByteBuffer.allocate(4).putInt(header.length).array()
                val messageSize = ByteBuffer.allocate(4).putInt(message.length).array()

                out.write(headerSize)
                out.write(messageSize)
                out.write(header.toByteArray())
                out.write(message.toByteArray())
                out.flush()
            } catch (e: ConnectException) {
                println("failed to send message")
            } catch (e: UnknownHostException) {
                println("failed to send message")
            } catch (e: SocketException) {
                try{
                    serverConnection = Socket(address, PORT)
                    sendMessage(message)
                } catch (e: ConnectException) {
                    println("cannot reconnect, try again soon \uD83E\uDD23")
                }
            }
        } else {
            println("failed to re-establish connection")
            exit(0)
        }
    }

    fun recieveMessages() {

        val input = serverConnection.getInputStream().buffered()
        try {
            read@ while (serverConnection.isConnected && !serverConnection.isClosed) {
                when (val message = decodeMessage(input)) {
                    is Closed -> { break@read }
                    is Error -> {
                        println("\n ${message.message}")
                        break@read
                    }
                    is Message -> {
                        println("\n${message.headers.username}: ${message.body}")
                    }
                }
            }
        } catch (e: SocketException) {
            println(e)
        }
    }
}