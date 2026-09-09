plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    // MySQL e HikariCP (necessário para runtime)
    implementation("com.mysql:mysql-connector-j:8.2.0")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly(files("../libs/panda-server.jar",))

}

repositories {
    mavenLocal()
    mavenCentral()
}

tasks.register<Copy>("copyJar") {
    dependsOn("jar")

    from("build/libs/auth-1.0.0-SNAPSHOT.jar")
    into("../server/auth/plugins/")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("${project.name}-${project.version}-all.jar")
}