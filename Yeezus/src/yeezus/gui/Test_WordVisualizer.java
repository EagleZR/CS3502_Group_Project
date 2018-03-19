package yeezus.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import yeezus.memory.Word;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_WordVisualizer extends Application {
	@Override public void start( Stage primaryStage ) throws Exception {
		WordVisualizer word = new WordVisualizer( 0, new Word( "0x12310321" ) );
		word.setPrefWidth( 200 );
		Scene scene = new Scene( word, 200, 100 );
		primaryStage.setScene( scene );
		primaryStage.show();
	}

}
