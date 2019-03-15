package client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.ServerSocket
import java.net.Socket

const val SERVERPORT = 27027

fun main() {
    val socket = ServerSocket(SERVERPORT)
    while (true) {
        val client = socket.accept()
        GlobalScope.launch { handleConnection(client) }
    }
}

suspend fun handleConnection(sock: Socket) {

}