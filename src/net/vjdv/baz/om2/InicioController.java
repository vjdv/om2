package net.vjdv.baz.om2;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.application.Platform;

import javax.xml.bind.JAXBException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.extern.java.Log;
import net.vjdv.baz.exceptions.GitException;
import net.vjdv.baz.om2.dialogs.ProcedimientoForm;
import net.vjdv.baz.om2.dialogs.RepoInitializer;
import net.vjdv.baz.om2.dialogs.TablaForm;
import net.vjdv.baz.om2.models.Config;
import net.vjdv.baz.om2.models.Dialogos;
import net.vjdv.baz.om2.models.Git;
import net.vjdv.baz.om2.models.Procedimiento;
import net.vjdv.baz.om2.models.Proyecto;
import net.vjdv.baz.om2.models.Recurso;
import net.vjdv.baz.om2.models.Recursos;
import net.vjdv.baz.om2.models.Tabla;
import net.vjdv.baz.om2.models.Winmerge;
import org.controlsfx.control.textfield.CustomTextField;

/**
 *
 * @author B187926
 */
@Log(topic = "OM2")
public class InicioController implements Initializable {

    @FXML
    private MenuItem menuitem_spcopymp, menuitem_spcopyfl;
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
    @FXML
    private Label statusLabel;
    @FXML
    private Circle circleConCambios;
    @FXML
    private Circle circlePorSubir;
    @FXML
    private CustomTextField filteringField;
    // Variables
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Dialogos dialogs = new Dialogos();
    private final Path root = Paths.get("E:\\Users\\B187926\\Documents\\sitsql");
    private Proyecto proyecto;
    private ObservableList<Procedimiento> sps_data;
    private FilteredList<Procedimiento> sps_filtered;
    private ObservableList<Tabla> tbs_data;
    private FilteredList<Tabla> tbs_filtered;
    private Clipboard clipboard;
    private Config config;
    private Git git;
    private Path recursosPath;

    //Genera una tarea genérica a ejecutar, sin cachar excepciones.
    private final Function<ProcessBuilder, Task<Void>> taskGenerator = (pb) -> new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            pb.start();
            return null;
        }
    };

    @FXML
    public void refrescar(ActionEvent event) {
        ProyectoUpdater task = new ProyectoUpdater();
        bindStatus(task);
        task.setOnSucceeded(event2 -> {
            if (task.getValue() == null) {
                return;
            }
            // SPS
            sps_data = FXCollections.observableArrayList(task.getValue().getProcedimientos());
            sps_filtered = new FilteredList<>(sps_data, p -> true);
            SortedList<Procedimiento> sps_sorted = new SortedList<>(sps_filtered);
            sps_sorted.comparatorProperty().bind(tabla_sps.comparatorProperty());
            tabla_sps.setItems(sps_sorted);
            // TBS
            tbs_data = FXCollections.observableArrayList(task.getValue().getTablas());
            tbs_filtered = new FilteredList<>(tbs_data, p -> true);
            SortedList<Tabla> tbs_sorted = new SortedList<>(tbs_filtered);
            tbs_sorted.comparatorProperty().bind(tabla_tbs.comparatorProperty());
            tabla_tbs.setItems(tbs_sorted);
        });
        executor.execute(task);
    }

    @FXML
    private void agregarProcedimiento(ActionEvent event) {
        ProcedimientoForm dialog = new ProcedimientoForm(null);
        Optional<Recurso.Datos> r = dialog.showAndWait();
        r.ifPresent(d -> {
            if (sps_data.stream().anyMatch(p -> p.getSchema().equals(d.getEsquema()) && p.getNombre().equals(d.getNombre()))) {
                Dialogos.message("Ya existe el procedimiento en ese esquema y con ese nombre");
                return;
            }
            RecursosUpdater task = new RecursosUpdater(d);
            bindStatus(task);
            executor.execute(task);
        });
    }

    @FXML
    private void agregarTabla(ActionEvent event) {
        TablaForm dialog = new TablaForm(null);
        Optional<Recurso.Datos> r = dialog.showAndWait();
        r.ifPresent(d -> {
            if (tbs_data.stream().anyMatch(p -> p.getSchema().equals(d.getEsquema()) && p.getNombre().equals(d.getNombre()))) {
                Dialogos.message("Ya existe la tabla en ese esquema y con ese nombre");
                return;
            }
            RecursosUpdater task = new RecursosUpdater(d);
            bindStatus(task);
            executor.execute(task);
        });
    }

    @FXML
    private void filtrar(ActionEvent event) {
        filteringField.requestFocus();
        filteringField.selectAll();
    }

    @FXML
    private void clearFiltro(ActionEvent event) {
        filteringField.setText("");
    }

    @FXML
    private void abrirProcedimiento(ActionEvent event) {
        List<Path> paths = tabla_sps.getSelectionModel().getSelectedItems().stream().map(sp -> sp.getPath(git.getPath())).filter(path -> Files.exists(path)).collect(Collectors.toList());
        if (paths.isEmpty()) {
            Dialogos.message("No se encontr\u00f3 archivo alguno");
            return;
        }
        paths.stream().map(path -> new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (!config.getEditor().isEmpty()) {
                    new ProcessBuilder(config.getEditor(), path.toString()).start();
                } else if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(path.toFile());
                }
                return null;
            }
        }).forEach(executor::execute);
    }

    @FXML
    private void copiarProcedimiento(ActionEvent event) {
        if (event.getSource() == menuitem_spcopymp) {
            StringBuilder sb = new StringBuilder();
            tabla_sps.getSelectionModel().getSelectedItems().stream().map(sp -> sp.getMap()).forEach((str) -> {
                sb.append(str).append("\r\n");
            });
            setClipBoard(sb.toString().trim());
        } else if (event.getSource() == menuitem_spcopyfl) {
            List<File> list = tabla_sps.getSelectionModel().getSelectedItems().stream()
                    .map(sp -> sp.getPath(git.getPath()))
                    .filter(path -> Files.exists(path))
                    .map(path -> path.toFile())
                    .collect(Collectors.toList());
            setClipBoard(list);
        }
    }

    @FXML
    private void copiarRecurso(ActionEvent event) {
        StringBuilder sb = new StringBuilder();
        if (tabs.getSelectionModel().getSelectedIndex() == 1) {
            tabla_tbs.getSelectionModel().getSelectedItems().stream().map(tb -> tb.getNombre()).forEach((str) -> {
                sb.append(str).append("\r\n");
            });
        } else {
            tabla_sps.getSelectionModel().getSelectedItems().stream().map(sp -> sp.getNombre()).forEach((str) -> {
                sb.append(str).append("\r\n");
            });
        }
        setClipBoard(sb.toString().trim());
    }

    @FXML
    private void editarProcedimiento(ActionEvent event) {
        if (tabla_sps.getSelectionModel().getSelectedItems().isEmpty()) {
            dialogs.alert("Elija uno o m\u00e1s elementos");
            return;
        }
        tabla_sps.getSelectionModel().getSelectedItems().stream().map((sp) -> new ProcedimientoForm(sp)).map((dialog) -> dialog.showAndWait()).forEachOrdered((r) -> {
            r.ifPresent(d -> {
                RecursosUpdater task = new RecursosUpdater(d);
                bindStatus(task);
                executor.execute(task);
            });
        });
    }

    @FXML
    private void editarTabla(ActionEvent event) {
        if (tabla_tbs.getSelectionModel().getSelectedItems().isEmpty()) {
            dialogs.alert("Elija uno o m\u00e1s elementos");
            return;
        }
        tabla_tbs.getSelectionModel().getSelectedItems().stream().map((sp) -> new TablaForm(sp)).map((dialog) -> dialog.showAndWait()).forEachOrdered((r) -> {
            r.ifPresent(d -> {
                RecursosUpdater task = new RecursosUpdater(d);
                bindStatus(task);
                executor.execute(task);
            });
        });
    }

    @FXML
    private void quitarElementos(ActionEvent event) {
        List<Recurso> list = new ArrayList<>();
        // Quitar procedimientos
        if (tabs.getSelectionModel().getSelectedIndex() == 0) {
            list.addAll(tabla_sps.getSelectionModel().getSelectedItems());
        }
        // Quitar tablas
        if (tabs.getSelectionModel().getSelectedIndex() == 1) {
            list.addAll(tabla_tbs.getSelectionModel().getSelectedItems());
        }
        if (!Dialogos.confirm("\u00bfBorrar archivos y registros seleccionados?")) {
            return;
        }
        try (Connection conn = proyecto.getDataSource().getConnection()) {
            for (Recurso recurso : list) {
                PreparedStatement st = conn.prepareStatement(Recurso.sqlDelete());
                st.setString(1, recurso.getNombre());
                int n = st.executeUpdate();
                if (n == 1) {
                    statusconn_lb.setText("Borrado: " + recurso.getNombre());
                    if (recurso instanceof Procedimiento) {
                        Files.deleteIfExists(((Procedimiento) recurso).getPath(proyecto.getRepoPath()));
                    }
                } else {
                    statusconn_lb.setText("No se encontr\u00f3: " + recurso.getNombre());
                }
            }
            cargarProyecto();
        } catch (SQLException ex) {
            log.log(Level.WARNING, "No se pudo borrar registro", ex);
            statusconn_lb.setText("No fue posible borrarlo: " + ex.getMessage());
        } catch (IOException ex) {
            log.log(Level.WARNING, "Error al borrar", ex);
            statusconn_lb.setText("Error al borrar : " + ex.getMessage());
        }
    }

    // P A R A S Q L
    @FXML
    private void compararSP(ActionEvent event) {
        for (Procedimiento sp : tabla_sps.getSelectionModel().getSelectedItems()) {
            Path local = proyecto.getRepoPath().resolve(sp.getNombre() + ".sql");
            if (!Files.exists(local)) {
                Dialogos.message("No hay versi\u00f3n local de " + sp.getNombre());
                continue;
            }
            try (Connection conn = proyecto.getDataSource().getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT OBJECT_NAME(OBJECT_ID) sp, definition FROM sys.sql_modules WHERE OBJECT_NAME(OBJECT_ID)=?");
                ps.setString(1, sp.getNombre());
                File tmp = File.createTempFile(sp.getNombre() + "_", ".sql");
                tmp.deleteOnExit();
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String def = rs.getString("definition");
                    try (PrintWriter out = new PrintWriter(tmp)) {
                        out.print(def);
                    }
                } else {
                    Dialogos.message("No existe el procedimiento " + sp.getNombre() + " en el servidor");
                    continue;
                }
                Winmerge.compare(local.toAbsolutePath().toString(), "Versi\u00f3n del objeto local",
                        tmp.getAbsolutePath(), "Versi\u00f3n del objeto en DB");
            } catch (SQLException | IOException | NullPointerException ex) {
                dialogs.alert("Error al comparar procedimiento: " + ex.toString());
                log.log(Level.SEVERE, "Error al obtener o escribir procedimiento", ex);
            }
        }
    }

    @FXML
    private void guardarDesdeServidor(ActionEvent event) {
        tabla_sps.getSelectionModel().getSelectedItems().forEach(this::guardarDesdeServidor);
    }

    private void guardarDesdeServidor(Procedimiento sp) {
        Path local = sp.getPath(git.getPath());
        if (Files.exists(local) && !Dialogos.confirm("\u00bfSobreescribir " + local.getFileName() + "?")) {
            return;
        }
        try (Connection conn = proyecto.getDataSource().getConnection()) {
            try {
                local = local.toRealPath();
            } catch (NoSuchFileException ex) {
                log.log(Level.INFO, "El archivo es nuevo");
            }
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT OBJECT_NAME(OBJECT_ID) sp, definition FROM sys.sql_modules WHERE OBJECT_NAME(OBJECT_ID)=?");
            ps.setString(1, sp.getNombre());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String def = rs.getString("definition");
                try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(local.toFile()),
                        StandardCharsets.UTF_8)) {
                    writer.write(def);
                }
                statusconn_lb.setText("Guardada versi\u00f3n de " + sp.getNombre() + " de SqlServer");
            } else {
                Dialogos.message("No existe el procedimiento en el servidor.");
            }
        } catch (SQLException ex) {
            Dialogos.message("No fue posible obtener procedimiento: " + ex.getMessage());
            log.log(Level.SEVERE,
                    "Error al obtener o escribir procedimiento", ex);
        } catch (IOException ex) {
            Dialogos.message("No fue posible escribir archivo: " + ex.getMessage());
            log.log(Level.SEVERE, "Error al escribir procedimiento", ex);
        }
    }

    @FXML
    private void dependenciasSql(ActionEvent event) {
        if (tabs.getSelectionModel().getSelectedIndex() == 0) {
            dependenciasSql(tabla_sps.getSelectionModel().getSelectedItem());
        } else {
            dependenciasSql(tabla_tbs.getSelectionModel().getSelectedItem());
        }
    }

    private void dependenciasSql(Recurso r) {
        if (r == null) {
            Dialogos.message("Elija un elemento para consultar sus dependencias");
            return;
        }
        try (Connection conn = proyecto.getDataSource().getConnection()) {
            CallableStatement cs = conn.prepareCall("sp_depends " + r.getNombre());
            boolean continuar = cs.execute();
            Set<String> deps_obj = new HashSet<>();
            Set<String> deps_tab = new HashSet<>();
            while (continuar) {
                ResultSet rs = cs.getResultSet();
                ResultSetMetaData rsmd = rs.getMetaData();
                boolean cinco = rsmd.getColumnCount() == 5;
                while (rs.next()) {
                    String dep = rs.getString("name").replaceAll("dbo\\.", "");
                    if (cinco) {
                        deps_tab.add(dep);
                    } else {
                        deps_obj.add(dep);
                    }
                }
                continuar = cs.getMoreResults();
            }
            if (deps_obj.isEmpty() && deps_tab.isEmpty()) {
                Dialogos.message("Sin dependencias");
            } else {
                sps_filtered.setPredicate(sp -> {
                    return deps_obj.contains(sp.getNombre());
                });
                tbs_filtered.setPredicate(tb -> {
                    return deps_tab.contains(tb.getNombre());
                });
            }
        } catch (SQLException ex) {
            dialogs.alert("Error al obtener dependencias desde el servidor: " + ex.toString());
            log.log(Level.SEVERE,
                    "Error al obtener o escribir procedimiento", ex);
        }
    }

    // L O C A L
    @FXML
    private void crearArchivoParaSP(ActionEvent event) {
        Predicate<Procedimiento> notExists = p -> {
            if (Files.exists(p.getPath(git.getPath()))) {
                Dialogos.message("Ya existe " + p.getNombre() + ".sql");
                return false;
            }
            return true;
        };
        tabla_sps.getSelectionModel().getSelectedItems().stream()
                .filter(notExists)
                .map(sp -> new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Creando archivo local");
                String str = "/*************************************************************\r\n"
                        + "Proyecto:    " + config.getProyecto() + "\r\n"
                        + "Descripci\u00f3n: " + sp.getDescripcion() + "\r\n"
                        + "Entrada:     \r\n"
                        + "Salida:      \r\n"
                        + "Creador:     " + config.getDesarrollador() + " " + LocalDate.now() + "\r\n"
                        + "*************************************************************/\r\n";
                str += sp.getNombre().startsWith("F")
                        ? "CREATE FUNCTION [" + sp.getSchema() + "].[" + sp.getNombre() + "](\r\n) RETURNS SOMETHING AS\r\nBEGIN\r\n\r\n\r\nRETURN\r\n\r\nEND"
                        : "CREATE PROCEDURE [" + sp.getSchema() + "].[" + sp.getNombre() + "] (\r\n) AS\r\nBEGIN\r\n\r\n\r\nEND";
                Files.write(sp.getPath(git.getPath()), str.getBytes(Charset.forName("utf-8")));
                updateMessage("");
                return null;
            }
        }).map(this::bindStatus).forEach(executor::execute);
    }

    @FXML
    private void abrirUbicacion() {
        tabla_sps.getSelectionModel().getSelectedItems().stream()
                .map(sp -> sp.getPath(git.getPath()))
                .filter(path -> Files.exists(path))
                .map(path -> new ProcessBuilder("explorer.exe", "/select," + path))
                .map(taskGenerator)
                .forEach(executor::execute);
    }

    // C L E A R C A S E
    @FXML
    private void abrirUbicacion2() {
        Procedimiento sp = tabla_sps.getSelectionModel().getSelectedItem();
        Path path = sp.getPath(proyecto.getClearCasePath());
        System.out.println(path);
        try {
            new ProcessBuilder("explorer.exe", "/select," + path).start();
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void copiarToClearCase() {
        for (Procedimiento r : tabla_sps.getSelectionModel().getSelectedItems()) {
            Path ccd = r.getPath(proyecto.getClearCasePath());
            try {
                Path rep = r.getPath(proyecto.getRepoPath()).toRealPath();
                if (Files.exists(ccd)) {
                    ccd = ccd.toRealPath();
                    if (!Dialogos.confirm(
                            "\u00bfReemplazar versi\u00f3n de ClearCase con la versi\u00f3n del directorio local?",
                            r.getNombre())) {
                        return;
                    }
                }
                Files.copy(rep, ccd, StandardCopyOption.REPLACE_EXISTING);
                statusconn_lb.setText("Copiado " + r.getNombre() + " a clearcase");
            } catch (NoSuchFileException ex) {
                statusconn_lb.setText("No existe " + ex.getMessage());
            } catch (AccessDeniedException ex) {
                if (Dialogos.confirm("Archivo " + r.getNombre() + " bloqueado, \u00bfRealizar checkout?")) {
                    ProcessBuilder builder = new ProcessBuilder("cleartool", "checkout", "-ncomment", "-query",
                            ccd.getFileName().toString());
                    builder.directory(proyecto.getClearCasePath().toFile());
                    ProcessReader reader = new ProcessReader(builder);
                    statusconn_lb.textProperty().bind(reader.messageProperty());
                    reader.setOnSucceeded(event -> statusconn_lb.textProperty().unbind());
                    new Thread(reader).start();
                }
            } catch (IOException ex) {
                Dialogos.message("Error al copiar archivo: " + ex.toString());
                log.log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    private void copiarDesdeClearCase() {
        for (Procedimiento r : tabla_sps.getSelectionModel().getSelectedItems()) {
            try {
                Path rep = r.getPath(proyecto.getRepoPath());
                Path ccd = r.getPath(proyecto.getClearCasePath()).toRealPath();
                if (Files.exists(rep)) {
                    if (!Dialogos.confirm(
                            "\u00bfReemplazar versi\u00f3n local con la versi\u00f3n del directorio ClearCase?",
                            r.getNombre())) {
                        return;
                    }
                }
                Files.copy(ccd, rep, StandardCopyOption.REPLACE_EXISTING);
                statusconn_lb.setText("Copiado " + r.getNombre() + " al repo");
            } catch (NoSuchFileException ex) {
                statusconn_lb.setText("No existe " + ex.getMessage());
            } catch (IOException ex) {
                Dialogos.message("Error al copiar archivo: " + ex.toString());
                log.log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    private void compararSPCC(ActionEvent event) {
        for (Procedimiento sp : tabla_sps.getSelectionModel().getSelectedItems()) {
            Path local = sp.getPath(proyecto.getRepoPath());
            Path cc = sp.getPath(proyecto.getClearCasePath());
            if (!Files.exists(local)) {
                Dialogos.message("No hay versi\u00f3n local de " + sp.getNombre());
                continue;
            }
            if (!Files.exists(cc)) {
                Dialogos.message("No hay versi\u00f3n clear case de " + sp.getNombre());
                continue;
            }
            try {
                Winmerge.compare(local.toRealPath().toString(), "Versi\u00f3n del objeto local",
                        cc.toRealPath().toString(), "Versi\u00f3n ClearCase");
            } catch (IOException ex) {
                dialogs.alert("Error al comparar procedimiento: " + ex.toString());
                log.log(Level.SEVERE, "Error al comparar versiones", ex);
            }
        }
    }

    @FXML
    private void toggleConCambios(ActionEvent event) {
        Color color = Color.rgb(0, 0, 0, 0);
        if (circleConCambios.getFill().equals(color)) {
            circleConCambios.setFill(Color.rgb(52, 124, 168));
        } else {
            circleConCambios.setFill(color);
        }
    }

    @FXML
    private void togglePorSubir(ActionEvent event) {
        Color color = Color.rgb(0, 0, 0, 0);
        if (circlePorSubir.getFill().equals(color)) {
            circlePorSubir.setFill(Color.rgb(217, 130, 30));
        } else {
            circlePorSubir.setFill(color);
        }
    }

    @FXML
    private void ayuda() {
        Dialogos.message("Desarrollado por B187926\n\nDudas y comentarios a:\nvdiaz@elektra.com.mx\nvjdv@outlook.com",
                "Ayuda");
    }

    private void setClipBoard(String text) {
        if (clipboard == null) {
            clipboard = Clipboard.getSystemClipboard();
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    private void setClipBoard(List<File> files) {
        if (files.isEmpty()) {
            Dialogos.message("No se encontr\u00f3 archivo alguno");
            return;
        }
        if (clipboard == null) {
            clipboard = Clipboard.getSystemClipboard();
        }
        ClipboardContent content = new ClipboardContent();
        content.putFiles(files);
        clipboard.setContent(content);
    }

    public void filtrarProcedimientos(Predicate<Procedimiento> p) {
        sps_filtered.setPredicate(p);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Tables config
        colSpNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colSpMap.setCellValueFactory(cellData -> cellData.getValue().mapProperty());
        colSpDesc.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
        colTbNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colTbDesc.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
        tabla_sps.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tabla_tbs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // Filtering
        filteringField.textProperty().addListener((observable, oldValue, newValue) -> {
            String newValue2 = Normalizer.normalize(newValue, Normalizer.Form.NFD)
                    .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "").toLowerCase();
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
        // Drag&Drop archivos
        tabla_sps.setOnDragDetected((MouseEvent event) -> {
            if (tabla_sps.getSelectionModel().getSelectedItems().isEmpty()) {
                event.consume();
                return;
            }
            ClipboardContent filesToCopyClipboard = new ClipboardContent();
            List<File> files = new ArrayList<>();
            Dragboard db = tabla_sps.startDragAndDrop(TransferMode.ANY);
            tabla_sps.getSelectionModel().getSelectedItems().forEach((r) -> {
                try {
                    Path p = r.getPath(proyecto.getRepoPath()).toRealPath();
                    files.add(p.toFile());
                } catch (IOException ex) {
                    statusconn_lb.setText("No fue posible leer archivo " + r.getNombre() + ".sql: " + ex.getMessage());
                }
            });
            filesToCopyClipboard.putFiles(files);
            db.setContent(filesToCopyClipboard);
            event.consume();
        });
        tabla_sps.setOnDragDone((DragEvent event) -> {
            event.consume();
        });
        //cargarProyecto();
        ConfigReader configReader = new ConfigReader();
        bindStatus(configReader);
        configReader.setOnSucceeded(e -> {
            Path newPath = Paths.get(config.getRepositorio());
            if (config.getRepositorio().isEmpty() || !Files.exists(newPath)) {
                inicializarRepositorio();
            } else {
                refrescar(null);
            }
        });
        executor.execute(configReader);
    }

    private void cargarProyecto() {
        // TODO: eliminar referencias
    }

    private void inicializarRepositorio() {
        RepoInitializer dialog = new RepoInitializer(root.toFile());
        Optional<RepoInitializer.Datos> result = dialog.showAndWait();
        result.ifPresent(datos -> {
            if (datos == null || datos.getPaso() == 0) {
                Platform.exit();
            }
            ProyectoInitializer task = new ProyectoInitializer(datos);
            bindStatus(task);
            task.setOnSucceeded(evt -> refrescar(null));
            executor.execute(task);
        });
    }

    private Task<?> bindStatus(Task<?> task) {
        task.messageProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.startsWith("Error: ")) {
                Alert dialog = new Alert(Alert.AlertType.ERROR);
                dialog.setContentText(newValue.substring(7));
                dialog.showAndWait();
                Platform.exit();
            } else if (newValue.startsWith("Alerta: ")) {
                Alert dialog = new Alert(Alert.AlertType.WARNING);
                dialog.setContentText(newValue.substring(8));
                dialog.show();
            } else {
                statusLabel.setText(newValue);
            }
        });
        return task;
    }

    public void shutdown() {
        executor.shutdown();
    }

    class ConfigReader extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            try {
                updateMessage("Abriendo configuración");
                config = Config.open(root);
                git = new Git(config.getRepositorio());
                recursosPath = Paths.get(config.getRepositorio()).resolve("recursos.xml");
                updateMessage("");
            } catch (JAXBException | FileNotFoundException ex) {
                if (ex instanceof JAXBException) {
                    log.warning(ex.getMessage());
                    updateMessage("Alerta: El archivo de configuración es inválido, se creará uno nuevo");
                }
                updateMessage("Creando configuración");
                log.info("Creando nuevo archivo config.xml");
                config = new Config();
                config.setFile(root.resolve("config.xml").toFile());
                updateMessage(config.save() ? "" : "Error: No se pudo crear el archivo de configuración, verifique los permisos y reintente.");
            }
            return null;
        }
    }

    class ProyectoInitializer extends Task<Void> {

        private final RepoInitializer.Datos datosRepo;

        public ProyectoInitializer(RepoInitializer.Datos datosRepo) {
            this.datosRepo = datosRepo;
        }

        @Override
        protected Void call() throws Exception {
            int paso = datosRepo.getPaso();
            git = new Git(datosRepo.getCarpeta());
            try {
                if (paso == 1) {
                    updateMessage("Iniciando repositorio");
                    git.init();
                    if (!datosRepo.getUrl().isEmpty()) {
                        updateMessage("Agregando origen");
                        git.addOrigin(datosRepo.getUrl());
                    }
                    updateMessage("Guardando config");
                    config.setRepositorio(datosRepo.getCarpeta());
                    config.save();
                    updateMessage("Agregando archivos base");
                    Recursos r = new Recursos();
                    r.save(Paths.get(config.getRepositorio()).resolve("recursos.xml"));
                    git.addAndCommit("recursos.xml", "inicio proyecto");
                    if (!datosRepo.getUrl().isEmpty()) {
                        updateMessage("Subiendo a origen");
                        git.push();
                    }
                    updateMessage("");
                }
                if (paso == 2) {
                    updateMessage("Clonando repositorio");
                    git.clone(datosRepo.getUrl());
                    updateMessage("");
                }
                if (paso == 2 || paso == 3) {
                    if (!Files.exists(Paths.get(config.getRepositorio()).resolve("recursos.xml"))) {
                        throw new FileNotFoundException("No existe el archivo recursos.xml");
                    }
                }
                recursosPath = Paths.get(config.getRepositorio()).resolve("recursos.xml");
            } catch (FileNotFoundException | GitException ex) {
                updateMessage("Error: " + ex.getMessage());
                log.log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }

    class ProyectoUpdater extends Task<Recursos> {

        @Override
        protected Recursos call() {
            try {
                updateMessage("Actualizando repositorio");
                try {
                    git.pull();
                } catch (IOException ex) {
                    log.log(Level.WARNING, null, ex);
                    updateMessage("Alerta: No fue posible hacer pull: " + ex.getMessage());
                }
                updateMessage("Abriendo recursos.xml");
                Recursos r = Recursos.open(recursosPath);
                updateMessage("Comprobando cambios");
                String[] objs = git.status();
                for (String obj : objs) {
                    if (!obj.contains("/")) {
                        continue;
                    }
                    String[] parts = obj.split("/");
                    String schema = parts[0];
                    String objname = parts[1].substring(parts[1].indexOf("."));
                    Optional<Procedimiento> rp = r.getProcedimientos().stream().filter(p -> p.getSchema().equals(schema) && p.getNombre().equals(objname)).findAny();
                    if (rp.isPresent()) {
                        rp.get().setConCambios(true);
                    } else {
                        Procedimiento sp = new Procedimiento();
                        sp.setSchema(schema);
                        sp.setNombre(objname);
                        sp.setConCambios(true);
                    }
                }
                config.getPorSubir().forEach(obj -> {
                    String[] parts = obj.split(".");
                    String schema = parts[0];
                    String objname = parts[1];
                    Optional<Procedimiento> rp = r.getProcedimientos().stream().filter(p -> p.getSchema().equals(schema) && p.getNombre().equals(objname)).findAny();
                    if (rp.isPresent()) {
                        rp.get().setPendienteSubir(true);
                    } else {
                        Procedimiento sp = new Procedimiento();
                        sp.setSchema(schema);
                        sp.setNombre(objname);
                        sp.setPendienteSubir(true);
                    }
                });
                updateMessage("");
                return r;
            } catch (IOException | JAXBException ex) {
                updateMessage("Error: Inesperado: " + ex.getMessage());
                log.log(Level.SEVERE, "Inesperado", ex);
                return null;
            }
        }
    }

    class RecursosUpdater extends Task<Boolean> {

        private final Recurso.Datos datos;

        public RecursosUpdater(Recurso.Datos datos) {
            this.datos = datos;
        }

        @Override
        protected Boolean call() {
            try {
                updateMessage("Verificando cambios");
                try {
                    git.pull();
                } catch (IOException ex) {
                    log.log(Level.WARNING, null, ex);
                    updateMessage("Alerta: No fue posible hacer pull: " + ex.getMessage());
                }
                updateMessage("Abriendo recursos.xml");
                Recursos r = Recursos.open(recursosPath);
                updateMessage("Guardando cambios");
                if (datos.getTipo().equals(Recurso.PROCEDIMIENTO)) {
                    Optional<Procedimiento> osp = r.getProcedimientos().stream().filter(p -> p.getSchema().equals(datos.getEsquema()) && p.getNombre().equals(datos.getNombre())).findAny();
                    if (osp.isPresent()) {
                        Procedimiento sp = osp.get();
                        sp.setMap(datos.getMapeo());
                        sp.setDescripcion(datos.getDescripcion());
                    } else {
                        Procedimiento sp = new Procedimiento();
                        sp.setNombre(datos.getNombre());
                        sp.setSchema(datos.getEsquema());
                        sp.setMap(datos.getMapeo());
                        sp.setDescripcion(datos.getDescripcion());
                        r.getProcedimientos().add(sp);
                    }
                }
                if (datos.getTipo().equals(Recurso.TABLA)) {
                    Optional<Tabla> osp = r.getTablas().stream().filter(p -> p.getSchema().equals(datos.getEsquema()) && p.getNombre().equals(datos.getNombre())).findAny();
                    if (osp.isPresent()) {
                        Tabla tb = osp.get();
                        tb.setDescripcion(datos.getDescripcion());
                    } else {
                        Tabla tb = new Tabla();
                        tb.setNombre(datos.getNombre());
                        tb.setSchema(datos.getEsquema());
                        tb.setDescripcion(datos.getDescripcion());
                        r.getTablas().add(tb);
                    }
                }
                r.save(recursosPath);
                git.addAndCommit("recursos.xml", "recurso agregado o modificado");
                updateMessage("Subiendo cambios");
                git.push();
                updateMessage("");
                refrescar(null);
                return true;
            } catch (IOException | JAXBException | GitException ex) {
                updateMessage("Error: Inesperado: " + ex.getMessage());
                log.log(Level.SEVERE, "Inesperado", ex);
                return false;
            }
        }
    }

    class ProcessReader extends Task<Void> {

        private final ProcessBuilder builder;

        public ProcessReader(ProcessBuilder builder) {
            this.builder = builder;
        }

        @Override
        protected Void call() {
            try {
                Process p = builder.start();
                try (Scanner s = new Scanner(p.getInputStream(), "ISO-8859-1")) {
                    while (s.hasNextLine()) {
                        String line = s.nextLine();
                        if (!line.trim().isEmpty()) {
                            System.out.println("Salida: " + line);
                            updateMessage("Salida: " + line);
                        }
                    }
                }
                try (Scanner s = new Scanner(p.getErrorStream(), "ISO-8859-1")) {
                    while (s.hasNextLine()) {
                        String line = s.nextLine();
                        if (!line.trim().isEmpty()) {
                            System.out.println("Salida: " + line);
                            updateMessage("Error: " + line);
                        }
                    }
                }
            } catch (IOException ex) {
                updateMessage("Error al ejecutar: " + ex.getMessage());
                log.log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }

}
