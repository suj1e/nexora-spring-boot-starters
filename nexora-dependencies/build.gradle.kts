plugins {
    id("java-library")
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/spring") }
    mavenCentral()
}

dependencies {
    implementation(enforcedPlatform(libs.spring.boot.dependencies))
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    api(platform(libs.spring.boot.dependencies))
    api(libs.jackson.databind)
    compileOnly(libs.spring.boot.starter)
    compileOnly(libs.spring.boot.starter.web)
    testImplementation(libs.spring.boot.starter.test)
}
