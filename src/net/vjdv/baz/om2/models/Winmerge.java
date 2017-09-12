package net.vjdv.baz.om2.models;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author B187926
 */
public class Winmerge {

    public static String bin = null;

    public static void compare(String file1, String file2) throws IOException {
        if (!valid()) {
            return;
        }
        compare(file1, "versión uno", file2, "versión dos");
    }

    public static void compare(String file1, String cmt1, String file2, String cmt2) throws IOException {
        if (!valid()) {
            return;
        }
        String[] args = {bin, "/e", "/x", "/s", "/u", "/wr", "/dl", "\"" + cmt1 + "\"", "/dr", "\"" + cmt2 + "\"", "\"" + file1 + "\"", "\"" + file2 + "\""};
        System.out.println("Calling WinMerge");
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.start();
    }

    private static boolean valid() {
        if (bin == null) {
            Dialogos.message("Winmerge no está configurado");
            return false;
        } else if (!new File(bin).exists()) {
            Dialogos.message("No se encontró el binario de Winmerge");
            return false;
        }
        return true;
    }

}
