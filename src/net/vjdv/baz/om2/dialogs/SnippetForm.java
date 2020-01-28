package net.vjdv.baz.om2.dialogs;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import net.vjdv.baz.om2.models.Recurso.Datos;
import net.vjdv.baz.om2.models.Recurso;
import net.vjdv.baz.om2.models.Snippet;

/**
 *
 * @author B187926
 */
public class SnippetForm extends Dialog<Datos> {

    public SnippetForm(Snippet x) {
        super();
        setTitle((x == null ? "Nuevo" : "Modificar") + " snippet");
        //grid
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.getColumnConstraints().addAll(new ColumnConstraints(150), col2);
        //contenido
        TextField nombre = new TextField();
        TextField descripcion = new TextField();
        grid.add(new Label("Nombre de archivo:"), 0, 0);
        grid.add(nombre, 1, 0);
        grid.add(new Label("Descripci\u00f3n:"), 0, 1);
        grid.add(descripcion, 1, 1);
        getDialogPane().setContent(grid);
        //editar
        if (x != null) {
            nombre.setText(x.getNombre());
            descripcion.setText(x.getDescripcion());
            nombre.setDisable(true);
        }
        //botones
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button guardarBtn = (Button) getDialogPane().lookupButton(ButtonType.OK);
        //disabling
        guardarBtn.setDisable(x == null);
        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            guardarBtn.setDisable(newValue.isEmpty() || newValue.matches("^[a-zA-Z0-9 _]"));
        };
        nombre.textProperty().addListener(listener);
        //result
        setResultConverter(button -> {
            if (button == null || button == ButtonType.CANCEL) {
                return null;
            }
            Datos d = new Datos();
            d.setTipo(Recurso.SNIPPET);
            d.setNombre(nombre.getText());
            d.setDescripcion(descripcion.getText());
            return d;
        });
        Platform.runLater(x == null ? nombre::requestFocus : descripcion::requestFocus);
    }

}
