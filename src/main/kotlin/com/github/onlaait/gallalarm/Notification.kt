package com.github.onlaait.gallalarm

import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import javax.imageio.ImageIO
import kotlin.system.exitProcess

object Notification {

    private val trayIcon: TrayIcon

    init {
        val img = ImageIO.read(Thread.currentThread().contextClassLoader.getResourceAsStream("icon.png"))
        trayIcon = TrayIcon(img, "GallAlarm").apply {
            isImageAutoSize = true
        }
        val menuItem = MenuItem("Exit")
        menuItem.addActionListener { exitProcess(0) }
        val popupMenu = PopupMenu()
        popupMenu.add(menuItem)
        trayIcon.popupMenu = popupMenu
        SystemTray.getSystemTray().add(trayIcon)
    }

    fun display(title: String, content: String) {
        trayIcon.displayMessage(title, content, TrayIcon.MessageType.NONE)
        Log.logger.info("새로운 알림: ([$title]: [$content])")
    }
}