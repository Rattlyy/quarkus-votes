package it.rattly.utils

import it.rattly.jooq.tables.Lists.Companion.LISTS
import it.rattly.jooq.tables.Votes.Companion.VOTES
import kotlinx.coroutines.future.await
import org.jooq.DSLContext
import org.jooq.impl.DSL

typealias VOTESMap = Map<Int, Triple<String, Int, String>>

suspend fun getAllLists(dsl: DSLContext) = dsl.selectFrom(LISTS).fetchAsync().await().toList()
suspend fun submitVote(dsl: DSLContext, lista: Int): Int =
    dsl.insertInto(VOTES).values(DSL.defaultValue(), lista).executeAsync().await()

suspend fun getAllVotes(dsl: DSLContext) = mutableMapOf<Int, Triple<String, Int, String>>().apply {
    dsl.selectFrom(VOTES.join(LISTS).on(VOTES.VOTEDLIST.eq(LISTS.ID))).fetchAsync().await().forEach { e ->
        val id = e[LISTS.ID]!!
        val item = this[id]

        this[id] =
            if (item == null)
                Triple(e[LISTS.NAME]!!, 1, e[LISTS.COLOR]!!)
            else
                Triple(item.first, item.second + 1, item.third)
    }
}

fun <T> Iterable<T>.jsArray() =
    this.joinToString(",")