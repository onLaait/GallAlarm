package com.github.onlaait.gallalarm

import com.github.onlaait.gallalarm.Log.logger
import kotlin.system.exitProcess

object DefaultExceptionHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        logger.error(e.stackTraceToString())
        exitProcess(1)
    }
}