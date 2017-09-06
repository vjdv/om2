package net.vjdv.baz.om2.models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import static java.util.logging.Logger.getLogger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Guarda configuración básica de la aplicación
 *
 * @author B187926
 */
@XmlRootElement(name = "Config")
public class Config {

    private static final File FILE = new File("config.xml");
    private final List<String> recientes = new ArrayList<>();

    @XmlElement
    public boolean abrir_ultimo_al_iniciar = false;

    @XmlTransient
    public String getUltimo() {
        return recientes.get(0);
    }

    @XmlElementWrapper(name = "recientes")
    @XmlElement(name = "archivo")
    public List<String> getRecientes() {
        return recientes;
    }

    public void addReciente(String reciente) {
        if (recientes.contains(reciente)) {
            recientes.remove(reciente);
        }
        recientes.add(0, reciente);
    }

    public void save() {
        try {
            JAXBContext jc = JAXBContext.newInstance(Config.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, FILE);
        } catch (Exception ex) {
            getLogger("Config").log(Level.SEVERE, "Error al guardar configuración", ex);
        }
    }

    public static Config load() {
        try {
            JAXBContext jc = JAXBContext.newInstance(Config.class);
            Unmarshaller u = jc.createUnmarshaller();
            Config c = (Config) u.unmarshal(FILE);
            return c;
        } catch (JAXBException ex) {
            getLogger("Config").log(Level.SEVERE, "Error al cargar configuración", ex);
        }
        return new Config();
    }

}
