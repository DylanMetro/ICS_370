module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires json.simple;


    opens com.example.demo to javafx.fxml;
    opens com.example.demo.domain to javafx.fxml;
    exports com.example.demo;
    exports com.example.demo.domain;
}