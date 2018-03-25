package yeezus.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.util.LinkedList;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
class PCB_List extends ListView<PCB_Pane> implements Updatable {

	private ObservableList<PCB_Pane> panes;

	PCB_List() {
		this.panes = FXCollections.observableList( new LinkedList<PCB_Pane>() );
		this.setItems( this.panes );
		update();
	}

	@Override public void update() {
		this.panes.clear();
		for ( PCB pcb : TaskManager.INSTANCE ) {
			this.panes.add( new PCB_Pane( pcb ) );
		}
		for ( PCB_Pane pane : this.panes ) {
			pane.update();
		}
	}
}
