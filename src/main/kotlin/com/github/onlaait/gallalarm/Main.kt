package com.github.onlaait.gallalarm

import be.zvz.kotlininside.KotlinInside
import be.zvz.kotlininside.api.article.ArticleList
import be.zvz.kotlininside.api.article.ArticleRead
import be.zvz.kotlininside.http.DefaultHttpClient
import be.zvz.kotlininside.session.user.Anonymous
import com.github.onlaait.gallalarm.Log.logger
import org.jsoup.Jsoup
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.io.IOException
import java.util.regex.Pattern
import javax.imageio.ImageIO
import kotlin.system.exitProcess

const val GALL_ID = "steve"

fun main() {
    Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler)

    logger.info("인스턴스 생성 중")

    KotlinInside.createInstance(Anonymous("ㅇㅇ", "1234"), DefaultHttpClient(), true)

    logger.info("시작 중")

    val img = ImageIO.read(Thread.currentThread().contextClassLoader.getResourceAsStream("icon.png"))
    val trayIcon = TrayIcon(img, "GallAlarm").apply {
        isImageAutoSize = true
    }
    val menuItem = MenuItem("Exit")
    menuItem.addActionListener { exitProcess(0) }
    val popupMenu = PopupMenu()
    popupMenu.add(menuItem)
    trayIcon.popupMenu = popupMenu
    SystemTray.getSystemTray().add(trayIcon)

    val domainPattern = Pattern.compile("(([a-zA-Z0-9가-힣]+\\.)+[a-zA-Z가-힣]+|\\d{1,3}(\\.\\d{1,3}){3})(:\\d{1,5})?")
    var lastCheckedId = ArticleList(GALL_ID).apply { request() }.getGallList().first().identifier

    logger.info("시작됨")

    while (true) {
        Thread.sleep(5000)
        val articleList = ArticleList(GALL_ID, headId = 40).apply {
            while (true) {
                try {
                    request()
                } catch (e: IOException) {
                    logger.error("글 목록 읽기 중 오류: $e")
                    Thread.sleep(1000)
                    continue
                }
                break
            }
        }
        for (c in articleList.getGallList().sortedBy { it.identifier }) {
            val id = c.identifier
            if (id <= lastCheckedId) continue

            lastCheckedId = id
            val article = ArticleRead(GALL_ID, id).apply {
                while (true) {
                    try {
                        request()
                    } catch (e: IOException) {
                        logger.error("글 읽기 중 오류: $e")
                        Thread.sleep(1000)
                        continue
                    }
                    break
                }
            }
            val parsed = Jsoup.parse(Jsoup.parse(article.getViewMain().content).text()).body()
            var content = parsed.text()
            getAddress@for (e in parsed.children()) {
                if (e.className().isNotEmpty()) continue
                val text = e.text()
                val matcher = domainPattern.matcher(text)
                while (matcher.find()) {
                    val start = matcher.start()
                    val end = matcher.end()
                    val group = matcher.group()
                    if (start != 0 && text[start - 1] == '/' ||
                        text.length > end && text[end] == '/' ||
                        group.substringBefore('.') == "www"
                    ) continue
                    content = group
                    break@getAddress
                }
            }
            Notification.display(
                Jsoup.parse(article.getViewInfo().subject).text(),
                content,
                "https://gall.dcinside.com/$GALL_ID/$id"
            )
        }
    }
}