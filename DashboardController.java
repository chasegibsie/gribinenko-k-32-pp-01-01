package com.kartonplus.controller;

import com.kartonplus.Main;
import com.kartonplus.model.Order;
import com.kartonplus.model.RawMaterial;
import com.kartonplus.service.MaterialService;
import com.kartonplus.service.OrderService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardController {
    @FXML private GridPane metricsGrid;
    @FXML private VBox urgentBox;
    @FXML private Label urgentValue;
    @FXML private Label stockValue;
    @FXML private Label activeOrdersValue;
    @FXML private Label shippingValue;
    @FXML private TableView<Order> shippingTable;
    @FXML private TableColumn<Order, String> orderNumberCol;
    @FXML private TableColumn<Order, String> clientCol;
    @FXML private TableColumn<Order, String> productCol;
    @FXML private TableColumn<Order, String> shippingDateCol;
    @FXML private TableColumn<Order, String> statusCol;
    @FXML private HBox adminButtonContainer;
    @FXML private Label lastUpdateLabel;

    private MaterialService materialService = new MaterialService();
    private OrderService orderService = new OrderService();
    private Main main;
    private Timer autoRefreshTimer;
    private boolean isErrorShown = false;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public void setMain(Main main) {
        this.main = main;
        addAdminButtonIfNeeded();
    }

    @FXML
    public void initialize() {
        setupShippingTable();
        loadData();
        startAutoRefresh();
        Platform.runLater(() -> {
            if (shippingTable.getScene() != null) {
                shippingTable.getScene().getWindow().setOnCloseRequest(e -> stopAutoRefresh());
            }
        });
    }

    private void startAutoRefresh() {
        stopAutoRefresh();
        autoRefreshTimer = new Timer(true);
        autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(DashboardController.this::loadData);
            }
        }, 60000, 60000);
    }

    private void stopAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
            autoRefreshTimer = null;
        }
    }

    private void addAdminButtonIfNeeded() {
        if (main != null && main.getCurrentUser() != null &&
                "ADMIN".equals(main.getCurrentUser().getRole())) {

            Button adminPanelButton = new Button("⚙ Админ-панель");
            adminPanelButton.setStyle(
                    "-fx-background-color: #5E4B3B; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 16px; " +
                            "-fx-padding: 15 30; " +
                            "-fx-background-radius: 10; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);"
            );

            adminPanelButton.setOnMouseEntered(e ->
                    adminPanelButton.setStyle(
                            "-fx-background-color: #6B7B84; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-size: 16px; " +
                                    "-fx-padding: 15 30; " +
                                    "-fx-background-radius: 10; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 6);"
                    )
            );

            adminPanelButton.setOnMouseExited(e ->
                    adminPanelButton.setStyle(
                            "-fx-background-color: #5E4B3B; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-size: 16px; " +
                                    "-fx-padding: 15 30; " +
                                    "-fx-background-radius: 10; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);"
                    )
            );

            adminPanelButton.setOnAction(e -> main.loadView("/com/kartonplus/view/admin.fxml"));

            if (adminButtonContainer != null) {
                adminButtonContainer.getChildren().add(adminPanelButton);
            }
        }
    }

    private void setupShippingTable() {
        orderNumberCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getOrderNumber()));
        clientCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getClientName()));
        productCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProductType()));
        shippingDateCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getShippingDate() != null ?
                                data.getValue().getShippingDate().toString() : "Не назначена"));
        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus()));

        //цветовое выделение статусов
        statusCol.setCellFactory(col -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "В производстве":
                            setStyle("-fx-text-fill: #FF6B00; -fx-font-weight: bold;");
                            break;
                        case "Готов к отгрузке":
                            setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                            break;
                        case "Отгружен":
                            setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    public void loadData() {
        try {
            //загрузка срочных позиций
            try {
                List<RawMaterial> urgent = materialService.getUrgentMaterials();
                urgentValue.setText(String.valueOf(urgent.size()));

                if (urgent.size() > 0) {
                    urgentBox.setStyle(
                            "-fx-background-color: #FFF3F3; " +
                                    "-fx-border-radius: 10; " +
                                    "-fx-background-radius: 10; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                                    "-fx-border-left: 5px solid #D32F2F;"
                    );
                    urgentValue.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
                } else {
                    urgentBox.setStyle(
                            "-fx-background-color: white; " +
                                    "-fx-border-radius: 10; " +
                                    "-fx-background-radius: 10; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                                    "-fx-border-left: 5px solid #4CAF50;"
                    );
                    urgentValue.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                }
            } catch (Exception e) {
                System.err.println("Ошибка загрузки срочных позиций: " + e.getMessage());
                urgentValue.setText("?");
            }

            //загрузка данных о сырье
            try {
                double totalQuantity = materialService.getTotalQuantity();
                int totalRolls = materialService.getTotalRolls();
                stockValue.setText(String.format("%.1f тонн / %d рулонов", totalQuantity, totalRolls));
            } catch (Exception e) {
                System.err.println("Ошибка загрузки данных о сырье: " + e.getMessage());
                stockValue.setText("Ошибка загрузки");
            }

            //активные заказы
            try {
                List<Order> activeOrders = orderService.getActiveOrders();
                activeOrdersValue.setText(String.valueOf(activeOrders.size()));
            } catch (Exception e) {
                System.err.println("Ошибка загрузки активных заказов: " + e.getMessage());
                activeOrdersValue.setText("?");
            }

            try {
                List<Order> todayShipments = orderService.getTodayShipments();
                shippingValue.setText(todayShipments.size() + " заказов");
                shippingTable.setItems(FXCollections.observableArrayList(todayShipments));
            } catch (Exception e) {
                System.err.println("Ошибка загрузки отгрузок: " + e.getMessage());
                shippingValue.setText("?");
                shippingTable.setItems(FXCollections.observableArrayList());
            }

            lastUpdateLabel.setText("Обновлено: " + LocalDate.now().format(TIME_FORMATTER));
            isErrorShown = false;

        } catch (Exception e) {
            e.printStackTrace();
            if (!isErrorShown) {
                isErrorShown = true;
                showError("Ошибка загрузки данных", "Не удалось загрузить данные с сервера. Проверьте подключение.");
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #E6E4DA;");
        alert.showAndWait();
    }
}