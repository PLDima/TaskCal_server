package inc.dimasic.taskcal.backend.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.path
import org.slf4j.event.Level

/** Logs method, path and status only; headers (which carry the token) are never logged. */
fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { it.request.path().startsWith("/api") }
    }
}
