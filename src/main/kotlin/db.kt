
import com.zaxxer.hikari.HikariDataSource
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import org.ktorm.support.mysql.MySqlDialect


/*
CREATE TABLE `region_code` (
`code` varchar(12) NOT NULL COMMENT '行政区划代码',
`name` varchar(128) NOT NULL COMMENT '地名',
`fullname` varchar(256) DEFAULT NULL COMMENT '全名',
`url` varchar(128) DEFAULT NULL COMMENT '链接',
`version` varchar(32) DEFAULT NULL COMMENT '版本',
`html` mediumtext COMMENT '页面html原始数据',
`parent_code` varchar(12) DEFAULT NULL COMMENT '父节点code',
`cx_code` varchar(3) DEFAULT NULL COMMENT '城乡区划代码',
`level_code` int DEFAULT NULL COMMENT '级别编码',
`level_type` varchar(5) DEFAULT NULL COMMENT '级别类型'
);
 */

object db {
    val dataSource = HikariDataSource()
    val regionCodeDb: Database

    init {
        dataSource.jdbcUrl = "jdbc:mysql://ip:port/regioncode"
        dataSource.username = ""
        dataSource.password = ""
        dataSource.driverClassName = "com.mysql.cj.jdbc.Driver"
        dataSource.connectionTimeout = 5000L
        dataSource.maximumPoolSize = 20
        dataSource.maxLifetime = 690000 // dbp的idleTimeoutMinutes - 30s, dbp默认12min
        dataSource.minimumIdle = 10 // 最小连接数

        regionCodeDb = Database.connect(
            dataSource,
            dialect = MySqlDialect()
        )
    }
}

interface RegionCode : Entity<RegionCode> {
    companion object : Entity.Factory<RegionCode>()

    val code: String
    val name: String
    val fullname: String
    val url: String?
    val version: String
    var html: String?
    val parent_code: String?
    val cx_code: String?
    val level_code: Int?
    val level_type: String?
}

object RegionCodeTable : Table<RegionCode>("region_code") {
    var code = varchar("code").primaryKey().bindTo { it.code }
    var name = varchar("name").bindTo { it.name }
    var fullname = varchar("fullname").bindTo { it.fullname }
    var url = varchar("url").bindTo { it.url }
    var version = varchar("version").bindTo { it.version }
    var html = varchar("html").bindTo { it.html }
    var parent_code = varchar("parent_code").bindTo { it.parent_code }
    var cx_code = varchar("cx_code").bindTo { it.cx_code }
    var level_code = int("level_code").bindTo { it.level_code }
    var level_type = varchar("level_type").bindTo { it.level_type }
}