package com.kartonplus.controller;

import com.kartonplus.model.Order;
import com.kartonplus.service.OrderService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import java.time.LocalDate;
import java.util.Optional;

public class OrdersController {
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> orderNumberCol;
    @FXML private TableColumn<Order, String> clientNameCol;
    @FXML private TableColumn<Order, String> productTypeCol;
    @FXML private TableColumn<Order, Double> quantityCol;
    @FXML private TableColumn<Order, String> statusCol;
    @FXML private TableColumn<Order, String> productionDateCol;
    @FXML private TableColumn<Order, String> shippingDateCol;
    @FXML private TableColumn<Order, Double> consumptionCol;
    @FXML private TableColumn<Order, Void> actionsCol;
    @FXML private Button addButton;
    @FXML private Button refreshButton;

    private OrderService orderService = new OrderService();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        orderNumberCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getOrderNumber()));
        clientNameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getClientName()));
        productTypeCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProductType()));
        quantityCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getQuantity()).asObject());
        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus()));
        productionDateCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getProductionDate() != null ?
                                data.getValue().getProductionDate().toString() : ""));
        shippingDateCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getShippingDate() != null ?
                                data.getValue().getShippingDate().toString() : ""));
        consumptionCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getMaterialConsumption()).asObject());

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Ред.");
            private final Button statusBtn = new Button("Статус");
            private final HBox pane = new HBox(5, editBtn, statusBtn);

            {
                editBtn.setStyle("-fx-background-color: #6B7B84; -fx-text-fill: white; -fx-font-size: 12px;");
                statusBtn.setStyle("-fx-background-color: #5E4B3B; -fx-text-fill: white; -fx-font-size: 12px;");

                editBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleEditOrder(order);
                });

                statusBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleChangeStatus(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        //цветовое выделение статусов
        statusCol.setCellFactory(col -> new TableCell<>() {
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

    @FXML
    private void handleAddOrder() {
        Dialog<Order> dialog = createOrderDialog(null);
        Optional<Order> result = dialog.showAndWait();
        result.ifPresent(order -> {
            orderService.addOrder(order);
            loadData();
        });
    }

    private void handleEditOrder(Order order) {
        Dialog<Order> dialog = createOrderDialog(order);
        Optional<Order> result = dialog.showAndWait();
        result.ifPresent(updatedOrder -> {
            updatedOrder.setId(order.getId());
            orderService.updateOrder(updatedOrder);
            loadData();
        });
    }

    private void handleChangeStatus(Order order) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                order.getStatus(),
                "В производстве", "Готов к отгрузке", "Отгружен"
        );
        dialog.setTitle("Изменить статус");
        dialog.setHeaderText("Выберите новый статус для заказа " + order.getOrderNumber());
        dialog.setContentText("Статус:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newStatus -> {
            order.setStatus(newStatus);
            if ("Отгружен".equals(newStatus)) {
                order.setShippingDate(LocalDate.now());
            }
            orderService.updateOrder(order);
            loadData();
        });
    }

    private Dialog<Order> createOrderDialog(Order existing) {
        Dialog<Order> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Добавить заказ" : "Редактировать заказ");
        dialog.setHeaderText(existing == null ? "Введите данные заказа" : "Измените данные заказа");

        TextField orderNumberField = new TextField(existing != null ? existing.getOrderNumber() : "");
        orderNumberField.setPromptText("Номер заказа");

        TextField clientNameField = new TextField(existing != null ? existing.getClientName() : "");
        clientNameField.setPromptText("Название клиента");

        TextField productTypeField = new TextField(existing != null ? existing.getProductType() : "");
        productTypeField.setPromptText("Тип продукции");

        TextField quantityField = new TextField(existing != null ? String.valueOf(existing.getQuantity()) : "");
        quantityField.setPromptText("Количество");

        DatePicker productionDatePicker = new DatePicker(
                existing != null && existing.getProductionDate() != null ?
                        existing.getProductionDate() : LocalDate.now()
        );

        DatePicker shippingDatePicker = new DatePicker(
                existing != null && existing.getShippingDate() != null ?
                        existing.getShippingDate() : null
        );

        TextField consumptionField = new TextField(
                existing != null ? String.valueOf(existing.getMaterialConsumption()) : "0"
        );
        consumptionField.setPromptText("Расход сырья (тонн)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        grid.add(new Label("Номер заказа:"), 0, 0);
        grid.add(orderNumberField, 1, 0);
        grid.add(new Label("Клиент:"), 0, 1);
        grid.add(clientNameField, 1, 1);
        grid.add(new Label("Продукция:"), 0, 2);
        grid.add(productTypeField, 1, 2);
        grid.add(new Label("Количество:"), 0, 3);
        grid.add(quantityField, 1, 3);
        grid.add(new Label("Дата производства:"), 0, 4);
        grid.add(productionDatePicker, 1, 4);
        grid.add(new Label("Дата отгрузки:"), 0, 5);
        grid.add(shippingDatePicker, 1, 5);
        grid.add(new Label("Расход сырья (т):"), 0, 6);
        grid.add(consumptionField, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #E6E4DA;");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    return new Order(
                            existing != null ? existing.getId() : 0,
                            orderNumberField.getText(),
                            clientNameField.getText(),
                            productTypeField.getText(),
                            Double.parseDouble(quantityField.getText()),
                            existing != null ? existing.getStatus() : "В производстве",
                            productionDatePicker.getValue(),
                            shippingDatePicker.getValue(),
                            Double.parseDouble(consumptionField.getText())
                    );
                } catch (NumberFormatException e) {
                    showAlert("Ошибка", "Введите корректные числовые значения");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void loadData() {
        ordersTable.setItems(FXCollections.observableArrayList(orderService.getAllOrders()));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}