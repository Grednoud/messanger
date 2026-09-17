plugins {
    java
    application
}

dependencies {
    implementation(project(":message-protocol"))
}

application {
    mainClass.set("ru.codesteps.server.ChatServerApp")
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "ru.codesteps.server.ChatServerApp")
    }
}
