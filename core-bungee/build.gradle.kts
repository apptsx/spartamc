plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))
    
    implementation("com.mysql:mysql-connector-j:8.2.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.2")
    implementation("org.json:json:20240303")
    
    implementation("net.dv8tion:JDA:5.0.0-beta.24") {
        exclude(module = "opus-java")
    }
    
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    
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
    
    compileOnly("net.md-5:bungeecord-api:1.20-R0.1") {
        exclude(group = "net.md-5", module = "brigadier")
    }
    compileOnly("com.mojang:authlib:1.5.21")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://libraries.minecraft.net")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://repo.md-5.net/content/repositories/releases/")
    maven(url = "https://maven.dv8tion.net/releases")
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
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}