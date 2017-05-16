package net.vjdv.baz.om2.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import static java.util.logging.Logger.getLogger;

/**
 *
 * @author B187926
 */
@XmlRootElement
public class Proyecto implements UpdatedRecursoListener {

    @XmlTransient
    public File file;
    @XmlAttribute
    public String titulo;
    @XmlAttribute
    public String url;
    @XmlAttribute
    public String directorio_objetos;
    @XmlAttribute
    public String winmerge;
    @XmlElementWrapper(name = "Conexiones")
    @XmlElement(name = "Conexion")
    public List<ConexionDB> conexiones = new ArrayList<>();
    @XmlElementWrapper(name = "Procedimientos")
    @XmlElement(name = "SP")
    public List<Procedimiento> procedimientos = new ArrayList<>();
    @XmlElementWrapper(name = "Tablas")
    @XmlElement(name = "TB")
    public List<Tabla> tablas = new ArrayList<>();
    @XmlElementWrapper(name = "Otros")
    @XmlElement(name = "Recurso")
    public List<Recurso> otros = new ArrayList<>();

    /**
     * Abre un archivo local
     *
     * @param file Ruta del archivo
     * @return Objeto de proyecto
     */
    public static Proyecto abrir(File file) {
        try {
            JAXBContext jc = JAXBContext.newInstance(Proyecto.class);
            Unmarshaller u = jc.createUnmarshaller();
            Proyecto p = (Proyecto) u.unmarshal(new FileInputStream(file));
            p.file = file;
            if (p.directorio_objetos == null) {
                p.directorio_objetos = file.getParent() + "\\db_objects";
                File parent = new File(p.directorio_objetos);
                parent.mkdir();
            }
            if (p.url != null) {
                p.abrirURL();
            }
            return p;
        } catch (JAXBException | FileNotFoundException | MalformedURLException ex) {
            getLogger("BAZOM").log(Level.SEVERE, "Error al abrir proyecto", ex);
        }
        return null;
    }

    /**
     * Abre un proyecto disponible en una URL
     *
     * @throws JAXBException
     * @throws MalformedURLException
     */
    private void abrirURL() throws JAXBException, MalformedURLException {
        URL vurl = new URL(url);
        JAXBContext jc = JAXBContext.newInstance(Proyecto.class);
        Unmarshaller u = jc.createUnmarshaller();
        Proyecto p = (Proyecto) u.unmarshal(vurl);
        conexiones = p.conexiones;
        tablas = p.tablas;
        procedimientos = p.procedimientos;
        for (Procedimiento sp : procedimientos) {
            sp.addOnUpdatedListener(this);
        }
        for (Tabla tb : tablas) {
            tb.addOnUpdatedListener(this);
        }
    }

    /**
     * Guarda los cambios efectuados en un proyecto
     *
     * @return bandera de éxito
     */
    public boolean guardar() {
        try {
            JAXBContext jc = JAXBContext.newInstance(Proyecto.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, file);
            return true;
        } catch (Exception ex) {
            getLogger("BAZOM").log(Level.SEVERE, "Error al guardar proyecto", ex);
        }
        return false;
    }

    /**
     * Elimina procedimientos del proyecto
     *
     * @param sps Procedimientos a quitar
     */
    public void quitarProcedimiento(Procedimiento... sps) {
        for (Procedimiento sp : sps) {
            procedimientos.remove(sp);
        }
    }

    /**
     * Elimina tablas del proyecto
     *
     * @param tbs Tablas a quitar
     */
    public void quitarTabla(Tabla... tbs) {
        for (Tabla tb : tbs) {
            tablas.remove(tb);
        }
    }

    @Override
    public void onUpdatedRecurso(Recurso r) {
        if (url == null) {
            return;
        }
    }

}
