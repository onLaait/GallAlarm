package com.github.onlaait.gallalarm

import com.github.onlaait.gallalarm.Log.logger
import org.apache.commons.text.StringEscapeUtils
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit

object Notification {

    private val script =
        Thread.currentThread().contextClassLoader.getResourceAsStream("script.ps1")!!.reader().use { it.readText() }

    fun display(title: String, content: String, url: String) {
        logger.info("새로운 알림: ([$title]: [$content])")
        val cmd = script
            .replace("%title%", StringEscapeUtils.escapeXml11(title))
            .replace("%content%", StringEscapeUtils.escapeXml11(content))
            .replace("%url%", url)
        val process =
            ProcessBuilder(
                "powershell.exe",
                "-EncodedCommand",
                Base64.getEncoder().encodeToString(cmd.toByteArray(StandardCharsets.UTF_16LE))
            ).start()
        process.waitFor(3, TimeUnit.SECONDS)
        val errorReader = process.errorReader()
        if (errorReader.ready()) logger.error(errorReader.use { it.readText() })
    }
}