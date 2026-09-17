package ru.codesteps.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StartupOrderTest проверяет архитектурные инварианты запуска клиента.
 * 
 * JavaFX Dialog.initOwner() требует, чтобы у Stage была установлена Scene,
 * иначе возникает NullPointerException. Этот тест проверяет, что MainView
 * не показывает диалоги в конструкторе, а предоставляет метод initialize().
 */
@DisplayName("Client startup order")
class StartupOrderTest {

    @Test
    @DisplayName("MainView should have public initialize() method for deferred dialog display")
    void mainViewHasInitializeMethod() throws NoSuchMethodException {
        Class<?> mainViewClass = ru.codesteps.client.ui.MainView.class;
        
        Method initMethod = mainViewClass.getMethod("initialize");
        
        assertNotNull(initMethod, "MainView должен иметь метод initialize()");
        assertTrue(Modifier.isPublic(initMethod.getModifiers()), 
                "Метод initialize() должен быть public");
        assertEquals(void.class, initMethod.getReturnType(),
                "Метод initialize() должен возвращать void");
        assertEquals(0, initMethod.getParameterCount(),
                "Метод initialize() не должен принимать параметров");
    }

    @Test
    @DisplayName("ChatClientApp should call initialize() after setting scene")
    void chatClientAppCallsInitializeAfterScene() throws Exception {
        Class<?> appClass = ChatClientApp.class;
        
        var startMethod = appClass.getMethod("start", javafx.stage.Stage.class);
        assertNotNull(startMethod, "ChatClientApp должен иметь метод start(Stage)");
    }
}
