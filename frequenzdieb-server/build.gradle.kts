import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	java
	idea
	id("com.palantir.docker") version "0.25.0"
	id("org.springframework.boot") version "2.2.5.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	kotlin("jvm") version "1.3.70"
	kotlin("plugin.spring") version "1.3.70"
	kotlin("kapt") version "1.3.70"
}

group = "ch.frequenzdieb"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

idea {
	module {
		inheritOutputDirs = false
		outputDir = file("$buildDir/classes/kotlin/main")
	}
}

repositories {
	mavenCentral()
	jcenter()
	maven(url = "https://jitpack.io")
}

dependencies {
	implementation("com.openhtmltopdf:openhtmltopdf-core:1.0.1")
	implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.1")
	implementation("com.github.kenglxn.QRGen:javase:2.6.0")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("io.jsonwebtoken:jjwt:0.9.1")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
	implementation("commons-codec:commons-codec:1.14")
	implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.1")
	kapt("org.springframework.boot:spring-boot-configuration-processor")
	runtimeOnly("org.springframework.boot:spring-boot-devtools")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
	testImplementation("io.kotlintest:kotlintest-extensions-spring:3.4.2")
}

tasks {
	register("unpack", Copy::class) {
		val bootJar by existing
		dependsOn(bootJar)
		from(zipTree(bootJar.get().outputs.files.singleFile))
		into("build/dependency")
	}

	withType<Test> {
		useJUnitPlatform()
	}

	withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs = listOf("-Xjsr305=strict")
			jvmTarget = "11"
		}
	}
}

docker {
	val archiveBaseName = tasks.getByName<BootJar>("bootJar").archiveBaseName.get()
	name = "${project.group}/$archiveBaseName"
	setDockerfile(file("Dockerfile"))
	copySpec.from(tasks.getByName<Copy>("unpack").outputs).into("dependency")
	buildArgs(mapOf("DEPENDENCY" to "dependency"))
}
