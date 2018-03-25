package yeezus.gui;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
abstract class FramedPane extends Pane {

	FramedPane( Color backgroundColor, Color frameColor ) {
		// Draw background
		Rectangle background = new Rectangle();
		background.setFill( backgroundColor );
		background.layoutXProperty().setValue( 0 );
		background.layoutYProperty().setValue( 0 );
		background.widthProperty().bind( this.widthProperty() );
		background.heightProperty().bind( this.heightProperty() );
		this.getChildren().add( background );

		int offset = 2;

		// Draw Frame
		Line topLine = new Line( offset, offset, this.getWidth() - offset, offset );
		Line rightLine = new Line( this.getWidth() - offset, offset, this.getWidth() - offset,
				this.getHeight() - offset );
		Line leftLine = new Line( offset, offset, offset, this.getHeight() - offset );
		Line bottomLine = new Line( offset, this.getHeight() - offset, this.getWidth() - offset,
				this.getHeight() - offset );

		topLine.setStroke( frameColor );
		rightLine.setStroke( frameColor );
		leftLine.setStroke( frameColor );
		bottomLine.setStroke( frameColor );

		topLine.endXProperty().bind( this.widthProperty().subtract( offset ) );

		rightLine.startXProperty().bind( this.widthProperty().subtract( offset ) );
		rightLine.endXProperty().bind( rightLine.startXProperty() );
		rightLine.endYProperty().bind( this.heightProperty().subtract( offset ) );

		leftLine.endYProperty().bind( this.heightProperty().subtract( offset ) );

		bottomLine.startYProperty().bind( this.heightProperty().subtract( offset ) );
		bottomLine.endXProperty().bind( this.widthProperty().subtract( offset ) );
		bottomLine.endYProperty().bind( bottomLine.startYProperty() );

		this.getChildren().addAll( topLine, rightLine, leftLine, bottomLine );
	}
}
