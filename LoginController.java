package com.kartonplus.controller;

import com.kartonplus.Main;
import com.kartonplus.model.User;
import com.kartonplus.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private AuthService authService = new AuthService();
    private Main main;
    private int loginAttempts = 0;
    private static final int MAX_ATTEMPTS = 5;

    public void setMain(Main main) {
        this.main = main;
    }

    @FXML
    public void initialize() {
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        Platform.runLater(() -> usernameField.requestFocus());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        errorLabel.setVisible(false);
        if (username.isEmpty() || password.isEmpty()) {
            showError("Заполните все поля");
            return;
        }
        //проверка на количество попыток
        if (loginAttempts >= MAX_ATTEMPTS) {
            showError("Превышено количество попыток входа. Перезапустите приложение.");
            loginButton.setDisable(true);
            return;
        }

        loadingIndicator.setVisible(true);
        loginButton.setDisable(true);

        new Thread(() -> {
            try {
                User user = authService.authenticate(username, password);

                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loginButton.setDisable(false);

                    if (user != null) {
                        loginAttempts = 0;
                        main.setCurrentUser(user);
                        main.showMainLayout();
                    } else {
                        loginAttempts++;
                        int remaining = MAX_ATTEMPTS - loginAttempts;
                        showError("Неверный логин или пароль. Осталось попыток: " + remaining);
                        if (loginAttempts >= MAX_ATTEMPTS) {
                            loginButton.setDisable(true);
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loginButton.setDisable(false);
                    showError("Ошибка подключения к серверу: " + e.getMessage());
                });
            }
        }).start();
    }
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 14px;");
    }
}