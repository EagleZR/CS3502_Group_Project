package yeezus.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import yeezus.memory.InvalidAddressException;
import yeezus.memory.Memory;

import java.util.ArrayList;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class MemoryVisualizer extends ListView<WordVisualizer> { // TODO Use ListView so the focus can be set

	private Memory memory;
	private ObservableList<WordVisualizer> wordVisualizers;

	public MemoryVisualizer( Memory memory ) throws InvalidAddressException {
		this.memory = memory;
		setup();
	}

	private void setup() {
		ArrayList<WordVisualizer> words = new ArrayList<>();
		for ( int i = 0; i < memory.getCapacity(); i++ ) {
			WordVisualizer word = new WordVisualizer( i, memory.read( i ) );
			words.add( word );
		}
		this.wordVisualizers = FXCollections.observableList( words );
		this.setItems( this.wordVisualizers );
		update();
	}

	public void update() throws InvalidAddressException {
		for ( int i = 0; i < this.memory.getCapacity(); i++ ) {
			this.wordVisualizers.get( i ).update( this.memory.read( i ) );
		}
	}
}
