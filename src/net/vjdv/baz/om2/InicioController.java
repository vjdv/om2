package net.vjdv.baz.om2;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javax.swing.JOptionPane;
import net.vjdv.baz.om2.models.ConexionDB;
import net.vjdv.baz.om2.models.Dialogos;
import net.vjdv.baz.om2.models.Procedimiento;
import net.vjdv.baz.om2.models.Proyecto;
import net.vjdv.baz.om2.models.Recurso;
import net.vjdv.baz.om2.models.Tabla;

/**
 *
 * @author B187926
 */
public class InicioController implements Initializable {

    @FXML
    Menu menu_conexiones;
    @FXML
    private TableView<Procedimiento> tabla_sps;
    @FXML
    TableColumn<Procedimiento, String> colSpNombre, colSpDesc, colSpUri, colSpMap;
    @FXML
    private TableView<Tabla> tabla_tbs;
    @FXML
    TableColumn<Tabla, String> colTbNombre, colTbDesc;
    //Variables
    private final FileChooser filechooser = new FileChooser();
    private final Dialogos dialogs = new Dialogos();
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
        String strtitulo = dialogs.input("Título del proyecto:");
        if (strtitulo != null) {
            proyecto = new Proyecto();
            proyecto.titulo = strtitulo;
        }
    }

    @FXML
    private void abrirProyecto(ActionEvent event) {
        File f = filechooser.showOpenDialog(null);
        if (f != null) {
            proyecto = Proyecto.abrir(f);
            if (proyecto != null) {
                pintarTabla();
                //pintarConexiones();
            } else {

            }
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
        File f = new File("E:\\Users\\B187926\\Documents\\SITCB.xml");
        proyecto = Proyecto.abrir(f);
        proyecto.file = f;
        //dirobjs = f.getParent() + "\\db_objects";
        pintarTabla();
        //pintarConexiones();
    }

    @FXML
    private void agregarProcedimiento(ActionEvent event) {
        String esquema, nombre, map, descripcion, uri;
        esquema = dialogs.input("Esquema:", "dbo");
        if (esquema == null) {
            return;
        }
        nombre = dialogs.input("Nombre:");
        if (nombre == null) {
            return;
        }
        String tipo = "SP";
        map = dialogs.input("SqlMap:");
        if (map == null) {
            return;
        }
        descripcion = dialogs.input("Descripción:");
        if (descripcion == null) {
            return;
        }
        uri = dialogs.input("Archivo:", nombre + ".sql");
        if (uri == null) {
            return;
        }
        Procedimiento sp = new Procedimiento(nombre);
        sp.setSchema(esquema);
        sp.setTipo(tipo);
        sp.setMap(map);
        sp.setDescripcion(descripcion);
        sp.setUri(uri);
        proyecto.procedimientos.add(sp);
        sps_data.add(sp);
        pintarTabla();
    }

    @FXML
    private void agregarTabla(ActionEvent event) {
        String esquema, nombre, descripcion;
        esquema = dialogs.input("Esquema:", "dbo");
        if (esquema == null) {
            return;
        }
        nombre = dialogs.input("Nombre:");
        if (nombre == null) {
            return;
        }
        descripcion = dialogs.input("Descripción:");
        if (descripcion == null) {
            return;
        }
        String tipo = "TABLE";
        Tabla t = new Tabla(nombre);
        t.setSchema(esquema);
        t.setNombre(nombre);
        t.setTipo(tipo);
        t.setDescripcion(descripcion);
        proyecto.tablas.add(t);
        tbs_data.add(t);
        pintarTabla();
    }

    @FXML
    private void copiarNombreProcedimiento(ActionEvent event) {
        if (tabla_sps.getSelectionModel().getSelectedItems().isEmpty()) {
            dialogs.alert("Elija uno o más elementos");
            return;
        }
        String tmp = "";
        for (Procedimiento r : tabla_sps.getSelectionModel().getSelectedItems()) {
            tmp += (tmp.isEmpty() ? "" : "\r\n") + r.getNombre() + "\t" + r.getMap();
        }
        setClipBoard(tmp);
    }

    @FXML
    private void copiarNombreTabla(ActionEvent event) {
        if (tabla_tbs.getSelectionModel().getSelectedItems().isEmpty()) {
            dialogs.alert("Elija uno o más elementos");
            return;
        }
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
        for (Procedimiento r : tabla_sps.getSelectionModel().getSelectedItems()) {
            String nombre = dialogs.input("Nombre:", r.getNombre());
            if (nombre == null) {
                continue;
            }
            String map = dialogs.input("SqlMap:", r.getMap());
            if (map == null) {
                continue;
            }
            String descripcion = dialogs.input("Descripcion:", r.getDescripcion());
            if (descripcion == null) {
                continue;
            }
            String uri = dialogs.input("Archivo:", r.getUri());
            if (uri == null) {
                continue;
            }
            r.setNombre(nombre);
            r.setMap(map);
            r.setDescripcion(descripcion);
            r.setUri(uri);
        }
    }

    @FXML
    private void editarTabla(ActionEvent event) {
        if (tabla_tbs.getSelectionModel().getSelectedItems().isEmpty()) {
            dialogs.alert("Elija uno o más elementos");
            return;
        }
        for (Recurso r : tabla_tbs.getSelectionModel().getSelectedItems()) {
            String esquema = dialogs.input("Esquema:", r.getSchema());
            if (esquema == null) {
                return;
            }
            String nombre = dialogs.input("Nombre:", r.getNombre());
            if (nombre == null) {
                continue;
            }
            String descripcion = dialogs.input("Descripcion:", r.getDescripcion());
            if (descripcion == null) {
                continue;
            }
            r.setSchema(esquema);
            r.setNombre(nombre);
            r.setDescripcion(descripcion);
        }
    }

    @FXML
    private void quitarElementos(ActionEvent event) {
        /*TableView<Recurso> tabla = tabla_otros;
        if (tabs.getSelectionModel().getSelectedIndex() == 0) {
            tabla = tabla_sps;
        } else if (tabs.getSelectionModel().getSelectedIndex() == 1) {
            tabla = tabla_tbs;
        }
        if (tabla.getSelectionModel().getSelectedItems().isEmpty()) {
            dialogs.alert("Elija uno o más elementos");
            return;
        }
        tabla.getSelectionModel().getSelectedItems().forEach((r) -> {
            proyecto.procedimientos.remove(r);
            proyecto.tablas.remove(r);
            proyecto.otros.remove(r);
        });*/
        pintarTabla();
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
    private void agregarColumnas(ActionEvent event) {
        if (tabla_tbs.getSelectionModel().getSelectedItems().isEmpty()) {
            dialogs.alert("Elija uno o más elementos");
            return;
        }
        String tmp = "";
        for (Recurso r : tabla_tbs.getSelectionModel().getSelectedItems()) {
            String pref = "";
            while (true) {
                String newcol = dialogs.input("Nueva columna para " + r.getNombre(), pref);
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
        String nombre = dialogs.input("Nombre de la conexión:");
        if (nombre == null) {
            return;
        }
        ConexionDB c = new ConexionDB();
        c.nombre = nombre;
        String conexiones[] = {"mssql"};
        c.gestor = (String) JOptionPane.showInputDialog(null, "Tipo:", "Entrada", JOptionPane.QUESTION_MESSAGE, null, conexiones, "mssql");
        c.servidor = dialogs.input("Servidor:");
        c.puerto = Integer.parseInt(dialogs.input("Puerto:"));
        c.basededatos = dialogs.input("Base de datos:");
        c.usuario = dialogs.input("Usuario:");
        c.password = dialogs.input("Contraseña:");
        proyecto.conexiones.add(c);
        agregarConexion(c);
    }

    private void agregarConexion(ConexionDB c) {
        /*DataSource ds = null;
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
            final DataSource fds = ds;
            RadioMenuItem m = new RadioMenuItem(c.nombre);
            m.setToggleGroup(conexion_tg);
            m.setOnAction((ActionEvent event) -> {
                conn_name = c.nombre;
                conectarDataSource(fds);
            });
            menu_conexiones.getItems().add(m);
        }*/
    }

    @FXML
    private void verDependencias(ActionEvent event) {
        /*List<Tabla> tablas = new ArrayList<>();
        for (Recurso r : tabla_tbs.getSelectionModel().getSelectedItems()) {
            tablas.add(new Tabla(r.getNombre()));
        }
        DependenciasTablasDialog dialog = new DependenciasTablasDialog(tablas);*/
 /*dialog.show();
        dialog.start();*/
    }

    @FXML
    private void compararSP(ActionEvent event) {
        /*if (conn == null) {
            dialogs.alert("No está conectado a alguna base de datos");
            return;
        }
        for (Recurso r : tabla_sps.getSelectionModel().getSelectedItems()) {
            File local = new File(dirobjs + File.separator + r.getUri());
            if (!local.exists()) {
                dialogs.message("No hay versión local guardada");
                continue;
            }
            File tmp;
            try {
                tmp = File.createTempFile(r.getNombre() + "_", ".sql");
                tmp.deleteOnExit();
            } catch (IOException ex) {
                Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, "Error al crear archivo temporal", ex);
                continue;
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT OBJECT_NAME(OBJECT_ID) sp, definition FROM sys.sql_modules WHERE OBJECT_NAME(OBJECT_ID)='" + r.getNombre() + "'")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String def = rs.getString("definition");
                    def = def.replaceAll("CREATE PROCEDURE ", "ALTER PROCEDURE ");
                    def = def.replaceAll("CREATE FUNCTION ", "ALTER FUNCTION ");
                    try (PrintWriter out = new PrintWriter(tmp)) {
                        out.print(def);
                    }
                } else {
                    dialogs.message("No existe el procedimiento en el servidor.");
                    continue;
                }
            } catch (SQLException | FileNotFoundException | NullPointerException ex) {
                dialogs.alert("Error al obtener procedimiento desde el servidor: " + ex.toString());
                Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, "Error al obtener o escribir procedimiento", ex);
                continue;
            }
            Runtime rt = Runtime.getRuntime();
            try {
                System.out.println("Calling WinMerge");
                rt.exec("\"C:\\Program Files (x86)\\WinMerge\\WinMergeU\" /e /x /s /u /wr /dl \"versión de archivo local guardado\" /dr \"version del procedimiento en la DB\" \"" + local.getCanonicalPath() + "\" \"" + tmp.getCanonicalPath() + "\"");
            } catch (IOException ex) {
                Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, "Error al ejecutar comando en consola", ex);
            }
        }*/
    }

    @FXML
    private void guardarDesdeServidor(ActionEvent event) {
        if (conn == null) {
            dialogs.alert("No está conectado a alguna base de datos");
            return;
        }
        for (Recurso r : tabla_sps.getSelectionModel().getSelectedItems()) {
            guardarDesdeServidor(r);
        }
    }

    private void guardarDesdeServidor(Recurso r) {
        /*File local = new File(dirobjs + File.separator + r.getUri());
        if (local.exists()) {
            if (!dialogs.confirm("¿Sobreescribir " + r.getUri() + "?")) {
                return;
            }
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT OBJECT_NAME(OBJECT_ID) sp, definition FROM sys.sql_modules WHERE OBJECT_NAME(OBJECT_ID)='" + r.getNombre() + "'")) {
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
        }*/
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

    /*private void pintarConexiones() {
        conexion_tg = new ToggleGroup();
        for (ConexionDB c : proyecto.conexiones) {
            agregarConexion(c);
        }
    }

    private void agregarConexion(ConexionDB c) {
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
            final DataSource fds = ds;
            RadioMenuItem m = new RadioMenuItem(c.nombre);
            m.setToggleGroup(conexion_tg);
            m.setOnAction((ActionEvent event) -> {
                //conn_name = c.nombre;
                conectarDataSource(fds);
            });
            menu_conexiones.getItems().add(m);
        }
    }*/
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colSpNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colSpMap.setCellValueFactory(cellData -> cellData.getValue().mapProperty());
        colSpDesc.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
        colSpUri.setCellValueFactory(cellData -> cellData.getValue().uriProperty());
        colTbNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colTbDesc.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
    }

}
