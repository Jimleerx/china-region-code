plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "com.pomelotea.util"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral {
        url = uri("https://maven.aliyun.com/repository/public")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("org.apache.httpcomponents:fluent-hc:4.5.14") {
        exclude("commons-codec", "commons-codec")
    }
    implementation("mysql:mysql-connector-java:8.0.32")
    implementation("org.ktorm:ktorm-core:3.6.0")
    implementation("org.ktorm:ktorm-support-mysql:3.6.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.20")
    implementation("com.zaxxer:HikariCP:3.4.5")

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}