package server

import shared.*
import java.net.Socket
import java.net.SocketException

class Session(private val sock: Socket) {
    private val decode = ::decodeMessage
    private val output = sock.getOutputStream()
    private val input = sock.getInputStream().buffered()

    val closed: Boolean
        get() = sock.isClosed || !sock.isConnected || sock.isInputShutdown || sock.isInputShutdown

    init {
        println("connected to ${sock.inetAddress.hostName}")
    }

    fun read(): Response = if (closed) {
        Closed
    } else {
        try {
            decode(input)
        } catch (e: SocketException) {
            println(e)
            Err(e.message ?: "")
        }
    }


    fun write(message: Message): Closed? {
        if (closed) {
            return Closed
        } else {
            output.write(message.toByteArray())
            output.flush()
        }
        return null
    }

    fun close() {
        sock.close()
    }
}