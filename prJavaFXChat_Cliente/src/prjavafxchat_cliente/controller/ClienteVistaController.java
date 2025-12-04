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

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import model.Usuario;
import prjavafxchat_cliente.network.ClienteSocket;

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

    // Nombre local mostrado en el textfield
    private String nombreActual = "Anonimo";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if (tablaUsuarios != null) {
            tablaUsuarios.setItems(usuariosObservable);
            // Ajuste de columnas para ocupar todo el ancho
            tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
        if (colNombre != null) {
            colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        }
        if (colRol != null) {
            colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        }

        // Nombre inicial
        if (nombreField != null) {
            nombreField.setText(nombreActual);
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
        nombreActual = nombre;
        if (cliente != null) {
            cliente.enviar("/name " + nombre);
        }
        nombreField.setText(nombreActual);
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
                String seleccionAnterior = salasChoiceBox.getValue();
                salasChoiceBox.getItems().setAll(salas);

                if (seleccionAnterior != null && salas.contains(seleccionAnterior)) {
                    salasChoiceBox.setValue(seleccionAnterior);
                } else if (!salas.isEmpty()) {
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

    // Se llamara cuando el servidor confirme el cambio de nombre
    public void actualizarNombreLocal(String nuevoNombre) {
        nombreActual = nuevoNombre;
        Platform.runLater(() -> {
            if (nombreField != null) {
                nombreField.setText(nombreActual);
            }
        });
    }

    // Se llamara cuando el servidor confirme el cambio de sala
    public void actualizarSalaActual(String salaNueva) {
        Platform.runLater(() -> {
            if (salasChoiceBox != null) {
                if (!salasChoiceBox.getItems().contains(salaNueva)) {
                    salasChoiceBox.getItems().add(salaNueva);
                }
                salasChoiceBox.setValue(salaNueva);
            }
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

    // Llamado desde Main cuando se cierra la ventana
    public void cerrarAplicacion() {
        if (cliente != null) {
            cliente.cerrar();
        }
    }
}
