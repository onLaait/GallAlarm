package com.github.onlaait.gallalarm

import kotlin.system.exitProcess

object DefaultExceptionHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.logger.error(e.stackTraceToString())
        exitProcess(999)
    }
}