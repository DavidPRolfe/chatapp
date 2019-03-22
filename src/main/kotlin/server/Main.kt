package server

import shared.PORT

fun main() {
    startServer(PORT, MemoryStore())
}
