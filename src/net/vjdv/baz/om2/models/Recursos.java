package net.vjdv.baz.om2.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.extern.java.Log;

/**
 *
 * @author B187926
 */
@Log
@Data
@XmlRootElement(name = "Recursos")
@XmlAccessorType(XmlAccessType.FIELD)
public class Recursos {

    private static final Comparator<Recurso> SORTER = (o1, o2) -> o1.getSchema().equals(o2.getSchema()) ? o1.getNombre().compareTo(o2.getNombre()) : o1.getSchema().compareTo(o2.getSchema());

    @XmlElementWrapper(name = "Procedimientos")
    @XmlElement(name = "Procedimiento", nillable = false)
    private List<Procedimiento> procedimientos = new ArrayList<>();

    @XmlElementWrapper(name = "Tablas")
    @XmlElement(name = "Tabla", nillable = false)
    private List<Tabla> tablas = new ArrayList<>();

    @XmlElementWrapper(name = "Snippets")
    @XmlElement(name = "Snippet", nillable = false)
    private List<Snippet> snippets = new ArrayList<>();

    public void sort() {
        procedimientos.sort(SORTER);
        tablas.sort(SORTER);
        snippets.sort(SORTER);
    }

    public boolean save(Path path) {
        try {
            JAXBContext jc = JAXBContext.newInstance(Recursos.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, path.toFile());
            return true;
        } catch (JAXBException ex) {
            log.log(Level.SEVERE, "Error al guardar config", ex);
        }
        return false;
    }

    public static Recursos open(Path path) throws JAXBException, FileNotFoundException {
        File f = path.toFile();
        JAXBContext jc = JAXBContext.newInstance(Recursos.class);
        Unmarshaller u = jc.createUnmarshaller();
        Recursos r = (Recursos) u.unmarshal(new FileInputStream(f));
        return r;
    }

}
