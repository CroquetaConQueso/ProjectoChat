package prjavafxchat_cliente.network;

import prjavafxchat_cliente.controller.ClienteVistaController;

import java.io.*;
import java.net.Socket;

public class ClienteSocket {

    private ClienteVistaController controller;
    private PrintWriter out;

    public ClienteSocket(ClienteVistaController controller) {
        this.controller = controller;
    }

    public void conectar() {
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 5000)) {

                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                String linea;
                while ((linea = in.readLine()) != null) {
                    controller.recibirMensaje(linea);
                }

            } catch (IOException e) {
                controller.recibirMensaje("Error: " + e.getMessage());
            }
        }).start();
    }

    public void enviar(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }
}
