plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))
    implementation(project(":core-bungee"))
    
    implementation("com.mysql:mysql-connector-j:8.2.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.2")
     implementation("org.json:json:20240303")
     implementation("com.squareup.okhttp3:okhttp:4.12.0")
     implementation("com.google.code.gson:gson:2.10.1")
     
     implementation(fileTree("../libs") { include("*.jar") })
    
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:1.5.21")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://libraries.minecraft.net")
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

// LOBBY: Empacota todas as dependências no JAR final
tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    // Incluir apenas as classes dos projetos dependentes (sem libs externas)
    from(zipTree(project(":core").tasks.jar.get().archiveFile.get().asFile))
    from(zipTree(project(":core-bukkit").tasks.jar.get().archiveFile.get().asFile))
    from(zipTree(project(":core-bungee").tasks.jar.get().archiveFile.get().asFile))
}