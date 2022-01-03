import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

val kotlinVersion = "1.6.10"
val kotestVersion = "5.0.3"

plugins {
	java
	idea
	id("com.palantir.docker") version "0.31.0"
	id("org.springframework.boot") version "2.6.1"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("org.unbroken-dome.xjc") version "2.0.0"
	id("org.openapi.generator") version "5.3.1"
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.6.10"
	kotlin("kapt") version "1.6.10"
}

group = "ch.frequenzdieb.server"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

idea {
	module {
		inheritOutputDirs = false
		outputDir = file("$buildDir/classes/kotlin/main")
	}
}

val compileKotlin: KotlinCompile by tasks
val xjcGenerate: org.unbrokendome.gradle.plugins.xjc.XjcGenerate by tasks
compileKotlin.dependsOn(xjcGenerate)
compileKotlin.dependsOn("openApiGenerate")

repositories {
	maven(url = "https://jitpack.io")
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	compileOnly("org.springdoc:springdoc-openapi-webflux-ui:1.6.3")
	compileOnly("org.springdoc:springdoc-openapi-kotlin:1.6.3")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
	implementation("commons-codec:commons-codec:1.15")
	implementation("com.sun.xml.bind:jaxb-core:3.0.1")
	implementation("com.sun.xml.bind:jaxb-impl:3.0.1")
	implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-script-util:$kotlinVersion")
	implementation("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinVersion")
	implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
	implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$kotlinVersion")
	implementation("com.openhtmltopdf:openhtmltopdf-core:1.0.10")
	implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10")
	implementation("com.github.kenglxn.QRGen:javase:2.6.0")
	implementation("io.jsonwebtoken:jjwt:0.9.1")
	kapt("org.springframework.boot:spring-boot-configuration-processor")
	runtimeOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.2.4")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
	testImplementation("io.kotest:kotest-property:$kotestVersion")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}

tasks {
	val pathToGeneratedAngular = "$projectDir/../apps/dist/generated"

	openApiGenerate {
		generatorName.set("typescript-angular")
		inputSpec.set("$projectDir/rest-api.yaml")
		outputDir.set(pathToGeneratedAngular)
		configOptions.putAll(
			mutableMapOf(
				"legacyDiscriminatorBehavior" to "false",
				"supportsES6" to "true"
			)
		)
	}

	register("generateModelsForTicketingApi", Copy::class) {
		val openApiGenerate by existing
		dependsOn(openApiGenerate)
		from("$pathToGeneratedAngular/model") {
			exclude("*AllOf.ts")
			exclude("inline*.ts")
		}
		into("$projectDir/../apps/projects/ticketing-api/src/@types")
	}

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
