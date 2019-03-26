package shared

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

const val PORT = 35888
const val MAX_CHAR = Integer.MAX_VALUE //2147483647

sealed class Response

sealed class FailedRead: Response()

data class Message(val headers: Headers, val body: String): Response() {
    fun toByteArray(): ByteArray {
        val headerArray = headers.toByteArray()
        val headerSize = headerArray.size.toByteArray()

        val message = body.toByteArray()
        val messageSize = message.size.toByteArray()

        return headerSize + messageSize + headerArray + message
    }
}

data class Error(val message: String): FailedRead()

object Closed: FailedRead()

data class Headers(val username: String) {
    fun toByteArray(): ByteArray = username.toByteArray()
}

fun decodeMessage(input: InputStream): Response {
    // Reads header size
    val headerSize = ByteArray(4)
    when (val status = readBytes(input, headerSize)) {
        is Error, Closed -> return status
    }

    // Reads message size
    val messageSize = ByteArray(4)
    when (val status = readBytes(input, messageSize)) {
        is Error, Closed -> return status
    }

    // Read header
    val header = ByteArray(headerSize.toInt())
    when (val status = readBytes(input, header)) {
        is Error, Closed -> return status
    }

    // Read message
    val message = ByteArray(messageSize.toInt())
    when (val status = readBytes(input, message)) {
        is Error, Closed -> return status
    }

    return Message(Headers(String(header)), String(message))
}

fun readBytes(input: InputStream, byteArray: ByteArray): FailedRead? {
    val readSize = input.read(byteArray)
    // Reading 0 terminates the connection
    if (readSize == 0) {
        return Closed
    } else if (readSize != byteArray.size) {
        return Error("Failed to read expected number of bytes. Expected: ${byteArray.size}, Read $readSize")
    }
    return null
}

fun ByteArray.toInt(): Int = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN).int

fun Int.toByteArray(): ByteArray = ByteBuffer.allocate(4).putInt(this).array()