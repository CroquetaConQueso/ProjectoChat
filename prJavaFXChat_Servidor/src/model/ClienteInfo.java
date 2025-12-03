/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.io.PrintWriter;

public class ClienteInfo {

    private String nombre;
    private String salaActual;
    private PrintWriter out;
    private String rol;

    public ClienteInfo(String nombre, String salaActual, PrintWriter out, String rol) {
        this.nombre = nombre;
        this.salaActual = salaActual;
        this.out = out;
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSalaActual() {
        return salaActual;
    }

    public void setSalaActual(String salaActual) {
        this.salaActual = salaActual;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}