package net.vjdv.baz.om2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.vjdv.baz.om2.models.Dialogos;

/**
 * Ejecuta los comandos SVN y muestra los resultados en pantalla
 *
 * @author B187926
 */
public class SvnDialog extends Stage {

    private final String svnobj, objsdir;
    private final TextArea text = new TextArea();
    private final BooleanProperty working = new SimpleBooleanProperty(false);
    private String args[];

    public SvnDialog(InicioController inicio, String svnobj, String objsdir) {
        System.out.println(svnobj);
        System.out.println(objsdir);
        this.svnobj = svnobj;
        this.objsdir = objsdir;
        Button cerrar = new Button("Cerrar");
        cerrar.disableProperty().bind(working);
        cerrar.setOnAction((event) -> {
            hide();
        });
        Button limpiar = new Button("Limpiar");
        limpiar.disableProperty().bind(working);
        limpiar.setOnAction((event) -> {
            text.setText("");
        });
        Button marcar = new Button("Marcar cambios");
        marcar.disableProperty().bind(working);
        marcar.setOnAction((event) -> {
            String cadena = text.getText();
            String cadena2 = cadena.substring(Math.max(cadena.lastIndexOf("-----\n"), 0));
            inicio.filtrarProcedimientos((sp) -> {
                return cadena2.contains(sp.getNombre());
            });
        });
        HBox buttons = new HBox();
        buttons.setPadding(new Insets(5));
        buttons.setAlignment(Pos.TOP_RIGHT);
        buttons.setSpacing(10);
        buttons.getChildren().addAll(limpiar, marcar, cerrar);
        AnchorPane root = new AnchorPane();
        root.setPrefSize(600, 350);
        root.getChildren().addAll(text, buttons);
        AnchorPane.setTopAnchor(text, 0d);
        AnchorPane.setRightAnchor(text, 0d);
        AnchorPane.setBottomAnchor(text, 30d);
        AnchorPane.setLeftAnchor(text, 0d);
        AnchorPane.setRightAnchor(buttons, 5d);
        AnchorPane.setBottomAnchor(buttons, 0d);
        AnchorPane.setLeftAnchor(buttons, 5d);
        Scene scene = new Scene(root);
        setScene(scene);
        setTitle("Subversion");
    }

    public void update() {
        args = new String[]{svnobj, "update"};
        text.appendText("-----\nsvn update " + objsdir + "\n");
        run();
    }

    public void update(String... files) {
        args = new String[files.length + 2];
        args[0] = svnobj;
        args[1] = "update";
        System.arraycopy(files, 0, args, 2, files.length);
        text.appendText("-----\nsvn update " + files.length + " file(s)\n");
        run();
    }

    public void commit() {
        text.appendText("-----\nsvn commit " + objsdir + "\n");
        try {
            String msg = Dialogos.input("Mensaje:", "commit all");
            args = new String[]{svnobj, "commit", "--message", "\"" + msg + "\""};
            run();
        } catch (Dialogos.InputCancelled ex) {
            text.appendText("commit cancelled\n");
        }
    }

    public void commit(String... files) {
        text.appendText("-----\nsvn commit " + files.length + " file(s)\n");
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
            text.appendText("commit cancelled\n");
        }
    }

    public void changedFiles() {
        args = new String[]{svnobj, "status"};
        text.appendText("-----\nsvn status\n");
        run();
    }

    public void export(String obj, File file) throws IOException {
        text.appendText("-----\ncomparar " + obj + " con base\n");
        ProcessBuilder pb = new ProcessBuilder(new String[]{svnobj, "export", obj, "\"" + file.getAbsolutePath() + "\"", "--force", "--revision", "HEAD"});
        pb.directory(new File(objsdir));
        Process p = pb.start();
        new InputStreamReader(p.getInputStream()).run();
        new InputStreamReader(p.getErrorStream()).run();
    }

    private void run() {
        try {
            working.set(true);
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.directory(new File(objsdir));
            Process p = pb.start();
            new Thread(new InputStreamReader(p.getInputStream())).start();
            new Thread(new InputStreamReader(p.getErrorStream())).start();
        } catch (IOException ex) {
            Logger.getLogger(SvnDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class InputStreamReader implements Runnable {

        private final InputStream stream;

        public InputStreamReader(InputStream is) {
            stream = is;
        }

        @Override
        public void run() {
            Platform.runLater(() -> {
                Scanner s = new Scanner(stream);
                while (s.hasNextLine()) {
                    text.appendText(s.nextLine() + "\n");
                }
                working.set(false);
            });
        }

    }

}
