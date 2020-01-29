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
import net.vjdv.baz.om2.models.ConexionDB;

/**
 *
 * @author B187926
 */
public class ConexionForm extends Dialog<ConexionDB> {

    public ConexionForm(ConexionDB c) {
        super();
        setTitle((c == null ? "Nuevo" : "Modificar") + " procedimiento");
        //grid
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.getColumnConstraints().addAll(new ColumnConstraints(150), col2);
        //contenido
        TextField servidor = new TextField();
        TextField puerto = new TextField("1433");
        TextField base = new TextField();
        TextField usuario = new TextField();
        TextField pass = new TextField();
        grid.add(new Label("Servidor:"), 0, 0);
        grid.add(servidor, 1, 0);
        grid.add(new Label("Puerto:"), 0, 1);
        grid.add(puerto, 1, 1);
        grid.add(new Label("Base de datos:"), 0, 2);
        grid.add(base, 1, 2);
        grid.add(new Label("Usuario:"), 0, 3);
        grid.add(usuario, 1, 3);
        grid.add(new Label("Contrase\u00f1a:"), 0, 4);
        grid.add(pass, 1, 4);
        getDialogPane().setContent(grid);
        //editar
        if (c != null) {
            servidor.setText(c.getServer());
            puerto.setText("" + c.getPort());
            base.setText(c.getDb());
            usuario.setText(c.getUser());
            pass.setText(c.getPassword());
        }
        //botones
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button guardarBtn = (Button) getDialogPane().lookupButton(ButtonType.OK);
        //disabling
        guardarBtn.setDisable(c == null);
        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            guardarBtn.setDisable(servidor.getText().isEmpty() || puerto.getText().isEmpty() || base.getText().isEmpty() || usuario.getText().isEmpty() || pass.getText().isEmpty());
        };
        servidor.textProperty().addListener(listener);
        puerto.textProperty().addListener(listener);
        base.textProperty().addListener(listener);
        usuario.textProperty().addListener(listener);
        pass.textProperty().addListener(listener);
        //result
        setResultConverter(button -> {
            if (button == null || button == ButtonType.CANCEL) {
                return null;
            }
            ConexionDB c2 = c;
            if (c2 == null) {
                c2 = new ConexionDB();
            }
            c2.setServer(servidor.getText());
            c2.setPort(Integer.parseInt(puerto.getText().trim()));
            c2.setDb(base.getText());
            c2.setUser(usuario.getText());
            c2.setPassword(pass.getText());
            return c2;
        });
        Platform.runLater(servidor::requestFocus);
    }

}
