### 爬取最新国家统计局的统计用区划和城乡划分代码
http://www.stats.gov.cn/sj/tjbz/tjyqhdmhcxhfdm/

### 已包含最新2022年数据, 见html目录与fulldata.txt

### 如何使用
run Main.kt main()  
默认情况下
运行完成在项目目录可以看到html文件夹, 里面有最新行政区划代码的所有页面HTML源文件
可以在根目录看到fulldata.txt, 包含所有行政区划代码数据的csv格式文件

### 自定义
###### CommonRegion数据类
|   属性   |    说明    |         类型         |
|:------:|:--------:|:------------------:|
| parent |   父节点    |    CommonRegion    |
| depth  |   递归深度   |    Int    |
| name  |    地名    |    String    |
| fullname  | 包含父级的地名  |    String    |
| name  |    地名    |    String    |
| code  |  行政区划代码  | String |
| url  | 下级地区的url | String |
|vallageCode |  城乡分类代码  | String |

#### 接口
##### HTMLHandler.handle(region: CommonRegion, html: String)
递归时回掉此方法，传入commonRegion对象与正在处理的html页面源数据
###### FileHTMLHandler
HTMLHandler的默认实现, 把爬取的HTML文件按链接写入目录, 可以放到web服务器中做镜像页使用，可以解决访问次数过多后弹验证码的问题


##### RegionCodeHandler.handle(region: CommonRegion)
递归时回掉此方法，传入正在处理的commonRegion对象

###### CsvRegionCodeHandler
RegionCodeHandler的默认实现, 把CommonRegion对象写入csv文件

###### DatabaseRegionCodeHandler
把commonRegion对象写入数据库, 保留父子关系

