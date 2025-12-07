
plugins {
	id("org.springframework.boot") version "3.5.7"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	id ("org.flywaydb.flyway") version "7.14.0"
	val kotlinVersion = "2.2.0"
	kotlin("jvm") version kotlinVersion
	kotlin("plugin.spring") version kotlinVersion
}

group = "com.guillermonegrete"
version = "1.14.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.data:spring-data-commons")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	// For UDP
	implementation("org.springframework.integration:spring-integration-ip")

	// Mysql with jpa
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("com.mysql:mysql-connector-j:9.0.0")
	// For testing
	runtimeOnly("com.h2database:h2")

	implementation("net.bramp.ffmpeg:ffmpeg:0.8.0")
    implementation("com.github.usefulness:webp-imageio:0.10.2")

	// Database migration helper
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-mysql")

	implementation("org.springdoc:springdoc-openapi-ui:1.8.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "junit")
		exclude(module = "mockito-core")
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
	// For testing with mocks in Kotlin
	testImplementation("com.ninja-squad:springmockk:4.0.2")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	launchScript()
}

flyway {
	url = "jdbc:mysql://localhost:3306/db_gallery_test?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
	user = "gallery_user"
	password = ""

	// set this value when partial migrations are desired
//	target = "4"

	// So flyway can find the kotlin based migrations
	locations = arrayOf("classpath:db/migration")
	// The "BASE_PATH" is where the root folder containing all folders with images is located
	// IMPORT: You must set this environment variable before using flyway commands
	placeholders = mutableMapOf<Any, Any>("base_path" to System.getenv("BASE_PATH"))
}