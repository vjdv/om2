package net.vjdv.baz.om2.models;

import java.text.Normalizer;

import javax.xml.bind.annotation.XmlAttribute;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author B187926
 */
public class Procedimiento extends Recurso {

    private final SimpleStringProperty map = new SimpleStringProperty();
    private String filteringString = null;

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
            filteringString = filteringString.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        }
        return filteringString;
    }

}
