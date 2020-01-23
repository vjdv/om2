package net.vjdv.baz.om2.dialogs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import net.vjdv.baz.om2.models.Config;

/**
 * Formulario para cambiar archivo de configuración
 *
 * @author B187926
 */
public class ConfigForm extends Dialog<Config> {

    public ConfigForm(Config c) {
        super();
        setTitle("Cambiar configuraci\u00f3n");
        //grid
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.getColumnConstraints().addAll(new ColumnConstraints(150), col2);
        //contenido
        TextField carpeta = new TextField("dbo");
        TextField proyecto = new TextField();
        TextField desarrollador = new TextField();
        TextField editor = new TextField();
        TextField winmerge = new TextField();
        carpeta.setPromptText("dbo");
        grid.add(new Label("Carpeta del proyecto:"), 0, 0);
        grid.add(carpeta, 1, 0);
        grid.add(new Label("Proyecto:"), 0, 1);
        grid.add(proyecto, 1, 1);
        grid.add(new Label("Desarrollador:"), 0, 2);
        grid.add(desarrollador, 1, 2);
        grid.add(new Label("Editor:"), 0, 3);
        grid.add(editor, 1, 3);
        grid.add(new Label("Winmerge:"), 0, 4);
        grid.add(winmerge, 1, 4);
        getDialogPane().setContent(grid);
        //tooltips
        proyecto.setTooltip(new Tooltip("Proyecto con el que se generan los nuevos procedimientos"));
        desarrollador.setTooltip(new Tooltip("Alias con el que se firman los nuevos procedimientos"));
        editor.setTooltip(new Tooltip("Ruta del ejecutable preferido para abrir los archivos .sql"));
        winmerge.setTooltip(new Tooltip("Ruta del ejecutable de WinMerge para habilitar la comparaci\u00f3n de archivos"));
        //editar
        carpeta.setText(c.getRepositorio());
        proyecto.setText(c.getProyecto());
        desarrollador.setText(c.getDesarrollador());
        editor.setText(c.getEditor());
        winmerge.setText(c.getWinmerge());
        carpeta.setDisable(true);
        //botones
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        //result
        setResultConverter(button -> {
            if (button == null || button == ButtonType.CANCEL) {
                return null;
            }
            c.setProyecto(proyecto.getText());
            c.setDesarrollador(desarrollador.getText());
            c.setEditor(editor.getText());
            c.setWinmerge(winmerge.getText());
            return c;
        });
        Platform.runLater(proyecto::requestFocus );
    }

}
