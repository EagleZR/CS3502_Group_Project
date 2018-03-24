package yeezus.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.util.ArrayList;

public class PCB_List extends ListView<PCB_Pane> {

	ObservableList<PCB_Pane> panes;

	public PCB_List( TaskManager taskManager ) {
		ArrayList<PCB_Pane> pcbs = new ArrayList<>();
		for ( PCB pcb : taskManager ) {
			pcbs.add( new PCB_Pane( pcb ) );
		}
		this.panes = FXCollections.observableList( pcbs );
		this.setItems( this.panes );
	}

	private void update() {
		for ( PCB_Pane pane : this.panes ) {
			pane.update();
		}
	}
}
