plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

group = "w3cp"
version = "0.9.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.annotationProcessorPath = configurations.annotationProcessor.get()
}

tasks.withType<Javadoc>().configureEach {
    enabled = false
}

dependencies {
    // Lombok (for boilerplate-free DTOs)
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    // Jackson (explicitly needed since no Quarkus here)
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    
    // Validation
    implementation("javax.validation:validation-api:2.0.1.Final")
    
    // JSR-305 annotations
    implementation("com.google.code.findbugs:jsr305:3.0.2")
}

publishing {
    publications {
        create<MavenPublication>("w3cpDto") {
            from(components["java"])
            groupId = "w3cp"
            artifactId = "w3cp-dto"
        }
    }
    repositories {
        mavenLocal()
    }
}
