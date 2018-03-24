package yeezus.gui;

import com.sun.istack.internal.NotNull;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import yeezus.cpu.CPU;
import yeezus.driver.CPUSchedulingPolicy;
import yeezus.driver.Driver;
import yeezus.memory.Memory;
import yeezus.memory.Word;
import yeezus.pcb.TaskManager;

public class GUI extends Application {

	private CPUSchedulingPolicy schedulingPolicy;
	private int numCPUs = 1;

	@Override public void start( Stage primaryStage ) {

		Pane contentPane = new Pane();

		MemoryPane memoryPane = new MemoryPane( new Memory( 2048 ), new Memory( 1024 ), new Memory( 16 ) );
		memoryPane.layoutXProperty().setValue( 5 );
		memoryPane.layoutYProperty().setValue( 5 );

		contentPane.getChildren().add( memoryPane );

		// PCBs

		// CPUs

		Scene scene = new Scene( contentPane, 1500, 600 );
		primaryStage.setScene( scene );
		primaryStage.show();
	}

	private void reset() {
		TaskManager.INSTANCE.reset();
		CPU.reset();
		Driver.reset();


	}

	private void execute() {

	}

	private void clearMemory( @NotNull Memory memory ) {
		for ( int i = 0; i < memory.getCapacity(); i++ ) {
			memory.write( i, new Word( 0 ) );
		}
	}
}
