package net.vjdv.baz.om2.svn;

import net.vjdv.baz.om2.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vjdv.baz.om2.models.Dialogos;

/**
 * Ejecuta los comandos SVN y muestra los resultados en pantalla
 *
 * @author B187926
 */
public class SvnManager {

    private final String svnobj, objsdir;
    private final InfoStage svninfo;
    private String args[];

    public SvnManager(InicioController inicio, String svnobj, String objsdir) {
        this.svnobj = svnobj;
        this.objsdir = objsdir;
        svninfo = new InfoStage(inicio);
    }

    public void add(String... files) {
        args = new String[files.length + 2];
        args[0] = svnobj;
        args[1] = "add";
        System.arraycopy(files, 0, args, 2, files.length);
        svninfo.appendText("-----\nsvn add " + files.length + " file(s)\n");
        run();
    }

    public void update() {
        args = new String[]{svnobj, "update"};
        svninfo.appendText("-----\nsvn update " + objsdir + "\n");
        run();
    }

    public void update(String... files) {
        args = new String[files.length + 2];
        args[0] = svnobj;
        args[1] = "update";
        System.arraycopy(files, 0, args, 2, files.length);
        svninfo.appendText("-----\nsvn update " + files.length + " file(s)\n");
        run();
    }

    public void commit() {
        svninfo.show();
        svninfo.appendText("-----\nsvn commit " + objsdir + "\n");
        try {
            String msg = Dialogos.input("Mensaje:", "commit all");
            args = new String[]{svnobj, "commit", "--message", "\"" + msg + "\""};
            run();
        } catch (Dialogos.InputCancelled ex) {
            svninfo.appendText("commit cancelled\n");
        }
    }

    public void commit(String... files) {
        svninfo.show();
        svninfo.appendText("-----\nsvn commit " + files.length + " file(s)\n");
        try {
            String msg = Dialogos.input("Mensaje:", "commit " + files.length + " file(s)");
            args = new String[files.length + 4];
            args[0] = svnobj;
            args[1] = "commit";
            System.arraycopy(files, 0, args, 2, files.length);
            args[files.length + 2] = "--message";
            args[files.length + 3] = "\"" + msg + "\"";
            run();
        } catch (Dialogos.InputCancelled ex) {
            svninfo.appendText("commit cancelled\n");
        }
    }

    public void log(String file) {
        try {
            HistoryStage history = new HistoryStage(this, file);
            history.show();
            ProcessBuilder pb = new ProcessBuilder(new String[]{svnobj, "log", file});
            pb.directory(new File(objsdir));
            Process p = pb.start();
            new Thread(history.new InputStreamReader(p.getInputStream())).start();
        } catch (IOException ex) {
            Logger.getLogger(SvnManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void changedFiles() {
        args = new String[]{svnobj, "status"};
        svninfo.appendText("-----\nsvn status\n");
        run();
    }

    public void export(String obj, File file) throws IOException {
        export(obj, file, "HEAD");
    }

    public void export(String obj, File file, String revision) throws IOException {
        svninfo.appendText("-----\ncomparar " + obj + " con " + revision + "\n");
        ProcessBuilder pb = new ProcessBuilder(new String[]{svnobj, "export", obj, "\"" + file.getAbsolutePath() + "\"", "--force", "--revision", revision});
        pb.directory(new File(objsdir));
        Process p = pb.start();
        svninfo.new InputStreamReader(p.getInputStream()).run();
        svninfo.new InputStreamReader(p.getErrorStream()).run();
        svninfo.show();
    }

    private void run() {
        try {
            svninfo.show();
            svninfo.setWorking(true);
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.directory(new File(objsdir));
            Process p = pb.start();
            new Thread(svninfo.new InputStreamReader(p.getInputStream())).start();
            new Thread(svninfo.new InputStreamReader(p.getErrorStream())).start();
        } catch (IOException ex) {
            Logger.getLogger(SvnManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
