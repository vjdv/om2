package net.vjdv.baz.om2.models;

import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javax.xml.bind.annotation.XmlAttribute;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
    private final SimpleObjectProperty<List<Circle>> marcas = new SimpleObjectProperty<>(new ArrayList<>());
    private String filteringString = null;
    private boolean conCambios = false;
    private boolean pendienteSubir = false;
    private Path lastParentPath = null;
    private Path path = null;

    @XmlAttribute
    public String getSchema() {
        return schema.get();
    }

    public final void setSchema(String schema) {
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

    public ObjectProperty<List<Circle>> marcasProperty() {
        return marcas;
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
        if (conCambios) {
            Circle c = new Circle();
            c.setFill(Color.web("#347ca8"));
            c.setRadius(7);
            c.setStroke(Color.web("#333"));
            c.setStrokeWidth(1);
            marcas.get().add(c);
        }
    }

    @XmlTransient
    public boolean isPendienteSubir() {
        return pendienteSubir;
    }

    public void setPendienteSubir(boolean pendienteSubir) {
        this.pendienteSubir = pendienteSubir;
        if (pendienteSubir) {
            Circle c = new Circle();
            c.setFill(Color.web("#d9821e"));
            c.setRadius(7);
            c.setStroke(Color.web("#333"));
            c.setStrokeWidth(1);
            marcas.get().add(c);
        }
    }

    public Path getPath(Path padre) {
        if (path == null || !lastParentPath.equals(padre)) {
            lastParentPath = padre;
            path = padre.resolve(getSchema()).resolve(getNombre() + ".sql");
        }
        return path;
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
