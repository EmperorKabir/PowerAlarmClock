package com.poweralarm.integrations.restapi

import com.poweralarm.core.domain.model.Alarm
import com.poweralarm.core.domain.model.DomainJson
import com.poweralarm.core.domain.port.AlarmRepository
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer

/**
 * Token-protected local-LAN HTTP server. Bind interface and port come from settings.
 * The token is passed as `Authorization: Bearer <token>`.
 */
class AlarmHttpServer(
    private val repository: AlarmRepository,
    private val tokenHash: String,
    private val tokenMatcher: (String) -> Boolean,
    private val port: Int,
    private val host: String,
) {
    private var engine: ApplicationEngine? = null

    fun start() {
        engine = embeddedServer(CIO, port = port, host = host) {
            install(ContentNegotiation) { json(DomainJson) }
            install(Authentication) {
                bearer {
                    realm = "Power Alarm"
                    authenticate { credential ->
                        if (tokenMatcher(credential.token)) Principal else null
                    }
                }
            }
            routing {
                authenticate {
                    get("/alarms") { call.respond(repository.all()) }
                    get("/alarms/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatus404())
                        val a = repository.byId(id) ?: return@get call.respond(HttpStatus404())
                        call.respond(a)
                    }
                    post("/alarms") {
                        val alarm = call.receive<Alarm>()
                        val newId = repository.save(alarm)
                        call.respond(mapOf("id" to newId))
                    }
                    put("/alarms/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatus404())
                        val alarm = call.receive<Alarm>().copy(id = id)
                        repository.save(alarm)
                        call.respond(mapOf("id" to id))
                    }
                    delete("/alarms/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatus404())
                        repository.delete(id)
                        call.respond(mapOf("deleted" to id))
                    }
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        engine?.stop(0L, 0L)
        engine = null
    }
}

private object Principal : io.ktor.server.auth.Principal
private class HttpStatus404 : Throwable("not found")
