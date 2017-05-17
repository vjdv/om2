package net.vjdv.baz.om2.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author B187926
 */
public class Tabla extends Recurso {

    public Tabla() {
        setTipo(TABLA);
    }

    public Tabla(String n) {
        this();
        setNombre(n);
    }

    public void exportarDatosInsert(Connection conn, StringBuilder sb) {
        sb.append("INSERT INTO ").append(getNombre()).append("\r\n");
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + getNombre()); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            int columnas = md.getColumnCount();
            for (int i = 1; i <= columnas; i++) {
                sb.append(md.getColumnName(i));
                if (i < columnas) {
                    sb.append(",");
                }
            }
            sb.append(" VALUES\r\n");
            while (rs.next()) {
                sb.append("(");
                for (int i = 1; i <= columnas; i++) {
                    int type = md.getColumnType(i);
                    boolean comillas = (type == Types.CHAR || type == Types.VARCHAR || type == Types.DATE);
                    if (comillas) {
                        sb.append("'");
                        sb.append(rs.getString(i));
                        sb.append("'");
                    } else {
                        sb.append(rs.getString(i));
                    }
                    if (i < columnas) {
                        sb.append(",");
                    }
                }
                sb.append("),\r\n");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Tabla.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean exportarDatosInsert(Connection conn, File file) {
        StringBuilder sb = new StringBuilder();
        exportarDatosInsert(conn, sb);
        try (PrintWriter out = new PrintWriter(file)) {
            out.print(sb.toString());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Tabla.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public void exportarDatosPlano(Connection conn, StringBuilder sb) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + getNombre()); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            int columnas = md.getColumnCount();
            for (int i = 1; i <= columnas; i++) {
                sb.append(md.getColumnName(i));
                if (i < columnas) {
                    sb.append("\t");
                }
            }
            sb.append("\r\n");
            while (rs.next()) {
                for (int i = 1; i <= columnas; i++) {
                    sb.append(rs.getString(i));
                    if (i < columnas) {
                        sb.append(",");
                    }
                }
                sb.append("\r\n");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Tabla.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean exportarDatosPlano(Connection conn, File file) {
        StringBuilder sb = new StringBuilder();
        exportarDatosPlano(conn, sb);
        try (PrintWriter out = new PrintWriter(file)) {
            out.print(sb.toString());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Tabla.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public String toString() {
        return getNombre();
    }

}
