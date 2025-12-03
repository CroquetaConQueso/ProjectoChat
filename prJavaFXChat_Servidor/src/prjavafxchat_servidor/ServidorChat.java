package prjavafxchat_servidor;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorChat {
    private ServerSocket serverSocket;
    private Map<String, List<PrintWriter>> salas = Collections.synchronizedMap(new HashMap<String, List<PrintWriter>>());

    public ServidorChat(int puerto) throws IOException {
        serverSocket = new ServerSocket(puerto);
        System.out.println("Servidor iniciado en puerto " + puerto);
        
        // Sala por defecto
        salas.put("General", Collections.synchronizedList(new ArrayList<PrintWriter>()));

        Thread hiloAceptar = new Thread(new Runnable() {
            @Override
            public void run() {
                aceptarClientes();
            }
        });
        hiloAceptar.start();
    }

    private void aceptarClientes() {
        while (true) {
            try {
                final Socket socket = serverSocket.accept();
                Thread hiloCliente = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        manejarCliente(socket);
                    }
                });
                hiloCliente.start();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void manejarCliente(Socket socket) {
        String nombre = "Anonimo";
        String salaActual = "General";
        PrintWriter out = null;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Añadir cliente a sala general
            salas.get(salaActual).add(out);
            out.println("Bienvenido a la sala General. Usa /name para cambiar nombre y /join para cambiar de sala.");

            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                if (mensaje.startsWith("/name ")) {
                    nombre = mensaje.substring(6).trim();
                    out.println("Tu nombre se ha cambiado a: " + nombre);
                } else if (mensaje.startsWith("/join ")) {
                    String nuevaSala = mensaje.substring(6).trim();
                    if (!salas.containsKey(nuevaSala)) {
                        salas.put(nuevaSala, Collections.synchronizedList(new ArrayList<PrintWriter>()));
                    }
                    // Mover cliente a la nueva sala
                    salas.get(salaActual).remove(out);
                    salaActual = nuevaSala;
                    salas.get(salaActual).add(out);
                    out.println("Has cambiado a la sala: " + salaActual);
                } else {
                    // Mensaje normal, reenviar a todos en la misma sala
                    String completo = nombre + ": " + mensaje;
                    for (PrintWriter cliente : salas.get(salaActual)) {
                        cliente.println(completo);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                salas.get(salaActual).remove(out);
            }
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public static void main(String[] args) throws IOException {
        new ServidorChat(5000);
    }
}
