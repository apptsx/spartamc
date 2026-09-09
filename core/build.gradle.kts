plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    // Dependências externas
    implementation("com.mysql:mysql-connector-j:8.2.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.2")
    implementation("org.json:json:20240303")
    
    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    
    // JARs locais
    compileOnly(files(
        "../libs/panda-api.jar",
        "../libs/panda-server.jar", 
        "../libs/zartema-api.jar",
        "../libs/slime.jar",
        "../libs/slime_api.jar",
        "../libs/protocol.jar",
        "../libs/spigot1.jar",
        "../libs/zartema-server.jar"
    ))
    
    compileOnly("com.mojang:authlib:1.5.21")
}

repositories {
    mavenCentral()
    maven(url = "https://libraries.minecraft.net")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

// Configuração do ShadowJar - apenas se necessário para distribuição
tasks.shadowJar {
    archiveBaseName.set(project.name)
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("all")
    manifest {
        attributes["Main-Class"] = "com.minecraft.core.Core"
    }
}