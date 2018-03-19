package yeezus.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import yeezus.cpu.CPU;
import yeezus.driver.CPUSchedulingPolicy;
import yeezus.driver.Driver;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.memory.Word;
import yeezus.pcb.TaskManager;

import java.io.File;

public class GUI extends Application {

	Memory disk;
	Memory RAM;
	Memory registers;
	MMU mmu;

	CPUSchedulingPolicy schedulingPolicy;
	int numCPUs;

	@Override public void start( Stage primaryStage ) throws Exception {
		Pane contentPane = new Pane();

		// Memory
		disk = new Memory( 2048 );
		RAM = new Memory( 1024 );
		mmu = new MMU( RAM );
		registers = new Memory( 16 );

		MemoryPane memoryPane = new MemoryPane( disk, RAM, registers );
		memoryPane.layoutXProperty().setValue( 5 );
		memoryPane.layoutYProperty().setValue( 5 );

		contentPane.getChildren().add( memoryPane );

		// PCBs

		// CPUs

		Scene scene = new Scene( contentPane, 1000, 700 );
		primaryStage.setScene( scene );
		primaryStage.show();
	}

	private void initialize() throws Exception {
		TaskManager.INSTANCE.reset();
		CPU.reset();
		Driver.reset();

		// Re-initialize memory
		clearMemory( registers );
		clearMemory( RAM );
		clearMemory( disk );
		this.mmu.reset();

		Driver.loadFile( disk, new File( "src/yeezus/Program-File.txt" ) );
	}

	private void execute() throws Exception {

	}

	private void clearMemory( Memory memory ) {
		for ( int i = 0; i < memory.getCapacity(); i++ ) {
			memory.write( i, new Word( 0 ) );
		}
	}
}
