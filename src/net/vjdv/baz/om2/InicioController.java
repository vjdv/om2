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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.xml.bind.JAXBException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
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
import javafx.util.Callback;
import lombok.extern.java.Log;
import net.vjdv.baz.exceptions.GitException;
import net.vjdv.baz.om2.dialogs.CommitForm;
import net.vjdv.baz.om2.dialogs.ConexionForm;
import net.vjdv.baz.om2.dialogs.ConfigForm;
import net.vjdv.baz.om2.dialogs.GitHistory;
import net.vjdv.baz.om2.dialogs.HelpDialog;
import net.vjdv.baz.om2.dialogs.ProcedimientoForm;
import net.vjdv.baz.om2.dialogs.RepoInitializer;
import net.vjdv.baz.om2.dialogs.SnippetForm;
import net.vjdv.baz.om2.dialogs.TablaForm;
import net.vjdv.baz.om2.models.ConexionDB;
import net.vjdv.baz.om2.models.Config;
import net.vjdv.baz.om2.models.Dialogos;
import net.vjdv.baz.om2.models.Git;
import net.vjdv.baz.om2.models.Procedimiento;
import net.vjdv.baz.om2.models.Proyecto;
import net.vjdv.baz.om2.models.Recurso;
import net.vjdv.baz.om2.models.Recursos;
import net.vjdv.baz.om2.models.Snippet;
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
  private TableColumn<Recurso, List<Circle>> colSpMarcas, colTbMarcas, colSnMarcas;
  @FXML
  private TableView<Tabla> tabla_tbs;
  @FXML
  private TableColumn<Tabla, String> colTbNombre, colTbDesc;
  @FXML
  private TableView<Snippet> tabla_snp;
  @FXML
  private TableColumn<Snippet, String> colSnNombre, colSnDesc;
  @FXML
  private Label statusLabel;
  @FXML
  private Circle circleConCambios, circlePorCorregir, circlePorSubir, circleSinArchivo;
  @FXML
  private CustomTextField filteringField;
  @FXML
  private ComboBox<ConexionDB> comboDB;
  // Variables
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final Dialogos dialogs = new Dialogos();
  private final Path root = Paths.get("");
  private final BooleanProperty filtroConCambios = new SimpleBooleanProperty(false);
  private final BooleanProperty filtroPorSubir = new SimpleBooleanProperty(false);
  private final BooleanProperty filtroPorCorregir = new SimpleBooleanProperty(false);
  private final BooleanProperty filtroSinArchivo = new SimpleBooleanProperty(false);
  private final StringProperty titulo = new SimpleStringProperty("OM2");
  private Proyecto proyecto;
  private ObservableList<Procedimiento> sps_data;
  private FilteredList<Procedimiento> sps_filtered_marks;
  private FilteredList<Procedimiento> sps_filtered;
  private ObservableList<Tabla> tbs_data;
  private FilteredList<Tabla> tbs_filtered_marks;
  private FilteredList<Tabla> tbs_filtered;
  private ObservableList<Snippet> snps_data;
  private FilteredList<Snippet> snps_filtered_marks;
  private FilteredList<Snippet> snps_filtered;
  private Clipboard clipboard;
  private Config config;
  private Git git;
  private Path recursosPath;
  private Winmerge winmerge;

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
      sps_filtered_marks = new FilteredList<>(sps_data, p -> true);
      sps_filtered = new FilteredList<>(sps_filtered_marks, p -> true);
      SortedList<Procedimiento> sps_sorted = new SortedList<>(sps_filtered);
      sps_sorted.comparatorProperty().bind(tabla_sps.comparatorProperty());
      tabla_sps.setItems(sps_sorted);
      // TBS
      tbs_data = FXCollections.observableArrayList(task.getValue().getTablas());
      tbs_filtered_marks = new FilteredList<>(tbs_data, p -> true);
      tbs_filtered = new FilteredList<>(tbs_filtered_marks, p -> true);
      SortedList<Tabla> tbs_sorted = new SortedList<>(tbs_filtered);
      tbs_sorted.comparatorProperty().bind(tabla_tbs.comparatorProperty());
      tabla_tbs.setItems(tbs_sorted);
      // SNPS
      snps_data = FXCollections.observableArrayList(task.getValue().getSnippets());
      snps_filtered_marks = new FilteredList<>(snps_data, p -> true);
      snps_filtered = new FilteredList<>(snps_filtered_marks, p -> true);
      SortedList<Snippet> snps_sorted = new SortedList<>(snps_filtered);
      snps_sorted.comparatorProperty().bind(tabla_snp.comparatorProperty());
      tabla_snp.setItems(snps_sorted);
      //filtros reset
      filteringField.setText("");
      filtroConCambios.set(false);
      filtroPorCorregir.set(false);
      filtroPorSubir.set(false);
      filtroSinArchivo.set(false);
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
      actualizaRecurso(d);
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
      actualizaRecurso(d);
    });
  }

  @FXML
  private void agregarSnippet(ActionEvent event) {
    SnippetForm dialog = new SnippetForm(null);
    Optional<Recurso.Datos> r = dialog.showAndWait();
    r.ifPresent(d -> {
      if (snps_data.stream().anyMatch(sn -> sn.getNombre().equals(d.getNombre()))) {
        Dialogos.message("Ya existe el snippet con ese nombre de archivo");
        return;
      }
      actualizaRecurso(d);
    });
  }

  @FXML
  private void agregarConexion(ActionEvent event) {
    ConexionForm dialog = new ConexionForm(null);
    Optional<ConexionDB> r = dialog.showAndWait();
    r.ifPresent(c -> {
      if (config.getConns().stream().anyMatch(cx -> cx.equals(c))) {
        Dialogos.message("Ya una configuraci\u00f3n DB igual");
        return;
      }
      config.getConns().add(c);
      comboDB.getItems().clear();
      comboDB.getItems().addAll(config.getConns());
      comboDB.getSelectionModel().select(c);
      config.save();
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
  private void marcasPorSubir(ActionEvent event) {
    List<? extends Recurso> lista = getSelectedItems();
    if (lista.isEmpty()) {
      Dialogos.message("Elija uno o m\u00e1s elementos para agregar/quitar la marca");
    }
    MarcasUpdater task = new MarcasUpdater(lista, "PORSUBIR");
    bindStatus(task);
    executor.execute(task);
  }

  @FXML
  private void marcasPorCorregir(ActionEvent event) {
    List<? extends Recurso> lista = getSelectedItems();
    if (lista.isEmpty()) {
      Dialogos.message("Elija uno o m\u00e1s elementos para agregar/quitar la marca");
    }
    MarcasUpdater task = new MarcasUpdater(lista, "PORCORREGIR");
    bindStatus(task);
    executor.execute(task);
  }

  @FXML
  private void abrirArchivo(ActionEvent event) {
    List<? extends Recurso> lista = getSelectedItems();
    List<Path> paths = lista.stream().map(r -> r.getPath(git.getPath())).filter(path -> Files.exists(path)).collect(Collectors.toList());
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
    getSelectedItems().stream().map(r -> r.getNombre()).forEach(str -> sb.append(str).append("\r\n"));
    setClipBoard(sb.toString().trim());
  }

  @FXML
  private void editarProcedimiento(ActionEvent event) {
    if (tabla_sps.getSelectionModel().getSelectedItems().isEmpty()) {
      dialogs.alert("Elija uno o m\u00e1s elementos");
      return;
    }
    tabla_sps.getSelectionModel().getSelectedItems().stream().map((sp) -> new ProcedimientoForm(sp)).map((dialog) -> dialog.showAndWait()).forEachOrdered((r) -> {
      r.ifPresent(this::actualizaRecurso);
    });
  }

  @FXML
  private void editarTabla(ActionEvent event) {
    if (tabla_tbs.getSelectionModel().getSelectedItems().isEmpty()) {
      dialogs.alert("Elija uno o m\u00e1s elementos");
      return;
    }
    tabla_tbs.getSelectionModel().getSelectedItems().stream().map((sp) -> new TablaForm(sp)).map((dialog) -> dialog.showAndWait()).forEachOrdered((r) -> {
      r.ifPresent(this::actualizaRecurso);
    });
  }

  @FXML
  private void editarSnippet(ActionEvent event) {
    if (tabla_snp.getSelectionModel().getSelectedItems().isEmpty()) {
      dialogs.alert("Elija uno o m\u00e1s elementos");
      return;
    }
    tabla_snp.getSelectionModel().getSelectedItems().stream().map((sp) -> new SnippetForm(sp)).map((dialog) -> dialog.showAndWait()).sorted().forEachOrdered((r) -> {
      r.ifPresent(this::actualizaRecurso);
    });
  }

  @FXML
  private void quitarElementos(ActionEvent event) {
    List<? extends Recurso> lista = getSelectedItems();
    if (lista.isEmpty()) {
      Dialogos.message("No se seleccionaron elementos");
    } else if (Dialogos.confirm("\u00bfSeguro de borrar archivos y registros seleccionados?")) {
      RecursosDeleter task = new RecursosDeleter(lista);
      bindStatus(task);
      task.setOnSucceeded(evt -> refrescar(event));
      executor.execute(task);
    }
  }

  private List<? extends Recurso> getSelectedItems() {
    int index = tabs.getSelectionModel().getSelectedIndex();
    TableView<? extends Recurso> tabla = index == 0 ? tabla_sps : index == 1 ? tabla_tbs : tabla_snp;
    return tabla.getSelectionModel().getSelectedItems();
  }

  // P A R A S Q L
  @FXML
  private void compararSP(ActionEvent event) {
    ConexionDB cx = comboDB.getSelectionModel().getSelectedItem();
    if (cx == null) {
      Dialogos.message("No se han configurado conexiones DB");
      return;
    }
    if (winmerge == null) {
      Dialogos.message("No se ha configurado Winmerge");
      return;
    }
    for (Procedimiento sp : tabla_sps.getSelectionModel().getSelectedItems()) {
      Path local = sp.getPath(git.getPath());
      if (!Files.exists(local)) {
        Dialogos.message("No hay versi\u00f3n local de " + sp.getNombre());
        continue;
      }
      try (Connection conn = cx.getDataSource().getConnection()) {
        PreparedStatement ps = conn.prepareStatement("SELECT OBJECT_NAME(OBJECT_ID) sp, definition FROM sys.sql_modules WHERE OBJECT_NAME(OBJECT_ID)=?");
        ps.setString(1, sp.getNombre());
        File tmp = File.createTempFile(sp.getNombre(), ".sql");
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
        winmerge.compare(local.toAbsolutePath().toString(), "Versi\u00f3n del objeto local", tmp.getAbsolutePath(), "Versi\u00f3n del objeto en DB");
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
    ConexionDB cx = comboDB.getSelectionModel().getSelectedItem();
    if (cx == null) {
      Dialogos.message("No se han configurado conexiones DB");
      return;
    }
    Path local = sp.getPath(git.getPath());
    if (Files.exists(local) && !Dialogos.confirm("\u00bfSobreescribir " + local.getFileName() + "?")) {
      return;
    }
    try (Connection conn = cx.getDataSource().getConnection()) {
      try {
        local = local.toRealPath();
      } catch (NoSuchFileException ex) {
        log.log(Level.INFO, "El archivo es nuevo");
      }
      PreparedStatement ps = conn.prepareStatement("SELECT OBJECT_NAME(OBJECT_ID) sp, definition FROM sys.sql_modules WHERE OBJECT_NAME(OBJECT_ID)=?");
      ps.setString(1, sp.getNombre());
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        String def = rs.getString("definition");
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(local.toFile()), StandardCharsets.UTF_8)) {
          writer.write(def);
        }
        Dialogos.message("Guardada versi\u00f3n de " + sp.getNombre() + " desde " + cx);
      } else {
        Dialogos.message("No existe el procedimiento en el servidor.");
      }
    } catch (SQLException ex) {
      Dialogos.message("No fue posible obtener procedimiento: " + ex.getMessage());
      log.log(Level.SEVERE, "Error al obtener procedimiento", ex);
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

  @FXML
  private void configuracion(ActionEvent event) {
    ConfigForm dialog = new ConfigForm(config);
    dialog.showAndWait().ifPresent(c -> {
      if (!c.getWinmerge().isEmpty()) {
        winmerge = new Winmerge(c.getWinmerge());
      }
      if (!c.getProyecto().isEmpty()) {
        titulo.set(config.getProyecto());
      }
      c.save();
    });
  }

  @FXML
  private void gitlog(ActionEvent event) {
    git.openVisor();
  }

  @FXML
  private void githistory(ActionEvent event) {
    getSelectedItems().forEach(r -> {
      Path p = r.getPath(git.getPath());
      GitHistory stage = new GitHistory(p, git);
      stage.setWinmerge(winmerge);
      stage.show();
      Task<String> t = new Task<String>() {
        @Override
        protected String call() throws Exception {
          return git.getLog(p.toString());
        }
      };
      t.setOnSucceeded(evt2 -> stage.setResult(t.getValue()));
      executor.execute(t);
    });
  }

  // L O C A L
  @FXML
  private void crearArchivo(ActionEvent event) {
    Predicate<Recurso> notExists = p -> {
      if (Files.exists(p.getPath(git.getPath()))) {
        Dialogos.message("Ya existe " + p.getNombre() + ".sql");
        return false;
      }
      return true;
    };
    getSelectedItems().stream().filter(notExists).sorted().map(r -> new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        try {
          updateMessage("Creando archivo local");
          String str;
          if (r instanceof Procedimiento) {
            Procedimiento sp = (Procedimiento) r;
            str = "/*************************************************************\r\n"
                    + "Proyecto:    " + config.getProyecto() + "\r\n"
                    + "Descripci\u00f3n: " + sp.getDescripcion() + "\r\n"
                    + "Entrada:     \r\n"
                    + "Salida:      \r\n"
                    + "Creador:     " + config.getDesarrollador() + " " + LocalDate.now() + "\r\n"
                    + "*************************************************************/\r\n";
            str += sp.getNombre().startsWith("F")
                    ? "CREATE FUNCTION [" + sp.getSchema() + "].[" + sp.getNombre() + "](\r\n) RETURNS SOMETHING AS\r\nBEGIN\r\n\r\n\r\nRETURN\r\n\r\nEND"
                    : "CREATE PROCEDURE [" + sp.getSchema() + "].[" + sp.getNombre() + "] (\r\n) AS\r\nBEGIN\r\n\r\n\r\nEND";
          } else {
            str = "/*************************************************************\r\n"
                    + "Proyecto:    " + config.getProyecto() + "\r\n"
                    + "Descripci\u00f3n: " + r.getDescripcion() + "\r\n"
                    + "*************************************************************/\r\n\r\n";
          }
          Path path = r.getPath(git.getPath());
          path.getParent().toFile().mkdirs();
          Files.write(r.getPath(git.getPath()), str.getBytes(Charset.forName("utf-8")));
          updateMessage("");
        } catch (IOException ex) {
          log.log(Level.WARNING, "No se puedo crear archivo para recurso", ex);
          updateMessage("Alerta: No se puedo crear el archivo: " + ex.getMessage());
        }
        return null;
      }
    }).map(this::bindStatus).forEach(executor::execute);
    executor.execute(() -> refrescar(null));
  }

  @FXML
  private void abrirUbicacion() {
    getSelectedItems().stream()
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
    if (config.getClearcase().isEmpty()) {
      Dialogos.message("No est\u00e1 configurado el directorio de la vista de ClearCase");
      return;
    }
    for (Procedimiento r : tabla_sps.getSelectionModel().getSelectedItems()) {
      Path vista = Paths.get(config.getVistacc());
      Path ccd = vista.resolve(r.getNombre() + ".sql");
      try {
        Path rep = r.getPath(git.getPath());
        if (Files.exists(ccd)) {
          ccd = ccd.toRealPath();
          if (!Dialogos.confirm("\u00bfReemplazar versi\u00f3n de ClearCase con la versi\u00f3n del directorio local?", r.getNombre())) {
            return;
          }
        }
        Files.copy(rep, ccd, StandardCopyOption.REPLACE_EXISTING);
        statusLabel.setText("Copiado " + r.getNombre() + " a clearcase");
        executor.schedule(() -> statusLabel.setText(""), 3, TimeUnit.SECONDS);
      } catch (NoSuchFileException ex) {
        statusLabel.setText("No existe " + ex.getMessage());
        executor.schedule(() -> statusLabel.setText(""), 3, TimeUnit.SECONDS);
      } catch (AccessDeniedException ex) {
        if (Dialogos.confirm("Archivo " + r.getNombre() + " bloqueado, \u00bfRealizar checkout?")) {
          ProcessBuilder builder = new ProcessBuilder("cleartool", "checkout", "-ncomment", "-query", ccd.getFileName().toString());
          builder.directory(vista.toFile());
          ProcessReader reader = new ProcessReader(builder);
          bindStatus(reader);
          executor.execute(reader);
        }
      } catch (IOException ex) {
        Dialogos.message("Error al copiar archivo: " + ex.toString());
        log.log(Level.SEVERE, null, ex);
      }
    }
  }

  @FXML
  private void copiarDesdeClearCase() {
    if (config.getClearcase().isEmpty()) {
      Dialogos.message("No est\u00e1 configurado el directorio de la vista de ClearCase");
      return;
    }
    Path vista = Paths.get(config.getVistacc());
    for (Procedimiento r : tabla_sps.getSelectionModel().getSelectedItems()) {
      try {
        Path rep = r.getPath(git.getPath());
        Path ccd = vista.resolve(r.getNombre() + ".sql").toRealPath();
        if (Files.exists(rep)) {
          if (!Dialogos.confirm("\u00bfReemplazar versi\u00f3n local con la versi\u00f3n del directorio ClearCase?", r.getNombre())) {
            return;
          }
        }
        Files.copy(ccd, rep, StandardCopyOption.REPLACE_EXISTING);
        statusLabel.setText("Copiado " + r.getNombre() + " al repositorio");
        executor.schedule(() -> statusLabel.setText(""), 3, TimeUnit.SECONDS);
      } catch (NoSuchFileException ex) {
        Dialogos.message("No existe: " + ex.getMessage());
      } catch (IOException ex) {
        Dialogos.message("Error al copiar archivo: " + ex.getMessage());
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
        winmerge.compare(local.toRealPath().toString(), "Versi\u00f3n del objeto local", cc.toRealPath().toString(), "Versi\u00f3n ClearCase");
      } catch (IOException ex) {
        dialogs.alert("Error al comparar procedimiento: " + ex.toString());
        log.log(Level.SEVERE, "Error al comparar versiones", ex);
      }
    }
  }

  @FXML
  private void toggleFiltroMarca(ActionEvent event) {
    Hyperlink source = (Hyperlink) event.getSource();
    Node g = source.getGraphic();
    if (g == circleConCambios) {
      filtroConCambios.set(!filtroConCambios.get());
    } else if (g == circlePorCorregir) {
      filtroPorCorregir.set(!filtroPorCorregir.get());
    } else if (g == circlePorSubir) {
      filtroPorSubir.set(!filtroPorSubir.get());
    } else if (g == circleSinArchivo) {
      filtroSinArchivo.set(!filtroSinArchivo.get());
    }
  }

  @FXML
  private void compartir(ActionEvent event) {
    List<String> paths = getSelectedItems().stream().map(r -> r.getPath(git.getPath())).filter(p -> Files.exists(p)).map(p -> git.getPath().relativize(p).toString()).collect(Collectors.toList());
    if (paths.isEmpty()) {
      Dialogos.message("No se seleccionaron elementos o no se encontraron archivos");
    } else {
      new CommitForm().showAndWait().ifPresent(str -> {
        FileUpdater task = new FileUpdater(paths, str);
        bindStatus(task);
        task.setOnSucceeded(evt -> refrescar(event));
        executor.execute(task);
      });
    }
  }

  @FXML
  private void gitpull(ActionEvent event) {
    GitPull task = new GitPull();
    executor.execute(task);
  }

  @FXML
  private void gitpush(ActionEvent event) {
    GitPush task = new GitPush();
    executor.execute(task);
  }

  @FXML
  private void ayuda() {
    new HelpDialog().show();
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

  private void updatePredicate() {
    Predicate<Recurso> p = r -> (!filtroConCambios.get() && !filtroPorCorregir.get() && !filtroPorSubir.get() && !filtroSinArchivo.get())
            || ((filtroConCambios.get() && r.isConCambios())
            || (filtroPorCorregir.get() && r.isPorCorregir())
            || (filtroPorSubir.get() && r.isPendienteSubir())
            || (filtroSinArchivo.get() && r.isSinArchivo()));
    sps_filtered_marks.setPredicate(p);
    tbs_filtered_marks.setPredicate(p);
    snps_filtered_marks.setPredicate(p);
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    // Tables config
    Callback<TableColumn<Recurso, List<Circle>>, TableCell<Recurso, List<Circle>>> marcasFactory = param -> {
      final Group g = new Group();
      TableCell<Recurso, List<Circle>> cell = new TableCell<Recurso, List<Circle>>() {
        @Override
        public void updateItem(List<Circle> circles, boolean empty) {
          g.getChildren().clear();
          if (circles != null) {
            IntStream.range(0, circles.size()).forEach(i -> circles.get(i).setCenterX(17 * i + 2));
            g.getChildren().addAll(circles);
          }
        }
      };
      cell.setGraphic(g);
      return cell;
    };
    colSpMarcas.setCellFactory(marcasFactory);
    colSpMarcas.setCellValueFactory(cellData -> cellData.getValue().marcasProperty());
    colSpNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
    colSpMap.setCellValueFactory(cellData -> cellData.getValue().mapProperty());
    colSpDesc.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
    colTbMarcas.setCellFactory(marcasFactory);
    colTbMarcas.setCellValueFactory(cellData -> cellData.getValue().marcasProperty());
    colTbNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
    colTbDesc.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
    colSnMarcas.setCellFactory(marcasFactory);
    colSnMarcas.setCellValueFactory(cellData -> cellData.getValue().marcasProperty());
    colSnNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
    colSnDesc.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
    tabla_sps.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tabla_tbs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tabla_snp.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    // marcas circles
    filtroConCambios.addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        circleConCambios.setFill(Color.web("#347ca8"));
        circleConCambios.setStroke(Color.web("#333"));
        circleConCambios.setStrokeWidth(1);
      } else {
        circleConCambios.setFill(Color.TRANSPARENT);
        circleConCambios.setStroke(Color.web("#347ca8"));
        circleConCambios.setStrokeWidth(2);
      }
      updatePredicate();
    });
    filtroPorCorregir.addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        circlePorCorregir.setFill(Color.web("#b03131"));
        circlePorCorregir.setStroke(Color.web("#333"));
        circlePorCorregir.setStrokeWidth(1);
      } else {
        circlePorCorregir.setFill(Color.TRANSPARENT);
        circlePorCorregir.setStroke(Color.web("#b03131"));
        circlePorCorregir.setStrokeWidth(2);
      }
      updatePredicate();
    });
    filtroPorSubir.addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        circlePorSubir.setFill(Color.web("#d9821e"));
        circlePorSubir.setStroke(Color.web("#333"));
        circlePorSubir.setStrokeWidth(1);
      } else {
        circlePorSubir.setFill(Color.TRANSPARENT);
        circlePorSubir.setStroke(Color.web("#d9821e"));
        circlePorSubir.setStrokeWidth(2);
      }
      updatePredicate();
    });
    filtroSinArchivo.addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        circleSinArchivo.setFill(Color.web("#b5b5b5"));
        circleSinArchivo.setStroke(Color.web("#333"));
        circleSinArchivo.setStrokeWidth(1);
      } else {
        circleSinArchivo.setFill(Color.TRANSPARENT);
        circleSinArchivo.setStroke(Color.web("#b5b5b5"));
        circleSinArchivo.setStrokeWidth(2);
      }
      updatePredicate();
    });
    // Filtering
    Predicate<Recurso> alwaysTrue = r -> true;
    filteringField.textProperty().addListener((observable, oldValue, newValue) -> {
      String str = Normalizer.normalize(newValue, Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "").toLowerCase().trim();
      if (str.isEmpty()) {
        sps_filtered.setPredicate(alwaysTrue);
        tbs_filtered.setPredicate(alwaysTrue);
        snps_filtered.setPredicate(alwaysTrue);
      } else {
        Predicate<Recurso> searchPredicate = p -> p.getFilteringString().contains(str);
        sps_filtered.setPredicate(searchPredicate);
        tbs_filtered.setPredicate(searchPredicate);
        snps_filtered.setPredicate(searchPredicate);
      }
    });
    // Drag&Drop archivos
    EventHandler<MouseEvent> onDrag = event -> {
      if (getSelectedItems().isEmpty()) {
        event.consume();
        return;
      }
      ClipboardContent filesToCopyClipboard = new ClipboardContent();
      Dragboard db = ((TableView) event.getSource()).startDragAndDrop(TransferMode.ANY);
      List<File> files = getSelectedItems().stream().map(r -> r.getPath(git.getPath())).filter(p -> Files.exists(p)).map(p -> p.toFile()).collect(Collectors.toList());
      filesToCopyClipboard.putFiles(files);
      db.setContent(filesToCopyClipboard);
      event.consume();
    };
    EventHandler<DragEvent> onDone = event -> event.consume();
    tabla_sps.setOnDragDetected(onDrag);
    tabla_sps.setOnDragDone(onDone);
    tabla_tbs.setOnDragDetected(onDrag);
    tabla_tbs.setOnDragDone(onDone);
    tabla_snp.setOnDragDetected(onDrag);
    tabla_snp.setOnDragDone(onDone);
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

  private void inicializarRepositorio() {
    RepoInitializer dialog = new RepoInitializer(root.toAbsolutePath().toFile());
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
        if (!config.getConns().isEmpty()) {
          comboDB.getItems().addAll(config.getConns());
          comboDB.getSelectionModel().select(config.getConns().get(0));
        }
        git = new Git(config.getRepositorio());
        recursosPath = Paths.get(config.getRepositorio()).resolve("recursos.xml");
        if (!config.getWinmerge().isEmpty()) {
          winmerge = new Winmerge(config.getWinmerge());
        }
        if (!config.getProyecto().isEmpty()) {
          titulo.set(config.getProyecto());
        }
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

  class GitPull extends Task<Void> {

    @Override
    protected Void call() {
      updateMessage("Bajando cambios de origen");
      try {
        git.pull();
      } catch (IOException ex) {
        log.log(Level.WARNING, null, ex);
        updateMessage("Alerta: No fue posible hacer pull: " + ex.getMessage());
      }
      updateMessage("");
      return null;
    }

  }

  class GitPush extends Task<Void> {

    @Override
    protected Void call() {
      updateMessage("Subiendo cambios a origen");
      try {
        git.push();
      } catch (IOException ex) {
        log.log(Level.WARNING, null, ex);
        updateMessage("Alerta: No fue posible hacer push: " + ex.getMessage());
      }
      updateMessage("");
      return null;
    }

  }

  class ProyectoUpdater extends Task<Recursos> {

    @Override
    protected Recursos call() {
      try {
        updateMessage("Abriendo recursos.xml");
        Recursos r = Recursos.open(recursosPath);
        updateMessage("Comprobando cambios");
        List<Recurso> todosRecursos = new ArrayList<>();
        todosRecursos.addAll(r.getProcedimientos());
        todosRecursos.addAll(r.getTablas());
        todosRecursos.addAll(r.getSnippets());
        String[] objs = git.status();
        for (String obj : objs) {
          if (!obj.contains("/")) {
            continue;
          }
          String[] parts = obj.split("/");
          String schema = parts[0];
          String objname = parts[1].substring(0, parts[1].indexOf("."));
          Optional<Recurso> rx = todosRecursos.stream().filter(t -> t.getSchema().equals(schema) && t.getNombre().equals(objname)).findAny();
          if (rx.isPresent()) {
            rx.get().setConCambios(true);
          } else {
            if (schema.equals("snippets")) {
              Snippet sn = new Snippet();
              sn.setNombre(objname);
              r.getSnippets().add(sn);
            } else if (objname.startsWith("T") || objname.startsWith("t")) {
              Tabla tb = new Tabla();
              tb.setSchema(schema);
              tb.setNombre(objname);
              tb.setConCambios(true);
              r.getTablas().add(tb);
            } else {
              Procedimiento sp = new Procedimiento();
              sp.setSchema(schema);
              sp.setNombre(objname);
              sp.setConCambios(true);
              r.getProcedimientos().add(sp);
            }
          }
        }
        config.getPorSubir().stream().map(o -> o.split("\\.")).forEach(parts -> {
          String schema = parts[0];
          String objname = parts[1];
          Optional<Recurso> rp = todosRecursos.stream().filter(p -> p.getSchema().equals(schema) && p.getNombre().equals(objname)).findAny();
          rp.ifPresent(rx -> rx.setPendienteSubir(true));
        });
        config.getPorCorregir().stream().map(o -> o.split("\\.")).forEach(parts -> {
          String schema = parts[0];
          String objname = parts[1];
          Optional<Recurso> rp = todosRecursos.stream().filter(p -> p.getSchema().equals(schema) && p.getNombre().equals(objname)).findAny();
          rp.ifPresent(rx -> rx.setPorCorregir(true));
        });
        todosRecursos.stream().filter(x -> !Files.exists(x.getPath(git.getPath()))).forEach(x -> x.setSinArchivo(true));
        updateMessage("");
        return r;
      } catch (IOException | JAXBException ex) {
        updateMessage("Error: Inesperado: " + ex.getMessage());
        log.log(Level.SEVERE, "Inesperado", ex);
        return null;
      }
    }
  }

  private void actualizaRecurso(Recurso.Datos d) {
    RecursosUpdater task = new RecursosUpdater(d);
    bindStatus(task);
    task.setOnSucceeded(evt -> refrescar(null));
    executor.execute(task);
  }

  class RecursosUpdater extends Task<Boolean> {

    private final Recurso.Datos datos;

    public RecursosUpdater(Recurso.Datos datos) {
      this.datos = datos;
    }

    @Override
    protected Boolean call() {
      try {
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
        if (datos.getTipo().equals(Recurso.SNIPPET)) {
          Optional<Snippet> osp = r.getSnippets().stream().filter(p -> p.getNombre().equals(datos.getNombre())).findAny();
          if (osp.isPresent()) {
            Snippet sn = osp.get();
            sn.setDescripcion(datos.getDescripcion());
          } else {
            Snippet sn = new Snippet();
            sn.setNombre(datos.getNombre());
            sn.setDescripcion(datos.getDescripcion());
            r.getSnippets().add(sn);
          }
        }
        r.sort();
        r.save(recursosPath);
        updateMessage("versionando");
        git.addAndCommit("recursos.xml", "recurso agregado o modificado");
        updateMessage("");
        return true;
      } catch (IOException | JAXBException | GitException ex) {
        updateMessage("Error: Inesperado: " + ex.getMessage());
        log.log(Level.SEVERE, "Inesperado", ex);
        return false;
      }
    }
  }

  class RecursosDeleter extends Task<Void> {

    private final List<? extends Recurso> recursos;

    public RecursosDeleter(List<? extends Recurso> recursos) {
      this.recursos = recursos;
    }

    @Override
    protected Void call() {
      try {
        updateMessage("Borrando archivos");
        recursos.stream().map(r -> r.getPath(git.getPath())).filter(p -> {
          try {
            return Files.deleteIfExists(p);
          } catch (IOException ex) {
            updateMessage("Alerta: No se pudo borrar " + p);
            return false;
          }
        }).map(p -> git.getPath().relativize(p).toString()).forEach(git::add);
        updateMessage("Abriendo recursos.xml");
        Recursos r = Recursos.open(recursosPath);
        updateMessage("Guardando cambios");
        recursos.stream().filter(x -> x instanceof Procedimiento).forEach(x -> r.getProcedimientos().removeIf(y -> y.getSchema().equals(x.getSchema()) && y.getNombre().equals(x.getNombre())));
        recursos.stream().filter(x -> x instanceof Tabla).forEach(x -> r.getTablas().removeIf(y -> y.getSchema().equals(x.getSchema()) && y.getNombre().equals(x.getNombre())));
        recursos.stream().filter(x -> x instanceof Snippet).forEach(x -> r.getSnippets().removeIf(y -> y.getSchema().equals(x.getSchema()) && y.getNombre().equals(x.getNombre())));
        r.sort();
        r.save(recursosPath);
        git.addAndCommit("recursos.xml", "recurso eliminado");
        updateMessage("");
      } catch (IOException | JAXBException | GitException ex) {
        updateMessage("Error: Inesperado: " + ex.getMessage());
        log.log(Level.SEVERE, "Inesperado", ex);
      }
      return null;
    }
  }

  class FileUpdater extends Task<Boolean> {

    private final List<String> paths;
    private final String msg;

    public FileUpdater(List<String> paths, String msg) {
      this.paths = paths;
      this.msg = msg;
    }

    @Override
    protected Boolean call() {
      try {
        paths.forEach((p) -> {
          updateMessage("Versionando " + p);
          git.add(p);
        });
        updateMessage("commit");
        git.commit(msg);
        updateMessage("");
        return true;
      } catch (IOException | GitException ex) {
        updateMessage("Error: Inesperado: " + ex.getMessage());
        log.log(Level.SEVERE, "Inesperado", ex);
        return false;
      }
    }
  }

  class MarcasUpdater extends Task<Void> {

    private final List<? extends Recurso> recursos;
    private final String marca;

    public MarcasUpdater(List<? extends Recurso> recursos, String marca) {
      this.recursos = recursos;
      this.marca = marca;
    }

    @Override
    protected Void call() {
      updateMessage("Guardando configuraci\u00f3n");
      List<Recurso> todosRecursos = new ArrayList<>();
      todosRecursos.addAll(sps_data);
      todosRecursos.addAll(tbs_data);
      todosRecursos.addAll(snps_data);
      List<String> list = recursos.stream().map(r -> r.getSchema() + "." + r.getNombre()).collect(Collectors.toList());
      list.forEach(str -> {
        if (marca.equals("PORSUBIR") && !config.getPorSubir().contains(str)) {
          config.getPorSubir().add(str);
        } else if (marca.equals("PORSUBIR")) {
          config.getPorSubir().remove(str);
        } else if (marca.equals("PORCORREGIR") && !config.getPorCorregir().contains(str)) {
          config.getPorCorregir().add(str);
        } else {
          config.getPorCorregir().remove(str);
        }
      });
      config.save();
      todosRecursos.stream().forEach(r -> {
        String str = r.getSchema() + "." + r.getNombre();
        r.setPendienteSubir(config.getPorSubir().contains(str));
        r.setPorCorregir(config.getPorCorregir().contains(str));
      });
      updateMessage("");
      return null;
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

  public StringProperty tituloProperty() {
    return titulo;
  }

}
