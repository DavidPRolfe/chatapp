package shared

const val PORT = 35888
const val MAX_CHAR = Integer.MAX_VALUE //2147483647

data class Message(val headers: Headers, val body: String)

data class Headers(val username: String)