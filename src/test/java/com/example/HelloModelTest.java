package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class HelloModelTest {

    private NtfyConnectionSpy spy;
    private HelloModel model;

    @BeforeEach
    void setUp() {
        spy = new NtfyConnectionSpy();
        model = new HelloModel(spy);
    }

    @Test
    @DisplayName("Given a model with messageToSend when calling sendMessage then send method on connection should be called")
    void sendMessageCallsConnectionWithMessageToSend() {
        // Arrange
        model.setMessageToSend("Hello World");

        // Act
        boolean result = model.sendMessage();

        // Assert
        assertThat(result).isTrue();
        assertThat(spy.message).isEqualTo("Hello World");
        assertThat(spy.sendCallCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Empty message should not be sent")
    void emptyMessageShouldNotBeSent() {
        // Arrange
        model.setMessageToSend("");

        // Act
        boolean result = model.sendMessage();

        // Assert
        assertThat(result).isFalse();
        assertThat(spy.message).isNull();
        assertThat(spy.sendCallCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Null message should not be sent")
    void nullMessageShouldNotBeSent() {
        // Arrange
        model.setMessageToSend(null);

        // Act
        boolean result = model.sendMessage();

        // Assert
        assertThat(result).isFalse();
        assertThat(spy.message).isNull();
        assertThat(spy.sendCallCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Whitespace only message should not be sent")
    void whitespaceMessageShouldNotBeSent() {
        // Arrange
        model.setMessageToSend("   ");

        // Act
        boolean result = model.sendMessage();

        // Assert
        assertThat(result).isFalse();
        assertThat(spy.message).isNull();
        assertThat(spy.sendCallCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Send file calls connection with file")
    void sendFileCallsConnectionWithFile() throws IOException {
        // Arrange
        File tempFile = File.createTempFile("test", ".txt");
        Files.write(tempFile.toPath(), "Test content".getBytes());

        // Act
        boolean result = model.sendFile(tempFile);

        // Assert
        assertThat(result).isTrue();
        assertThat(spy.file).isNotNull();
        assertThat(spy.file).isEqualTo(tempFile); // Testa att samma fil skickas
        assertThat(spy.sendFileCallCount).isEqualTo(1);

        // Cleanup
        tempFile.delete();
    }

    @Test
    @DisplayName("Send null file should not call connection")
    void sendNullFileShouldNotCallConnection() {
        // Act
        boolean result = model.sendFile(null);

        // Assert
        assertThat(result).isFalse();
        assertThat(spy.file).isNull();
        assertThat(spy.sendFileCallCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Send non-existent file should not call connection")
    void sendNonExistentFileShouldNotCallConnection() {
        // Arrange
        File nonExistentFile = new File("non_existent_file.txt");

        // Act
        boolean result = model.sendFile(nonExistentFile);

        // Assert
        assertThat(result).isFalse();
        assertThat(spy.file).isNull();
        assertThat(spy.sendFileCallCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Receive message adds message to list")
    void receiveMessageAddsMessageToList() {
        // Arrange - Use direct method call instead of Platform.runLater for testing
        NtfyMessageDto testMessage = new NtfyMessageDto(
                "test-id",
                System.currentTimeMillis(),
                "message",
                "mytopic",
                "Test message from spy",
                null,
                null
        );

        // Act - Add message directly to model (bypassing Platform.runLater)
        model.addTestMessage(testMessage);

        // Assert
        assertThat(model.getMessages()).hasSize(1);
        assertThat(model.getMessages().get(0).message()).isEqualTo("Test message from spy");
    }
}