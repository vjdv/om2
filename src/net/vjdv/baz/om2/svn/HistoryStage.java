package net.vjdv.baz.om2.svn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.vjdv.baz.om2.models.Winmerge;

/**
 * Muestra el histórico de cambios en un archivo
 *
 * @author B187926
 */
public class HistoryStage extends Stage {

    private final TableView<Resultado> tabla = new TableView<>();
    private final Clipboard clipboard = Clipboard.getSystemClipboard();

    public HistoryStage(SvnManager svn, String spfile) {
        MenuItem mi1 = new MenuItem("Usuario");
        MenuItem mi2 = new MenuItem("Fecha");
        MenuItem mi3 = new MenuItem("Mensaje");
        MenuItem mi4 = new MenuItem("Comparar con anterior");
        mi1.setOnAction((event) -> {
            toClipboard(tabla.getSelectionModel().getSelectedItem().usuario.get());
        });
        mi2.setOnAction((event) -> {
            toClipboard(tabla.getSelectionModel().getSelectedItem().fecha.get());
        });
        mi3.setOnAction((event) -> {
            toClipboard(tabla.getSelectionModel().getSelectedItem().comentario.get());
        });
        mi4.setOnAction((event) -> {
            try {
                Resultado r1 = tabla.getSelectionModel().getSelectedItem();
                Resultado r2 = tabla.getItems().get(tabla.getSelectionModel().getSelectedIndex() + 1);
                File tmp1 = File.createTempFile(spfile.replaceAll("\\.sql", "_"), ".sql");
                File tmp2 = File.createTempFile(spfile.replaceAll("\\.sql", "_"), ".sql");
                tmp1.deleteOnExit();
                tmp2.deleteOnExit();
                svn.export(spfile, tmp1, r1.version.get());
                svn.export(spfile, tmp2, r2.version.get());
                Winmerge.compare(tmp2.getAbsolutePath(), r2.version.get(), tmp1.getAbsolutePath(), r1.version.get() + " " + r1.comentario.get());
            } catch (IOException ex) {
                Logger.getLogger(HistoryStage.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        Menu menu = new Menu("Copiar");
        menu.getItems().addAll(mi1, mi2, mi3);
        ContextMenu menuroot = new ContextMenu();
        menuroot.getItems().addAll(menu, mi4);
        tabla.setContextMenu(menuroot);
        TableColumn<Resultado, String> colVersion = new TableColumn<>("Versión");
        TableColumn<Resultado, String> colUsuario = new TableColumn<>("Usuario");
        TableColumn<Resultado, String> colFecha = new TableColumn<>("Fecha");
        TableColumn<Resultado, String> colMsg = new TableColumn<>("Mensaje");
        colVersion.setCellValueFactory(cellData -> cellData.getValue().version);
        colUsuario.setCellValueFactory(cellData -> cellData.getValue().usuario);
        colFecha.setCellValueFactory(cellData -> cellData.getValue().fecha);
        colMsg.setCellValueFactory(cellData -> cellData.getValue().comentario);
        colVersion.setSortable(false);
        colUsuario.setSortable(false);
        colFecha.setSortable(false);
        colMsg.setSortable(false);
        colVersion.setMinWidth(60d);
        colVersion.setMaxWidth(70d);
        colUsuario.setMinWidth(100d);
        colUsuario.setMaxWidth(120d);
        colFecha.setMinWidth(120d);
        colFecha.setMaxWidth(120d);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.getColumns().add(colVersion);
        tabla.getColumns().add(colUsuario);
        tabla.getColumns().add(colFecha);
        tabla.getColumns().add(colMsg);
        tabla.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            mi4.setDisable(newValue.intValue() + 1 >= tabla.getItems().size());
        });
        AnchorPane root = new AnchorPane();
        root.setPrefSize(500, 300);
        root.getChildren().addAll(tabla);
        AnchorPane.setTopAnchor(tabla, 0d);
        AnchorPane.setRightAnchor(tabla, 0d);
        AnchorPane.setBottomAnchor(tabla, 0d);
        AnchorPane.setLeftAnchor(tabla, 0d);
        Scene scene = new Scene(root);
        setScene(scene);
        setTitle("Histórico de cambios (buscando)");
    }

    private void toClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    class InputStreamReader implements Runnable {

        private final InputStream stream;

        public InputStreamReader(InputStream is) {
            stream = is;
        }

        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            Scanner s = new Scanner(stream, "ISO-8859-1");
            while (s.hasNextLine()) {
                String line = s.nextLine();
                if (!line.trim().isEmpty()) {
                    sb.append(line).append("\n");
                }
            }
            String[] cambios = sb.toString().split("------------------------------------------------------------------------");
            Platform.runLater(() -> {
                for (String cambio : cambios) {
                    if (cambio.trim().isEmpty()) {
                        continue;
                    }
                    System.out.println("----");
                    System.out.println(cambio.trim());
                    System.out.println("----");
                    String partes[] = cambio.trim().split("\n", 2);
                    String[] datos = partes[0].split("\\|");
                    Resultado r = new Resultado();
                    r.comentario.set(partes.length > 1 ? partes[1] : "");
                    r.version.set(datos[0].trim());
                    r.usuario.set(datos[1].trim());
                    r.fecha.set(datos[2].trim().substring(0, 19));
                    tabla.getItems().add(r);
                }
                setTitle("Histórico de cambios");
            });
        }

    }

    public static class Resultado {

        public SimpleStringProperty version = new SimpleStringProperty();
        public SimpleStringProperty usuario = new SimpleStringProperty();
        public SimpleStringProperty fecha = new SimpleStringProperty();
        public SimpleStringProperty comentario = new SimpleStringProperty();

    }

}
