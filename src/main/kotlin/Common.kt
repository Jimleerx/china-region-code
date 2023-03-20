import org.jsoup.Jsoup
import java.io.*
import java.nio.channels.FileChannel
import org.apache.http.HttpHost
import org.apache.http.HttpVersion
import org.apache.http.client.HttpClient
import org.apache.http.client.fluent.Executor
import org.apache.http.client.fluent.Request
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.routing.HttpRoute
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.LayeredConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.SSLInitializationException
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import java.io.IOException
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext

object Http {

    private const val maxPerRoute = 200
    private const val maxTotal = 400
    private val connectionManager: PoolingHttpClientConnectionManager
    private val httpClient: HttpClient
    private val executor: Executor

    init {
        // 预留https访问方式, 后续网站升级可能仅支持https
        var ssl: LayeredConnectionSocketFactory? = null
        try {
            ssl = SSLConnectionSocketFactory.getSystemSocketFactory()
        } catch (e: SSLInitializationException) {
            val sslcontext: SSLContext
            try {
                sslcontext = SSLContext.getInstance(SSLConnectionSocketFactory.TLS)
                sslcontext.init(null, null, null)
                ssl = SSLConnectionSocketFactory(sslcontext)
            } catch (ignore: SecurityException) {
            } catch (ignore: KeyManagementException) {
            } catch (ignore: NoSuchAlgorithmException) {
            }
        }
        val sfr: Registry<ConnectionSocketFactory> = RegistryBuilder.create<ConnectionSocketFactory>()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", ssl ?: SSLConnectionSocketFactory.getSocketFactory()).build()

        connectionManager = PoolingHttpClientConnectionManager(sfr)
        connectionManager.defaultMaxPerRoute = maxPerRoute
        connectionManager.maxTotal = maxTotal
        connectionManager.validateAfterInactivity = 1000
        connectionManager.setMaxPerRoute(HttpRoute(HttpHost("www.stats.gov.cn", 443)), 400)
        httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager).build()
        executor = Executor.newInstance(httpClient)
    }

    @Throws(IOException::class)
    fun get(uri: String?, connectTimeout: Int = 100000000, socketTimeout: Int = 100000000): String {
        return executor.execute(Request.Get(uri).version(HttpVersion.HTTP_1_1).connectTimeout(connectTimeout).socketTimeout(socketTimeout))
            .returnContent().asString(StandardCharsets.UTF_8)
    }

    fun getHtml(url: String): String {
        return try {
            get(url)
        } catch (e: Exception) {
            e.printStackTrace()
            "null"
        }
    }

    fun getHtmlAutoRetry(
        url: String, maxRetry: Int,
    ): String? {
        println("HTTP: ${url}")
        var responseData: String? = null
        var currentRetryCount = 0
        do {
            try {
                responseData = getHtml(url)
            } catch (throwable: Throwable) {
                println("Http请求异常: ${url}")
            }
            currentRetryCount++
            if ((responseData == null || responseData.contains("Please enable JavaScript")) && currentRetryCount < maxRetry) {
                println("重试次数: ${maxRetry}, 当前第次${currentRetryCount}重试")
                println("60秒后重试: ${url}")
                try {
                    TimeUnit.SECONDS.sleep(60)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        } while ((responseData == null || responseData.contains("Please enable JavaScript")) && currentRetryCount <= maxRetry)
        if (currentRetryCount > 1) {
            println("服务重试次数: ${currentRetryCount}")
        }
        return responseData
    }

    fun getLastDataUrl(): String {
        // 获取 rootHtml数据, 从 http://www.stats.gov.cn/sj/tjbz/tjyqhdmhcxhfdm/ 获取
        val rootHtml: String = getHtmlAutoRetry(ROOT_URL, 1000)!!
        // 找出最新一年的行政区划代码 href
        val rootDoc = Jsoup.parse(rootHtml)
        val href = rootDoc.select(LAST_YEAR_TABLE_SELECTOR).first()?.attr("href")
        return href?.substring(href.lastIndexOf('/', href.lastIndexOf('/') -1) + 1)?.let {
            ROOT_URL + it
        } ?: throw NullPointerException("未获取到最新年份行政规划地址")
    }

    fun generateFullUrl(parentUrl: String, shortUrl: String): String {
        // 根url去除末尾的.html
        val noSuffixParentUrl = parentUrl.substring(0, parentUrl.lastIndexOf('/') + 1)
        return noSuffixParentUrl + shortUrl
    }

}

/**
 * 按顺序打印region的名称, 包含父级名称
 */
fun printFullPath(region: CommonRegion) {
    var logStr = region.name
    var current: CommonRegion? = region.parent
    while (current != null) {
        logStr = current.name + " -> " + logStr
        current = current.parent
    }
    println(logStr)
}


/**
 * 按顺序打印region的名称, 包含父级名称
 */
fun getFullPath(region: CommonRegion, name: String? = null): String {
    var logStr = region.name
    var current: CommonRegion? = region.parent
    while (current != null) {
        logStr = current.name + '-' + logStr
        current = current.parent
    }
    name?.let {
        logStr += "-${name}"
    }
    return logStr
}

data class CommonRegion(
    // 父节点
    val parent: CommonRegion? = null,
    // 递归深度
    val depth: Int,
    // 名称
    val name: String,
    // 包含所有上级的全称 如name = 丰台区, fullname = 北京市-丰台区
    val fullname: String,
    // code
    val code: String,
    // 下一层级的url地址
    val url: String? = null,
    // 城乡区划代码 三位数字 居委会独有
    val villageCode: String? = null
)
