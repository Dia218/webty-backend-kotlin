plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
}

noArg { // @Entity 어노테이션이 붙은 클래스 자동으로 기본 생성자 생성
    annotation("jakarta.persistence.Entity")
}

group = "org.team14"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    // WebSocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    // devtools
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // security
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.security:spring-security-test")
    // OAuth2
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    // Jwt
    implementation("io.jsonwebtoken:jjwt-api:0.12.2")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.2")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.2")
    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // MySQL
    runtimeOnly("com.mysql:mysql-connector-j")
    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // H2
    implementation("com.h2database:h2")
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // JUnit
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    // mockito-kotlin (test)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    // lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // logger
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
    // kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    // kotlin jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
