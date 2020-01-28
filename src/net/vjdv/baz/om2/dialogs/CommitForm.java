package net.vjdv.baz.om2.dialogs;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;

/**
 *
 * @author B187926
 */
public class CommitForm extends Dialog<String> {

    public CommitForm() {
        super();
        setTitle("Mensaje commit");
        TextField msg = new TextField("");
        //contenido
        setHeaderText("Detalles del cambio:");
        getDialogPane().setPadding(new Insets(10));
        getDialogPane().setContent(msg);
        //botones
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button guardarBtn = (Button) getDialogPane().lookupButton(ButtonType.OK);
        //disabling
        guardarBtn.setDisable(true);
        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            guardarBtn.setDisable(newValue.trim().isEmpty());
        };
        msg.textProperty().addListener(listener);
        //result
        setResultConverter(button -> {
            if (button == null || button == ButtonType.CANCEL) {
                return null;
            }
            return msg.getText();
        });
        Platform.runLater(msg::requestFocus);
    }

}
