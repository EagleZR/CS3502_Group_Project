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
public class MemoryVisualizer extends ListView<WordVisualizer> implements Updatable {

	private Memory memory;
	private ObservableList<WordVisualizer> wordVisualizers;

	MemoryVisualizer( Memory memory ) throws InvalidAddressException {
		this.memory = memory;
		setup();
	}

	private void setup() {
		ArrayList<WordVisualizer> words = new ArrayList<>();
		for ( int i = 0; i < this.memory.getCapacity(); i++ ) {
			WordVisualizer word = new WordVisualizer( i, this.memory.read( i ) );
			words.add( word );
		}
		this.wordVisualizers = FXCollections.observableList( words );
		this.setItems( this.wordVisualizers );
		update();
	}

	@Override public void update() throws InvalidAddressException {
		for ( int i = 0; i < this.memory.getCapacity(); i++ ) {
			this.wordVisualizers.get( i ).update( this.memory.read( i ) );
		}
	}

	void setMemory( Memory memory ) {
		this.memory = memory;
		update();
	}
}
