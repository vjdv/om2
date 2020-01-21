package net.vjdv.baz.om2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        Scene scene = new Scene(root);
        InicioController controller = loader.getController();
        //stage.getIcons().add(new Image("/net/vjdv/baz/om2/img/objects.png"));
        stage.setTitle("Administrador de objetos de SQL");
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
