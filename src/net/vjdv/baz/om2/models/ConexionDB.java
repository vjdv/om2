package net.vjdv.baz.om2.models;

import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author B187926
 */
public class ConexionDB {
    @XmlAttribute
    public String server, db, user, password;
    @XmlAttribute
    public int port;
}