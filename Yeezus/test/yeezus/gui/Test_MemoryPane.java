package yeezus.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import yeezus.memory.Memory;

public class Test_MemoryPane extends Application {
	@Override public void start( Stage primaryStage ) {
		MemoryPane pane = new MemoryPane( new Memory( 2048 ), new Memory( 1024 ), Color.LIGHTGRAY, Color.BLUE,
				new Memory( 16 ) );
		Scene scene = new Scene( pane, 800, 500 );
		primaryStage.setScene( scene );
		primaryStage.show();
	}
}
