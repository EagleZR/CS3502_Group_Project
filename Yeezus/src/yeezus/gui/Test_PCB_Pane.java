package yeezus.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import yeezus.pcb.TaskManager;

public class Test_PCB_Pane extends Application {
	@Override public void start( Stage primaryStage ) throws Exception {
		TaskManager taskManager = TaskManager.INSTANCE;
		taskManager.addPCB( 0, 0, 15, 34, 61, 15, 7 );
		PCB_Pane pcb_pane = new PCB_Pane( taskManager.getPCB( 0 ) );
		Scene scene = new Scene( pcb_pane, 200, 100 );
		primaryStage.setScene( scene );
		primaryStage.show();

	}

	@Override public void stop() throws Exception {
		super.stop();
		TaskManager.INSTANCE.reset();
	}
}
