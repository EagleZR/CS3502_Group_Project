package yeezus.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import yeezus.driver.Driver;
import yeezus.memory.Memory;

import java.io.File;
import java.net.URLDecoder;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_TaskManagerPane extends Application {
	@Override public void start( Stage primaryStage ) throws Exception {
		Memory disk = new Memory( 2048 );
		Driver.loadFile( disk, new File( URLDecoder
				.decode( this.getClass().getClassLoader().getResource( "Program-File.txt" ).getFile(), "UTF-8" ) ) );

		TaskManagerPane taskManagerPane = new TaskManagerPane( Color.LIGHTGRAY, Color.BLUE );

		Scene scene = new Scene( taskManagerPane, 175, 400 );
		primaryStage.setScene( scene );
		primaryStage.show();
	}
}
