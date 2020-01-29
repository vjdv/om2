package net.vjdv.baz.om2.models;

import java.io.IOException;

/**
 *
 * @author B187926
 */
public class Winmerge {

    public final String bin;

    public Winmerge(String bin) {
        this.bin = bin;
    }

    public void compare(String file1, String file2) throws IOException {
        compare(file1, "versi\u00f3n uno", file2, "versi\u00f3n dos");
    }

    public void compare(String file1, String cmt1, String file2, String cmt2) throws IOException {
        String[] args = {bin, "/e", "/x", "/s", "/u", "/wr", "/dl", "\"" + cmt1 + "\"", "/dr", "\"" + cmt2 + "\"", "\"" + file1 + "\"", "\"" + file2 + "\""};
        System.out.println("Calling WinMerge");
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.start();
    }

}
