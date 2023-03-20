


// 从哪个页面爬取，这里是年度列表页面，自动取第一行最新数据的url
const val ROOT_URL = "http://www.stats.gov.cn/sj/tjbz/tjyqhdmhcxhfdm/"
const val RETRY_LIMIT = 1000
const val PROVINCE_CODE = 1
const val CITY_CODE = 2
const val COUNTY_CODE = 3
const val TOWN_CODE = 4
const val VILLAGE_CODE = 5

// 类型配置，按递归深度区分类型
val levalMap = mapOf(PROVINCE_CODE to "省", CITY_CODE to "市", COUNTY_CODE to "区", TOWN_CODE to "镇", VILLAGE_CODE to "居委会")

// 省市区县层级 tr的 class, 从大到小
val LEVEL_CLASS_SELECTOR = arrayOf("", ".provincetable a", ".citytr", ".countytr", ".towntr", ".villagetr")
// 年度数据列表的table class，第一条为最新数据
val LAST_YEAR_TABLE_SELECTOR: String = ".center_list_contlist a"

// 保存到csv文件的路径
const val csvDataSavePath = "./fulldata.txt"
// 爬取原始网页html文件的保存目录
const val htmlSavePath = "./html"

/**
 * 启动
 */
fun main() {
    Spider(Http.getLastDataUrl(), CsvRegionCodeHandler(), FileHTMLHandler()).run()
}