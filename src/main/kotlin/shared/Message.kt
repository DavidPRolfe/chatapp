package shared

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

const val PORT = 35888
const val MAX_CHAR = Integer.MAX_VALUE //2147483647

data class Message(val headers: Headers, val body: String)

data class Headers(val username: String)

fun decodeMessage(input: InputStream): Message? {
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