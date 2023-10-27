package it.rattly

import io.quarkus.qute.Template
import it.rattly.utils.SseBus
import it.rattly.utils.getAllLists
import it.rattly.utils.submitVote
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.sse.Sse
import jakarta.ws.rs.sse.SseEventSink
import org.jboss.resteasy.reactive.RestResponse
import org.jooq.DSLContext

@Path("/")
class Index(val index: Template, val dsl: DSLContext) {
    companion object {
        internal var isVotingLocked = false
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    suspend fun get() =
        index.data("items", getAllLists(dsl))!!

    @POST
    @Path("/sendVote")
    @Consumes(MediaType.WILDCARD)
    suspend fun sendVoto(@QueryParam("id") voted: Int): RestResponse<String> {
        if (isVotingLocked)
            return RestResponse.status(403)

        isVotingLocked = true

        submitVote(dsl, voted)
        SseBus.publish("refreshTerminals", "vote")

        return RestResponse.ok()
    }

    @GET
    @Path("/tablePartial")
    suspend fun tablePartial(): Any =
        if (isVotingLocked)
            "<h1 style=\"text-align: center\">Wait for this terminal to get unlocked by an admin...</h1>"
        else
            index.getFragment("table").data("items", getAllLists(dsl))

    @GET
    @Path("/refreshTerminals")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    suspend fun indexSse(
        @Context sink: SseEventSink,
        @Context sse: Sse
    ) = SseBus.flow(sink, sse, "refreshTerminals")
}
