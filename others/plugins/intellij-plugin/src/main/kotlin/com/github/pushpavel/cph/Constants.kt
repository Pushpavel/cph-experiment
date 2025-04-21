package com.github.pushpavel.cph

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object Constants {
    val IS_DEV = System.getenv("CPH_DEVELOPMENT") == "true"
    val BACKEND_START_TIMEOUT = if (IS_DEV) 2.minutes else 15.seconds
    val DEV_BACKEND_ADDRESS = "http://127.0.0.1:8888"
}