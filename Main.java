package com.kartonplus;

import com.kartonplus.controller.AdminController;
import com.kartonplus.controller.DashboardController;
import com.kartonplus.model.User;
import com.kartonplus.service.AuthService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    private static final String COLOR_BG = "#E6E4DA";
    private static final String COLOR_PRIMARY = "#6B7B84";
    private static final String COLOR_SECONDARY = "#5E4B3B";
    private static final String COLOR_ACCENT = "#A9ACA9";
    private static final String COLOR_DARK = "#2E2E2E";
    private Stage primaryStage;
    private BorderPane mainLayout;
    private User currentUser;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLoginScreen();
    }

    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/kartonplus/view/login.fxml"));
            Parent loginView = loader.load();

            com.kartonplus.controller.LoginController controller = loader.getController();
            controller.setMain(this);

            Scene scene = new Scene(loginView, 500, 450);
            primaryStage.setTitle("Картон-Плюс - Вход в систему");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showMainLayout() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + COLOR_BG + ";");

        HBox topBar = createTopBar();
        mainLayout.setTop(topBar);
        loadView("/com/kartonplus/view/dashboard.fxml");

        Scene scene = new Scene(mainLayout, 1200, 800);
        primaryStage.setTitle("Картон-Плюс - " + currentUser.getFullName());
        primaryStage.setScene(scene);
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(10);
        topBar.setPadding(new javafx.geometry.Insets(15, 20, 15, 20));
        topBar.setStyle("-fx-background-color: " + COLOR_DARK + ";");
        topBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label logoLabel = new Label("КАРТОН-ПЛЮС");
        logoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        logoLabel.setTextFill(Color.web(COLOR_BG));

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button dashboardBtn = createNavButton("Панель управления");
        Button materialsBtn = createNavButton("Сырье на складе");
        Button suppliesBtn = createNavButton("Поставки сырья");
        Button ordersBtn = createNavButton("Заказы");

        dashboardBtn.setOnAction(e -> loadView("/com/kartonplus/view/dashboard.fxml"));
        materialsBtn.setOnAction(e -> loadView("/com/kartonplus/view/materials.fxml"));
        suppliesBtn.setOnAction(e -> loadView("/com/kartonplus/view/supplies.fxml"));
        ordersBtn.setOnAction(e -> loadView("/com/kartonplus/view/orders.fxml"));

        topBar.getChildren().add(logoLabel);
        topBar.getChildren().add(spacer);
        topBar.getChildren().addAll(dashboardBtn, materialsBtn, suppliesBtn, ordersBtn);

        //кнопка администрирования
        if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
            Button adminBtn = createNavButton("Администрирование");
            adminBtn.setOnAction(e -> loadView("/com/kartonplus/view/admin.fxml"));
            topBar.getChildren().add(adminBtn);
        }

        Label userLabel = new Label(currentUser.getFullName());
        userLabel.setTextFill(Color.web(COLOR_BG));
        userLabel.setFont(Font.font("Arial", 14));

        Button logoutBtn = new Button("Выход");
        logoutBtn.setStyle(createButtonStyle(COLOR_SECONDARY));
        logoutBtn.setOnAction(e -> {
            currentUser = null;
            showLoginScreen();
        });

        topBar.getChildren().addAll(userLabel, logoutBtn);

        return topBar;
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: " + COLOR_BG + "; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 8 15;");

        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; " +
                        "-fx-text-fill: white; -fx-font-size: 14px; " +
                        "-fx-cursor: hand; -fx-padding: 8 15;"));

        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: transparent; " +
                        "-fx-text-fill: " + COLOR_BG + "; " +
                        "-fx-font-size: 14px; -fx-cursor: hand; " +
                        "-fx-padding: 8 15;"));

        return button;
    }

    private String createButtonStyle(String color) {
        return String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 12 25; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-weight: bold;", color);
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            mainLayout.setCenter(view);
            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setMain(this);
                ((DashboardController) controller).loadData();
            } else if (controller instanceof AdminController) {
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Ошибка", "Не удалось загрузить страницу: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public static void main(String[] args) {
        launch(args);
    }
}