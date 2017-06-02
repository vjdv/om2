package net.vjdv.baz.om2.models;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

/**
 *
 * @author B187926
 */
public class Dialogos {

    private final Alert infoDialog = new Alert(Alert.AlertType.INFORMATION);
    private final Alert alertDialog = new Alert(Alert.AlertType.WARNING);
    private final Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
    private final TextInputDialog inputDialog = new TextInputDialog();

    public Dialogos() {
        infoDialog.setHeaderText(null);
        alertDialog.setHeaderText(null);
        confirmDialog.setHeaderText(null);
        inputDialog.setHeaderText(null);
    }

    public void alert(String msg) {
        alert(msg, "Alerta");
    }

    public void alert(String msg, String title) {
        alertDialog.setContentText(msg);
        alertDialog.setTitle(title);
        alertDialog.showAndWait();
    }

    public void message(String msg) {
        message(msg, "Información");
    }

    public void message(String msg, String title) {
        infoDialog.setContentText(msg);
        infoDialog.setTitle(title);
        infoDialog.showAndWait();
    }

    public boolean confirm(String msg) {
        return confirm(msg, "Confirme");
    }

    public boolean confirm(String msg, String title) {
        confirmDialog.setContentText(msg);
        Optional<ButtonType> result = confirmDialog.showAndWait();
        return result.get() == ButtonType.OK;
    }

    public String input(String msg, String title, String curval) {
        inputDialog.setContentText(msg);
        inputDialog.getEditor().setText(curval);
        Optional<String> res = inputDialog.showAndWait();
        if (res.isPresent()) {
            return res.get();
        }
        return null;
    }

    public String input(String msg, String curval) {
        return input(msg, "Entrada:", curval);
    }

    public String input(String msg) {
        return input(msg, "Entrada:", "");
    }
    
    public TextField getEditor() {
        return inputDialog.getEditor();
    }
}
