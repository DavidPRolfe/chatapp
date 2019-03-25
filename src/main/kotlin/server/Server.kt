package server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import shared.Closed
import shared.Message
import shared.Response
import shared.decodeMessage
import java.io.InputStream
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

fun server(port: Int, handleConnection: (Socket) -> Unit) {
    val socket = ServerSocket(port)
    while (true) {
        val client = socket.accept()
        GlobalScope.launch { handleConnection(client) }
    }
}

fun handleConnection(sock: Socket, dataStore: DataStore, decode: (InputStream) -> Response) {
    println("connected to ${sock.inetAddress.hostName}")
    val out = sock.getOutputStream()
    val input = sock.getInputStream().buffered()
    val newMessage = { message: Message ->
        out.write(message.toByteArray())
        out.flush()
    }
    dataStore.addListener(newMessage)

    try {
        if (sock.isConnected && !sock.isClosed) {
            dataStore.messages.forEach{
                out.write(it.toByteArray())
                out.flush()
            }
        }
    } catch (e: SocketException) {
        println(e)
        sock.close()
    }

    try {
        read@ while (sock.isConnected && !sock.isClosed) {
            when (val message = decode(input)) {
                is Closed -> { break@read }
                is Error -> {
                    println(message.message)
                    break@read
                }
                is Message -> {
                    println(message.headers)
                    println(message.body)
                    dataStore.add(message)
                }
            }
        }
    } catch (e: SocketException) {
        println(e)
    }
    dataStore.removeListener(newMessage)
    println(newMessage)
    sock.close()
}

fun startServer(port: Int, dataStore: DataStore) {
    server(port) { sock ->
        handleConnection(sock, dataStore) {
            decodeMessage(it)
        }
    }
}
