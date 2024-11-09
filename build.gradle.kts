plugins {
    val kotlinVersion = "1.9.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.13.2"
}

group = "github.miracleur"
version = "0.1.0"


repositories {
    if (System.getenv("CI")?.toBoolean() != true) {
        maven("https://maven.aliyun.com/repository/public")
    }
    mavenCentral()
}

val openaiClientVersion = "3.8.2"
val jLatexMathVersion = "1.0.7"

dependencies {
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("com.squareup.okhttp3:okhttp:4.2.2")
    implementation("com.alibaba:fastjson:1.2.83")
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.aallam.openai:openai-client:$openaiClientVersion")
    implementation("org.scilab.forge:jlatexmath:$jLatexMathVersion")
}
