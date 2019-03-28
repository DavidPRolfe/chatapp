package server

import shared.Message

interface DataStore {
    val messages: List<Message>

    fun add(message: Message)
    fun addListener(listener: (Message) -> Unit)
    fun removeListener(listener: (Message) -> Unit)
}

class MemoryStore(private val max: Int = 100): DataStore {
    private val messageStore = mutableListOf<Message>()
    override val messages: List<Message>
        get() = messageStore

    private val listeners: MutableList<(Message) -> Unit> = mutableListOf()

    override fun add(message: Message) {
        if (messageStore.size > max) {
            messageStore.removeAt(0)
        }
        messageStore.add(message)

        notifyListeners(message)
    }

    private fun notifyListeners(message: Message) {
        listeners.forEach { it(message) }
    }

    override fun addListener(listener: (Message) -> Unit) {
        listeners.add(listener)
    }

    override fun removeListener(listener: (Message) -> Unit) {
        listeners.remove(listener)
    }
}