package prjavafxchat_cliente.network;

import prjavafxchat_cliente.controller.ClienteVistaController;
import model.Usuario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClienteSocket {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    private static final String PREFIJO_LISTA = "[SERVER_LIST]:";
    private static final String PREFIJO_USUARIOS = "[SERVER_USERS]:";

    private ClienteVistaController controller;
    private PrintWriter out;

    private Socket socket;
    private BufferedReader in;
    private volatile boolean ejecutando = false;

    public ClienteSocket(ClienteVistaController controller) {
        this.controller = controller;
    }

    //Conexion y bucle de lectura
    public void conectar() {
        new Thread(() -> {
            try {
                socket = new Socket(HOST, PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ejecutando = true;

                controller.recibirMensaje("Conectado al servidor " + HOST + ":" + PORT);

                //Comandos iniciales opcionales
                out.println("/list");
                out.println("/users");

                String linea;
                while (ejecutando && (linea = in.readLine()) != null) {
                    procesarLineaServidor(linea);
                }

            } catch (IOException e) {
                controller.recibirMensaje("Error: " + e.getMessage());
            } finally {
                cerrarInterno();
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
            String prefNombre = "Tu nuevo nombre es: ";
            if (linea.startsWith(prefNombre)) {
                String nuevoNombre = linea.substring(prefNombre.length()).trim();
                controller.actualizarNombreLocal(nuevoNombre);
            }

            String prefSala = "Te has movido a la sala: ";
            if (linea.startsWith(prefSala)) {
                String salaNueva = linea.substring(prefSala.length()).trim();
                controller.actualizarSalaActual(salaNueva);
            }

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

    //Convierte "Pedro/Anonimo,Juan/Profesor" en lista de Usuario
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

    //Llamado desde el controlador cuando se cierra la ventana
    public void cerrar() {
        ejecutando = false;
        cerrarInterno();
    }

    private void cerrarInterno() {
        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            //Nada especial
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
            //Nada especial
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            //Nada especial
        }
    }
}
