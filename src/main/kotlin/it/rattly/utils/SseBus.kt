package it.rattly.utils

import jakarta.ws.rs.sse.Sse
import jakarta.ws.rs.sse.SseEventSink
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest

private data class Event(val name: String, val data: String)
object SseBus {
    private val flows: MutableMap<String, MutableSharedFlow<Event>> = mutableMapOf()

    suspend fun publish(id: String, eventName: String = "message", data: String = ".") {
        if (flows[id] == null) {
            flows[id] = MutableSharedFlow()
        }

        flows[id]!!.emit(Event(eventName, data))
    }

    suspend fun flow(sink: SseEventSink, sse: Sse, id: String) {
        if (flows[id] == null) {
            flows[id] = MutableSharedFlow()
        }

        coroutineScope {
            flows[id]!!.collectLatest {
                if (sink.isClosed) {
                    this.cancel()
                    return@collectLatest
                }

                sink.send(sse.newEvent(it.name, it.data))
            }
        }
    }
}