package yeezus.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import yeezus.cpu.CPU;

import java.util.LinkedList;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
class CPUsPane extends FramedPane implements Updatable {

	private ObservableList<CPUPane> cpus;
	private Color backgroundColor;
	private Color frameColor;

	CPUsPane( Color backgroundColor, Color frameColor ) {
		super( backgroundColor, frameColor );
		this.backgroundColor = backgroundColor;
		this.frameColor = frameColor;

		this.cpus = FXCollections.observableList( new LinkedList<CPUPane>() );

		Label cpuLabel = new Label( "CPUs" );
		cpuLabel.setFont( new Font( 24 ) );
		cpuLabel.layoutXProperty().setValue( 5 );
		cpuLabel.layoutYProperty().setValue( 5 );
		this.getChildren().add( cpuLabel );

		ListView<CPUPane> panes = new ListView<>( this.cpus );
		panes.layoutXProperty().setValue( 5 );
		panes.layoutYProperty().bind( cpuLabel.layoutYProperty().add( cpuLabel.heightProperty() ).add( 5 ) );
		panes.prefWidthProperty().bind( this.widthProperty().subtract( 10 ) );
		panes.prefHeightProperty().bind( this.heightProperty().subtract( cpuLabel.heightProperty() ).subtract( 15 ) );
		this.getChildren().add( panes );
	}

	void setCPUs( CPU... cpus ) {
		this.cpus.clear();
		for ( CPU cpu : cpus ) {
			CPUPane cpuPane = new CPUPane( this.backgroundColor, this.frameColor );
			cpuPane.setCPU( cpu );
			this.cpus.add( cpuPane );
		}
		this.cpus.addAll();
		update();
	}

	@Override public void update() {
		for ( CPUPane cpuPane : this.cpus ) {
			cpuPane.update();
		}
	}
}
