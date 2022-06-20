module com.example.network_chat {
    requires javafx.controls;
    requires javafx.fxml;


    exports com.example.network_chat.client;
    opens com.example.network_chat.client to javafx.fxml;
}