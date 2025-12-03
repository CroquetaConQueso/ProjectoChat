package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Usuario{

    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty rol = new SimpleStringProperty();

    public Usuario(String nombre, String rol) {
        this.nombre.set(nombre);
        this.rol.set(rol);
    }

    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public String getRol() {
        return rol.get();
    }

    public void setRol(String rol) {
        this.rol.set(rol);
    }

    public StringProperty rolProperty() {
        return rol;
    }
}
