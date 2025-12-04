import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import prjavafxchat_cliente.controller.ClienteVistaController;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/prjavafxchat_cliente/view/ClienteVista.fxml"));
        Parent root = loader.load();
        ClienteVistaController controller = loader.getController();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Cliente Chat");

        // Al cerrar la ventana, cerramos el socket
        stage.setOnCloseRequest(e -> {controller.cerrarAplicacion();});
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
