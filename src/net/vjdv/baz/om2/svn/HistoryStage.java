package net.vjdv.baz.om2.svn;

import java.io.InputStream;
import java.util.Scanner;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author B187926
 */
public class HistoryStage extends Stage {

    private final TableView<Resultado> tabla = new TableView<>();

    public HistoryStage() {
        TableColumn<Resultado, String> colVersion = new TableColumn<>("Versión");
        TableColumn<Resultado, String> colUsuario = new TableColumn<>("Usuario");
        TableColumn<Resultado, String> colFecha = new TableColumn<>("Fecha");
        TableColumn<Resultado, String> colMsg = new TableColumn<>("Mensaje");
        colVersion.setCellValueFactory(cellData -> cellData.getValue().version);
        colUsuario.setCellValueFactory(cellData -> cellData.getValue().usuario);
        colFecha.setCellValueFactory(cellData -> cellData.getValue().fecha);
        colMsg.setCellValueFactory(cellData -> cellData.getValue().comentario);
        colVersion.setMinWidth(60d);
        colVersion.setMaxWidth(70d);
        colUsuario.setMinWidth(100d);
        colUsuario.setMaxWidth(120d);
        colFecha.setMinWidth(120d);
        colFecha.setMaxWidth(120d);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.getColumns().addAll(colVersion, colUsuario, colFecha, colMsg);
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

    class InputStreamReader implements Runnable {

        private final InputStream stream;

        public InputStreamReader(InputStream is) {
            stream = is;
        }

        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            Scanner s = new Scanner(stream);
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
                    r.comentario.set(partes[1]);
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
