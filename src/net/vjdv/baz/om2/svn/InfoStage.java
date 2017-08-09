package net.vjdv.baz.om2.svn;

import net.vjdv.baz.om2.*;
import java.io.InputStream;
import java.util.Scanner;
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

/**
 * Ejecuta los comandos SVN y muestra los resultados en pantalla
 *
 * @author B187926
 */
public class InfoStage extends Stage {

    private final TextArea text = new TextArea();
    private final BooleanProperty working = new SimpleBooleanProperty(false);
    private String args[];

    public InfoStage(InicioController inicio) {
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

    public void setWorking(boolean b) {
        working.set(b);
    }

    public void appendText(String text) {
        this.text.appendText(text);
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
