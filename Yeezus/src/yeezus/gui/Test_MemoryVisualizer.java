package yeezus.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import yeezus.memory.Memory;
import yeezus.memory.Word;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_MemoryVisualizer extends Application {
	@Override public void start( Stage primaryStage ) throws Exception {
		int capacity = 1024;
		Memory memory = new Memory( capacity );
		for ( int i = 0; i < capacity; i++ ) {
			memory.write( i, new Word( i ) );
		}
		MemoryVisualizer memoryVisualizer = new MemoryVisualizer( memory );

		Scene scene = new Scene( memoryVisualizer, 120, 300 );
		primaryStage.setScene( scene );
		primaryStage.show();
	}
}
