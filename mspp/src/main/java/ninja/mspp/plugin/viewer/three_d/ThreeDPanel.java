package ninja.mspp.plugin.viewer.three_d;

import java.net.URL;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import ninja.mspp.model.dataobject.Heatmap;

@Component
public class ThreeDPanel implements Initializable {
	@FXML
	private BorderPane pane;

	@FXML
	private Slider slider;

	private double scale;
	private Group group;
	private MeshView view;

	private static final double DEFAULT_Z_POSITION = 180.0;
	private static final double Z_POSITION_RANGE = 180.0;

	private double px;
	private double py;
	private double xAngle;
	private double zAngle;

	/**
	 * translates group
	 * @param group group
	 * @param width width
	 * @param height height
	 */
	private void onSize( Group group, PerspectiveCamera camera, double width, double height ) {
		group.getTransforms().clear();
		double scale = height / 100.0;
		this.scale = scale;

		group.setScaleX( scale );
		group.setScaleY( scale );
		group.setScaleZ( scale );
		group.setTranslateX( width / 2.0 );
		group.setTranslateY( height / 2.0 );
		group.setTranslateZ( scale * DEFAULT_Z_POSITION );

		this.slider.setValue( DEFAULT_Z_POSITION );
	}

	/**
	 * creates heatmap
	 * @param heatmap heatmap
	 * @param group group
	 */
	private void createHeatmap( Heatmap heatmap, Group group ) {
		Image image = this.createTextureImage();
		PhongMaterial material = new PhongMaterial();
		material.setDiffuseMap( image );
		material.setSpecularColor( Color.TRANSPARENT );
		material.setSpecularPower( 0.0 );

		TriangleMesh mesh = new TriangleMesh();

		int rtCount = Heatmap.RT_SIZE;
		int mzCount = Heatmap.MZ_SIZE;

		double[][] data = heatmap.getData();

		for( int i = 0; i <= 1000; i++ ) {
			mesh.getTexCoords().addAll( ( float )( ( double )i / 2000.0 ), 0.0f );
		}

		List< Integer > textures = new ArrayList< Integer >();

		for( int i = 0; i < rtCount; i++ ) {
			double x = ( ( double )i / ( double )rtCount - 0.5 ) * 200.0;
			for( int j = 0; j < mzCount; j++ ) {
				double y = ( ( double )j / ( double )mzCount - 0.5 ) * 200.0;
				double intensity = data[ j ][ i ];

				double z = - 50.0 * intensity;

				mesh.getPoints().addAll( ( float )x, ( float )y, ( float )z );

				int texture = ( int )Math.round(Math.sqrt( intensity ) * 1000.0 );
				textures.add( texture );
			}
		}

		for( int i = 0; i < rtCount - 1; i++ ) {
			for( int j = 0; j < mzCount - 1; j++ ) {
				int index0 = i * mzCount + j;
				int index1 = index0 + 1;
				int index2 = ( i + 1 ) * mzCount + j;
				int index3 = index2 + 1;

				int texture0 = textures.get( index0 );
				int texture1 = textures.get( index1 );
				int texture2 = textures.get( index2 );
				int texture3 = textures.get( index3 );

				mesh.getFaces().addAll(
					index0, texture0, index1, texture1, index2, texture2,
					index3, texture3, index2, texture2, index1, texture1
				);
			}
		}

		MeshView view = new MeshView();
		view.setMaterial( material );
		view.setMesh( mesh );
		this.view = view;

		this.xAngle = - 60.0;
		this.zAngle = 0.0;

		this.setRotation();

		group.getChildren().add( view );
	}

	/**
	 * sets the rotation
	 */
	private void setRotation() {
		if( this.view == null ) {
			return;
		}

		this.view.getTransforms().clear();
		this.view.getTransforms().add( new Rotate( this.xAngle, new Point3D( 1.0, 0.0, 0.0 ) ) );
		this.view.getTransforms().add( new Rotate( this.zAngle, new Point3D( 0.0, 0.0, 1.0 ) ) );
	}

	/**
	 *
	 * @return
	 */
	private Image createTextureImage() {
		WritableImage image = new WritableImage( 2000, 1 );

		WritablePixelFormat< IntBuffer > format = WritablePixelFormat.getIntArgbInstance();
		int[] pixels = new int[ 2000 ];

		for( int i = 0; i < 2000; i++ ) {
			int red = 0;
			int green = 0;
			int blue = 0;
			int alpha = 255;

			if( i == 1001 ) {
				blue = 255;
			}
			else if( i < 100 ) {    // black -> blue
				blue = ( int )Math.round( 255.0 * ( double )i / 100.0 );
			}
			else if( i < 250 ) {    // blue -> cyan
				blue = 255;
				green = ( int )Math.round( 255.0 * ( double )( i - 100 ) / 150.0 );
			}
			else if( i < 450 ) {    // cyan -> green
				green = 255;
				blue = ( int )Math.round( 255.0 * ( 1.0 - ( double )( i - 250 ) / 200.0 ) );
			}
			else if( i < 700 ) {    // green -> yellow
				green = 255;
				red = ( int )Math.round( 255.0 * ( double )( i - 450 ) / 250.0 );
			}
			else if( i < 1000 ) {    // yellow -> red
				red = 255;
				green = ( int )Math.round( 255.0 * ( 1.0 - ( double )( i - 700 ) / 300.0 ) );
			}
			else if( i < 1500 ) {
				red = 255;
			}

			int pixel = ( alpha << 24 ) | ( red << 16 ) | ( green << 8 ) | blue;
			pixels[ i ] = pixel;
		}
		image.getPixelWriter().setPixels( 0,  0,  2000,  1,  format,  pixels, 0, 2000 );

		return image;
	}

	/**
	 * on slider
	 * @param value slider value
	 */
	private void onSlider( double value ) {
		if( this.group == null ) {
			return;
		}

		double scale = DEFAULT_Z_POSITION - ( value - DEFAULT_Z_POSITION );
		scale = this.scale * scale;
		this.group.setTranslateZ( scale );
	}

	/**
	 * sets the heat map
	 * @param heatmap
	 */
	public void setHeatmap( Heatmap heatmap ) {
		Group group = new Group();
		group.getChildren().clear();
		this.createHeatmap( heatmap, group );
		this.group = group;

		AmbientLight light = new AmbientLight();
		light.setColor( Color.WHITE );
		group.getChildren().add( light );

		ThreeDPanel me = this;

		SubScene scene = new SubScene( group, 100.0, 100.0, true, SceneAntialiasing.BALANCED );
		scene.setFill( Color.WHITE );

		PerspectiveCamera camera = new PerspectiveCamera();
		scene.setCamera( camera );

		scene.widthProperty().bind( this.pane.widthProperty().subtract( 20.0 ) );
		scene.heightProperty().bind( this.pane.heightProperty() );
		scene.widthProperty().addListener(
			( observable, oldValue, newValue ) -> {
				double width = newValue.doubleValue();
				double height = scene.getHeight();
				me.onSize( group, camera, width,  height );
			}
		);
		scene.heightProperty().addListener(
			( observable, oldValue, newValue ) -> {
				double width = scene.getWidth();
				double height = newValue.doubleValue();
				me.onSize( group, camera, width,  height );
			}
		);
		scene.setOnMousePressed(
			( event ) -> {
				me.onMousePressed( event );
			}
		);
		scene.setOnMouseDragged(
			( event ) -> {
				me.onMouseDrag( event );
			}
		);

		this.pane.setCenter( scene );
		this.onSize( group, camera, scene.getWidth(),  scene.getHeight() );
	}

	/**
	 * on mouse pressed
	 * @param event mouse event
	 */
	private void onMousePressed( MouseEvent event ) {
		this.px = event.getX();
		this.py = event.getY();
	}

	/**
	 * on mouse drag
	 * @param event mouse event
	 */
	private void onMouseDrag( MouseEvent event ) {
		double x = event.getX();
		double y = event.getY();

		double dx = x - this.px;
		double dy = y - this.py;

		if( this.view != null ) {
			if( dx != 0.0 ) {
				double angle = - dx;
				this.zAngle += angle;

				while( this.zAngle > 360.0 ) {
					this.zAngle -= 360.0;
				}
				while( this.zAngle < - 360.0 ) {
					this.zAngle += 360.0;
				}
			}

			if( dy != 0.0 ) {
				double angle = dy;
				this.xAngle += angle;

				if( this.xAngle < - 90.0 ) {
					this.xAngle = - 90.0;
				}
				if( this.xAngle > 0.0 ) {
					this.xAngle = 0.0;
				}
			}

			this.setRotation();
		}

		this.px = x;
		this.py = y;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ThreeDPanel me = this;

		this.slider.setPrefWidth( 20.0 );
		this.pane.heightProperty().addListener(
			( observable, oldValue, newValue ) -> {
				this.slider.setPrefHeight( newValue.doubleValue() );
			}
		);
		this.slider.setMax( DEFAULT_Z_POSITION + Z_POSITION_RANGE );
		this.slider.setMin( DEFAULT_Z_POSITION - Z_POSITION_RANGE );
		this.slider.setValue( DEFAULT_Z_POSITION );
		this.slider.valueProperty().addListener(
			( observable, oldValue, newValue ) -> {
				me.onSlider( newValue.doubleValue() );
			}
		);

		this.group = null;
	}

}