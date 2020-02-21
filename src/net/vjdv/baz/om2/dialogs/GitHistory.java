package net.vjdv.baz.om2.dialogs;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import lombok.Data;
import lombok.Setter;
import net.vjdv.baz.om2.models.Git;
import net.vjdv.baz.om2.models.Winmerge;

/**
 *
 * @author B187926
 */
public class GitHistory extends Stage {

    private final Label info = new Label("");
    private final ObservableList<Resultado> list = FXCollections.observableArrayList(new ArrayList<>());
    private int selectedIndex;
    @Setter
    private Winmerge winmerge;

    public GitHistory(Path p, Git git) {
        String file = git.getPath().relativize(p).toString().replaceAll("\\\\", "/");
        TableView<Resultado> tabla = new TableView<>(list);
        TableColumn<Resultado, String> colAutor = new TableColumn<>("Autor");
        TableColumn<Resultado, String> colFecha = new TableColumn<>("Fecha");
        TableColumn<Resultado, String> colComentario = new TableColumn<>("Comentario");
        colAutor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAutor()));
        colFecha.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFecha()));
        colComentario.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getComentario()));
        colFecha.setPrefWidth(130);
        colComentario.setPrefWidth(150);
        tabla.getColumns().add(colAutor);
        tabla.getColumns().add(colFecha);
        tabla.getColumns().add(colComentario);
        tabla.setPrefWidth(2000);
        tabla.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        info.setTextAlignment(TextAlignment.CENTER);
        info.setPrefWidth(2000);
        Button btn1 = new Button("Comparar con actual");
        Button btn2 = new Button("Comparar con anterior");
        Button btn3 = new Button("Abrir como archivo");
        Button btn4 = new Button("Copiar hash");
        btn1.setMinWidth(150);
        btn2.setMinWidth(150);
        btn3.setMinWidth(150);
        btn4.setMinWidth(150);
        btn1.setMaxWidth(150);
        btn2.setMaxWidth(150);
        btn3.setMaxWidth(150);
        btn4.setMaxWidth(150);
        btn1.setDisable(true);
        btn2.setDisable(true);
        btn3.setDisable(true);
        btn4.setDisable(true);
        tabla.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            int i = newValue.intValue();
            boolean disabled = i == -1;
            btn1.setDisable(disabled);
            btn2.setDisable(i + 1 == list.size() || disabled);
            btn3.setDisable(disabled);
            btn4.setDisable(disabled);
            selectedIndex = i;
        });
        btn1.setOnAction(evt -> {
            try {
                Resultado rx = list.get(selectedIndex);
                File f = git.export(rx.getHash(), file);
                winmerge.compare(f.getAbsolutePath(), rx.getHash(), p.toString(), "archivo actual");
            } catch (IOException ex) {
                info.setText(ex.getMessage());
            }
        });
        btn2.setOnAction(evt -> {
            try {
                Resultado r1 = list.get(selectedIndex);
                Resultado r2 = list.get(selectedIndex + 1);
                File f1 = git.export(r1.getHash(), file);
                File f2 = git.export(r2.getHash(), file);
                winmerge.compare(f1.getAbsolutePath(), r1.getHash(), f2.getAbsolutePath(), r2.getHash());
            } catch (IOException ex) {
                info.setText(ex.getMessage());
            }
        });
        btn3.setOnAction(evt -> {
            try {
                File f = git.export(list.get(selectedIndex).getHash(), file);
                Desktop desktop = Desktop.getDesktop();
                desktop.open(f);
            } catch (IOException ex) {
                info.setText(ex.getMessage());
            }
        });
        btn4.setOnAction(evt -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(list.get(selectedIndex).getHash());
            Clipboard.getSystemClipboard().setContent(content);
        });
        GridPane root = new GridPane();
        root.add(tabla, 0, 0, 1, 5);
        root.add(btn1, 1, 0);
        root.add(btn2, 1, 1);
        root.add(btn3, 1, 2);
        root.add(btn4, 1, 3);
        root.add(new Label(), 1, 4);
        root.add(info, 0, 5, 2, 1);
        root.setHgap(5);
        root.setVgap(5);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 500, 180);
        setTitle("Historial de " + file);
        setScene(scene);
        setResizable(false);
    }

    public void setResult(String result) {
        if (result.isEmpty() || result.contains("unknown")) {
            info.setText("No hay historial del archivo en git");
        } else {
            String[] results = result.split("\n");
            for (String line : results) {
                String[] parts = line.split("\\|");
                Resultado r = new Resultado();
                r.setHash(parts[0]);
                r.setAutor(parts[1]);
                r.setFecha(parts[2] + " (" + parts[3] + ")");
                r.setComentario(parts[4]);
                list.add(r);
            }
        }
    }

    @Data
    private class Resultado {

        private String hash;
        private String autor;
        private String fecha;
        private String comentario;
    }

}
