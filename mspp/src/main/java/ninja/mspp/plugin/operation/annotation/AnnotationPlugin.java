package ninja.mspp.plugin.operation.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.math3.linear.RealMatrix;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ninja.mspp.annotation.DrawSpectrumForeground;
import ninja.mspp.annotation.Menu;
import ninja.mspp.annotation.MenuAction;
import ninja.mspp.annotation.MenuPosition;
import ninja.mspp.annotation.Plugin;
import ninja.mspp.model.dataobject.FastDrawData;
import ninja.mspp.model.dataobject.Range;
import ninja.mspp.model.dataobject.Rect;
import ninja.mspp.model.gui.MenuInfo;

@Plugin( name = "annotation")
@Menu
public class AnnotationPlugin {
	MenuInfo menu;
	ArrayList< Annotation > annotations;

	/**
	 * constructor
	 */
	public AnnotationPlugin() {
		this.menu = MenuInfo.TOOLS_MENU.item( "Annotation" );
		this.annotations  = new ArrayList< Annotation >();
	}

	@MenuPosition
	public MenuInfo getMenuPosition() {
		return this.menu;
	}

	@MenuAction
	public void action() throws Exception {
		FileChooser chooser = new FileChooser();
		File file = chooser.showOpenDialog( new Stage() );
		if( file == null ) {
			return;
		}

		this.annotations = this.readAnnotations( file );
	}

	@DrawSpectrumForeground
	public void drawAnnotation(
			GraphicsContext g,
			ArrayList< FastDrawData.Element > points,
			Range< Double > xRange,
			Range< Double > yRange,
			Integer width,
			Integer height,
			Rect< Integer > margin,
			RealMatrix drawMatrix
	) {
		g.setGlobalAlpha( 0.3 );

		for( int i = 0; i < this.annotations.size(); i++ ) {
			g.beginPath();
			g.setStroke( Color.CYAN );
			Annotation annotation = this.annotations.get( i );
			Integer posX = (int)Math.round( annotation.getPosition() * drawMatrix.getEntry( 0,  0 ) + drawMatrix.getEntry( 0,  2 ) );
			g.moveTo( (double)posX,  margin.getTop() );
			g.lineTo( (double)posX,  height - margin.getBottom() );
			g.closePath();
			g.stroke();

			g.beginPath();
			g.setStroke( Color.DARKBLUE );
			Integer posY = i % 3 + 1;
			posY = margin.getTop() + (int)Math.round( g.getFont().getSize() * 3.0 * posY / 2.0 );
			g.strokeText( annotation.getAnnotation(),  (double)( posX + 3 ),  (double)posY );
			g.closePath();
			g.stroke();
		}

		g.setGlobalAlpha( 1.0 );
	}

	/**
	 * reads annotations
	 * @param file file
	 * @return annotations
	 */
	ArrayList< Annotation > readAnnotations( File file ) throws Exception {
		ArrayList< Annotation > annotations = new ArrayList< Annotation >();

		BufferedReader reader = new BufferedReader( new FileReader( file ) );
		String line = null;
		while( ( line = reader.readLine() ) != null ) {
			StringTokenizer tokenizer = new StringTokenizer( line, "," );
			try {
				Double position = Double.parseDouble( tokenizer.nextToken() );
				String text = tokenizer.nextToken();

				Annotation annotation = new Annotation();
				annotation.setPosition( position );
				annotation.setAnnotation( text );
				annotations.add( annotation );
			}
			catch( Exception e ) {
			}
		}
		reader.close();

		annotations.sort(
			( anno0, anno1 ) -> {
				if( anno0.getPosition() < anno1.getPosition() ) {
					return -1;
				}
				if( anno0.getPosition() > anno1.getPosition() ) {
					return 1;
				}
				return 0;
			}
		);

		return annotations;
	}
}