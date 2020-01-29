package net.vjdv.baz.om2.dialogs;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/**
 *
 * @author B187926
 */
public class HelpDialog extends Dialog<String> {

    public HelpDialog() {
        setTitle("Ayuda");
        setContentText("Desarrollado por B187926\n\nC\u00f3digo fuente e instrucciones de uso disponibles en:\nhttps://github.com/vjdv/om2\n\nSe aceptan pull requests");
        ButtonType ir = new ButtonType("Ir al repositorio");
        getDialogPane().getButtonTypes().addAll(ir, ButtonType.CANCEL);
        setResultConverter(button -> {
            if (button == null || button == ButtonType.CANCEL) {
                return null;
            }
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/vjdv/om2"));
            } catch (URISyntaxException | IOException ex) {
                setContentText(ex.getMessage());
                return null;
            }
            return "";
        });
    }

}
