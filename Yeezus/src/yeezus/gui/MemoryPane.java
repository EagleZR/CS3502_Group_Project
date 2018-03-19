package yeezus.gui;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import yeezus.memory.Memory;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class MemoryPane extends Pane {

	private MemoryVisualizer diskVisualizer, ramVisualizer, registersVisualizer;

	public MemoryPane( Memory disk, Memory RAM, Memory registers ) {
		// Initialize memory visualizers
		diskVisualizer = new MemoryVisualizer( disk );
		ramVisualizer = new MemoryVisualizer( RAM );
		registersVisualizer = new MemoryVisualizer( registers );

		// Initialize memory labels
		Label diskLabel = new Label( "Disk:" );
		Label ramLabel = new Label( "RAM:" );
		Label registerLabel = new Label( "Registers:" );

		diskLabel.layoutXProperty().setValue( 5 );
		diskLabel.layoutYProperty().setValue( 5 );

		diskVisualizer.layoutXProperty().bind( diskLabel.layoutXProperty() );
		diskVisualizer.layoutYProperty().bind( diskLabel.layoutYProperty().add( diskLabel.heightProperty() ) );

		ramVisualizer.layoutXProperty()
				.bind( diskVisualizer.layoutXProperty().add( diskVisualizer.widthProperty() ).add( 5 ) );
		ramVisualizer.layoutYProperty().bind( diskVisualizer.layoutYProperty() );

		registersVisualizer.layoutXProperty()
				.bind( ramVisualizer.layoutXProperty().add( ramVisualizer.widthProperty() ).add( 5 ) );
		registersVisualizer.layoutYProperty().bind( diskVisualizer.layoutYProperty() );

		ramLabel.layoutXProperty().bind( ramVisualizer.layoutXProperty() );
		ramLabel.layoutYProperty().bind( diskLabel.layoutYProperty() );

		registerLabel.layoutXProperty().bind( registersVisualizer.layoutXProperty() );
		registerLabel.layoutYProperty().bind( ramLabel.layoutYProperty() );

		this.getChildren()
				.addAll( diskLabel, ramLabel, registerLabel, diskVisualizer, ramVisualizer, registersVisualizer );
	}

	public void update() {
		this.diskVisualizer.update();
		this.ramVisualizer.update();
		this.registersVisualizer.update();
	}
}
