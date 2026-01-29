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

    // Include all nexora starters
    api(project(":nexora-dependencies"))
    api(project(":nexora-spring-boot-starter-id"))
    api(project(":nexora-spring-boot-starter-web"))
    api(project(":nexora-spring-boot-starter-redis"))
    api(project(":nexora-spring-boot-starter-kafka"))
    api(project(":nexora-spring-boot-starter-resilience"))
    api(project(":nexora-spring-boot-starter-security"))
    testImplementation(libs.spring.boot.starter.test)
}
