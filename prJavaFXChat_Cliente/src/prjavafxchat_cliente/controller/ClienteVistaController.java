package prjavafxchat_cliente.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import model.Usuario;
import prjavafxchat_cliente.network.ClienteSocket;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ClienteVistaController implements Initializable {

    @FXML
    private TextArea mensajesArea;

    @FXML
    private TextField entradaField;

    @FXML
    private TextField nombreField;

    @FXML
    private ChoiceBox<String> salasChoiceBox;

    @FXML
    private Button botonUnirse;

    @FXML
    private TableView<Usuario> tablaUsuarios;

    @FXML
    private TableColumn<Usuario, String> colNombre;

    @FXML
    private TableColumn<Usuario, String> colRol;

    private ClienteSocket cliente;
    private ObservableList<Usuario> usuariosObservable = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (tablaUsuarios != null) {
            tablaUsuarios.setItems(usuariosObservable);
        }
        if (colNombre != null) {
            colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        }
        if (colRol != null) {
            colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        }

        cliente = new ClienteSocket(this);
        cliente.conectar();
    }

    @FXML
    private void enviarMensaje() {
        if (entradaField == null) {
            return;
        }
        String msg = entradaField.getText();
        if (msg != null) {
            msg = msg.trim();
        }
        if (msg == null || msg.isEmpty()) {
            return;
        }
        if (cliente != null) {
            cliente.enviar(msg);
        }
        entradaField.clear();
    }

    @FXML
    private void enviarMensajeAction() {
        enviarMensaje();
    }

    @FXML
    private void cambiarNombre() {
        if (nombreField == null) {
            return;
        }
        String nombre = nombreField.getText();
        if (nombre != null) {
            nombre = nombre.trim();
        }
        if (nombre == null || nombre.isEmpty()) {
            return;
        }
        if (cliente != null) {
            cliente.enviar("/name " + nombre);
        }
    }

    @FXML
    private void unirseSala() {
        if (salasChoiceBox == null) {
            return;
        }
        String sala = salasChoiceBox.getValue();
        if (sala == null || sala.trim().isEmpty()) {
            return;
        }
        if (cliente != null) {
            cliente.enviar("/join " + sala.trim());
        }
    }

    public void recibirMensaje(String msg) {
        Platform.runLater(() -> {
            if (mensajesArea != null) {
                mensajesArea.appendText(msg + "\n");
            }
        });
    }

    public void actualizarSalas(List<String> salas) {
        Platform.runLater(() -> {
            if (salasChoiceBox != null) {
                salasChoiceBox.getItems().setAll(salas);
                if (!salas.isEmpty() && salasChoiceBox.getValue() == null) {
                    salasChoiceBox.setValue(salas.get(0));
                }
            }
        });
    }

    public void actualizarUsuarios(List<Usuario> listaUsuarios) {
        Platform.runLater(() -> {
            usuariosObservable.setAll(listaUsuarios);
        });
    }

    public void desactivarControles() {
        Platform.runLater(() -> {
            if (entradaField != null) {
                entradaField.setDisable(true);
            }
            if (nombreField != null) {
                nombreField.setDisable(true);
            }
            if (salasChoiceBox != null) {
                salasChoiceBox.setDisable(true);
            }
            if (botonUnirse != null) {
                botonUnirse.setDisable(true);
            }
        });
    }
}
