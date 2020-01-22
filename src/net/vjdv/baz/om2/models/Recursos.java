package net.vjdv.baz.om2.models;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.extern.java.Log;

/**
 *
 * @author B187926
 */
@Log
@XmlRootElement(name = "Recursos")
@XmlAccessorType(XmlAccessType.FIELD)
public class Recursos {

    @XmlElementWrapper(name = "Procedimientos")
    @XmlElement(name = "Procedimiento", nillable = false)
    private List<Procedimiento> procedimientos = new ArrayList<>();

    @XmlElementWrapper(name = "Tablas")
    @XmlElement(name = "Tabla", nillable = false)
    private List<Tabla> tablas = new ArrayList<>();

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

}
