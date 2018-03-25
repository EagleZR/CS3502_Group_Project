package yeezus.gui;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import yeezus.pcb.PCB;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class PCB_Pane extends Pane implements Updatable {

	private PCB pcb;
	private Label statusLabel;
	private Label runtimeLabel;
	private Label waitTimeLabel;

	PCB_Pane( PCB pcb ) {
		this.pcb = pcb;
		this.prefWidthProperty().setValue( 100 );
		setup();
	}

	private void setup() {
		// PID
		Label label1 = new Label( "PID:" );
		label1.layoutXProperty().setValue( 5 );
		label1.layoutYProperty().setValue( 0 );
		this.getChildren().add( label1 );

		Label pidLabel = new Label( "" + this.pcb.getPID() );
		pidLabel.layoutXProperty().bind( this.widthProperty().subtract( pidLabel.widthProperty() ).subtract( 5 ) );
		pidLabel.layoutYProperty().bind( label1.layoutYProperty() );
		this.getChildren().add( pidLabel );

		// Priority
		Label label5 = new Label( "Priority:" );
		label5.layoutXProperty().setValue( 5 );
		label5.layoutYProperty().bind( label1.layoutYProperty().add( label1.heightProperty() ).add( 5 ) );
		this.getChildren().add( label5 );

		Label priorityLabel = new Label( "" + this.pcb.getPriority() );
		priorityLabel.layoutXProperty()
				.bind( this.widthProperty().subtract( priorityLabel.widthProperty() ).subtract( 5 ) );
		priorityLabel.layoutYProperty().bind( label5.layoutYProperty() );
		this.getChildren().add( priorityLabel );

		// Status
		Label label2 = new Label( "Status:" );
		label2.layoutXProperty().setValue( 5 );
		label2.layoutYProperty().bind( label5.layoutYProperty().add( label5.heightProperty() ).add( 5 ) );
		this.getChildren().add( label2 );

		this.statusLabel = new Label();
		this.statusLabel.layoutXProperty()
				.bind( this.widthProperty().subtract( this.statusLabel.widthProperty() ).subtract( 5 ) );
		this.statusLabel.layoutYProperty().bind( label2.layoutYProperty() );
		this.getChildren().add( this.statusLabel );

		// Run-time
		Label label3 = new Label( "Runtime:" );
		label3.layoutXProperty().setValue( 5 );
		label3.layoutYProperty().bind( label2.layoutYProperty().add( label2.heightProperty() ).add( 5 ) );
		this.getChildren().add( label3 );

		this.runtimeLabel = new Label();
		this.runtimeLabel.layoutXProperty()
				.bind( this.widthProperty().subtract( this.runtimeLabel.widthProperty() ).subtract( 5 ) );
		this.runtimeLabel.layoutYProperty().bind( label3.layoutYProperty() );
		this.getChildren().add( this.runtimeLabel );

		// Wait-time
		Label label4 = new Label( "Wait Time:" );
		label4.layoutXProperty().setValue( 5 );
		label4.layoutYProperty().bind( label3.layoutYProperty().add( label3.heightProperty() ).add( 5 ) );
		this.getChildren().add( label4 );

		this.waitTimeLabel = new Label();
		this.waitTimeLabel.layoutXProperty()
				.bind( this.widthProperty().subtract( this.waitTimeLabel.widthProperty() ).subtract( 5 ) );
		this.waitTimeLabel.layoutYProperty().bind( label4.layoutYProperty() );
		this.getChildren().add( this.waitTimeLabel );

		// Line
		Line line = new Line();
		line.startXProperty().setValue( 0 );
		line.startYProperty().bind( label4.layoutYProperty().add( label4.heightProperty() ).add( 10 ) );
		line.endXProperty().bind( this.widthProperty() );
		line.endYProperty().bind( line.startYProperty() );
		this.getChildren().add( line );

		update();
	}

	@Override public void update() {
		this.statusLabel.setText( this.pcb.getStatus().toString() );
		this.runtimeLabel.setText( "" + this.pcb.getElapsedRunTime() );
		this.waitTimeLabel.setText( "" + this.pcb.getElapsedWaitTime() );
	}
}
