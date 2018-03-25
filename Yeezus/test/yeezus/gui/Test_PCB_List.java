package yeezus.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import yeezus.driver.Driver;
import yeezus.memory.Memory;

import java.io.File;
import java.net.URLDecoder;

public class Test_PCB_List extends Application {
	@Override public void start( Stage primaryStage ) throws Exception {
		Memory disk = new Memory( 2048 );
		Driver.loadFile( disk, new File( URLDecoder
				.decode( this.getClass().getClassLoader().getResource( "Program-File.txt" ).getFile(), "UTF-8" ) ) );

		PCB_List pcbs = new PCB_List();

		Scene scene = new Scene( pcbs, 300, 500 );
		primaryStage.setScene( scene );
		primaryStage.show();

	}
}