package yeezus.gui;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import yeezus.memory.Word;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class WordVisualizer extends Pane {

	private Label label2;

	public WordVisualizer( int address, Word word ) {
		Label label1 = new Label( address + ": " );
		label1.layoutXProperty().setValue( 5 );
		label1.layoutYProperty().bind( this.heightProperty().divide( 2 ) );

		label2 = new Label( word.toString() );
		label2.layoutXProperty().bind( this.widthProperty().subtract( label2.widthProperty() ).subtract( 5 ) );
		label2.layoutYProperty().bind( label1.layoutYProperty() );

		Line line = new Line();
		line.startXProperty().bind( label1.layoutXProperty() );
		line.endXProperty().bind( label2.layoutXProperty().add( label2.widthProperty() ) );
		line.startYProperty().bind( label1.layoutYProperty().add( label1.heightProperty() ) );
		line.endYProperty().bind( line.startYProperty() );

		this.getChildren().addAll( label1, label2, line );

		this.setHeight( 10 );
	}

	public void update( Word word ) {
		this.label2.setText( word.toString() );
	}

}
