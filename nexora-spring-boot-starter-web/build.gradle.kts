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

    api(libs.spring.boot.starter.web)
    compileOnly(libs.spring.boot.starter.validation)
    annotationProcessor(libs.spring.boot.configuration.processor)
    api(libs.jackson.databind)
    testImplementation(libs.spring.boot.starter.test)
}
