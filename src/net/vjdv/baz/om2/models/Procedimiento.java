package net.vjdv.baz.om2.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author B187926
 */
public class Procedimiento extends Recurso {

    private final SimpleStringProperty map = new SimpleStringProperty();
    private final SimpleStringProperty uri = new SimpleStringProperty();
    private String cuerpo, cuerpo2;

    public Procedimiento() {
        setTipo(PROCEDIMIENTO);
    }

    public Procedimiento(String n) {
        this();
        setNombre(n);
    }

    @XmlAttribute
    public String getUri() {
        return uri.get();
    }

    public void setUri(String uri) {
        this.uri.set(uri);
    }

    public StringProperty uriProperty() {
        return uri;
    }

    @XmlAttribute
    public String getMap() {
        return map.get();
    }

    public void setMap(String map) {
        this.map.set(map);
    }

    public StringProperty mapProperty() {
        return map;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public String getCuerpoCleaned() {
        return cuerpo2;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
        String filtered = cuerpo.replaceAll("/\\*.*\\*/", "");
        filtered = filtered.replaceAll("--.*(?=\\n)", "");
        cuerpo2 = filtered.replaceAll("--.*(?=\\r\\n)", "");
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
