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

    compileOnly(files(
        "../libs/slime.jar",
        "../libs/zartema-api.jar",
        "../libs/panda-server.jar",
        "../libs/panda-api.jar",
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

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
}