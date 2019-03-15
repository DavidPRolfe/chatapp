package shared

import java.lang.IllegalArgumentException

data class Message(val headers: Headers, val body: String)

data class Headers(val username: String)