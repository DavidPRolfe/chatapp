package client

import java.nio.ByteBuffer
import java.net.Socket
import java.lang.System.exit
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*

import shared.PORT
import java.io.InputStream
import java.io.OutputStream
import java.lang.System.out
import java.net.SocketException

fun main() {
    var ip = "10.112.146.242"

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

    val scanner = Scanner(System.`in`)
    println("what's your username friend?")
    print("> ")
    val username = scanner.nextLine()
    client.username = username
    while (true) {
        print("> ")
        val message = scanner.nextLine()
        if (message == "exit") {
            client.close()
            exit(0)
        }
        if (message == "change username") {
            println("new username?")
            print("> ")
            client.username = scanner.nextLine()
            continue
        }
        client.sendMessage(message)
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
            println("sent message")
        } else {
            println("failed to re-establish connection")
            exit(0)
        }

    }
}