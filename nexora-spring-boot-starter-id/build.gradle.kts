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
    annotationProcessor(enforcedPlatform(libs.spring.boot.dependencies))

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(libs.spring.boot.starter)
    annotationProcessor(libs.spring.boot.configuration.processor)
    testImplementation(libs.spring.boot.starter.test)
}
