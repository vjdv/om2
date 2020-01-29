package net.vjdv.baz.om2.dialogs;

import javafx.beans.property.StringProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;

/**
 *
 * @author B187926
 */
public class GitLogDialog extends Dialog<Void> {

    private final TextArea area = new TextArea();

    public GitLogDialog() {
        setTitle("GIT");
        setContentText("Bit\u00e1cora de git:");
        getDialogPane().setExpandableContent(area);
        getDialogPane().setExpanded(true);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        initModality(Modality.NONE);
    }

    public void bind(StringProperty property) {
        area.textProperty().bind(property);
        property.addListener((observable) -> area.setScrollTop(Double.MAX_VALUE));
    }

}
