package com.github.onlaait.gallalarm

import be.zvz.kotlininside.KotlinInside
import be.zvz.kotlininside.api.article.ArticleList
import be.zvz.kotlininside.api.article.ArticleRead
import be.zvz.kotlininside.http.DefaultHttpClient
import be.zvz.kotlininside.http.HttpException
import be.zvz.kotlininside.session.user.Anonymous
import com.github.onlaait.gallalarm.Log.logger
import org.jsoup.Jsoup
import java.io.IOException
import java.util.regex.Pattern

const val GALL_ID = "steve"

fun main() {
    logger.info("시작 중")
    Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler)
    Notification

    logger.info("인스턴스 생성 중")
    KotlinInside.createInstance(Anonymous("ㅇㅇ", "1234"), DefaultHttpClient(), true)

    logger.info("준비 중")
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
        val latestArticle = articleList.getGallList().first()
        val id = latestArticle.identifier
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
        var content = Jsoup.parse(Jsoup.parse(article.getViewMain().content).text()).text()
        val matcher = domainPattern.matcher(content)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            if (start != 0 && content[start-1] == '/' ||
                content.length > end && content[end] == '/'
                ) continue
            content = matcher.group()
            break
        }
        Notification.display(
            Jsoup.parse(article.getViewInfo().subject).text(),
            content
        )
    }
}