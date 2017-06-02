package net.vjdv.baz.om2.models;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author B187926
 */
public class Token {

    private final String semilla;
    private long timestamp;
    private String token = null;

    /**
     * Representa al token y sus objetos
     *
     * @param semilla Cadena base sobre la que se generará el token
     */
    public Token(String semilla) {
        this.semilla = semilla;
        resetTimestamp();
    }

    /**
     * Genera una nueva estampa de tiempo para generar este token.
     */
    public final void resetTimestamp() {
        Instant ts = Instant.now().atZone(ZoneId.of("GMT-5")).toInstant();
        timestamp = ts.toEpochMilli();
        token = null;
    }

    /**
     * Devuelve la estampa de tiempo en la que se genera este token
     *
     * @return Número de segundos desde 1970 blah blah
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Genera una cadena token basándose en la semilla y estampa de tiempo en la
     * que fue creado el objeto.
     *
     * @return Token
     */
    public String get() {
        if (token != null) {
            return token;
        }
        String temp = semilla + timestamp;
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-256");
            crypt.reset();
            crypt.update(temp.getBytes("UTF-8"));
            return byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(Token.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    /**
     * Valida un token
     *
     * @param vtoken Token a validar
     * @param vtimestamp Estampa de tiempo con la que se generó el token a
     * validar
     * @return true si el token es válido
     */
    public boolean isValid(String vtoken, long vtimestamp) {
        if ((vtimestamp - timestamp) > 300000 || (vtimestamp - timestamp) < -300000) {
            return false;
        }
        String temp = semilla + vtimestamp;
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-256");
            crypt.reset();
            crypt.update(temp.getBytes("UTF-8"));
            temp = byteToHex(crypt.digest());
            return token.equals(temp);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(Token.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private static String byteToHex(final byte[] hash) {
        String result;
        try (Formatter formatter = new Formatter()) {
            for (byte b : hash) {
                formatter.format("%02x", b);
            }
            result = formatter.toString();
        }
        return result;
    }

}
