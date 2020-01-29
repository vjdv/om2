package net.vjdv.baz.om2.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
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
import javax.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.extern.java.Log;

/**
 * Configuración individual de cada usuario
 *
 * @author B187926
 */
@XmlRootElement(name = "Config")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@Log
public class Config {

    @XmlTransient
    private File file;

    @XmlElement
    private String proyecto = "";

    @XmlElement
    private String desarrollador = "";

    @XmlElement
    private String repositorio = "";

    @XmlElement
    private String editor = "";

    @XmlElement
    private String winmerge = "";

    @XmlElement
    private String clearcase = "";

    @XmlElement
    private String vistacc = "";

    @XmlElementWrapper(name = "Databases")
    @XmlElement(name = "Database", nillable = false)
    private List<ConexionDB> conns = new ArrayList<>();

    @XmlElementWrapper(name = "PorSubir")
    @XmlElement(name = "string", nillable = false)
    private List<String> porSubir = new ArrayList<>();

    @XmlElementWrapper(name = "PorCorregir")
    @XmlElement(name = "string", nillable = false)
    private List<String> porCorregir = new ArrayList<>();

    /**
     * Abre el archivo local
     *
     * @param initialPath Ruta inicial
     * @return Objeto Config
     * @throws javax.xml.bind.JAXBException Archivo no válido
     * @throws java.io.FileNotFoundException Archivo no existente
     */
    public static Config open(Path initialPath) throws JAXBException, FileNotFoundException {
        File f = initialPath.resolve("config.xml").toFile();
        JAXBContext jc = JAXBContext.newInstance(Config.class);
        Unmarshaller u = jc.createUnmarshaller();
        Config c = (Config) u.unmarshal(new FileInputStream(f));
        c.setFile(f);
        return c;
    }

    /**
     * Guarda la configuración actual a disco
     *
     * @return success if saved
     */
    public boolean save() {
        try {
            JAXBContext jc = JAXBContext.newInstance(Config.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, file);
            return true;
        } catch (JAXBException ex) {
            log.log(Level.SEVERE, "Error al guardar config", ex);
        }
        return false;
    }

}
