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
import net.vjdv.baz.om2.models.Tabla;

/**
 *
 * @author B187926
 */
public class TablaForm extends Dialog<Datos> {

    public TablaForm(Tabla t) {
        super();
        setTitle((t == null ? "Nueva" : "Modificar") + " tabla");
        //grid
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.getColumnConstraints().addAll(new ColumnConstraints(150), col2);
        //contenido
        TextField esquema = new TextField("dbo");
        TextField nombre = new TextField();
        TextField descripcion = new TextField();
        esquema.setPromptText("dbo");
        grid.add(new Label("Esquema:"), 0, 0);
        grid.add(esquema, 1, 0);
        grid.add(new Label("Nombre de objeto:"), 0, 1);
        grid.add(nombre, 1, 1);
        grid.add(new Label("Descripci\u00f3n:"), 0, 3);
        grid.add(descripcion, 1, 3);
        getDialogPane().setContent(grid);
        //editar
        if (t != null) {
            esquema.setText(t.getSchema());
            nombre.setText(t.getNombre());
            descripcion.setText(t.getDescripcion());
            esquema.setDisable(true);
            nombre.setDisable(true);
        }
        //botones
        ButtonType guardar = new ButtonType("Guardar");
        getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);
        Button guardarBtn = (Button) getDialogPane().lookupButton(guardar);
        //disabling
        guardarBtn.setDisable(t == null);
        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            guardarBtn.setDisable(esquema.getText().isEmpty() || nombre.getText().isEmpty() || nombre.getText().matches("^[a-zA-Z0-9]"));
        };
        esquema.textProperty().addListener(listener);
        nombre.textProperty().addListener(listener);
        //result
        setResultConverter(button -> {
            if (button == null || button == ButtonType.CANCEL) {
                return null;
            }
            Datos d = new Datos();
            d.setTipo(Recurso.TABLA);
            d.setEsquema(esquema.getText());
            d.setNombre(nombre.getText());
            d.setDescripcion(descripcion.getText());
            return d;
        });
        Platform.runLater(t == null ? nombre::requestFocus : descripcion::requestFocus);
    }

}
