package server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
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

fun handleConnection(sock: Socket) {
    println("connected to ${sock.inetAddress.hostName}")
    val out = PrintWriter(sock.getOutputStream(), true)
    val input = BufferedReader(InputStreamReader(sock.getInputStream()))

    for (line in input.lines()) {
        println(line)
        out.println(line)
    }
}