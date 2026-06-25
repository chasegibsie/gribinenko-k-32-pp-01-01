package com.kartonplus.controller;

import com.kartonplus.Main;
import com.kartonplus.model.User;
import com.kartonplus.service.AuthService;
import com.kartonplus.util.AlertHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import java.util.List;

public class AdminController {
    @FXML private GridPane userForm;
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField newFullNameField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label adminMessageLabel;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> fullNameCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, Void> actionsCol;
    @FXML private Button addUserButton;
    @FXML private Button refreshUsersButton;
    @FXML private ProgressIndicator loadingIndicator;

    private AuthService authService = new AuthService();
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private Main main;

    public void setMain(Main main) {
        this.main = main;
    }

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("USER", "ADMIN");
        roleComboBox.setValue("USER");

        setupUsersTable();
        loadUsers();
        newUsernameField.setOnAction(e -> newPasswordField.requestFocus());
        newPasswordField.setOnAction(e -> newFullNameField.requestFocus());
        newFullNameField.setOnAction(e -> handleAddUser());
    }

    private void setupUsersTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("ADMIN".equals(item)) {
                        setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    }
                }
            }
        });

        actionsCol.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button deleteBtn = new Button("Удалить");
            private final HBox pane = new HBox(deleteBtn);

            {
                deleteBtn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;");
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if ("ADMIN".equals(user.getRole())) {
                        long adminCount = usersList.stream()
                                .filter(u -> "ADMIN".equals(u.getRole()))
                                .count();

                        if (adminCount <= 1) {
                            setGraphic(null);
                            return;
                        }
                    }
                    setGraphic(pane);
                }
            }
        });
    }

    @FXML
    private void handleAddUser() {
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText().trim();
        String fullName = newFullNameField.getText().trim();
        String role = roleComboBox.getValue();


        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            showMessage("Заполните все поля", true);
            return;
        }
        if (username.length() < 3) {
            showMessage("Логин должен быть не менее 3 символов", true);
            return;
        }
        if (password.length() < 6) {
            showMessage("Пароль должен быть не менее 6 символов", true);
            return;
        }
        boolean userExists = usersList.stream()
                .anyMatch(u -> u.getUsername().equals(username));

        if (userExists) {
            showMessage("Пользователь с таким логином уже существует", true);
            return;
        }
        loadingIndicator.setVisible(true);
        addUserButton.setDisable(true);

        new Thread(() -> {
            try {
                boolean success = authService.addUser(username, password, fullName, role);
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    addUserButton.setDisable(false);

                    if (success) {
                        showMessage("Пользователь '" + fullName + "' успешно добавлен", false);
                        clearFields();
                        loadUsers();
                    } else {
                        showMessage("Ошибка при добавлении пользователя. Проверьте подключение к БД", true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    addUserButton.setDisable(false);
                    showMessage("Ошибка: " + e.getMessage(), true);
                });
            }
        }).start();
    }

    private void handleDeleteUser(User user) {
        //нельзя удалить последнего администратора
        if ("ADMIN".equals(user.getRole())) {
            long adminCount = usersList.stream()
                    .filter(u -> "ADMIN".equals(u.getRole()))
                    .count();

            if (adminCount <= 1) {
                AlertHelper.showError("Ошибка", "Нельзя удалить последнего администратора системы");
                return;
            }
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление пользователя");
        alert.setContentText("Вы действительно хотите удалить пользователя '" +
                user.getFullName() + "' (" + user.getUsername() + ")?");
        alert.getDialogPane().setStyle("-fx-background-color: #E6E4DA;");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                loadingIndicator.setVisible(true);

                new Thread(() -> {
                    try {
                        boolean deleted = authService.deleteUser(user.getId());

                        Platform.runLater(() -> {
                            loadingIndicator.setVisible(false);
                            if (deleted) {
                                showMessage("Пользователь '" + user.getFullName() + "' удален", false);
                                loadUsers();
                            } else {
                                showMessage("Ошибка при удалении пользователя", true);
                            }
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            loadingIndicator.setVisible(false);
                            showMessage("Ошибка: " + e.getMessage(), true);
                        });
                    }
                }).start();
            }
        });
    }
    @FXML
    private void handleRefreshUsers() {
        loadUsers();
        showMessage("Список пользователей обновлен", false);
    }

    private void loadUsers() {
        loadingIndicator.setVisible(true);

        new Thread(() -> {
            try {
                List<User> users = authService.getAllUsers();

                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    usersList.clear();
                    usersList.addAll(users);
                    usersTable.setItems(usersList);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    showMessage("Ошибка загрузки пользователей: " + e.getMessage(), true);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void showMessage(String message, boolean isError) {
        adminMessageLabel.setText(message);
        adminMessageLabel.setVisible(true);

        if (isError) {
            adminMessageLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 14px;");
        } else {
            adminMessageLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 14px;");
        }

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> adminMessageLabel.setVisible(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void clearFields() {
        newUsernameField.clear();
        newPasswordField.clear();
        newFullNameField.clear();
        roleComboBox.setValue("USER");
    }
}