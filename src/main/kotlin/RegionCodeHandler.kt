import RegionCodeTable.html
import org.ktorm.dsl.insert
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter

/**
 * 处理读取出来的regionCode数据
 *
 * @see CommonRegion
 */
interface RegionCodeHandler {
    fun handle(region: CommonRegion)
}

class CsvRegionCodeHandler : RegionCodeHandler {

    val outFile = File(csvDataSavePath)
    var bw: BufferedWriter = BufferedWriter(OutputStreamWriter(outFile.outputStream()))

    override fun handle(region: CommonRegion) {
        printFullPath(region)
        bw.write("${getFullPath(region)},${region.name},${region.code},${levalMap[region.depth]},${region.url ?: ""}")
        bw.newLine()
    }
}


/**
 * 把commonRegiom数据写入数据库, 包含父子关系
 */
class DatabaseRegionCodeHandler : RegionCodeHandler {
    override fun handle(region: CommonRegion) {
        db.regionCodeDb.insert(RegionCodeTable) {
            set(it.code, region.code)
            set(it.name, region.name)
            set(it.fullname, region.fullname)
            set(it.url, region.url)
            set(it.version, "2022")
            set(it.html, null)
            set(it.parent_code, region.parent?.code)
            set(it.cx_code, region.villageCode)
            set(it.level_code, region.depth)
            set(it.level_type, levalMap[region.depth])
        }
    }
}