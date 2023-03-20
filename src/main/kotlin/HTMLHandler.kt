import java.io.File
import java.nio.charset.StandardCharsets

/**
 * 处理获取到的所有html数据
 */
interface HTMLHandler {

    /**
     * @param region 区划对象
     * @param html 根据region.url 获取到的html原始数据
     */
    fun handle(region: CommonRegion, html: String)
}

/*
把html写入数据库
class DatabaseHTMLHandler : HTMLHandler {
    override fun handle(region: CommonRegion, html: String) {
        db.regionCodeDb.insert(RegionCodeTable) {
            set(it.code, region.code)
            set(it.name, region.name)
            set(it.fullname, region.fullname)
            set(it.url, region.url)
            set(it.version, "2022")
            set(it.html, html)
            set(it.parent_code, region.parent?.code)
            set(it.cx_code, region.villageCode)
            set(it.level_code, region.depth)
            set(it.level_type, levalMap[region.depth])
        }
    }
}*/

/**
 * html按服务器目录格式写入到本地文件夹, 可以部署到web服务器做镜像使用
 */
class FileHTMLHandler : HTMLHandler {

    val rootDirectory = File(htmlSavePath)

    init {
        if (!rootDirectory.exists()) rootDirectory.mkdir()
    }

    override fun handle(region: CommonRegion, html: String) {
        val urlSuffix = region.url?.substring(ROOT_URL.length)
        val dirArray = urlSuffix?.split('/')
        if (dirArray != null) {
            var dirPath = ""
            // 最后一个元素是文件名
            for (index in 0 until dirArray.size - 1) {
                dirPath += "/" + dirArray[index]
                // 与服务器html目录保持一致，方便自己做镜像页面
                val dirFile = File("${rootDirectory.path}/${dirPath}")
                if (!dirFile.exists()) {
                    dirFile.mkdir()
                }
            }
            java.io.File("${rootDirectory.path}/${urlSuffix}").writeText(html, StandardCharsets.UTF_8)
        }
    }

}