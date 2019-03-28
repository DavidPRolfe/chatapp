package server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import shared.Closed
import shared.Err
import shared.Message
import java.net.ServerSocket

fun server(port: Int, dataStore: DataStore) {
    val socket = ServerSocket(port)
    while (true) {
        val connection = socket.accept()
        CoroutineScope(Dispatchers.Default).launch {
            manageSession(dataStore, Session(connection))
        }
    }
}

fun manageSession(dataStore: DataStore, session: Session) {
    var connected = true
    val listener = { message: Message ->
        if (session.write(message) is Closed) {
            connected = false
        }
    }
    dataStore.addListener(listener)

    readLoop@ while (connected) {
        when (val resp = session.read()) {
            is Message -> {
                println(resp.headers)
                println(resp.body)
                dataStore.add(resp)
            }
            is Closed -> break@readLoop
            is Err -> {
                println(resp.message)
            }
        }
    }

    dataStore.removeListener(listener)
    session.close()
}