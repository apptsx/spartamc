plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    // MySQL e HikariCP
    implementation("com.mysql:mysql-connector-j:8.2.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.2")
    implementation("org.json:json:20240303")

    // JARs locais
    compileOnly(files(
        "../libs/slime.jar",
        "../libs/panda-server.jar",
        "../libs/panda-api.jar",
        "../libs/zartema-api.jar",
        "../libs/protocol.jar",
        "../libs/spigot1.jar",
        "../libs/zartema-server.jar"
    ))
    
    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
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

// COMENTE ou REMOVA a task copyJar (ou corrija o caminho)
tasks.register<Copy>("copyJar") {
    dependsOn("jar")
    from("build/libs/pvp-0.0.1-SNAPSHOT.jar")
    // Comente esta linha ou mude para um caminho válido no Android
    // into("C:/Users/kaust/OneDrive/Área de Trabalho/jars-duzy/")
    
    // Se quiser copiar para uma pasta local no Android:
    into("../server/pvp/plugins/")  // ← Caminho para Android
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
}