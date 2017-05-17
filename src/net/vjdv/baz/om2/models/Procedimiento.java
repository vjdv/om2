package net.vjdv.baz.om2.models;

import java.text.Normalizer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author B187926
 */
public class Procedimiento extends Recurso {

    private final SimpleStringProperty map = new SimpleStringProperty();
    private String cuerpo, cuerpo2;
    private String filteringString = null;

    public Procedimiento() {
        setTipo(PROCEDIMIENTO);
    }

    public Procedimiento(String n) {
        this();
        setNombre(n);
    }

    public String getUri() {
        return getNombre() + ".sql";
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

    @Override
    public String getFilteringString() {
        if (filteringString == null) {
            filteringString = (getNombre() + " " + getMap() + " " + getDescripcion()).toLowerCase();
            filteringString = Normalizer.normalize(filteringString, Normalizer.Form.NFD);
        }
        return filteringString;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public String getCuerpoCleaned() {
        return cuerpo2 == null ? "" : cuerpo2;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
        String filtered = cuerpo.replaceAll("/\\*.*\\*/", "");
        filtered = filtered.replaceAll("--.*(?=\\n)", "");
        filtered = filtered.replaceAll("--.*(?=\\r\\n)", "");
        cuerpo2 = filtered.toLowerCase().trim();
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
