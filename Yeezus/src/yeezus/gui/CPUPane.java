package yeezus.gui;

import com.sun.istack.internal.Nullable;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import yeezus.cpu.CPU;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
class CPUPane extends FramedPane implements Updatable {

	private Label cpuDisplay;
	private Label pidDisplay;
	private Label processCountDisplay;
	private Label executionCountDisplay;

	private CPU cpu = null;

	CPUPane( Color backgroundColor, Color frameColor ) {
		super( backgroundColor, frameColor );

		this.prefWidthProperty().setValue( 100 );

		Label cpuLabel = new Label( "CPU: " );
		cpuLabel.layoutXProperty().setValue( 5 );
		cpuLabel.layoutYProperty().setValue( 5 );
		this.getChildren().add( cpuLabel );

		this.cpuDisplay = new Label( "" );
		this.cpuDisplay.layoutXProperty()
				.bind( this.widthProperty().subtract( this.cpuDisplay.widthProperty() ).subtract( 5 ) );
		this.cpuDisplay.layoutYProperty().bind( cpuLabel.layoutYProperty() );
		this.getChildren().add( this.cpuDisplay );

		Label pidLabel = new Label( "Process: " );
		pidLabel.layoutXProperty().bind( cpuLabel.layoutXProperty() );
		pidLabel.layoutYProperty().bind( cpuLabel.layoutYProperty().add( cpuLabel.heightProperty() ).add( 5 ) );
		this.getChildren().add( pidLabel );

		this.pidDisplay = new Label( "" );
		this.pidDisplay.layoutXProperty()
				.bind( this.widthProperty().subtract( this.pidDisplay.widthProperty() ).subtract( 5 ) );
		this.pidDisplay.layoutYProperty().bind( pidLabel.layoutYProperty() );
		this.getChildren().add( this.pidDisplay );

		Label processCountLabel = new Label( "Processes Executed: " );
		processCountLabel.layoutXProperty().bind( pidLabel.layoutXProperty() );
		processCountLabel.layoutYProperty()
				.bind( pidLabel.layoutYProperty().add( pidLabel.heightProperty() ).add( 5 ) );
		this.getChildren().add( processCountLabel );

		this.processCountDisplay = new Label( "" );
		this.processCountDisplay.layoutXProperty()
				.bind( this.widthProperty().subtract( this.processCountDisplay.widthProperty() ).subtract( 5 ) );
		this.processCountDisplay.layoutYProperty().bind( processCountLabel.layoutYProperty() );
		this.getChildren().add( this.processCountDisplay );

		Label executionCountLabel = new Label( "Instructions Executed: " );
		executionCountLabel.layoutXProperty().bind( processCountLabel.layoutXProperty() );
		executionCountLabel.layoutYProperty()
				.bind( processCountLabel.layoutYProperty().add( processCountLabel.heightProperty() ).add( 5 ) );
		this.getChildren().add( executionCountLabel );

		this.executionCountDisplay = new Label( "" );
		this.executionCountDisplay.layoutXProperty()
				.bind( this.widthProperty().subtract( this.executionCountDisplay.widthProperty() ).subtract( 5 ) );
		this.executionCountDisplay.layoutYProperty().bind( executionCountLabel.layoutYProperty() );
		this.getChildren().add( this.executionCountDisplay );
	}

	void setCPU( @Nullable CPU cpu ) {
		this.cpu = cpu;
		update();
	}

	@Override public void update() {
		if ( this.cpu == null ) {
			this.cpuDisplay.setText( "" );
			this.pidDisplay.setText( "" );
			this.processCountDisplay.setText( "" );
			this.executionCountDisplay.setText( "" );
		} else {
			this.cpuDisplay.setText( "" + this.cpu.getCPUID() );
			this.pidDisplay.setText( "" + ( this.cpu.getProcess() != null ? this.cpu.getProcess().getPID() : "" ) );
			this.processCountDisplay.setText( "" + this.cpu.getNumProcesses() );
			this.executionCountDisplay.setText( "" + this.cpu.getNumInstructionsExecuted() );
		}
	}
}
