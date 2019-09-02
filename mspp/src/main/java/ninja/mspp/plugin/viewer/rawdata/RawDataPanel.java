package ninja.mspp.plugin.viewer.rawdata;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import ninja.mspp.MsppManager;
import ninja.mspp.annotation.method.FileInput;
import ninja.mspp.annotation.method.OnRawdataSample;
import ninja.mspp.annotation.method.SamplePanel;
import ninja.mspp.model.PluginMethod;
import ninja.mspp.model.dataobject.SampleObject;
import ninja.mspp.model.entity.Sample;
import ninja.mspp.model.entity.Spectrum;
import ninja.mspp.service.RawDataService;
import ninja.mspp.tools.FXTools;
import ninja.mspp.tools.FileTool;


@Component
public class RawDataPanel implements Initializable {
	private static String RECENT_FILE_KEY = "Recent Open File";

	@FXML
	private TableView< Sample > table;

	@FXML
	private TabPane tabPane;

	@FXML
	private TableColumn< Sample, String > nameColomn;

	@FXML
	private TableColumn< Sample, String > acquisitionColumn;

	@FXML
	private TableColumn< Sample, String > instrumentColumn;

	@FXML
	private TableColumn< Sample, Timestamp > dateColumn;

	@FXML
	private TableColumn< Sample, Number > spectraColumn;

	@FXML
	private TableColumn< Sample, String  > commentColumn;

	@FXML
	private Button importButton;

	@FXML
	private Button commentButton;

	@Autowired
	private RawDataService rawDataService;

	@FXML
	private void onImport( ActionEvent event ) {
		MsppManager manager = MsppManager.getInstance();
		String path = manager.loadString( RECENT_FILE_KEY, "" );
		File file = null;
		if( !path.isEmpty() ) {
			file = new File( path );
		}

		List< PluginMethod< FileInput > > methods = manager.getMethods( FileInput.class );

		FileChooser chooser = new FileChooser();
		chooser.setTitle( "Open File" );
		chooser.getExtensionFilters().clear();
		chooser.getExtensionFilters().add( new ExtensionFilter( "All Files", "*.*" ) );
		for( PluginMethod< FileInput > method: methods ) {
			FileInput annotation = method.getAnnotation();
			chooser.getExtensionFilters().add(
				new ExtensionFilter( annotation.title(), "*." + annotation.ext() )
			);
		}

		if( file != null ) {
			chooser.setInitialDirectory( file.getParentFile() );
			chooser.setInitialFileName( file.getName() );
		}

		Stage stage = new Stage();
		file = chooser.showOpenDialog( stage );
		if( file != null ) {
			SampleObject sampleObject = this.openFile( file );
			this.rawDataService.register( sampleObject, new ProgressIndicator() );
			this.updateTable();
		}
	}

	@FXML
	private void onComment( ActionEvent event ) {
		Sample sample = this.table.getSelectionModel().getSelectedItem();
		if( sample == null ) {
			Alert alert = new Alert( AlertType.ERROR );
			alert.setTitle( "Error" );
			alert.setHeaderText( "A sample is not selected." );
			alert.setContentText( "Select a sample before setting comment." );
			alert.showAndWait();
		}
		else {
			TextInputDialog dialog = new TextInputDialog( sample.getUserComment() );
			dialog.getEditor().setPrefWidth( 400.0 );
			dialog.setTitle( "Sample comment" );
			dialog.setHeaderText( sample.getName() );
			dialog.setContentText( "Input the comment." );
			Optional< String > result = dialog.showAndWait();
			if( result.isPresent() ) {
				sample.setUserComment( result.get() );
				this.rawDataService.saveSample( sample );
				this.table.refresh();
			}
		}
	}

	/**
	 * opens file
	 * @param file file
	 * @return file data
	 */
	private SampleObject openFile( File file ) {
		MsppManager manager = MsppManager.getInstance();

		String path = file.getAbsolutePath();
		String ext = FileTool.getExtension( path );
		SampleObject sample = null;

		List< PluginMethod< FileInput > > methods = manager.getMethods( FileInput.class );

		for( PluginMethod< FileInput > method: methods ) {
			Object plugin = method.getPlugin();
			FileInput annotation = method.getAnnotation();
			if( sample == null && annotation.ext().compareToIgnoreCase( ext ) == 0 ) {
				try {
					sample = (SampleObject)method.getMethod().invoke( plugin,  path );
				}
				catch( Exception e ) {
					e.printStackTrace();
				}
			}
		}
		return sample;
	}

	/**
	 * updates table
	 */
	private void updateTable() {
		this.table.getItems().clear();
		for( Sample sample : this.rawDataService.findSamples() ) {
			this.table.getItems().add( sample );
		}
	}

	/**
	 * import button icon
	 */
	private void setImportButtonIcon() {
		Text icon = GlyphsDude.createIcon( FontAwesomeIcon.UPLOAD );
		this.importButton.setText( "" );
		this.importButton.setGraphic( icon );
		this.importButton.setTooltip( new Tooltip( "Import from file." ) );
	}

	/**
	 * comment button icon
	 */
	private void setCommentButtonIcon() {
		Text icon = GlyphsDude.createIcon( FontAwesomeIcon.COMMENT );
		this.commentButton.setText( "" );
		this.commentButton.setGraphic( icon );
		this.commentButton.setTooltip( new Tooltip( "Comment" ) );
	}

	/**
	 * sets sample panels
	 */
	private void setSamplePanels() {
		MsppManager manager = MsppManager.getInstance();
		List< PluginMethod< SamplePanel > > methods = manager.getMethods( SamplePanel.class );
		for( PluginMethod< SamplePanel > method : methods ) {
			Node node = ( Node )method.invoke();
			if( node != null ) {
				Tab tab = new Tab( method.getAnnotation().value() );
				BorderPane pane = new BorderPane();
				pane.setCenter( node );
				tab.setContent( pane );
				this.tabPane.getTabs().add( tab );
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.setImportButtonIcon();
		this.setCommentButtonIcon();

		this.setSamplePanels();

		this.nameColomn.setCellValueFactory( new PropertyValueFactory< Sample, String >( "name" ) );
		this.instrumentColumn.setCellValueFactory( new PropertyValueFactory< Sample, String >( "instrumentvendor" ) );
		this.acquisitionColumn.setCellValueFactory( new PropertyValueFactory< Sample, String >( "acquisitionsoftware" ) );
		this.dateColumn.setCellValueFactory( new PropertyValueFactory< Sample, Timestamp >( "registrationDate" ) );
		this.commentColumn.setCellValueFactory( new PropertyValueFactory< Sample, String >( "userComment" ) );
		this.spectraColumn.setCellValueFactory(
			( cellData ) -> {
				Integer num = 0;
				List< Spectrum > spectra = cellData.getValue().getSpectras();
				if( spectra != null ) {
					num = spectra.size();
				}
				return new ReadOnlyIntegerWrapper( num );
			}
		);
		FXTools.setTableColumnRightAlign( this.spectraColumn );
		this.updateTable();

		MsppManager manager = MsppManager.getInstance();

		this.table.getSelectionModel().selectedItemProperty().addListener(
			( observable, oldValue, newValue ) -> {
				manager.invokeAll( OnRawdataSample.class, newValue );
			}
		);
	}
}