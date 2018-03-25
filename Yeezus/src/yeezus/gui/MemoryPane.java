package yeezus.gui;

import com.sun.istack.internal.NotNull;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import yeezus.memory.Memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class MemoryPane extends FramedPane implements Updatable {

	private MemoryVisualizer diskVisualizer, ramVisualizer, registersVisualizer;
	private List<Memory> registers;
	private ComboBox<Integer> registerSelect;
	private Label registerLabel;

	MemoryPane( @NotNull Memory disk, @NotNull Memory RAM, Color backgroundColor, Color frameColor,
			@NotNull Memory... registers ) {
		super( backgroundColor, frameColor );
		this.registers = new LinkedList<>( Arrays.asList( registers ) );

		// Initialize memory visualizers
		this.diskVisualizer = new MemoryVisualizer( disk );
		this.ramVisualizer = new MemoryVisualizer( RAM );
		this.registersVisualizer = new MemoryVisualizer( registers[0] );

		Label memoryLabel = new Label( "Memory" );
		memoryLabel.setFont( new Font( 24 ) );
		memoryLabel.layoutXProperty().setValue( 5 );
		memoryLabel.layoutYProperty().setValue( 5 );
		this.getChildren().add( memoryLabel );

		// Initialize memory labels
		Label diskLabel = new Label( "Disk:" );
		Label ramLabel = new Label( "RAM:" );
		this.registerLabel = new Label( "Registers for CPU" );

		diskLabel.layoutXProperty().setValue( 5 );
		diskLabel.layoutYProperty().bind( memoryLabel.layoutYProperty().add( memoryLabel.heightProperty() ).add( 5 ) );

		this.diskVisualizer.layoutXProperty().bind( diskLabel.layoutXProperty() );
		this.diskVisualizer.layoutYProperty().bind( diskLabel.layoutYProperty().add( diskLabel.heightProperty() ) );
		this.diskVisualizer.prefWidthProperty().bind( this.widthProperty().subtract( 20 ).divide( 3 ) );
		this.diskVisualizer.prefHeightProperty().bind( this.heightProperty().subtract( memoryLabel.heightProperty() )
				.subtract( diskLabel.heightProperty() ).subtract( 15 ) );

		this.ramVisualizer.layoutXProperty()
				.bind( this.diskVisualizer.layoutXProperty().add( this.diskVisualizer.widthProperty() ).add( 5 ) );
		this.ramVisualizer.layoutYProperty().bind( this.diskVisualizer.layoutYProperty() );
		this.ramVisualizer.prefWidthProperty().bind( this.diskVisualizer.widthProperty() );
		this.ramVisualizer.prefHeightProperty().bind( this.diskVisualizer.heightProperty() );

		this.registersVisualizer.layoutXProperty()
				.bind( this.ramVisualizer.layoutXProperty().add( this.ramVisualizer.widthProperty() ).add( 5 ) );
		this.registersVisualizer.layoutYProperty().bind( this.diskVisualizer.layoutYProperty() );
		this.registersVisualizer.prefWidthProperty().bind( this.diskVisualizer.widthProperty() );
		this.registersVisualizer.prefHeightProperty().bind( this.diskVisualizer.heightProperty() );

		ObservableList<Integer> registerValues = FXCollections.observableList( new ArrayList<>() );
		registerValues.add( 0 );
		this.registerSelect = new ComboBox<>( registerValues );
		this.registerSelect.setValue( registerValues.get( 0 ) );
		this.registerSelect.setOnAction(
				e -> this.registersVisualizer.setMemory( this.registers.get( this.registerSelect.getValue() ) ) );
		this.registerSelect.layoutXProperty()
				.bind( this.registerLabel.layoutXProperty().add( this.registerLabel.widthProperty() ).add( 5 ) );
		this.registerSelect.layoutYProperty()
				.bind( this.registerLabel.layoutYProperty().add( this.registerLabel.heightProperty() )
						.subtract( this.registerSelect.heightProperty() ).subtract( 1 ) );

		ramLabel.layoutXProperty().bind( this.ramVisualizer.layoutXProperty() );
		ramLabel.layoutYProperty().bind( diskLabel.layoutYProperty() );

		this.registerLabel.layoutXProperty().bind( this.registersVisualizer.layoutXProperty() );
		this.registerLabel.layoutYProperty().bind( ramLabel.layoutYProperty() );

		this.getChildren().addAll( diskLabel, ramLabel, this.registerLabel, this.diskVisualizer, this.ramVisualizer,
				this.registersVisualizer, this.registerSelect );
	}

	@Override public void update() {
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
		ArrayList<Integer> newList = new ArrayList<>();
		for ( int i = 0; i < registers.length; i++ ) {
			newList.add( i );
		}
		updateComboBox( newList );
	}

	private void updateComboBox( ArrayList<Integer> newList ) {
		this.getChildren().remove( this.registerSelect );
		this.registerSelect.setOnAction( e -> {
			// Do Nothing
		} );

		ObservableList<Integer> registerValues = FXCollections.observableList( newList );
		this.registerSelect = new ComboBox<>( registerValues );
		this.registerSelect.setValue( registerValues.get( 0 ) );
		this.registerSelect.setOnAction(
				e -> this.registersVisualizer.setMemory( this.registers.get( this.registerSelect.getValue() ) ) );
		this.registerSelect.layoutXProperty()
				.bind( this.registerLabel.layoutXProperty().add( this.registerLabel.widthProperty() ).add( 5 ) );
		this.registerSelect.layoutYProperty()
				.bind( this.registerLabel.layoutYProperty().add( this.registerLabel.heightProperty() )
						.subtract( this.registerSelect.heightProperty() ).subtract( 1 ) );
		this.getChildren().add( this.registerSelect );
	}
}
