package prjavafxchat_cliente.network;

import model.Usuario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import prjavafxchat_cliente.controller.ClienteVistaController;

public class ClienteSocket {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    private static final String PREFIJO_LISTA = "[SERVER_LIST]:";
    private static final String PREFIJO_USUARIOS = "[SERVER_USERS]:";

    private ClienteVistaController controller;
    private PrintWriter out;

    public ClienteSocket(ClienteVistaController controller) {
        this.controller = controller;
    }

    //Conexion y bucle de lectura
    public void conectar() {
        new Thread(() -> {
            Socket socket = null;
            BufferedReader in = null;

            try {
                socket = new Socket(HOST, PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                controller.recibirMensaje("Conectado al servidor " + HOST + ":" + PORT);

                //Comandos iniciales opcionales
                out.println("/list");
                out.println("/users");

                String linea;
                while ((linea = in.readLine()) != null) {
                    procesarLineaServidor(linea);
                }

            } catch (IOException e) {
                controller.recibirMensaje("Error: " + e.getMessage());
                controller.desactivarControles();
            } finally {
                if (out != null) {
                    out.close();
                }
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    //Nada especial que hacer aqui
                }
                controller.desactivarControles();
            }
        }).start();
    }

    //Procesa cada linea segun el prefijo que haya enviado el servidor
    private void procesarLineaServidor(String linea) {
        if (linea.startsWith(PREFIJO_LISTA)) {
            String contenido = linea.substring(PREFIJO_LISTA.length());
            List<String> salas = parsearSalas(contenido);
            controller.actualizarSalas(salas);

        } else if (linea.startsWith(PREFIJO_USUARIOS)) {
            String contenido = linea.substring(PREFIJO_USUARIOS.length());
            List<Usuario> usuarios = parsearUsuarios(contenido);
            controller.actualizarUsuarios(usuarios);

        } else {
            //Mensaje normal, bienvenida o errores
            controller.recibirMensaje(linea);
        }
    }

    //Convierte "General,Sala2,Sala3" en lista de strings
    private List<String> parsearSalas(String contenido) {
        List<String> resultado = new ArrayList<String>();
        if (contenido == null || contenido.isEmpty()) {
            return resultado;
        }
        String[] partes = contenido.split(",");
        for (String s : partes) {
            String sala = s.trim();
            if (!sala.isEmpty()) {
                resultado.add(sala);
            }
        }
        return resultado;
    }

    //Pasa los usuarios a la lista, parseandola 
    private List<Usuario> parsearUsuarios(String contenido) {
        List<Usuario> resultado = new ArrayList<Usuario>();
        if (contenido == null || contenido.isEmpty()) {
            return resultado;
        }
        String[] partes = contenido.split(",");
        for (String parte : partes) {
            String token = parte.trim();
            if (token.isEmpty()) {
                continue;
            }
            String[] campos = token.split("/");
            String nombre = campos.length > 0 ? campos[0].trim() : "";
            String rol = campos.length > 1 ? campos[1].trim() : "";
            if (!nombre.isEmpty()) {
                Usuario u = new Usuario(nombre, rol);
                resultado.add(u);
            }

        }
        return resultado;
    }

    public void enviar(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }
}
