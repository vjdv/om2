package net.vjdv.baz.om2.models;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Data;

/**
 *
 * @author B187926
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ConexionDB {

    @XmlAttribute
    private String server;
    @XmlAttribute
    private String db;
    @XmlAttribute
    private String user;
    @XmlAttribute
    private String password;
    @XmlAttribute
    private int port;
    @XmlTransient
    private SQLServerDataSource ds = null;

    @XmlTransient
    public DataSource getDataSource() {
        if (ds == null) {
            ds = new SQLServerDataSource();
            ds.setServerName(server);
            ds.setPortNumber(port);
            ds.setDatabaseName(db);
            ds.setUser(user);
            ds.setPassword(password);
        }
        return ds;
    }

    @Override
    public String toString() {
        return server + ":" + port + "/" + db;
    }

}
