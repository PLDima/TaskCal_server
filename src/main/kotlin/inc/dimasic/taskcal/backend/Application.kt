package inc.dimasic.taskcal.backend

import inc.dimasic.taskcal.backend.api.route.authRoutes
import inc.dimasic.taskcal.backend.api.route.foodRoutes
import inc.dimasic.taskcal.backend.api.route.meRoutes
import inc.dimasic.taskcal.backend.api.route.taskRoutes
import inc.dimasic.taskcal.backend.data.AppDatabase
import inc.dimasic.taskcal.backend.data.DatabaseConfig
import inc.dimasic.taskcal.backend.data.repository.CompletionRepository
import inc.dimasic.taskcal.backend.data.repository.FoodRepository
import inc.dimasic.taskcal.backend.data.repository.ProfileRepository
import inc.dimasic.taskcal.backend.data.repository.TaskRepository
import inc.dimasic.taskcal.backend.data.repository.UserRepository
import inc.dimasic.taskcal.backend.domain.service.AuthService
import inc.dimasic.taskcal.backend.domain.service.FoodService
import inc.dimasic.taskcal.backend.domain.service.ProfileService
import inc.dimasic.taskcal.backend.domain.service.TaskService
import inc.dimasic.taskcal.backend.plugins.DEVICE_AUTH
import inc.dimasic.taskcal.backend.plugins.configureMonitoring
import inc.dimasic.taskcal.backend.plugins.configureSecurity
import inc.dimasic.taskcal.backend.plugins.configureSerialization
import inc.dimasic.taskcal.backend.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.auth.authenticate
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val appDatabase = AppDatabase.connect(DatabaseConfig.from(environment.config))
    monitor.subscribe(ApplicationStopped) { appDatabase.close() }
    val db = appDatabase.database

    val authService = AuthService(UserRepository(db))
    val profileService = ProfileService(ProfileRepository(db))
    val taskService = TaskService(TaskRepository(db), CompletionRepository(db))
    val foodService = FoodService(FoodRepository(db))

    configureSerialization()
    configureMonitoring()
    configureStatusPages()
    configureSecurity(authService)

    routing {
        get("/health") { call.respondText("OK") }
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

        route("/api/v1") {
            authRoutes(authService)
            authenticate(DEVICE_AUTH) {
                meRoutes(authService, profileService)
                taskRoutes(taskService)
                foodRoutes(foodService)
            }
        }
    }
}
