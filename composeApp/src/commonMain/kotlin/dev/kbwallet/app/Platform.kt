package dev.kbwallet.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform