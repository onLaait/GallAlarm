package com.github.onlaait.gallalarm

import be.zvz.kotlininside.KotlinInside
import be.zvz.kotlininside.api.article.ArticleList
import be.zvz.kotlininside.api.article.ArticleRead
import be.zvz.kotlininside.http.DefaultHttpClient
import be.zvz.kotlininside.session.user.Anonymous
import com.github.onlaait.gallalarm.Log.logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Entities
import kotlin.system.exitProcess

const val GALL_ID = "steve"

fun main() {
    Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler)

    logger.info("인스턴스 생성 중")

    KotlinInside.createInstance(Anonymous("ㅇㅇ", "1234"), DefaultHttpClient(), true)

    logger.info("시작 중")

    Notification
    val rgxDomain = Regex("(([a-zA-Z0-9가-힣-]+\\.)+([a-zA-Z]{2,}|한국)|\\d{1,3}(\\.\\d{1,3}){3})(:\\d{1,5})?")
    val rgxUrl = Regex("https?://${rgxDomain.pattern}(/\\S+)?")
    var lastCheckedId = ArticleList(GALL_ID).apply { requestUntilNoException() }.getGallList().first().identifier

    logger.info("시작됨")

    while (true) {
        Thread.sleep(5000)
        val articleList = ArticleList(GALL_ID, headId = 40).apply { requestUntilNoException() }
        val list = articleList.getGallList()

        if (list.isEmpty()) {
            logger.error("글 목록이 비어있음 (세션 만료됨)")
            exitProcess(-1)
        }

        val newArticles = list.filter { it.identifier > lastCheckedId }.sortedBy { it.identifier }
        newArticles.forEach { c ->
            val id = c.identifier
            lastCheckedId = id
            val article = ArticleRead(GALL_ID, id).apply {
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
            val html = Jsoup.parseBodyFragment(Entities.unescape(article.getViewMain().content)).body()
            html.select("[class]").remove()
            val text = html.text()
            var notifContent = text

            val domainMatches = rgxDomain.findAll(text).toMutableList()
            if (domainMatches.isNotEmpty()) {
                domainMatches.reverse()
                val urlRanges = rgxUrl.findAll(text).map { it.range }.toList()
                val pureDomain =
                    domainMatches.find { mat ->
                        urlRanges.none { mat.range.first in it || mat.range.last in it }
                    }
                if (pureDomain != null) notifContent = pureDomain.value
            }

            Notification.display(
                Entities.unescape(article.getViewInfo().subject),
                notifContent,
                "https://gall.dcinside.com/$GALL_ID/$id"
            )
        }
    }
}

fun ArticleList.requestUntilNoException() {
    while (true) {
        try {
            request()
            return
        } catch (e: Exception) {
            logger.error("글 목록 읽기 중 오류: $e")
        }
        Thread.sleep(1000)
    }
}