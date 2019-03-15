package shared

class Message(val message: String) {
    val size: Int = message.toByteArray().size

    val encoded: ByteArray
        get() {
            return "".toByteArray()
        }
}