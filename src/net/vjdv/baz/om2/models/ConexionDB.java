package net.vjdv.baz.om2.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
}
