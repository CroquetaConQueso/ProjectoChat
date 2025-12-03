package prjavafxchat_servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.ClienteInfo;

public class ServidorChat {

    private static final String PREFIJO_LISTA = "[SERVER_LIST]:";
    private static final String PREFIJO_USUARIOS = "[SERVER_USERS]:";

    private ServerSocket serverSocket;
    private Map<String, List<ClienteInfo>> salas = new HashMap<String, List<ClienteInfo>>();

    public ServidorChat(int puerto) throws IOException {
        serverSocket = new ServerSocket(puerto);
        System.out.println("Servidor iniciado en puerto " + puerto);

        // Sala por defecto
        salas.put("General", new ArrayList<ClienteInfo>());

        Thread hiloAceptar = new Thread(new Runnable() {
            @Override
            public void run() {
                aceptarClientes();
            }
        });
        hiloAceptar.start();
    }

    //Todo el que entra va a poder unirse, ya que es un bucle eterno
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void manejarCliente(Socket socket) {
        ClienteInfo cliente = null;

        try {
            //Para leer los mensajes
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //Para enviar los mensajes
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);

            //Sala inicial
            String salaInicial = "General";
            //Pojo con los valores
            cliente = new ClienteInfo("Anonimo", salaInicial, salida, "Anonimo");

            anadirClienteASala(cliente, salaInicial);
            salida.println("Bienvenido a la sala General. Usa /name para cambiar nombre y /join para cambiar de sala. Usa /list y /users para obtener informacion.");

            //Se enseñan todas las salas
            enviarListaSalas(cliente);
            //Misma logica pero con clientes
            enviarListaUsuariosSala(salaInicial);

            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                mensaje = mensaje.trim();
                if (mensaje.isEmpty()) {
                    continue;
                }
                procesarMensaje(cliente, mensaje);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (cliente != null) {
                String sala = cliente.getSalaActual();
                eliminarClienteDeSala(cliente, sala);
                enviarListaUsuariosSala(sala);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void procesarMensaje(ClienteInfo cliente, String mensaje) {
        if (mensaje.charAt(0) == '/') {
            //Si empieza con el "/" lo llevo a un switch
            procesarComando(cliente, mensaje);
        } else {
            //Si no es un mensaje que empieza por "/" se envia normal
            String mensajeCompleto = cliente.getNombre() + ": " + mensaje;
            enviarMensajeSala(cliente.getSalaActual(), mensajeCompleto);
        }
    }

    private String[] dividirComando(String mensaje) {
        int indiceEspacio = mensaje.indexOf(' ');
        if (indiceEspacio == -1) {
            return new String[]{mensaje, ""};
        }
        String comando = mensaje.substring(0, indiceEspacio);
        String argumento = mensaje.substring(indiceEspacio + 1).trim();
        return new String[]{comando, argumento};
    }

    //Control de la logica de las funcionalidades
    private synchronized void procesarComando(ClienteInfo cliente, String mensaje) {
        String[] partes = dividirComando(mensaje);
        String comando = partes[0];
        String argumento = partes[1];

        switch (comando) {
            case "/name":
                //En el caso de que quiera lidiar con el nombre
                manejarComandoName(cliente, argumento);
                break;
            case "/join":
                //Para unirse a una sala
                manejarComandoJoin(cliente, argumento);
                break;
            case "/list":
                //Para tomar las salas
                enviarListaSalas(cliente);
                break;
            case "/users":
                //Para tomar los usuarios
                enviarListaUsuariosSala(cliente.getSalaActual());
                break;
            default:
                cliente.getOut().println("Comando no reconocido: " + comando);
        }
    }

    private void manejarComandoName(ClienteInfo cliente, String nuevoNombre) {
        //El argumento ya contiene solo el nombre introducido tras /name
        if (nuevoNombre.isEmpty()) {
            cliente.getOut().println("Debes de tener un nombre. Intentalo de nuevo.");
        } else {
            cliente.setNombre(nuevoNombre);
            cliente.getOut().println("Tu nuevo nombre es: " + nuevoNombre);
            enviarListaUsuariosSala(cliente.getSalaActual());
        }
    }

    private void manejarComandoJoin(ClienteInfo cliente, String nuevaSala) {
        if (nuevaSala.isEmpty()) {
            cliente.getOut().println("Debes indicar una sala. Ejemplo: /join General");
            return;
        }
        String salaAnterior = cliente.getSalaActual();
        moverClienteASala(cliente, salaAnterior, nuevaSala);
        cliente.getOut().println("Te has movido a la sala: " + cliente.getSalaActual());
        enviarListaUsuariosSala(salaAnterior);
        enviarListaUsuariosSala(cliente.getSalaActual());
        enviarListaSalas(cliente);
    }

    private synchronized List<ClienteInfo> obtenerListaSala(String nombreSala) {
        List<ClienteInfo> lista = salas.get(nombreSala);
        if (lista == null) {
            lista = new ArrayList<ClienteInfo>();
            salas.put(nombreSala, lista);
        }
        return lista;
    }

    private synchronized void anadirClienteASala(ClienteInfo cliente, String nombreSala) {
        List<ClienteInfo> lista = obtenerListaSala(nombreSala);
        lista.add(cliente);
        cliente.setSalaActual(nombreSala);
    }

    private synchronized void moverClienteASala(ClienteInfo cliente, String salaAnterior, String nuevaSala) {
        eliminarClienteDeSala(cliente, salaAnterior);
        anadirClienteASala(cliente, nuevaSala);
    }

    private synchronized void eliminarClienteDeSala(ClienteInfo cliente, String nombreSala) {
        List<ClienteInfo> lista = salas.get(nombreSala);
        if (lista != null) {
            lista.remove(cliente);
        }
    }

    private synchronized void enviarListaSalas(ClienteInfo destino) {
        List<String> nombresSalas = new ArrayList<String>(salas.keySet());

        //Se toman los keys y se formatea el string para mostrar todos los valores
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nombresSalas.size(); i++) {
            sb.append(nombresSalas.get(i));
            if (i < nombresSalas.size() - 1) {
                sb.append(",");
            }
        }
        String contenido = sb.toString();
        destino.getOut().println(PREFIJO_LISTA + contenido);
    }

    private synchronized void enviarListaUsuariosSala(String nombreSala) {
        List<ClienteInfo> lista = salas.get(nombreSala);
        if (lista == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lista.size(); i++) {
            ClienteInfo c = lista.get(i);
            sb.append(c.getNombre()).append("/").append(c.getRol());
            if (i < lista.size() - 1) {
                sb.append(",");
            }
        }
        String mensaje = PREFIJO_USUARIOS + sb.toString();

        //Reutilizamos la misma lista para enviar el mensaje a todos los clientes de la sala
        for (ClienteInfo c : lista) {
            c.getOut().println(mensaje);
        }
    }

    private synchronized void enviarMensajeSala(String nombreSala, String mensaje) {
        List<ClienteInfo> lista = salas.get(nombreSala);
        if (lista == null) {
            //Si no esta en una sala por cualquier cosa, se vuelve atras
            return;
        }
        for (ClienteInfo c : lista) {
            c.getOut().println(mensaje);
        }
    }

    public static void main(String[] args) throws IOException {
        new ServidorChat(5000);
    }
}
