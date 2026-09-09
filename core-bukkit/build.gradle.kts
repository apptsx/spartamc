plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))
    
    implementation("com.mysql:mysql-connector-j:8.2.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.2")
    implementation("org.json:json:20240303")
    
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    
    implementation(fileTree("../libs") { include("*.jar") })
    
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

// JAR simples, sem empacotar dependências
tasks.jar {
    enabled = true
    from(sourceSets.main.get().output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}