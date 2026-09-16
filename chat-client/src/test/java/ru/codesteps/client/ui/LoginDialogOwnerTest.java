package ru.codesteps.client.ui;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * LoginDialogOwnerTest проверяет защиту от NPE в Dialog.initOwner().
 */
@DisplayName("LoginDialog owner binding")
class LoginDialogOwnerTest {

    private static boolean toolkitStarted;

    @BeforeAll
    static void startToolkit() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            toolkitStarted = latch.await(10, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyStarted) {
            toolkitStarted = true;
        } catch (Throwable unavailable) {
            toolkitStarted = false;
        }
    }

    @Test
    @DisplayName("null owner cannot be bound")
    void nullOwnerCannotBind() {
        assertFalse(LoginDialog.canBindOwner(null));
    }

    @Test
    @DisplayName("stage without scene cannot be bound")
    void stageWithoutSceneCannotBind() throws Exception {
        assumeTrue(toolkitStarted, "JavaFX toolkit is not available");

        onFxThread(() -> {
            var stage = new Stage();
            assertFalse(LoginDialog.canBindOwner(stage));
        });
    }

    @Test
    @DisplayName("stage with scene can be bound")
    void stageWithSceneCanBind() throws Exception {
        assumeTrue(toolkitStarted, "JavaFX toolkit is not available");

        onFxThread(() -> {
            var stage = new Stage();
            stage.setScene(new Scene(new Group()));
            assertTrue(LoginDialog.canBindOwner(stage));
        });
    }

    @Test
    @DisplayName("constructing dialog for stage without scene does not throw")
    void constructWithoutSceneDoesNotThrow() throws Exception {
        assumeTrue(toolkitStarted, "JavaFX toolkit is not available");

        onFxThread(() -> new LoginDialog(new Stage()));
    }

    private static void onFxThread(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX task timed out");
        assertNull(error.get(), () -> "JavaFX task failed: " + error.get());
    }
}
