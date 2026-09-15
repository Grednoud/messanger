plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

dependencies {
    implementation(project(":message-protocol"))
}

javafx {
    version = "21.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("ru.codesteps.client.ChatClientApp")
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "ru.codesteps.client.ChatClientApp")
    }
}
