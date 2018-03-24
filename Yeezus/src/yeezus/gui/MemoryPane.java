package yeezus.gui;

import com.sun.istack.internal.NotNull;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import yeezus.memory.Memory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class MemoryPane extends Pane {

	private MemoryVisualizer diskVisualizer, ramVisualizer, registersVisualizer;
	private List<Memory> registers;

	MemoryPane( @NotNull Memory disk, @NotNull Memory RAM, @NotNull Memory... registers ) {
		this.registers = new LinkedList<>( Arrays.asList( registers ) );

		// Initialize memory visualizers
		this.diskVisualizer = new MemoryVisualizer( disk );
		this.ramVisualizer = new MemoryVisualizer( RAM );
		this.registersVisualizer = new MemoryVisualizer( registers[0] );

		// Initialize memory labels
		Label diskLabel = new Label( "Disk:" );
		Label ramLabel = new Label( "RAM:" );
		Label registerLabel = new Label( "Registers:" );

		diskLabel.layoutXProperty().setValue( 5 );
		diskLabel.layoutYProperty().setValue( 5 );

		this.diskVisualizer.layoutXProperty().bind( diskLabel.layoutXProperty() );
		this.diskVisualizer.layoutYProperty().bind( diskLabel.layoutYProperty().add( diskLabel.heightProperty() ) );

		this.ramVisualizer.layoutXProperty()
				.bind( this.diskVisualizer.layoutXProperty().add( this.diskVisualizer.widthProperty() ).add( 5 ) );
		this.ramVisualizer.layoutYProperty().bind( this.diskVisualizer.layoutYProperty() );

		this.registersVisualizer.layoutXProperty()
				.bind( this.ramVisualizer.layoutXProperty().add( this.ramVisualizer.widthProperty() ).add( 5 ) );
		this.registersVisualizer.layoutYProperty().bind( this.diskVisualizer.layoutYProperty() );

		ramLabel.layoutXProperty().bind( this.ramVisualizer.layoutXProperty() );
		ramLabel.layoutYProperty().bind( diskLabel.layoutYProperty() );

		registerLabel.layoutXProperty().bind( this.registersVisualizer.layoutXProperty() );
		registerLabel.layoutYProperty().bind( ramLabel.layoutYProperty() );

		this.getChildren().addAll( diskLabel, ramLabel, registerLabel, this.diskVisualizer, this.ramVisualizer,
				this.registersVisualizer );
	}

	public void update() {
		this.diskVisualizer.update();
		this.ramVisualizer.update();
		this.registersVisualizer.update();
	}

	public void setMemory( Memory disk, Memory RAM, Memory[] registers ) {
		this.registers.clear();
		this.registers.addAll( Arrays.asList( registers ) );

		this.diskVisualizer.setMemory( disk );
		this.ramVisualizer.setMemory( RAM );
		this.registersVisualizer.setMemory( registers[0] );
	}
}
