plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.example"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    // Nexus에서 db-query-lib 및 전이 의존성 resolve
    maven {
        url = uri("http://localhost:8081/repository/maven-public/")
        isAllowInsecureProtocol = true
    }
    mavenCentral()
}

dependencies {
    // Nexus에 배포된 DB 조회 라이브러리 (접속정보 포함)
    implementation("com.example:db-query-lib:1.0.2")
    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
