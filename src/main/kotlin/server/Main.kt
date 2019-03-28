package server

import shared.PORT

fun main() {
    server(PORT, MemoryStore())
}
