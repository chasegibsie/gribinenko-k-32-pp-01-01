package com.kartonplus.controller;

import com.kartonplus.model.RawMaterial;
import com.kartonplus.service.MaterialService;
import com.kartonplus.util.AlertHelper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class MaterialsController {
    @FXML private TableView<RawMaterial> materialsTable;
    @FXML private TableColumn<RawMaterial, String> nameCol;
    @FXML private TableColumn<RawMaterial, String> typeCol;
    @FXML private TableColumn<RawMaterial, Double> quantityCol;
    @FXML private TableColumn<RawMaterial, Integer> rollsCol;
    @FXML private TableColumn<RawMaterial, Double> dailyConsumptionCol;
    @FXML private TableColumn<RawMaterial, Integer> daysCol;
    @FXML private TableColumn<RawMaterial, String> supplierCol;
    @FXML private TableColumn<RawMaterial, Void> actionsCol;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Label statusLabel;

    private MaterialService materialService = new MaterialService();
    private ObservableList<RawMaterial> materials = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        daysCol.setCellFactory(col -> new TableCell<RawMaterial, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    if (item < 2) {
                        setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
                    } else if (item < 7) {
                        setStyle("-fx-text-fill: #FF6B00; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2E7D32;");
                    }
                }
            }
        });
    }

    private void setupTable() {
        nameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getName()));
        typeCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getType()));
        quantityCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getQuantity()).asObject());
        rollsCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getRolls()).asObject());
        dailyConsumptionCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getDailyConsumption()).asObject());
        daysCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getDaysRemaining()).asObject());
        supplierCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSupplier()));

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Ред.");
            private final Button deleteBtn = new Button("Удал.");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #6B7B84; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;");

                editBtn.setOnAction(e -> {
                    RawMaterial material = getTableView().getItems().get(getIndex());
                    handleEditMaterial(material);
                });

                deleteBtn.setOnAction(e -> {
                    RawMaterial material = getTableView().getItems().get(getIndex());
                    handleDeleteMaterial(material);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML
    private void handleAddMaterial() {
        Dialog<RawMaterial> dialog = createMaterialDialog(null);
        Optional<RawMaterial> result = dialog.showAndWait();
        result.ifPresent(material -> {
            try {
                RawMaterial added = materialService.addMaterial(material);
                if (added != null) {
                    showStatus("Материал '" + material.getName() + "' успешно добавлен", false);
                    loadData();
                } else {
                    showStatus("Ошибка при добавлении материала", true);
                }
            } catch (Exception e) {
                showStatus("Ошибка: " + e.getMessage(), true);
                e.printStackTrace();
            }
        });
    }

    private void handleEditMaterial(RawMaterial material) {
        Dialog<RawMaterial> dialog = createMaterialDialog(material);
        Optional<RawMaterial> result = dialog.showAndWait();
        result.ifPresent(updatedMaterial -> {
            updatedMaterial.setId(material.getId());
            try {
                boolean success = materialService.updateMaterial(updatedMaterial);
                if (success) {
                    showStatus("Материал '" + updatedMaterial.getName() + "' обновлен", false);
                    loadData();
                } else {
                    showStatus("Ошибка при обновлении материала", true);
                }
            } catch (Exception e) {
                showStatus("Ошибка: " + e.getMessage(), true);
                e.printStackTrace();
            }
        });
    }

    private void handleDeleteMaterial(RawMaterial material) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Удалить материал?");
        alert.setContentText("Вы уверены, что хотите удалить '" + material.getName() + "'?");
        alert.getDialogPane().setStyle("-fx-background-color: #E6E4DA;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = materialService.deleteMaterial(material.getId());
                if (success) {
                    showStatus("Материал удален", false);
                    loadData();
                } else {
                    showStatus("Ошибка при удалении материала", true);
                }

            } catch (Exception e) {
                showStatus("Ошибка: " + e.getMessage(), true);
                e.printStackTrace();
            }
        }
    }

    private Dialog<RawMaterial> createMaterialDialog(RawMaterial existing) {
        Dialog<RawMaterial> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Добавить материал" : "Редактировать материал");
        dialog.setHeaderText(existing == null ? "Введите данные нового материала" : "Измените данные материала");

        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        nameField.setPromptText("Наименование материала");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Картон", "Гофрокартон", "Бумага", "Пленка", "Другое");
        typeCombo.setValue(existing != null ? existing.getType() : "Картон");

        TextField quantityField = new TextField(existing != null ? String.valueOf(existing.getQuantity()) : "");
        quantityField.setPromptText("Количество (тонн)");

        TextField rollsField = new TextField(existing != null ? String.valueOf(existing.getRolls()) : "");
        rollsField.setPromptText("Количество рулонов");

        TextField consumptionField = new TextField(existing != null ? String.valueOf(existing.getDailyConsumption()) : "");
        consumptionField.setPromptText("Дневной расход (тонн)");

        TextField supplierField = new TextField(existing != null ? existing.getSupplier() : "");
        supplierField.setPromptText("Поставщик");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        grid.add(new Label("Наименование:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Тип:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Количество (т):"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Рулонов:"), 0, 3);
        grid.add(rollsField, 1, 3);
        grid.add(new Label("Дневной расход:"), 0, 4);
        grid.add(consumptionField, 1, 4);
        grid.add(new Label("Поставщик:"), 0, 5);
        grid.add(supplierField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #E6E4DA;");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    String name = nameField.getText().trim();
                    if (name.isEmpty()) {
                        AlertHelper.showError("Ошибка", "Введите наименование материала");
                        return null;
                    }

                    double quantity = Double.parseDouble(quantityField.getText().trim());
                    if (quantity < 0) {
                        AlertHelper.showError("Ошибка", "Количество не может быть отрицательным");
                        return null;
                    }

                    int rolls = Integer.parseInt(rollsField.getText().trim());
                    if (rolls < 0) {
                        AlertHelper.showError("Ошибка", "Количество рулонов не может быть отрицательным");
                        return null;
                    }

                    double consumption = Double.parseDouble(consumptionField.getText().trim());
                    if (consumption < 0) {
                        AlertHelper.showError("Ошибка", "Дневной расход не может быть отрицательным");
                        return null;
                    }

                    return new RawMaterial(
                            existing != null ? existing.getId() : 0,
                            name,
                            typeCombo.getValue(),
                            quantity,
                            rolls,
                            supplierField.getText().trim(),
                            existing != null ? existing.getLastSupplyDate() : null,
                            consumption
                    );
                } catch (NumberFormatException e) {
                    AlertHelper.showError("Ошибка", "Введите корректные числовые значения");
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
        showStatus("Данные обновлены", false);
    }

    private void loadData() {
        try {
            materials.clear();
            materials.addAll(materialService.getAllMaterials());
            materialsTable.setItems(materials);
        } catch (Exception e) {
            showStatus("Ошибка загрузки данных: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setStyle(isError ?
                "-fx-text-fill: #D32F2F; -fx-font-size: 14px;" :
                "-fx-text-fill: #2E7D32; -fx-font-size: 14px;");

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> statusLabel.setVisible(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}