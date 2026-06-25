module com.kartonplus {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    requires java.sql;
    requires java.net.http;
    requires com.google.gson;

    opens com.kartonplus to javafx.fxml;
    opens com.kartonplus.controller to javafx.fxml;
    opens com.kartonplus.model to com.google.gson;
    exports com.kartonplus;
    exports com.kartonplus.controller;
    exports com.kartonplus.model;
    exports com.kartonplus.service;
    exports com.kartonplus.util;
    exports com.kartonplus.config;
}