package net.vjdv.baz.om2.models;

/**
 *
 * @author B187926
 */
public class Tabla extends Recurso {

    public Tabla() {
        setTipo(TABLA);
    }

    public Tabla(String n) {
        this();
        setNombre(n);
    }

    @Override
    public String toString() {
        return getNombre();
    }

}
