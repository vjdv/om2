package net.vjdv.baz.om2.models;

import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author B187926
 */
public class ConexionDB {
    @XmlAttribute
    public String nombre, gestor, servidor, basededatos, usuario, password;
    @XmlAttribute
    public int puerto;
}