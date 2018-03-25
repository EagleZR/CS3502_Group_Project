package yeezus.gui;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
class TaskManagerPane extends FramedPane implements Updatable {

	private PCB_List list;

	TaskManagerPane( Color backgroundColor, Color frameColor ) {
		super( backgroundColor, frameColor );

		Label taskManagerLabel = new Label( "Task Manager" );
		taskManagerLabel.setFont( new Font( 24 ) );
		taskManagerLabel.layoutXProperty().setValue( 5 );
		taskManagerLabel.layoutYProperty().setValue( 5 );
		this.getChildren().add( taskManagerLabel );

		this.list = new PCB_List();
		this.list.layoutXProperty().bind( taskManagerLabel.layoutXProperty() );
		this.list.layoutYProperty().bind( taskManagerLabel.layoutYProperty().add( taskManagerLabel.heightProperty() ) );
		this.list.prefWidthProperty().bind( this.widthProperty().subtract( 10 ) );
		this.list.prefHeightProperty()
				.bind( this.heightProperty().subtract( taskManagerLabel.heightProperty() ).subtract( 10 ) );
		this.list.update();
		this.getChildren().add( this.list );
	}

	@Override public void update() {
		this.list.update();
	}
}
