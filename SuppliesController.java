package com.kartonplus.controller;

import com.kartonplus.model.RawMaterial;
import com.kartonplus.model.Supply;
import com.kartonplus.service.MaterialService;
import com.kartonplus.service.SupplyService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.Optional;

public class SuppliesController {
    @FXML private TableView<Supply> suppliesTable;
    @FXML private TableColumn<Supply, Integer> idCol;
    @FXML private TableColumn<Supply, String> materialNameCol;
    @FXML private TableColumn<Supply, Double> quantityCol;
    @FXML private TableColumn<Supply, Integer> rollsCol;
    @FXML private TableColumn<Supply, String> supplyDateCol;
    @FXML private TableColumn<Supply, String> supplierCol;
    @FXML private TableColumn<Supply, String> documentCol;
    @FXML private Button addButton;
    @FXML private Button refreshButton;

    private SupplyService supplyService = new SupplyService();
    private MaterialService materialService = new MaterialService();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        idCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getId()).asObject());
        materialNameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMaterialName()));
        quantityCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getQuantity()).asObject());
        rollsCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getRolls()).asObject());
        supplyDateCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSupplyDate().toString()));
        supplierCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSupplier()));
        documentCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDocumentNumber()));
    }

    @FXML
    private void handleAddSupply() {
        Dialog<Supply> dialog = createSupplyDialog();
        Optional<Supply> result = dialog.showAndWait();
        result.ifPresent(supply -> {
            Supply added = supplyService.addSupply(supply);
            if (added != null) {
                //обновление количество материала на складе
                RawMaterial material = materialService.getAllMaterials()
                        .stream()
                        .filter(m -> m.getId() == supply.getMaterialId())
                        .findFirst()
                        .orElse(null);

                if (material != null) {
                    material.setQuantity(material.getQuantity() + supply.getQuantity());
                    material.setRolls(material.getRolls() + supply.getRolls());
                    material.setLastSupplyDate(supply.getSupplyDate());
                    materialService.updateMaterial(material);
                }

                loadData();
            }
        });
    }

    private Dialog<Supply> createSupplyDialog() {
        Dialog<Supply> dialog = new Dialog<>();
        dialog.setTitle("Добавить поставку");
        dialog.setHeaderText("Введите данные о поставке");

        //выпадающий список материалов
        ComboBox<RawMaterial> materialCombo = new ComboBox<>();
        materialCombo.setItems(FXCollections.observableArrayList(materialService.getAllMaterials()));
        materialCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(RawMaterial item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        materialCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(RawMaterial item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        TextField quantityField = new TextField();
        quantityField.setPromptText("Количество в тоннах");

        TextField rollsField = new TextField();
        rollsField.setPromptText("Количество рулонов");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        TextField supplierField = new TextField();
        supplierField.setPromptText("Название поставщика");

        TextField documentField = new TextField();
        documentField.setPromptText("Номер документа");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        grid.add(new Label("Материал:"), 0, 0);
        grid.add(materialCombo, 1, 0);
        grid.add(new Label("Количество (т):"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Рулонов:"), 0, 2);
        grid.add(rollsField, 1, 2);
        grid.add(new Label("Дата поставки:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Поставщик:"), 0, 4);
        grid.add(supplierField, 1, 4);
        grid.add(new Label("Документ №:"), 0, 5);
        grid.add(documentField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.getDialogPane().setStyle("-fx-background-color: #E6E4DA;");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    RawMaterial selectedMaterial = materialCombo.getValue();
                    if (selectedMaterial == null) {
                        showAlert("Ошибка", "Выберите материал");
                        return null;
                    }

                    double quantity = Double.parseDouble(quantityField.getText());
                    int rolls = Integer.parseInt(rollsField.getText());

                    return new Supply(
                            0,
                            selectedMaterial.getId(),
                            selectedMaterial.getName(),
                            quantity,
                            rolls,
                            datePicker.getValue(),
                            supplierField.getText(),
                            documentField.getText()
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
        suppliesTable.setItems(FXCollections.observableArrayList(supplyService.getAllSupplies()));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}