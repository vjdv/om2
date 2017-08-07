package net.vjdv.baz.om2;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.sql.DataSource;
import javax.swing.JOptionPane;
import net.vjdv.baz.om2.models.ConexionDB;
import net.vjdv.baz.om2.models.Dialogos;
import net.vjdv.baz.om2.models.Procedimiento;
import net.vjdv.baz.om2.models.Proyecto;
import net.vjdv.baz.om2.models.Recurso;
import net.vjdv.baz.om2.models.Tabla;
import static java.util.logging.Logger.getLogger;

/**
 *
 * @author B187926
 */
public class InicioController implements Initializable {

    @FXML
    private Menu menu_conexiones, menu_svn;
    @FXML
    private RadioMenuItem menuitem_conexion_default;
    @FXML
    private TabPane tabs;
    @FXML
    private TableView<Procedimiento> tabla_sps;
    @FXML
    private TableColumn<Procedimiento, String> colSpNombre, colSpDesc, colSpMap;
    @FXML
    private TableView<Tabla> tabla_tbs;
    @FXML
    private TableColumn<Tabla, String> colTbNombre, colTbDesc;
    @FXML
    Label statusconn_lb;
    //Variables
    private final TextInputDialog filteringDialog = new TextInputDialog();
    private final FileChooser filechooser = new FileChooser();
    private final Dialogos dialogs = new Dialogos();
    private SvnDialog svn;
    private Proyecto proyecto;
    private ObservableList<Procedimiento> sps_data;
    private FilteredList<Procedimiento> sps_filtered;
    private ObservableList<Tabla> tbs_data;
    private FilteredList<Tabla> tbs_filtered;
    private ToggleGroup conexion_tg;
    private Clipboard clipboard;
    private Connection conn;

    @FXML
    private void nuevoProyecto(ActionEvent event) {
        try {
            proyecto = new Proyecto();
            proyecto.titulo = Dialogos.input("Título del proyecto:");
        } catch (Dialogos.InputCancelled ex) {
            Logger.getLogger("ObjMan").log(Level.FINEST, "Input cancelled");
        }
    }

    @FXML
    private void abrirProyecto(ActionEvent event) {
        File f = filechooser.showOpenDialog(null);
        if (f != null) {
            abrirProyecto(f);
        }
    }

    private void abrirProyecto(File f) {
        proyecto = Proyecto.abrir(f);
        if (proyecto != null) {
            pintarTabla();
            pintarConexiones();
            menu_svn.setDisable(true);
            if (proyecto.svn != null && new File(proyecto.directorio_objetos + File.separator + ".svn").exists()) {
                svn = new SvnDialog(this, proyecto.svn, proyecto.directorio_objetos);
                menu_svn.setDisable(false);
            }
        } else {
            dialogs.alert("No se pudo abrir el archivo.");
        }
    }

    @FXML
    private void guardarProyecto(ActionEvent event) {
        if (proyecto.file == null) {
            FileChooser chooser = new FileChooser();
            File f = chooser.showSaveDialog(null);
            if (f != null) {
                proyecto.file = f;
                proyecto.guardar();
            }
        } else {
            proyecto.guardar();
        }
    }

    @FXML
    private void abrirProyecto2(ActionEvent event) {
        File f = new File("E:\\Users\\B187926\\Documents\\SITCB2.xml");
        abrirProyecto(f);
    }

    @FXML
    private void agregarProcedimiento(ActionEvent event) {
        try {
            String esquema = Dialogos.input("Esquema:", "Nuevo procedimiento", "dbo");
            String nombre = Dialogos.input("Nombre:", "Nuevo procedimiento");
            String tipo = "SP";
            String map = Dialogos.input("SqlMap:", "Nuevo procedimiento");
            String descripcion = Dialogos.input("Descripción:", "Nuevo procedimiento");
            Procedimiento sp = new Procedimiento(nombre);
            sp.setSchema(esquema);
            sp.setTipo(tipo);
            sp.setMap(map);
            sp.setDescripcion(descripcion);
            proyecto.procedimientos.add(sp);
            proyecto.onAddedRecurso(sp);
            sps_data.add(sp);
            pintarTabla();
        } catch (Dialogos.InputCancelled ex) {
            Logger.getLogger("ObjMan").log(Level.FINEST, "Input cancelled");
        }
    }

    @FXML
    private void agregarTabla(ActionEvent event) {
        try {
            String esquema = Dialogos.input("Esquema:", "Nueva tabla", "dbo");
            String nombre = Dialogos.input("Nombre:", "Nueva tabla");
            String descripcion = Dialogos.input("Descripción:", "Nueva tabla");
            String tipo = "TB";
            Tabla t = new Tabla(nombre);
            t.setSchema(esquema);
            t.setNombre(nombre);
            t.setTipo(tipo);
            t.setDescripcion(descripcion);
            proyecto.tablas.add(t);
            proyecto.onAddedRecurso(t);
            tbs_data.add(t);
            pintarTabla();
        } catch (Dialogos.InputCancelled ex) {
            Logger.getLogger("ObjMan").log(Level.FINEST, "Input cancelled");
        }
    }

    @FXML
    private void filtrar(ActionEvent event) {
        if (filteringDialog.isShowing()) {
            filteringDialog.getDialogPane().getScene().getWindow().requestFocus();
        } else {
            filteringDialog.show();
        }
        filteringDialog.getEditor().selectAll();
        filteringDialog.getEditor().requestFocus();
    }

    @FXML
    private void copiarNombreProcedimiento(ActionEvent event) {
        String tmp = "";
        for (Procedimiento r : tabla_sps.getSelectionModel().getSelectedItems()) {
            tmp += (tmp.isEmpty() ? "" : "\r\n") + r.getNombre() + "\t" + r.getMap();
        }
        setClipBoard(tmp);
    }

    @FXML
    private void copiarNombreTabla(ActionEvent event) {
        String tmp = "";
        for (Recurso r : tabla_tbs.getSelectionModel().getSelectedItems()) {
            tmp += (tmp.isEmpty() ? "" : "\r\n") + r.getNombre();
        }
        setClipBoard(tmp);
    }

    @FXML
    private void editarProcedimiento(ActionEvent event) {
        if (tabla_sps.getSelectionModel().getSelectedItems().isEmpty()) {
            dialogs.alert("Elija uno o más elementos");
            return;
        }
        try {
            for (Procedimiento r : tabla_sps.getSelectionModel().getSelectedItems()) {
                String nombre = Dialogos.input("Nombre:", "Editar procedimiento", r.getNombre(), proyecto.url == null);
                String map = Dialogos.input("SqlMap:", "Editar procedimiento", r.getMap());
                String descripcion = Dialogos.input("Descripcion:", "Editar procedimiento", r.getDescripcion());
                r.setNombre(nombre);
                r.setMap(map);
                r.setDescripcion(descripcion);
                r.updated();
            }
        } catch (Dialogos.InputCancelled ex) {
            Logger.getLogger("ObjMan").log(Level.FINEST, "Input cancelled");
        }
    }

    @FXML
    private void editarTabla(ActionEvent event) {
        if (tabla_tbs.getSelectionModel().getSelectedItems().isEmpty()) {
            dialogs.alert("Elija uno o más elementos");
            return;
        }
        try {
            for (Recurso r : tabla_tbs.getSelectionModel().getSelectedItems()) {
                String nombre = Dialogos.input("Nombre:", "Editar tabla", r.getNombre(), proyecto.url == null);
                String descripcion = Dialogos.input("Descripcion:", "Editar tabla", r.getDescripcion());
                r.setNombre(nombre);
                r.setDescripcion(descripcion);
            }
        } catch (Dialogos.InputCancelled ex) {
            Logger.getLogger("ObjMan").log(Level.FINEST, "Input cancelled");
        }
    }

    @FXML
    private void quitarElementos(ActionEvent event) {
        //Quitar procedimientos
        if (tabs.getSelectionModel().getSelectedIndex() == 0) {
            List<Procedimiento> list = tabla_sps.getSelectionModel().getSelectedItems();
            proyecto.quitarProcedimiento(list.toArray(new Procedimiento[list.size()]));
            sps_data.removeAll(list);
        } //Quitar tablas
        else if (tabs.getSelectionModel().getSelectedIndex() == 1) {
            List<Tabla> list = tabla_tbs.getSelectionModel().getSelectedItems();
            proyecto.quitarTabla(list.toArray(new Tabla[list.size()]));
            tbs_data.removeAll(list);
        }
    }

    @FXML
    private void renombrarElementos(ActionEvent event) {
        /*TableView<Recurso> tabla = tabla_otros;
        /*if (tabs.getSelectionModel().getSelectedIndex() == 0) {
            tabla = tabla_sps;
        } else if (tabs.getSelectionModel().getSelectedIndex() == 1) {
            tabla = tabla_tbs;
        }
        if (tabla.getSelectionModel().getSelectedItems().isEmpty()) {
            dialogs.alert("Elija uno o más elementos");
            return;
        }
        List<RenameScript> ren_list = new ArrayList<>();
        for (Recurso r : tabla.getSelectionModel().getSelectedItems()) {
            String newnameobj = dialogs.input("Nuevo nombre para " + r.getNombre(), r.getNombre());
            if (newnameobj == null) {
                continue;
            }
            RenameScript rn = r.getRenameScript(newnameobj);
            if (rn == null) {
                continue;
            }
            ren_list.add(rn);
        }
        Dialog dialog = new RenameDialog(ren_list);
        dialog.showAndWait();/*/
    }

    @FXML
    private void agregarColumnas(ActionEvent event) throws Dialogos.InputCancelled {
        if (tabla_tbs.getSelectionModel().getSelectedItems().isEmpty()) {
            dialogs.alert("Elija uno o más elementos");
            return;
        }
        String tmp = "";
        for (Recurso r : tabla_tbs.getSelectionModel().getSelectedItems()) {
            String pref = "";
            while (true) {
                String newcol = Dialogos.input("Nueva columna para " + r.getNombre(), pref);
                if (newcol == null) {
                    break;
                }
                if (pref.isEmpty()) {
                    String parts[] = newcol.split("_", 2);
                    pref = parts[0] + "_";
                }
                tmp += "ALTER TABLE " + r.getNombre() + " ADD " + newcol + "\r\n";
            }
        }
        dialogs.message(tmp);
        setClipBoard(tmp);
    }

    @FXML
    private void nuevaConexion(ActionEvent event) {
        try {
            ConexionDB c = new ConexionDB();
            c.nombre = Dialogos.input("Nombre de la conexión:");
            String conexiones[] = {"mssql"};
            c.gestor = (String) JOptionPane.showInputDialog(null, "Tipo:", "Entrada", JOptionPane.QUESTION_MESSAGE, null, conexiones, "mssql");
            c.servidor = Dialogos.input("Servidor:");
            c.puerto = Integer.parseInt(Dialogos.input("Puerto:"));
            c.basededatos = Dialogos.input("Base de datos:");
            c.usuario = Dialogos.input("Usuario:");
            c.password = Dialogos.input("Contraseña:");
            proyecto.conexiones.add(c);
            pintarConexion(c);
        } catch (Dialogos.InputCancelled ex) {
            Logger.getLogger("ObjMan").log(Level.FINEST, "Input cancelled");
        }
    }

    /*@FXML
    private void verDependencias(ActionEvent event) {
        /*List<Tabla> tablas = new ArrayList<>();
        for (Recurso r : tabla_tbs.getSelectionModel().getSelectedItems()) {
            tablas.add(new Tabla(r.getNombre()));
        }
        DependenciasTablasDialog dialog = new DependenciasTablasDialog(tablas);*/
 /*dialog.show();
        dialog.start();*/
    //}*/
    @FXML
    private void compararSP(ActionEvent event) {
        if (conn == null) {
            dialogs.alert("No está conectado a alguna base de datos");
            return;
        }
        File fwm = new File(proyecto.winmerge);
        if (proyecto.winmerge == null || !fwm.exists()) {
            dialogs.alert("No se encontró Winmerge");
            return;
        }
        for (Procedimiento sp : tabla_sps.getSelectionModel().getSelectedItems()) {
            File local = new File(proyecto.directorio_objetos + File.separator + sp.getUri());
            if (!local.exists()) {
                dialogs.message("No hay versión local guardada de " + sp.getNombre());
                continue;
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT OBJECT_NAME(OBJECT_ID) sp, definition FROM sys.sql_modules WHERE OBJECT_NAME(OBJECT_ID)='" + sp.getNombre() + "'")) {
                File tmp = File.createTempFile(sp.getNombre() + "_", ".sql");
                tmp.deleteOnExit();
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String def = rs.getString("definition");
                    def = def.replaceAll("CREATE PROCEDURE ", "ALTER PROCEDURE ");
                    def = def.replaceAll("CREATE FUNCTION ", "ALTER FUNCTION ");
                    try (PrintWriter out = new PrintWriter(tmp)) {
                        out.print(def);
                    }
                } else {
                    dialogs.message("No existe el procedimiento " + sp.getNombre() + "en el servidor " + conn.getMetaData().getURL());
                    continue;
                }
                System.out.println("Calling WinMerge");
                Runtime rt = Runtime.getRuntime();
                rt.exec("\"" + proyecto.winmerge + "\" /e /x /s /u /wr /dl \"versión de archivo local guardado\" /dr \"version del procedimiento en la DB\" \"" + local.getCanonicalPath() + "\" \"" + tmp.getCanonicalPath() + "\"");
            } catch (SQLException | IOException | NullPointerException ex) {
                dialogs.alert("Error al comparar procedimiento: " + ex.toString());
                Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, "Error al obtener o escribir procedimiento", ex);
            }
        }
    }

    @FXML
    private void guardarDesdeServidor(ActionEvent event) {
        if (conn == null) {
            dialogs.alert("No está conectado a alguna base de datos");
            return;
        }
        for (Procedimiento sp : tabla_sps.getSelectionModel().getSelectedItems()) {
            guardarDesdeServidor(sp);
        }
    }

    private void guardarDesdeServidor(Procedimiento sp) {
        File local = new File(proyecto.directorio_objetos + File.separator + sp.getUri());
        if (local.exists()) {
            if (!dialogs.confirm("¿Sobreescribir " + sp.getUri() + "?")) {
                return;
            }
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT OBJECT_NAME(OBJECT_ID) sp, definition FROM sys.sql_modules WHERE OBJECT_NAME(OBJECT_ID)='" + sp.getNombre() + "'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String def = rs.getString("definition");
                def = def.replaceAll("CREATE PROCEDURE ", "ALTER PROCEDURE ");
                def = def.replaceAll("CREATE FUNCTION ", "ALTER FUNCTION ");
                try (PrintWriter out = new PrintWriter(local)) {
                    out.print(def);
                }
            } else {
                dialogs.message("No existe el procedimiento en el servidor.");
            }
        } catch (SQLException | FileNotFoundException | NullPointerException ex) {
            dialogs.alert("Error al obtener procedimiento desde el servidor: " + ex.toString());
            Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, "Error al obtener o escribir procedimiento", ex);
        }
    }

    @FXML
    private void crearArchivoParaSP(ActionEvent event) {
        /*final SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
        Calendar now = Calendar.getInstance();
        String fecha = format.format(now.getTime());
        for (Recurso r : tabla_sps.getSelectionModel().getSelectedItems()) {
            File local = new File(dirobjs + File.separator + r.getUri());
            if (local.exists()) {
                dialogs.alert("Ya existe " + r.getUri());
                continue;
            }
            String str = "/*************************************************************\r\n"
                    + "Proyecto:				" + proyecto.titulo + "\r\n"
                    + "Descripción:			\r\n"
                    + "Parámetros de entrada:	\r\n"
                    + "Valor de retorno:		\r\n"
                    + "Creador:				 " + fecha.toUpperCase() + "\r\n"
                    + "*************************************************************-/\r\n";
            str += r.getNombre().startsWith("F")
                    ? "CREATE FUNCTION [dbo].[" + r.getNombre() + "]() RETURNS XML AS\r\n\r\n"
                    + "BEGIN\r\n"
                    + "\tDECLARE @RESP XML\r\n\t\r\n\t\r\n\t\r\n"
                    + "RETURN @RESP\r\n\r\n"
                    + "END"
                    : "CREATE PROCEDURE [dbo].[" + r.getNombre() + "] (  ) AS\r\n\r\n"
                    + "DECLARE @RESP XML\r\n\r\n"
                    + "BEGIN\r\n\r\n"
                    + "BEGIN TRY\r\n\tBEGIN TRANSACTION\r\n\t\r\n\t\r\n\t\r\n"
                    + "END TRY\r\n\r\n"
                    + "BEGIN CATCH\r\n\t\r\n"
                    + "END CATCH\r\n\r\n"
                    + "SELECT @RESP AS 'txt_xml_schema'\r\n\r\n"
                    + "END";
            try (PrintWriter out = new PrintWriter(local)) {
                out.print(str);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, "Error al crear el archivo", ex);
                dialogs.alert(ex.toString());
            }
        }*/
    }

    @FXML
    private void buscarEnSp(ActionEvent event) {
        try {
            String str = Dialogos.input("Texto a buscar:", "Buscar en procedimientos", "");
            sps_filtered.setPredicate((sp) -> {
                return sp.getCuerpoCleaned().contains(str.toLowerCase());
            });
        } catch (Dialogos.InputCancelled ex) {
            Logger.getLogger("ObjMan").log(Level.FINEST, "Input cancelled");
        }
    }

    @FXML
    private void verDependenciasSp(ActionEvent event) {
        Procedimiento spm = tabla_sps.getSelectionModel().getSelectedItem();
        sps_filtered.setPredicate((sp) -> {
            return sp.getCuerpoCleaned().contains(sp.getNombre().toLowerCase());
        });
        tbs_filtered.setPredicate((tb) -> {
            return spm.getCuerpoCleaned().contains(tb.getNombre().toLowerCase());
        });
    }

    @FXML
    private void verDependenciasTb(ActionEvent event) {
        Tabla tb = tabla_tbs.getSelectionModel().getSelectedItem();
        sps_filtered.setPredicate((sp) -> {
            return sp.getCuerpoCleaned().contains(tb.getNombre().toLowerCase());
        });
    }

    @FXML
    private void reindexarProcedimientos() {
        if (conn == null) {
            dialogs.alert("No está conectado a alguna base de datos");
            return;
        }
        Map<String, Procedimiento> map = new HashMap<>();
        for (Procedimiento p : proyecto.procedimientos) {
            map.put(p.getNombre().toUpperCase(), p);
        }
        try (PreparedStatement st = conn.prepareStatement("SELECT OBJECT_NAME(OBJECT_ID) sp, definition FROM sys.sql_modules WHERE objectproperty(OBJECT_ID, 'IsProcedure') = 1")) {
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String spn = rs.getString("sp").trim().toUpperCase();
                String def = rs.getString("definition");
                Procedimiento sp = map.get(spn);
                if (sp != null) {
                    sp.setCuerpo(def);
                }
            }
        } catch (SQLException ex) {
            dialogs.alert(ex.toString());
            Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void indexaProcedimientos() {

    }

    //  S U B V E R S I O N
    @FXML
    private void svnCommit() {
        List<Procedimiento> list = tabla_sps.getSelectionModel().getSelectedItems();
        String[] files = new String[list.size()];
        for (int i = 0; i < files.length; i++) {
            files[i] = list.get(i).getUri();
        }
        svn.show();
        svn.commit(files);
    }

    @FXML
    private void svnUpdate() {
        List<Procedimiento> list = tabla_sps.getSelectionModel().getSelectedItems();
        String[] files = new String[list.size()];
        for (int i = 0; i < files.length; i++) {
            files[i] = list.get(i).getUri();
        }
        svn.show();
        svn.update(files);
    }

    @FXML
    private void svnCommitAll() {
        svn.show();
        svn.commit();
    }

    @FXML
    private void svnUpdateAll() {
        svn.show();
        svn.update();
    }

    @FXML
    private void svnDiferencias() {
        svn.show();
        svn.changedFiles();
    }

    @FXML
    private void svnComparaCambios() {
        svn.show();
        for (Procedimiento sp : tabla_sps.getSelectionModel().getSelectedItems()) {
            try {
                File tmp = File.createTempFile(sp.getNombre(), ".sql");
                tmp.deleteOnExit();
                svn.export(sp.getUri(), tmp);
                Runtime rt = Runtime.getRuntime();
                rt.exec("\"" + proyecto.winmerge + "\" /e /x /s /u /wr /dl \"versión actual repositorio\" /dr \"version local modificada\" \"" + tmp.getCanonicalPath() + "\" \"" + proyecto.directorio_objetos + File.separator + sp.getUri() + "\"");
            } catch (IOException ex) {
                Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setClipBoard(String text) {
        if (clipboard == null) {
            clipboard = Clipboard.getSystemClipboard();
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    private void pintarTabla() {
        //SPS
        sps_data = FXCollections.observableArrayList(proyecto.procedimientos);
        sps_filtered = new FilteredList<>(sps_data, p -> true);
        SortedList<Procedimiento> sps_sorted = new SortedList<>(sps_filtered);
        sps_sorted.comparatorProperty().bind(tabla_sps.comparatorProperty());
        tabla_sps.setItems(sps_sorted);
        //TBS
        tbs_data = FXCollections.observableArrayList(proyecto.tablas);
        tbs_filtered = new FilteredList<>(tbs_data, p -> true);
        SortedList<Tabla> tbs_sorted = new SortedList<>(tbs_filtered);
        tbs_sorted.comparatorProperty().bind(tabla_tbs.comparatorProperty());
        tabla_tbs.setItems(tbs_sorted);
    }

    private void pintarConexiones() {
        conexion_tg = new ToggleGroup();
        for (ConexionDB c : proyecto.conexiones) {
            pintarConexion(c);
        }
    }

    private void pintarConexion(ConexionDB c) {
        DataSource ds = null;
        if (c.gestor.equals("mssql")) {
            SQLServerDataSource ssds = new SQLServerDataSource();
            ssds.setServerName(c.servidor);
            ssds.setPortNumber(c.puerto);
            ssds.setDatabaseName(c.basededatos);
            ssds.setUser(c.usuario);
            ssds.setPassword(c.password);
            ds = ssds;
        }
        if (ds != null) {
            DataSource fds = ds;
            RadioMenuItem m = new RadioMenuItem(c.nombre);
            m.setToggleGroup(conexion_tg);
            m.setOnAction((ActionEvent event) -> {
                conectarDataSource(fds, c.servidor + "/" + c.basededatos);
            });
            menu_conexiones.getItems().add(m);
        }
    }

    private void conectarDataSource(DataSource ds, String dsname) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            getLogger(InicioController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            conn = null;
            statusconn_lb.setText("No conectado");
        }
        try {
            conn = ds.getConnection();
            statusconn_lb.setText("Conectado a " + dsname);
        } catch (SQLException ex) {
            getLogger(InicioController.class.getName()).log(Level.SEVERE, null, ex);
            conn = null;
            statusconn_lb.setText("Error de conexión");
        }
    }

    public void filtrarProcedimientos(Predicate<Procedimiento> p) {
        sps_filtered.setPredicate(p);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Tables config
        colSpNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colSpMap.setCellValueFactory(cellData -> cellData.getValue().mapProperty());
        colSpDesc.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
        colTbNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colTbDesc.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
        tabla_sps.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tabla_tbs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //Filtering
        filteringDialog.setHeaderText(null);
        filteringDialog.setTitle("Filtrar");
        filteringDialog.setContentText("Filtrar por:");
        filteringDialog.initModality(Modality.NONE);
        filteringDialog.initStyle(StageStyle.UTILITY);
        Stage stage = (Stage) filteringDialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        filteringDialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            String newValue2 = newValue.toLowerCase();
            sps_filtered.setPredicate((sp) -> {
                if (newValue2.isEmpty()) {
                    return true;
                }
                return sp.getFilteringString().contains(newValue2);
            });
            tbs_filtered.setPredicate((tb) -> {
                if (newValue2.isEmpty()) {
                    return true;
                }
                return tb.getFilteringString().contains(newValue2);
            });
        });
        filteringDialog.setOnHidden(event -> {
            String result = filteringDialog.getResult();
            if (result == null) {
                filteringDialog.getEditor().setText("");
            }
        });
        //Drag&Drop archivos
        tabla_sps.setOnDragDetected((MouseEvent event) -> {
            if (tabla_sps.getSelectionModel().getSelectedItems().isEmpty()) {
                event.consume();
                return;
            }
            ClipboardContent filesToCopyClipboard = new ClipboardContent();
            List<File> files = new ArrayList<>();
            Dragboard db = tabla_sps.startDragAndDrop(TransferMode.ANY);
            tabla_sps.getSelectionModel().getSelectedItems().forEach((r) -> {
                if (r.getUri() == null || r.getUri().isEmpty()) {
                    return;
                }
                File f = new File(proyecto.directorio_objetos + File.separator + r.getUri());
                if (f.exists()) {
                    files.add(f);
                }
            });
            filesToCopyClipboard.putFiles(files);
            db.setContent(filesToCopyClipboard);
            event.consume();
        });
        tabla_sps.setOnDragDone((DragEvent event) -> {
            event.consume();
        });
        //Conexión default
        menuitem_conexion_default.setToggleGroup(conexion_tg);
    }

}
