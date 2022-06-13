plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.11.1"
}

group = "shiroi.top"
version = "1.0"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    // retrofit 库
    implementation("com.squareup.retrofit2:retrofit:2.0.2")
    // 使用gson解析数据内容
    implementation ("com.squareup.retrofit2:converter-gson:2.0.2")
}