package net.vjdv.baz.om2;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.spec.DSAGenParameterSpec;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Set;
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
import javax.xml.bind.JAXBException;

import net.vjdv.baz.om2.models.ConexionDB;
import net.vjdv.baz.om2.models.Dialogos;
import net.vjdv.baz.om2.models.Procedimiento;
import net.vjdv.baz.om2.models.Proyecto;
import net.vjdv.baz.om2.models.Recurso;
import net.vjdv.baz.om2.models.Tabla;
import javafx.concurrent.Task;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import net.vjdv.baz.om2.models.ProgressStage;
import net.vjdv.baz.om2.models.Winmerge;
import net.vjdv.baz.om2.svn.SvnManager;
import static java.util.logging.Logger.getLogger;

/**
 *
 * @author B187926
 */
public class InicioController implements Initializable {

	@FXML
	private Menu menu_conexiones, menu_recientes, menu_svn;
	@FXML
	private MenuItem menuitem_spcopynm, menuitem_spcopymp, menuitem_spcopyfl, menuitem_guardar;
	@FXML
	private CheckMenuItem menucheck_abrirultimo;
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
	// Variables
	private final SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
	private final TextInputDialog filteringDialog = new TextInputDialog();
	private final FileChooser filechooser = new FileChooser();
	private final Dialogos dialogs = new Dialogos();
	private SvnManager svn;
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
			proyecto.title = Dialogos.input("Título del proyecto:");
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
		throw new UnsupportedOperationException("Ya todo viene de config.xml");
		/*
		 * if (proyecto != null) { pintarTabla(); pintarConexiones();
		 * menu_svn.setDisable(true); if (proyecto.svn != null && new
		 * File(proyecto.repo_path + File.separator + ".svn").exists()) { svn = new
		 * SvnManager(this, proyecto.svn, proyecto.repo_path);
		 * menu_svn.setDisable(false); } Winmerge.bin = proyecto.winmerge;
		 * config.addReciente(f.getAbsolutePath()); pintarRecientes(); config.save(); }
		 * else { dialogs.alert("No se pudo abrir el archivo."); }
		 */
	}

	@FXML
	private void guardarProyecto(ActionEvent event) {
		throw new UnsupportedOperationException("No se soporta");
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
//            proyecto.onAddedRecurso(sp);
			throw new UnsupportedOperationException("No se soporta");
//            sps_data.add(sp);
//            pintarTabla();
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
//            proyecto.onAddedRecurso(t);
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
	private void abrirProcedimiento(ActionEvent event) throws IOException {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			for (Procedimiento r : tabla_sps.getSelectionModel().getSelectedItems()) {
				File f = new File(proyecto.repo_path + File.separator + r.getUri());
				if (f.exists()) {
					desktop.open(f);
				}
			}
		}
	}

	@FXML
	private void copiarProcedimiento(ActionEvent event) {
		if (event.getSource() == menuitem_spcopynm || event.getSource() == menuitem_spcopymp) {
			StringBuilder sb = new StringBuilder();
			for (Procedimiento sp : tabla_sps.getSelectionModel().getSelectedItems()) {
				if (sb.length() > 0) {
					sb.append("\r\n");
				}
				if (event.getSource() == menuitem_spcopynm) {
					sb.append(sp.getNombre());
				} else if (event.getSource() == menuitem_spcopymp) {
					sb.append(sp.getMap());
				}
			}
			setClipBoard(sb.toString());
		} else if (event.getSource() == menuitem_spcopyfl) {
			List<File> list = new ArrayList<>();
			for (Procedimiento sp : tabla_sps.getSelectionModel().getSelectedItems()) {
				File f = new File(proyecto.repo_path + File.separator + sp.getUri());
				list.add(f);
			}
			setClipBoard(list);
		}
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
			dialogs.alert("Elija uno o m\u00e1s elementos");
			return;
		}
		try {
			for (Procedimiento r : tabla_sps.getSelectionModel().getSelectedItems()) {
				String nombre = r.getNombre();
				String map = Dialogos.input("SqlMap:", "Editar procedimiento " + nombre, r.getMap());
				String descripcion = Dialogos.input("Descripcion:", "Editar procedimiento " + nombre,
						r.getDescripcion());
				try (Connection conn = InicioController.this.proyecto.getDataSource().getConnection()) {
					PreparedStatement ps = conn.prepareStatement(Recurso.sqlInsertUpdate());
					ps.setString(1, nombre);
					ps.setString(2, "SP");
					ps.setString(3, descripcion);
					ps.setString(4, map);
					int n = ps.executeUpdate();
					if (n == 1) {
						r.setMap(map);
						r.setDescripcion(descripcion);
					}
					statusconn_lb.setText("Procedimiento " + nombre + " guardado");
				}
			}
		} catch (Dialogos.InputCancelled ex) {
			Logger.getLogger("OM2").log(Level.FINEST, "Input cancelled");
			statusconn_lb.setText("");
		} catch (SQLException ex) {
			Logger.getLogger("OM2").log(Level.WARNING, "No fue insertar/actualizar procedimiento", ex);
			statusconn_lb.setText("Error al insertar/actualizar: " + ex.getMessage());
		}
	}

	@FXML
	private void editarTabla(ActionEvent event) {
		if (tabla_tbs.getSelectionModel().getSelectedItems().isEmpty()) {
			dialogs.alert("Elija una o m\u00e1s tablas");
			return;
		}
		try {
			for (Tabla r : tabla_tbs.getSelectionModel().getSelectedItems()) {
				String nombre = r.getNombre();
				String descripcion = Dialogos.input("Descripcion:", "Editar tabla " + nombre, r.getDescripcion());
				try (Connection conn = InicioController.this.proyecto.getDataSource().getConnection()) {
					PreparedStatement ps = conn.prepareStatement(Recurso.sqlInsertUpdate());
					ps.setString(1, nombre);
					ps.setString(2, "TB");
					ps.setString(3, descripcion);
					ps.setString(4, null);
					int n = ps.executeUpdate();
					if (n == 1) {
						r.setDescripcion(descripcion);
					}
					statusconn_lb.setText("Tabla " + nombre + " guardada");
				}
			}
		} catch (Dialogos.InputCancelled ex) {
			Logger.getLogger("OM2").log(Level.FINEST, "Input cancelled");
			statusconn_lb.setText("");
		} catch (SQLException ex) {
			Logger.getLogger("OM2").log(Level.WARNING, "No fue insertar/actualizar tabla", ex);
			statusconn_lb.setText("Error al insertar/actualizar: " + ex.getMessage());
		}
	}

	@FXML
	private void quitarElementos(ActionEvent event) {
		// Quitar procedimientos
		/*
		 * if (tabs.getSelectionModel().getSelectedIndex() == 0) { throw new
		 * UnsupportedOperationException("No se soporta"); List<Procedimiento> list =
		 * tabla_sps.getSelectionModel().getSelectedItems();
		 * proyecto.quitarProcedimiento(list.toArray(new Procedimiento[list.size()]));
		 * sps_data.removeAll(list); }
		 */
		// Quitar tablas
		if (tabs.getSelectionModel().getSelectedIndex() == 1) {
			List<Tabla> selected = tabla_tbs.getSelectionModel().getSelectedItems();
			if (selected.size() == 0)
				statusconn_lb.setText("No se seleccion� ning�n elemento");
//        	if(selected.size()==1 && Dialogos.)
//            List<Tabla> list = tabla_tbs.getSelectionModel().getSelectedItems();
//            proyecto.quitarTabla(list.toArray(new Tabla[list.size()]));
			tbs_data.removeAll(selected);
		}
	}

	@FXML
	private void joinFiles(ActionEvent event) {
		if (tabla_sps.getSelectionModel().getSelectedItems().size() < 2) {
			Dialogos.message("Elija al menos dos procedimientos para unirlos");
			return;
		}
		StringBuilder sb = new StringBuilder();
		StringBuilder ns = new StringBuilder();
		for (Procedimiento p : tabla_sps.getSelectionModel().getSelectedItems()) {
			ns.append('_').append(p.getNombre().substring(p.getNombre().length() - 4));
			try {
				byte[] encoded = Files.readAllBytes(Paths.get(proyecto.repo_path + File.separator + p.getUri()));
				CharsetDetector cd = new CharsetDetector();
				cd.setText(encoded);
				CharsetMatch cm = cd.detect();
				sb.append("\r\nGO\r\n").append(new String(encoded, cm.getName()));
			} catch (IOException ex) {
				Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		FileChooser chooser = new FileChooser();
		chooser.setInitialFileName(ns.substring(1) + ".sql");
		File f = chooser.showSaveDialog(null);
		if (f != null) {
			try (PrintWriter out = new PrintWriter(f)) {
				out.print(sb.toString());
			} catch (FileNotFoundException ex) {
				Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	// P A R A S Q L
	@FXML
	private void nuevaConexion(ActionEvent event) {
		throw new UnsupportedOperationException("Ya solo existe una conexi�n");
	}

	@FXML
	private void compararSP(ActionEvent event) {
		if (conn == null) {
			dialogs.alert("No está conectado a alguna base de datos");
			return;
		}
		for (Procedimiento sp : tabla_sps.getSelectionModel().getSelectedItems()) {
			File local = new File(proyecto.repo_path + File.separator + sp.getUri());
			if (!local.exists()) {
				Dialogos.message("No hay versión local guardada de " + sp.getNombre());
				continue;
			}
			try (PreparedStatement ps = conn.prepareStatement(
					"SELECT OBJECT_NAME(OBJECT_ID) sp, definition FROM sys.sql_modules WHERE OBJECT_NAME(OBJECT_ID)='"
							+ sp.getNombre() + "'")) {
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
					Dialogos.message("No existe el procedimiento " + sp.getNombre() + "en el servidor "
							+ conn.getMetaData().getURL());
					continue;
				}
				Winmerge.compare(local.getAbsolutePath(), "Versión del objeto local", tmp.getAbsolutePath(),
						"Versión del objeto en DB");
			} catch (SQLException | IOException | NullPointerException ex) {
				dialogs.alert("Error al comparar procedimiento: " + ex.toString());
				Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE,
						"Error al obtener o escribir procedimiento", ex);
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
		File local = new File(proyecto.repo_path + File.separator + sp.getUri());
		if (local.exists()) {
			if (!dialogs.confirm("¿Sobreescribir " + sp.getUri() + "?")) {
				return;
			}
		}
		try (PreparedStatement ps = conn.prepareStatement(
				"SELECT OBJECT_NAME(OBJECT_ID) sp, definition FROM sys.sql_modules WHERE OBJECT_NAME(OBJECT_ID)='"
						+ sp.getNombre() + "'")) {
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				String def = rs.getString("definition");
				def = def.replaceAll("CREATE PROCEDURE ", "ALTER PROCEDURE ");
				def = def.replaceAll("CREATE FUNCTION ", "ALTER FUNCTION ");
				try (PrintWriter out = new PrintWriter(local)) {
					out.print(def);
				}
			} else {
				Dialogos.message("No existe el procedimiento en el servidor.");
			}
		} catch (SQLException | FileNotFoundException | NullPointerException ex) {
			dialogs.alert("Error al obtener procedimiento desde el servidor: " + ex.toString());
			Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE,
					"Error al obtener o escribir procedimiento", ex);
		}
	}

	@FXML
	private void dependenciasSql(ActionEvent event) {
		if (conn == null) {
			dialogs.alert("No está conectado a alguna base de datos");
			return;
		}
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
		try (CallableStatement cs = conn.prepareCall("sp_depends " + r.getNombre())) {
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
			Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE,
					"Error al obtener o escribir procedimiento", ex);
		}
	}

	// L O C A L
	@FXML
	private void crearArchivoParaSP(ActionEvent event) {
		String fecha = format.format(new Date());
		for (Procedimiento sp : tabla_sps.getSelectionModel().getSelectedItems()) {
			File local = new File(proyecto.repo_path + File.separator + sp.getUri());
			if (local.exists()) {
				dialogs.alert("Ya existe " + sp.getUri());
				continue;
			}
			String str = "/*************************************************************\r\n"
					+ "Proyecto:				" + proyecto.title + "\r\n" + "Descripción:			"
					+ sp.getDescripcion() + "\r\n" + "Parámetros de entrada:	\r\n" + "Valor de retorno:		\r\n"
					+ "Creador:				 " + fecha.toUpperCase() + "\r\n"
					+ "*************************************************************/\r\n";
			str += sp.getNombre().startsWith("F")
					? "CREATE FUNCTION [dbo].[" + sp.getNombre() + "]() RETURNS XML AS\r\n\r\n" + "BEGIN\r\n"
							+ "\tDECLARE @RESP XML\r\n\t\r\n\t\r\n\t\r\n" + "RETURN @RESP\r\n\r\n" + "END"
					: "CREATE PROCEDURE [dbo].[" + sp.getNombre() + "] (  ) AS\r\n\r\n" + "DECLARE @RESP XML\r\n\r\n"
							+ "BEGIN\r\n\r\n" + "BEGIN TRY\r\n\t\r\n\t\r\n\t\r\n" + "END TRY\r\n\r\n"
							+ "BEGIN CATCH\r\n\t\r\n" + "END CATCH\r\n\r\n" + "SELECT @RESP AS 'txt_xml_schema'\r\n\r\n"
							+ "END";
			try (PrintWriter out = new PrintWriter(local)) {
				out.print(str);
			} catch (FileNotFoundException ex) {
				Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, "Error al crear el archivo", ex);
				dialogs.alert(ex.toString());
			}
		}
	}

	@FXML
	private void buscarEnSp(ActionEvent event) {
		try {
			String str = Dialogos.input("Texto a buscar:", "Buscar en procedimientos", "");
			// Tarea
			SearcherInFiles searcher = new SearcherInFiles(str);
			ProgressStage progress = new ProgressStage(searcher);
			new Thread(searcher).start();
		} catch (Dialogos.InputCancelled ex) {
			Logger.getLogger("ObjMan").log(Level.FINEST, "Input cancelled");
		}
	}

	@FXML
	private void verDependenciasSp(ActionEvent event) {
		if (tabla_sps.getSelectionModel().getSelectedItem() == null) {
			Dialogos.message("Elija al menos un procedimiento");
		}
		StringBuilder sb = new StringBuilder();
		for (Procedimiento sp : tabla_sps.getSelectionModel().getSelectedItems()) {
			if (sb.length() > 0) {
				sb.append("|");
			}
			sb.append(sp.getNombre());
		}
		SearcherInFiles searcher = new SearcherInFiles(sb.toString());
		ProgressStage progress = new ProgressStage(searcher);
		new Thread(searcher).start();
	}

	@FXML
	private void verDependenciasTb(ActionEvent event) {
		if (tabla_tbs.getSelectionModel().getSelectedItem() == null) {
			Dialogos.message("Elija al menos una tabla");
		}
		StringBuilder sb = new StringBuilder();
		for (Tabla tb : tabla_tbs.getSelectionModel().getSelectedItems()) {
			if (sb.length() > 0) {
				sb.append("|");
			}
			sb.append(tb.getNombre());
		}
		SearcherInFiles searcher = new SearcherInFiles(sb.toString());
		ProgressStage progress = new ProgressStage(searcher);
		new Thread(searcher).start();
		tabs.getSelectionModel().select(0);
	}

	// S U B V E R S I O N
	@FXML
	private void svnAdd() {
		List<Procedimiento> list = tabla_sps.getSelectionModel().getSelectedItems();
		String[] files = new String[list.size()];
		for (int i = 0; i < files.length; i++) {
			files[i] = list.get(i).getUri();
		}
		svn.add(files);
	}

	@FXML
	private void svnCommit() {
		List<Procedimiento> list = tabla_sps.getSelectionModel().getSelectedItems();
		String[] files = new String[list.size()];
		for (int i = 0; i < files.length; i++) {
			files[i] = list.get(i).getUri();
		}
		svn.commit(files);
	}

	@FXML
	private void svnUpdate() {
		List<Procedimiento> list = tabla_sps.getSelectionModel().getSelectedItems();
		String[] files = new String[list.size()];
		for (int i = 0; i < files.length; i++) {
			files[i] = list.get(i).getUri();
		}
		svn.update(files);
	}

	@FXML
	private void svnHistorico() {
		List<Procedimiento> list = tabla_sps.getSelectionModel().getSelectedItems();
		for (Procedimiento sp : list) {
			svn.log(sp.getUri());
		}
	}

	@FXML
	private void svnCommitAll() {
		svn.commit();
	}

	@FXML
	private void svnUpdateAll() {
		svn.update();
	}

	@FXML
	private void svnDiferencias() {
		svn.changedFiles();
	}

	@FXML
	private void svnComparaCambios() {
		for (Procedimiento sp : tabla_sps.getSelectionModel().getSelectedItems()) {
			try {
				File tmp = File.createTempFile(sp.getNombre(), ".sql");
				tmp.deleteOnExit();
				svn.export(sp.getUri(), tmp);
				Winmerge.compare(tmp.getAbsolutePath(), "Versión actual del repositorio",
						proyecto.repo_path + File.separator + sp.getUri(), "Versión de archivo local");
			} catch (IOException ex) {
				Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@FXML
	private void abrirUbicacion() {
		Procedimiento sp = tabla_sps.getSelectionModel().getSelectedItem();
		File f = new File(proyecto.repo_path + File.separator + sp.getUri());
		try {
			Process p = new ProcessBuilder("explorer.exe", "/select," + f.getAbsolutePath()).start();
		} catch (IOException ex) {
			Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	// C L E A R C A S E
	@FXML
	private void abrirUbicacion2() {
		Procedimiento sp = tabla_sps.getSelectionModel().getSelectedItem();
		File f = new File(proyecto.cc_path + File.separator + sp.getUri());
		try {
			Process p = new ProcessBuilder("explorer.exe", "/select," + f.getAbsolutePath()).start();
		} catch (IOException ex) {
			Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@FXML
	private void abrirDeCC() throws IOException {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			for (Procedimiento r : tabla_sps.getSelectionModel().getSelectedItems()) {
				File f = new File(proyecto.cc_path + File.separator + r.getNombre() + ".SQL");
				System.out.println(f.getAbsolutePath());
				if (f.exists()) {
					desktop.open(f);
				}
			}
		}
	}

	@FXML
	private void copiarToClearCase() {
		for (Procedimiento r : tabla_sps.getSelectionModel().getSelectedItems()) {
			File flocal = new File(proyecto.repo_path + File.separator + r.getNombre() + ".sql");
			File flcase = new File(proyecto.cc_path + File.separator + r.getNombre() + ".sql");
			File fucase = new File(proyecto.cc_path + File.separator + r.getNombre() + ".SQL");
			File fcc = fucase.exists() ? fucase : flcase;
			try {
				if (flocal.exists() && !dialogs
						.confirm("¿Reemplazar versión de ClearCase con la versión del directorio local?")) {
					return;
				}
				Files.copy(flocal.toPath(), fcc.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
				Dialogos.message("Error al copiar archivo: " + ex.toString());
				Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@FXML
	private void copiarDesdeClearCase() {
		for (Procedimiento r : tabla_sps.getSelectionModel().getSelectedItems()) {
			File flocal = new File(proyecto.repo_path + File.separator + r.getNombre() + ".sql");
			File flcase = new File(proyecto.cc_path + File.separator + r.getNombre() + ".sql");
			File fucase = new File(proyecto.cc_path + File.separator + r.getNombre() + ".SQL");
			File fcc = fucase.exists() ? fucase : flcase;
			try {
				if (flocal.exists()
						&& !dialogs.confirm("¿Reemplazar versión local con la versión del directorio ClearCase?")) {
					return;
				}
				Files.copy(fcc.toPath(), flocal.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
				Dialogos.message("Error al copiar archivo: " + ex.toString());
				Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, null, ex);
			}
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
		if (clipboard == null) {
			clipboard = Clipboard.getSystemClipboard();
		}
		ClipboardContent content = new ClipboardContent();
		content.putFiles(files);
		clipboard.setContent(content);
	}

	// private void setCl
	private void pintarTabla() {
		// SPS
		sps_data = FXCollections.observableArrayList(proyecto.procedimientos);
		sps_filtered = new FilteredList<>(sps_data, p -> true);
		SortedList<Procedimiento> sps_sorted = new SortedList<>(sps_filtered);
		sps_sorted.comparatorProperty().bind(tabla_sps.comparatorProperty());
		tabla_sps.setItems(sps_sorted);
		// TBS
		tbs_data = FXCollections.observableArrayList(proyecto.tablas);
		tbs_filtered = new FilteredList<>(tbs_data, p -> true);
		SortedList<Tabla> tbs_sorted = new SortedList<>(tbs_filtered);
		tbs_sorted.comparatorProperty().bind(tabla_tbs.comparatorProperty());
		tabla_tbs.setItems(tbs_sorted);
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
				if (r.getUri() == null || r.getUri().isEmpty()) {
					return;
				}
				File f = new File(proyecto.repo_path + File.separator + r.getUri());
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
		// Archivos recientes
		Logger.getLogger("OM2").log(Level.INFO, "Iniciado");
		ProyectoReader reader = new ProyectoReader();
		statusconn_lb.textProperty().bind(reader.messageProperty());
		reader.setOnSucceeded(e -> {
			proyecto = reader.getValue();
			pintarTabla();
			statusconn_lb.textProperty().unbind();
			statusconn_lb.setText("Listo");
		});
		new Thread(reader).start();

	}

	class SearcherInFiles extends Task<Void> {

		private final String aguja;

		public SearcherInFiles(String aguja) {
			this.aguja = aguja;
		}

		@Override
		protected Void call() {
			try {
				updateTitle("Búsqueda en procedimientos");
				Set<String> coincidencias = new HashSet<>();
				int length = proyecto.procedimientos.size();
				for (int i = 0; i < length; i++) {
					Procedimiento sp = proyecto.procedimientos.get(i);
					updateMessage(sp.getNombre());
					File f = new File(proyecto.repo_path + File.separator + sp.getUri());
					if (buscarEnArchivo(f, aguja)) {
						coincidencias.add(sp.getNombre());
					}
					updateProgress(i + 1, length);
				}
				sps_filtered.setPredicate((sp) -> {
					return coincidencias.contains(sp.getNombre());
				});
				return null;
			} catch (Exception ex) {
				Logger.getLogger(InicioController.class.getName()).log(Level.SEVERE, null, ex);
				return null;
			}
		}

		private boolean buscarEnArchivo(File pajar, String aguja) {
			try (Scanner scanner = new Scanner(pajar)) {
				return scanner.findWithinHorizon(aguja, 0) != null;
			} catch (FileNotFoundException ex) {
				Logger.getLogger(InicioController.class.getName()).log(Level.WARNING, "No existe el archivo {0}",
						pajar.getName());
			}
			return false;
		}

	}

	class ProyectoReader extends Task<Proyecto> {
		@Override
		protected Proyecto call() {
			updateMessage("Abriendo config.xml");
			Path p = Paths.get("").resolve("config.xml").toAbsolutePath();
			try {
				Proyecto proyecto = Proyecto.abrir(p.toFile());
				InicioController.this.proyecto = proyecto;
				ObjetosReader reader1 = new ObjetosReader();
				reader1.messageProperty().addListener((observable, oldvalue, newvalue) -> updateMessage(newvalue));
				List<Recurso> recursos = reader1.call();
				recursos.forEach(r -> {
					if (r instanceof Tabla)
						proyecto.tablas.add((Tabla) r);
					else
						proyecto.procedimientos.add((Procedimiento) r);
				});
				return proyecto;
			} catch (FileNotFoundException | JAXBException ex) {
				updateMessage("config.xml no existe o es inv�lido");
				Logger.getLogger(InicioController.class.getName()).log(Level.WARNING,
						"No fue posible cargar configuraci�n de " + p.toString(), ex);
				return null;
			}
		}
	}

	class ObjetosReader extends Task<List<Recurso>> {
		@Override
		protected List<Recurso> call() {
			updateMessage("Leyendo lista de objetos (DB)");
			List<Recurso> list = new ArrayList<>();
			try (Connection conn = InicioController.this.proyecto.getDataSource().getConnection()) {
				PreparedStatement ps = conn.prepareStatement("SET NOCOUNT ON SELECT * FROM dbo.om2_objects");
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					String tipo = rs.getString("tipo");
					if (tipo.equals("TB")) {
						Tabla t = new Tabla();
						t.setNombre(rs.getString("id_obj"));
						t.setSchema(rs.getString("esquema"));
						t.setDescripcion(rs.getString("descripcion"));
						list.add(t);
					} else {
						Procedimiento p = new Procedimiento();
						p.setNombre(rs.getString("id_obj"));
						p.setSchema(rs.getString("esquema"));
						p.setDescripcion(rs.getString("descripcion"));
						p.setMap(rs.getString("mapeo"));
						p.setTipo(rs.getString("tipo"));
						list.add(p);
					}
				}
			} catch (Exception ex) {
				Logger.getLogger("OM2").log(Level.WARNING, "No fue posible cargar objetos", ex);
				updateMessage("No fue posible cargar objetos de DB");
			}
			updateMessage("Leyendo lista de objetos (Repositorio)");
			Path path = Paths.get(proyecto.repo_path);
			try {
				Files.list(path).filter(Files::isRegularFile).forEach(file -> {
					String fname = file.getFileName().toString();
					fname = fname.substring(0, fname.indexOf("."));
					int index = -1;
					for (int i = 0; i < list.size(); i++) {
						Recurso r = list.get(i);
						if (r.getNombre().equalsIgnoreCase(fname)) {
							index = i;
							break;
						}
					}
					if (index == -1) {
						Procedimiento p = new Procedimiento();
						p.setNombre(fname);
						p.setSchema("dbo");
						p.setMap("");
						p.setDescripcion("");
						p.setTipo("NA");
						list.add(p);
						System.out.println("Falta: " + fname);
					}
				});
				;
			} catch (Exception ex) {
				Logger.getLogger("OM2").log(Level.WARNING, "No fue posible leer el directorio", ex);
			}
			return list;
		}
	}

}
