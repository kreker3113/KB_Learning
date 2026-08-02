package dev.kbwallet.server.routes

import dev.kbwallet.server.models.*
import dev.kbwallet.server.plugins.getUserRepository
import dev.kbwallet.server.plugins.getPasswordHasher
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRouting() {
    route("/api/user") {

        // Get current user profile
        authenticate("auth-jwt") {
            get("/profile") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: run {
                        call.respond(HttpStatusCode.Unauthorized, ErrorResponse("unauthorized", "Authentication required"))
                        return@get
                    }

                val userRepository = call.getUserRepository()
                val user = userRepository.findById(userId)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "User not found"))
                    return@get
                }

                call.respond(HttpStatusCode.OK, user.toResponse())
            }

            // Update profile
            put("/profile") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: run {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@put
                    }

                val request = call.receive<UpdateProfileRequest>()
                val userRepository = call.getUserRepository()
                val user = userRepository.findById(userId)
                    ?: run {
                        call.respond(HttpStatusCode.NotFound)
                        return@put
                    }

                val updated = user.copy(
                    username = request.username ?: user.username,
                    avatarUrl = request.avatarUrl ?: user.avatarUrl,
                    bio = request.bio ?: user.bio,
                    updatedAt = System.currentTimeMillis()
                )
                userRepository.update(updated)

                call.respond(HttpStatusCode.OK, updated.toResponse())
            }

            // Change password
            put("/password") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: run {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@put
                    }

                val request = call.receive<ChangePasswordRequest>()
                val userRepository = call.getUserRepository()
                val hasher = call.getPasswordHasher()
                val user = userRepository.findById(userId)
                    ?: run {
                        call.respond(HttpStatusCode.NotFound)
                        return@put
                    }

                if (!hasher.verify(request.currentPassword, user.passwordHash)) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("wrong_password", "Current password is incorrect"))
                    return@put
                }

                if (request.newPassword.length < 6) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("weak_password", "New password must be at least 6 characters"))
                    return@put
                }

                val newHash = hasher.hash(request.newPassword)
                val updated = user.copy(passwordHash = newHash, updatedAt = System.currentTimeMillis())
                userRepository.update(updated)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Password updated successfully"))
            }

            // Delete account
            delete("/profile") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: run {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@delete
                    }

                val userRepository = call.getUserRepository()
                val deleted = userRepository.delete(userId)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Account deleted successfully"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "User not found"))
                }
            }
        }
    }
}
