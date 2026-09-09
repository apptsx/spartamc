plugins {
    id("java")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://libraries.minecraft.net")
        maven(url = "https://repo.codemc.io/repository/maven-public/")
    }
}

subprojects {
    apply(plugin = "java")
    
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://libraries.minecraft.net")
        maven(url = "https://repo.codemc.io/repository/maven-public/")
    }
    
    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.30")
        annotationProcessor("org.projectlombok:lombok:1.18.30")
        
        implementation("redis.clients:jedis:5.1.2")
        implementation("org.json:json:20240303")
        implementation("com.mysql:mysql-connector-j:8.2.0")
        implementation("com.zaxxer:HikariCP:5.1.0")
        
        compileOnly(fileTree(mapOf("dir" to rootProject.file("libs"), "include" to listOf("*.jar"))))
        implementation(fileTree(mapOf("dir" to rootProject.file("libs"), "include" to listOf("*.jar"))))
        
        compileOnly("com.mojang:authlib:1.5.21")
    }
    
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
