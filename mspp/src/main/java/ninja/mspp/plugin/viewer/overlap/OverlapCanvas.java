package ninja.mspp.plugin.viewer.overlap;

import java.util.ArrayList;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import ninja.mspp.model.dataobject.FastDrawData;
import ninja.mspp.model.dataobject.Range;
import ninja.mspp.model.dataobject.Rect;
import ninja.mspp.model.dataobject.XYData;
import ninja.mspp.view.control.canvas.ProfileCanvas;

/**
 * overlap canvas
 */
public class OverlapCanvas extends ProfileCanvas {
	private String xTitle;
	private String yTitle;
	private Range< Double > xRange;

	private ArrayList< XYData > xyDataArray;
	private ArrayList< FastDrawData > drawArray;

	/**
	 * constructor
	 * @param xTitle
	 * @param yTitle
	 */
	public OverlapCanvas( String xTitle, String yTitle ) {
		this.xTitle = xTitle;
		this.yTitle = yTitle;

		this.xyDataArray = new ArrayList< XYData >();
		this.drawArray = new ArrayList< FastDrawData >();
	}

	/**
	 * adds xy data
	 * @param xyData xy data
	 */
	public void addXYData( XYData xyData ) {
		this.xyDataArray.add( xyData );
		this.drawArray.add( new FastDrawData( xyData ) );

		this.xRange = this.getXRange( this.xyDataArray );

		this.draw();
	}

	/**
	 * gets the x range
	 * @param array xy data array
	 * @return x range
	 */
	protected Range< Double > getXRange( ArrayList< XYData > array ) {
		if( array == null || array.size() == 0 ) {
			return null;
		}

		Double minX = null;
		Double maxX = null;

		for( XYData data : array ) {
			double currentMinX = data.getMinX();
			double currentMaxX = data.getMaxX();

			if( minX == null || maxX == null ) {
				minX = currentMinX;
				maxX = currentMaxX;
			}
			else {
				minX = Math.min( minX,  currentMinX );
				maxX = Math.max( maxX,  currentMaxX );
			}
		}

		minX = Math.max( 0.0,  minX );
		maxX = Math.max( maxX,  minX + 0.0001 );

		Range< Double > range = new Range< Double >( minX, maxX );
		return range;
	}

	/**
	 * gets the margin
	 * @param g graphics
	 * @param yRange y range
	 * @param width  width
	 * @param height height
	 * @return margin
	 */
	Rect< Integer > getMargin( GraphicsContext g, Range< Double > yRange, Integer width, Integer height ) {
		Integer top = ProfileCanvas.DEFAULT_MARGIN;
		Integer right = ProfileCanvas.DEFAULT_MARGIN;
		Integer bottom = 50;
		Integer left = ProfileCanvas.DEFAULT_MARGIN;

		left = left + this.getMaxYValueWidth( g, yRange, height, top, bottom );

		Rect< Integer > margin = new Rect< Integer >( top, right, bottom, left );
		return margin;
	}

	@Override
	protected void onDraw(GraphicsContext g, Integer width, Integer height) {
		if( this.xyDataArray.isEmpty() ) {
			return;
		}

		Range< Double > xRange = this.xRange;

		ArrayList< ArrayList< FastDrawData.Element > > arrays = new ArrayList< ArrayList< FastDrawData.Element > >();
		for( FastDrawData data : this.drawArray ) {
			arrays.add( this.getPoints( data,  width,  xRange ) );
		}

		Range< Double > yRange = this.getYRange( arrays,  xRange );

		Rect< Integer > margin = this.getMargin( g,  yRange,  width,  height );

		String xTitle = this.xTitle;
		String yTitle = this.yTitle;

		RealMatrix matrix = this.getDrawMatrix(
			width,
			height,
			xRange,
			yRange,
			margin,
			MatrixUtils.createRealIdentityMatrix( 3 )
		);

		Paint[] colors = {
			Color.RED,
			Color.GREEN,
			Color.ORANGE,
			Color.PURPLE,
			Color.BLUE
		};

		g.setGlobalAlpha( 0.5 );

		for( int i = 0; i < arrays.size(); i++ ) {
			Paint paint = colors[ i % colors.length ];
			this.drawProfile( g,  arrays.get( i ),  matrix,  paint );
		}

		g.setGlobalAlpha( 1.0 );
		this.drawScale( g,  xRange,  yRange,  width,  height,  margin,  matrix,  xTitle,  yTitle );
	}

}