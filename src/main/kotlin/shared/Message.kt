package shared

const val PORT = 35888
const val MAX_CHAR = Integer.MAX_VALUE //2147483647

class Message(val message: String) {
    val size: Int = message.toByteArray().size

    val encoded: ByteArray
        get() {
            return "".toByteArray()
        }
}