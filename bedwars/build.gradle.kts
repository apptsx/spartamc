plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    // MySQL e HikariCP (necessário para runtime)
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("xyz.xenondevs:particle:1.8.4")

    compileOnly(files("../libs/slime.jar"))
    compileOnly(files("../libs/zartema-api.jar"))
    compileOnly(files("../libs/protocol.jar", "../libs/panda-server.jar",))
    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")

}

repositories {
    mavenLocal()
    mavenCentral()
}

tasks.register<Copy>("copyJar") {
    dependsOn("jar")

    from("build/libs/bedwars-1.0.0-SNAPSHOT.jar")
    into("../server/bedwars/plugins/")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("${project.name}-${project.version}-all.jar")
}
