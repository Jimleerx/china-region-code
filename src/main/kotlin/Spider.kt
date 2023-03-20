/**
 * 爬取国家统计局行政区划代码
 */
class Spider(val url: String, val handler: IRegionCodeHandler?, val htmlHandler: HTMLHandler?) {

    fun run() {
        getData()
    }

    /**
     * 获取省(province)数据, 并且递归获取到镇(town)的数据, 保存所有数据的url链接
     */
    private fun getData() {
        val html = Http.getHtmlAutoRetry(url, RETRY_LIMIT)
        // 生成一个首页的commonRegion对象
        htmlHandler?.handle(CommonRegion(
            parent = null,
            depth = 0,
            name = "index",
            fullname = "首页",
            url = url,
            code = "0"
        ), html!!)
        val doc = org.jsoup.Jsoup.parse(html!!)
        val depth = 1
        doc.select(LEVEL_CLASS_SELECTOR[depth]).forEach {

            val href = it.attr("href")
            val commonRegion = CommonRegion(
                parent = null,
                depth = depth,
                code = href.substring(0, href.indexOf('.')),
                name = it.text(),
                fullname = it.text(),
                url = Http.generateFullUrl(url, href)
            )
            handler?.handle(commonRegion)
            getData(depth + 1, commonRegion)
        }
    }

    /**
     * 递归获取下一层级数据
     */
    private fun getData(depth: Int, region: CommonRegion) {
        if (region.url == null) {
            return
        }
        val html = Http.getHtmlAutoRetry(region.url, RETRY_LIMIT)
        htmlHandler?.handle(region, html!!)
        val doc = org.jsoup.Jsoup.parse(html!!)
        doc.select(LEVEL_CLASS_SELECTOR[depth]).forEach {
            val tdas = it.select("td a")
            val commonRegion = if (tdas.size == 0) {
                // 市辖区, 没有子级县市的
                val tds = it.select("td")
                if (tds.size == 2) {
                    CommonRegion(
                        parent = region,
                        depth = depth,
                        name = tds[1].text(),
                        fullname = getFullPath(region, tds[1].text()),
                        code = tds[0].text()
                    )
                } else {
                    // 最后一层包含城乡分类代码的
                    CommonRegion(
                        parent = region,
                        depth = depth,
                        name = tds[2].text(),
                        fullname = getFullPath(region, tds[2].text()),
                        code = tds[0].text(),
                        villageCode = tds[1].text()
                    )
                }
            } else {
                val href = tdas[0].attr("href")
                CommonRegion(
                    parent = region,
                    depth = depth,
                    name = tdas[1].text(),
                    fullname = getFullPath(region, tdas[1].text()),
                    code = tdas[0].text(),
                    url = Http.generateFullUrl(region.url, href)
                )
            }
            handler?.handle(commonRegion)
            getData(depth + 1, commonRegion)
        }
    }
}

