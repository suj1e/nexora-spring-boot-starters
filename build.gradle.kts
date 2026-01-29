plugins {
    id("java")
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.dependency.management) apply false
    id("maven-publish")
}

allprojects {
    group = "com.nexora"
    version = "1.0.0"

    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/spring") }
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
}
