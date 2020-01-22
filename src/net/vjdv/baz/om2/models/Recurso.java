package net.vjdv.baz.om2.models;

import java.text.Normalizer;

import javax.xml.bind.annotation.XmlAttribute;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Data;

/**
 *
 * @author B187926
 */
public class Recurso {

    public static final String TABLA = "TB";
    public static final String PROCEDIMIENTO = "SP";
    public static final String FUNCION = "FN";
    public static final String SNIPPET = "SNIPPET";
    public static final String[] TIPOS = {TABLA, PROCEDIMIENTO, FUNCION, SNIPPET};
    private final SimpleStringProperty schema = new SimpleStringProperty("dbo");
    private final SimpleStringProperty nombre = new SimpleStringProperty();
    private final SimpleStringProperty tipo = new SimpleStringProperty();
    private final SimpleStringProperty descripcion = new SimpleStringProperty();
    private String filteringString = null;
    private boolean conCambios = false;
    private boolean pendienteSubir = false;

    @XmlAttribute
    public String getSchema() {
        return schema.get();
    }

    public void setSchema(String schema) {
        this.schema.set(schema);
    }

    public StringProperty schemaProperty() {
        return schema;
    }

    @XmlAttribute
    public String getNombre() {
        return nombre.get();
    }

    public final void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    @XmlAttribute
    public String getTipo() {
        return tipo.get();
    }

    public final void setTipo(String tipo) {
        this.tipo.set(tipo);
    }

    public StringProperty tipoProperty() {
        return tipo;
    }

    @XmlAttribute
    public String getDescripcion() {
        return descripcion.get();
    }

    public void setDescripcion(String descripcion) {
        this.descripcion.set(descripcion);
    }

    public StringProperty descripcionProperty() {
        return descripcion;
    }

    public String getFilteringString() {
        if (filteringString == null) {
            filteringString = (getNombre() + " " + getDescripcion()).toLowerCase();
            filteringString = Normalizer.normalize(filteringString, Normalizer.Form.NFD);
            filteringString = filteringString.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        }
        return filteringString;
    }

    @Override
    public String toString() {
        return nombre.get();
    }

    public static String sqlDelete() {
        return "DELETE FROM OM2_OBJECTS WHERE id_obj=?";
    }

    @XmlTransient
    public boolean isConCambios() {
        return conCambios;
    }

    public void setConCambios(boolean conCambios) {
        this.conCambios = conCambios;
    }

    @XmlTransient
    public boolean isPendienteSubir() {
        return pendienteSubir;
    }

    public void setPendienteSubir(boolean pendienteSubir) {
        this.pendienteSubir = pendienteSubir;
    }

    @Data
    public static class Datos {

        private String tipo;
        private String esquema;
        private String nombre;
        private String mapeo;
        private String descripcion;
    }

}
