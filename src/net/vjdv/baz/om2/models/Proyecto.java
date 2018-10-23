package net.vjdv.baz.om2.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

/**
 *
 * @author B187926
 */
@XmlRootElement(name="Project")
public class Proyecto  {

	@XmlElement
	public String title;
	@XmlElement
	public String repo_path;
	@XmlElement
	public String winmerge;
	@XmlElement
	public String cc_path;
	@XmlElement
	public String cctool;
	@XmlElement(name = "Database",nillable=false)
	public ConexionDB db;
	@XmlTransient
	public List<Procedimiento> procedimientos = new ArrayList<>();
	@XmlTransient
	public List<Tabla> tablas = new ArrayList<>();

	/**
	 * Abre un archivo local
	 *
	 * @param file Ruta del archivo
	 * @return Objeto de proyecto
	 */
	public static Proyecto abrir(File file) throws JAXBException, FileNotFoundException {
		JAXBContext jc = JAXBContext.newInstance(Proyecto.class);
		Unmarshaller u = jc.createUnmarshaller();
		Proyecto p = (Proyecto) u.unmarshal(new FileInputStream(file));
		return p;
	}
	
	public DataSource getDataSource() {
		SQLServerDataSource ssds = new SQLServerDataSource();
        ssds.setServerName(db.server);
        ssds.setPortNumber(db.port);
        ssds.setDatabaseName(db.db);
        ssds.setUser(db.user);
        ssds.setPassword(db.password);
        return ssds;
	}

}
