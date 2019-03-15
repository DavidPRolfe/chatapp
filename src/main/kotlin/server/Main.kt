package server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import shared.Headers
import shared.Message
import java.io.InputStream
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.ByteOrder

const val SERVERPORT = 35888

fun main() {
    val socket = ServerSocket(SERVERPORT)
    while (true) {
        val client = socket.accept()
        GlobalScope.launch { handleConnection(client) }
    }
}

fun handleConnection(sock: Socket) {
    println("connected to ${sock.inetAddress.hostName}")
    val out = PrintWriter(sock.getOutputStream(), true)
    val input = sock.getInputStream().buffered()

    try {
        while (sock.isConnected && !sock.isClosed) {
            val message = processInput(input) ?: break
            println(message)
            out.print(message)
        }
    } catch (e: SocketException) {
        return
    }

}

fun processInput(input: InputStream): Message? {
    // Reads header size
    val headerSize = ByteArray(4)
    if (input.read(headerSize) != 4) {
        println(headerSize.toInt())
        println("Didn't read header size")
        return null
    }

    // Reads message size
    val messageSize = ByteArray(4)
    if (input.read(messageSize) != 4) {
        println("Didn't read message size")
        return null
    }

    // Read header
    val header = ByteArray(headerSize.toInt())
    if (input.read(header) != headerSize.toInt()) {
        println("Didn't read header")
        return null
    }

    // Read message
    val message = ByteArray(messageSize.toInt())
    if (input.read(message) != messageSize.toInt()) {
        println("Didn't read message")
        return null
    }

    return Message(Headers(String(header)), String(message))
}

fun ByteArray.toInt(): Int = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN).int