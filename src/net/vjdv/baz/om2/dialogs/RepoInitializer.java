package net.vjdv.baz.om2.dialogs;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.File;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import lombok.Data;
import net.vjdv.baz.om2.dialogs.RepoInitializer.Datos;
import org.controlsfx.control.textfield.CustomTextField;

/**
 *
 * @author B187926
 */
public class RepoInitializer extends Dialog<Datos> {

    public RepoInitializer(File initialFolder) {
        super();
        setTitle("Selecci\u00f3n de repositorio");
        //grid
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.getColumnConstraints().addAll(new ColumnConstraints(150), col2);
        //contenido
        CustomTextField carpeta = new CustomTextField();
        Hyperlink choose = new Hyperlink("");
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.SEARCH);
        choose.setGraphic(icon);
        choose.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(initialFolder);
            File selectedDirectory = directoryChooser.showDialog(getOwner());
            carpeta.setText(selectedDirectory == null ? "" : selectedDirectory.toString());
        });
        carpeta.setPromptText("/");
        carpeta.setRight(choose);
        TextField url = new TextField();
        url.setPromptText("http://");
        grid.add(new Label("Carpeta del repositorio:"), 0, 0);
        grid.add(carpeta, 1, 0);
        grid.add(new Label("URL del repositorio:"), 0, 1);
        grid.add(url, 1, 1);
        getDialogPane().setContent(grid);
        //botones
        ButtonType desdecero = new ButtonType("Crear desde cero");
        ButtonType clonar = new ButtonType("Clonar de origen");
        ButtonType elegir = new ButtonType("Seleccionar existente");
        ButtonType cancelar = new ButtonType("Cancelar");
        getDialogPane().getButtonTypes().addAll(desdecero, clonar, elegir, cancelar);
        //tooltips
        Button desdeceroBtn = (Button) getDialogPane().lookupButton(desdecero);
        desdeceroBtn.setTooltip(new Tooltip("Inicializa un repositorio nuevo en la carpeta elegida y le agrega la url como remoto, también agrega los archivos básicos para un proyecto vacío."));
        Button clonarBtn = (Button) getDialogPane().lookupButton(clonar);
        clonarBtn.setTooltip(new Tooltip("Descarga los archivos de el repositorio de la url en la carpeta elegida."));
        Button elegirBtn = (Button) getDialogPane().lookupButton(elegir);
        elegirBtn.setTooltip(new Tooltip("Selecciona la carpeta elegida como el repositorio del proyecto ya existente."));
        Button cancelarBtn = (Button) getDialogPane().lookupButton(cancelar);
        //disabling
        desdeceroBtn.setDisable(true);
        clonarBtn.setDisable(true);
        elegirBtn.setDisable(true);
        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            desdeceroBtn.setDisable(carpeta.getText().isEmpty());
            clonarBtn.setDisable(carpeta.getText().isEmpty() || url.getText().isEmpty());
            elegirBtn.setDisable(carpeta.getText().isEmpty() || !url.getText().isEmpty());
        };
        carpeta.textProperty().addListener(listener);
        url.textProperty().addListener(listener);
        //result
        setResultConverter(button -> {
            Datos d = new Datos();
            d.paso = button == desdecero ? 1 : button == clonar ? 2 : button == elegir ? 3 : 0;
            d.carpeta = carpeta.getText();
            d.url = url.getText();
            return d;
        });
        Platform.runLater(cancelarBtn::requestFocus);
    }

    @Data
    public static class Datos {

        private int paso;
        private String carpeta;
        private String url;
    }

}
