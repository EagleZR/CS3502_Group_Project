package yeezus.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import yeezus.driver.Driver;
import yeezus.memory.Memory;
import yeezus.pcb.TaskManager;

import java.io.File;

public class Test_PCB_List extends Application {
	@Override public void start( Stage primaryStage ) throws Exception {
		TaskManager taskManager = TaskManager.INSTANCE;
		Memory disk = new Memory( 2048 );
		Driver.loadFile( disk, new File( "src/yeezus/Program-File.txt" ) );

		PCB_List pcbs = new PCB_List( taskManager );

		Scene scene = new Scene( pcbs, 300, 500 );
		primaryStage.setScene( scene );
		primaryStage.show();

	}
}
