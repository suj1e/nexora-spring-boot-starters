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

    // Configure publishing for Aliyun Yunxiao
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                // Add pom information
                pom {
                    name.set(project.name)
                    description.set("Nexora Spring Boot Starters - ${project.name}")
                    url.set("https://github.com/suj1e/nexora-spring-boot-starters")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("sujie")
                            name.set("Sujie")
                            email.set("sujie@example.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com:suj1e/nexora-spring-boot-starters.git")
                        developerConnection.set("scm:git:ssh://github.com:suj1e/nexora-spring-boot-starters.git")
                        url.set("https://github.com/suj1e/nexora-spring-boot-starters")
                    }
                }
            }
        }

        repositories {
            // Snapshot repository (for main branch pushes)
            maven {
                name = "YunxiaoSnapshotRepository"
                url = uri(project.findProperty("YunxiaoSnapshotRepositoryUrl") ?: "https://packages.aliyun.com/maven/repository/snapshot")
                credentials {
                    username = project.findProperty("YunxiaoSnapshotUsername") ?: System.getenv("YUNXIAO_USERNAME")
                    password = project.findProperty("YunxiaoSnapshotPassword") ?: System.getenv("YUNXIAO_PASSWORD")
                }
            }

            // Release repository (for tags/releases)
            maven {
                name = "YunxiaoReleaseRepository"
                url = uri(project.findProperty("YunxiaoReleaseRepositoryUrl") ?: "https://packages.aliyun.com/maven/repository/release")
                credentials {
                    username = project.findProperty("YunxiaoReleaseUsername") ?: System.getenv("YUNXIAO_USERNAME")
                    password = project.findProperty("YunxiaoReleasePassword") ?: System.getenv("YUNXIAO_PASSWORD")
                }
            }
        }
    }
}

// Task to publish all subprojects
tasks.register("publishAll") {
    dependsOn(subprojects.map { it.tasks.withType<PublishToMavenRepository>() })
}
