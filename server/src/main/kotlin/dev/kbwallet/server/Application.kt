package dev.kbwallet.server

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import dev.kbwallet.server.plugins.*

// EngineMain reads application.conf (ktor.deployment.port/host and
// ktor.application.modules) so environment.config.property(...) calls in the
// plugins below actually resolve — a manual embeddedServer(...) call here
// does NOT load application.conf and every config lookup fails at startup.
fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureSerialization()
    configureDependencies()
    configureSecurity()
    configureRouting()
}
