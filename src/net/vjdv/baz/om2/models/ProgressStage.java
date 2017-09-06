package net.vjdv.baz.om2.models;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Muestra el progreso de una tarea
 *
 * @author B187926
 */
public class ProgressStage extends Stage {

    private final ProgressBar pb = new ProgressBar();
    private final Label info = new Label();
    private final Label pblb = new Label();

    public ProgressStage(Task<?> task) {
        //Layout
        initStyle(StageStyle.UTILITY);
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
        info.setText("Espere...");
        pb.setProgress(-1F);
        pblb.setText("0%");
        VBox vb = new VBox();
        vb.setSpacing(5);
        vb.setAlignment(Pos.CENTER);
        vb.getChildren().addAll(info, pb, pblb);
        vb.setPrefWidth(200);
        vb.setPrefHeight(60);
        Scene scene = new Scene(vb);
        setScene(scene);
        //Task
        task.progressProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            pb.setProgress(newValue.doubleValue());
            pblb.setText(((int) (newValue.doubleValue() * 100)) + "%");
        });
        info.textProperty().bind(task.messageProperty());
        titleProperty().bind(task.titleProperty());
        show();
        task.setOnSucceeded(evt -> hide());
    }

}
