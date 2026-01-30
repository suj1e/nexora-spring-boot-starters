plugins {
    id("java-library")
}

tasks.withType<Test> {
    useJUnitPlatform()
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
    compileOnly("org.aspectj:aspectjweaver")
    compileOnly("jakarta.persistence:jakarta.persistence-api")
    api(libs.jackson.databind)
    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
