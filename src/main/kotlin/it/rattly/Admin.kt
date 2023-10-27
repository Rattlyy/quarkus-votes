package it.rattly

import io.quarkus.qute.Template
import it.rattly.utils.SseBus
import it.rattly.utils.getAllVotes
import it.rattly.utils.jsArray
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.jooq.DSLContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

@Path("/admin")
class Admin(val admin: Template, val dsl: DSLContext) {
    @GET
    suspend fun admin() =
        admin.adminData()

    @GET
    @Path("/unlockVotes")
    @Produces(MediaType.TEXT_HTML)
    suspend fun unlockVotes(): String {
        Index.isVotingLocked = false
        SseBus.publish("refreshTerminals", "unlock")

        //language=HTML
        return """
            <div id="flash">
                <b>Re-enabled terminals. ${SimpleDateFormat("HH:mm:ss").format(Date.from(Instant.now()))}</b>
            </div>
            """.trimIndent()
    }

    @GET
    @Path("/piePartial")
    suspend fun piePartial() =
        admin.getFragment("pie").adminData()

    @GET
    @Path("/statusPartial")
    suspend fun statusPartial() =
        admin.getFragment("status").data(
            "isVotingLocked", !Index.isVotingLocked
        )!!

    private suspend fun Template.adminData() = getAllVotes(dsl).run {
        this@adminData.data(
            "chartId", UUID.randomUUID().toString(),
            "votesLabel", this.map { "\"" + it.value.first + "\"" }.jsArray(),
            "colors", this.map { "\"#" + it.value.third + "\"" }.jsArray(),
            "votesData", this.map { it.value.second }.jsArray(),
        ).data("isVotingLocked", !Index.isVotingLocked)!!
    }
}