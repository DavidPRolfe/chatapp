package client

import shared.*
import java.net.ConnectException
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException

class Client(private val address: String){
    var username: String = "user"
    private lateinit var serverConnection: Socket

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

    @Throws(SendMessageException::class)
    fun sendMessage(body: String) {
        if (this.connect()) {
            try {
                val message = Message(Headers(this.username), body)
                val out = serverConnection.getOutputStream()

                out.write(message.toByteArray())
                out.flush()
                return
            } catch (e: ConnectException) {
                throw SendMessageException("Connection lost while sending message")
            } catch (e: SocketException) {
                throw SendMessageException("Socket connection was terminated, try reconnecting")
            }
        }
        throw SendMessageException("Cannot connect to server")
    }

    fun receiveMessages() {
        val input = serverConnection.getInputStream().buffered()
        try {
            read@ while (serverConnection.isConnected && !serverConnection.isClosed) {
                when (val message = decodeMessage(input)) {
                    is Closed -> { break@read }
                    is Err -> {
                        println(message.message)
                        break@read
                    }
                    is Message -> {
                        println("${message.headers.username}: ${message.body}")
                    }
                }
            }
        } catch (e: SocketException) {
            println(e)
        }
    }

    fun close() {
        if (this::serverConnection.isInitialized && serverConnection.isConnected) {
            serverConnection.getOutputStream().close()
            serverConnection.close()
        }
    }


}

class SendMessageException(errorMessage: String) : Exception(errorMessage)