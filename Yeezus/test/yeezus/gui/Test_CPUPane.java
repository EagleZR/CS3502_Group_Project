package yeezus.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_CPUPane extends Application {
	@Override public void start( Stage primaryStage ) {
		CPUPane cpuPane = new CPUPane( Color.WHITE, Color.BLACK );
		Scene scene = new Scene( cpuPane );
		primaryStage.setScene( scene );
		primaryStage.show();
	}
}
