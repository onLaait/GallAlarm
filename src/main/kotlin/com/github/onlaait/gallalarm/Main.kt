package com.github.onlaait.gallalarm

import be.zvz.kotlininside.KotlinInside
import be.zvz.kotlininside.api.article.ArticleList
import be.zvz.kotlininside.api.article.ArticleRead
import be.zvz.kotlininside.http.DefaultHttpClient
import be.zvz.kotlininside.session.user.Anonymous
import com.github.onlaait.gallalarm.Log.logger
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Entities
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import javax.imageio.ImageIO
import kotlin.system.exitProcess

const val GALL_ID = "steve"
const val HEAD_ID = 40

fun main() {
    logger.info("시작 중")

    Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler)

    val img = ImageIO.read(Thread.currentThread().contextClassLoader.getResourceAsStream("icon.png"))
    val trayIcon = TrayIcon(img, "GallAlarm").apply { isImageAutoSize = true }
    val menuItem = MenuItem("Exit")
    menuItem.addActionListener { exitProcess(0) }
    val popupMenu = PopupMenu()
    popupMenu.add(menuItem)
    trayIcon.popupMenu = popupMenu
    SystemTray.getSystemTray().add(trayIcon)

    Notification

    KotlinInside.createInstance(Anonymous("ㅇㅇ", "1234"), DefaultHttpClient(), true)

    val rgxDomain = Regex("(([a-zA-Z0-9가-힣-]+\\.)+([a-zA-Z]{2,}|한국)|\\d{1,3}(\\.\\d{1,3}){3})(:\\d{1,5})?")
    val rgxUrl = Regex("https?://${rgxDomain.pattern}(/\\S+)?")

    val checked = mutableListOf<Int>()
    val minArticle = getArticles().last()

    logger.info("시작됨")

    while (true) {
        Thread.sleep(5000)

        val list = getArticles()

        if (list.isEmpty()) {
            logger.error("글 목록이 비어있음 (세션 만료됨)")
            exitProcess(1)
        }

        val newArticles = list.filter { it > minArticle && !checked.contains(it) }
        if (newArticles.isEmpty()) continue
        newArticles.forEach { articleId ->
            val articleRead = ArticleRead(GALL_ID, articleId).apply {
                while (true) {
                    try {
                        request()
                    } catch (e: Exception) {
                        logger.error("글 읽기 중 오류: $e")
                        Thread.sleep(1000)
                        continue
                    }
                    break
                }
            }
            val html = Jsoup.parseBodyFragment(Entities.unescape(articleRead.getViewMain().content)).body()
            html.select("[class], [href]").remove()
            val text = html.text()
            var notifContent = text

            val domainMats = rgxDomain.findAll(text).toList()
            if (domainMats.isNotEmpty()) {
                val urlRanges = rgxUrl.findAll(text).map { it.range }
                val domains =
                    domainMats.filter { mat ->
                        urlRanges.none { mat.range.first in it || mat.range.last in it }
                    }.map { it.value }.distinct()
                if (domains.isNotEmpty()) notifContent = domains.joinToString(limit = 5)
            }

            Notification.display(
                StringEscapeUtils.unescapeHtml4(articleRead.getViewInfo().subject),
                notifContent,
                "https://gall.dcinside.com/$GALL_ID/$articleId"
            )

            checked += articleId
        }
    }
}

fun getArticles(): List<Int> =
    ArticleList(GALL_ID, headId = HEAD_ID).apply { requestUntilNoException() }.getGallList()
        .stream()
        .map { it.identifier }
        .distinct()
        .sorted()
        .toList()

fun ArticleList.requestUntilNoException() {
    while (true) {
        try {
            return request()
        } catch (e: Exception) {
            logger.error("글 목록 읽기 중 오류: $e")
        }
        Thread.sleep(1000)
    }
}