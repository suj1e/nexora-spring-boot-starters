plugins {
    id("java-library")
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/spring") }
    mavenCentral()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(enforcedPlatform(libs.spring.boot.dependencies))
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    api(libs.spring.boot.starter)
    api(libs.spring.boot.starter.web)
    api(libs.spring.boot.starter.validation)

    // Optional cloud storage dependencies
    compileOnly("com.aliyun.oss:aliyun-sdk-oss:3.17.4")
    compileOnly("software.amazon.awssdk:s3:2.25.0")
    compileOnly("io.minio:minio:8.5.7")
    api("commons-io:commons-io:2.15.1")

    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
