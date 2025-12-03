package prjavafxchat_cliente.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import prjavafxchat_cliente.network.ClienteSocket;

public class ClienteVistaController {

    @FXML
    private TextArea mensajesArea;
    @FXML
    private TextField entradaField;

    private ClienteSocket cliente;

    @FXML
    public void initialize() {
        // Crear socket y conectar
        cliente = new ClienteSocket(this);
        cliente.conectar();
    }

    @FXML
    private void enviarMensaje() {
        String msg = entradaField.getText();

        if (!msg.isEmpty()) {
//            mensajesArea.appendText("Yo: " + msg + "\n");  // Mostrar en la UI
            entradaField.clear();                          // Borrar el campo de texto

            // Si tienes cliente socket:
            if (cliente != null) {
                cliente.enviar(msg);
            }
        }
    }

    // Método llamado desde ClienteSocket para mostrar mensajes
    public void recibirMensaje(String msg) {
        Platform.runLater(() -> mensajesArea.appendText(msg + "\n"));
    }
}
