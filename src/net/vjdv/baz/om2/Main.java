package net.vjdv.baz.om2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 *
 * @author B187926
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/net/vjdv/baz/om2/Inicio.fxml"));
        Parent root = loader.load();
        InicioController controller = loader.getController();
        Scene scene = new Scene(root);
        //aceleradores
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN), () -> controller.refrescar(null));
        stage.getIcons().add(new Image("/net/vjdv/baz/om2/img/objects.png"));
        stage.setTitle("Administrador de objetos de SQL");
        controller.tituloProperty().addListener((observable, oldValue, newValue) -> stage.setTitle(newValue + " - Administrador de objetos de SQL"));
        stage.setScene(scene);
        stage.setOnHidden(e -> controller.shutdown());
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
