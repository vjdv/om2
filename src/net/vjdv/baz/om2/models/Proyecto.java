package net.vjdv.baz.om2.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public String idproyecto;
    @XmlAttribute
    public String directorio_objetos;
    @XmlAttribute
    public String winmerge;
    @XmlAttribute
    public String proxyHost;
    @XmlAttribute
    public String proxyPort;
    @XmlAttribute
    public String svn;
    @XmlAttribute
    public String ccvob;
    @XmlAttribute
    public String cctool;
    @XmlElementWrapper(name = "Conexiones")
    @XmlElement(name = "Conexion")
    public List<ConexionDB> conexiones = new ArrayList<>();
    @XmlElementWrapper(name = "Procedimientos")
    @XmlElement(name = "SP")
    public List<Procedimiento> procedimientos = new ArrayList<>();
    @XmlElementWrapper(name = "Tablas")
    @XmlElement(name = "TB")
    public List<Tabla> tablas = new ArrayList<>();

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
            if (p.proxyHost != null && p.proxyPort != null) {
                Properties properties = System.getProperties();
                properties.setProperty("http.proxyHost", p.proxyHost);
                properties.setProperty("http.proxyPort", p.proxyPort);
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
        URL vurl = new URL(url + "/" + idproyecto);
        System.out.println(url + "/" + idproyecto);
        JAXBContext jc = JAXBContext.newInstance(Proyecto.class);
        Unmarshaller u = jc.createUnmarshaller();
        Proyecto p = (Proyecto) u.unmarshal(vurl);
        titulo = p.titulo;
        conexiones.addAll(p.conexiones);
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
        try {
            URL vurl = new URL(url + "/" + idproyecto + "/uptObj");
            PeticionHTTP http = new PeticionHTTP(vurl);
            http.addParam("schema", r.getSchema());
            http.addParam("nombre", r.getNombre());
            http.addParam("descripcion", r.getDescripcion());
            if (r instanceof Tabla) {
                http.addParam("mapeo", "");
            } else if (r instanceof Procedimiento) {
                http.addParam("mapeo", ((Procedimiento) r).getMap());
            }
            http.enviarConsulta();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Proyecto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onAddedRecurso(Recurso r) {
        if (url == null) {
            return;
        }
        try {
            URL vurl = new URL(url + "/" + idproyecto + "/addObj");
            PeticionHTTP http = new PeticionHTTP(vurl);
            http.addParam("schema", r.getSchema());
            http.addParam("nombre", r.getNombre());
            http.addParam("descripcion", r.getDescripcion());
            http.addParam("tipo", r.getTipo());
            if (r instanceof Tabla) {
                http.addParam("mapeo", "");
            } else if (r instanceof Procedimiento) {
                http.addParam("mapeo", ((Procedimiento) r).getMap());
            }
            http.enviarConsulta();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Proyecto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
