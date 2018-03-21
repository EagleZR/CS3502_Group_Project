package yeezus.gui;

import com.sun.istack.internal.NotNull;
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
import java.util.ArrayList;

public class GUI extends Application {

	private Memory disk;
	private Memory RAM;
	private Memory registers;
	private MMU mmu;

	private CPUSchedulingPolicy schedulingPolicy;
	private int numCPUs = 1;
	private ArrayList<Driver> drivers;

	@Override public void start( Stage primaryStage ) throws Exception {
		this.drivers = new ArrayList<>();

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

		Scene scene = new Scene( contentPane, 1500, 600 );
		primaryStage.setScene( scene );
		primaryStage.show();
	}

	private void reset() throws Exception {
		TaskManager.INSTANCE.reset();
		CPU.reset();
		Driver.reset();

		// Re-initialize memory
		clearMemory( registers );
		clearMemory( RAM );
		clearMemory( disk );
		this.mmu.reset();

		this.drivers.clear();
		for ( int i = 0; i < this.numCPUs; i++ ) {
			this.drivers.add( new Driver( i, disk, mmu, registers, this.schedulingPolicy ) );
		}

		Driver.loadFile( disk, new File( "src/yeezus/Program-File.txt" ) );
	}

	private void execute() throws Exception {
		reset();

		// TODO Enact changes

		Thread[] threads = new Thread[numCPUs];

		// Start Drivers
		for ( int i = 0; i < numCPUs; i++ ) {
			threads[i] = new Thread( drivers.get( i ) );
			threads[i].run();
		}

		for ( Thread thread : threads ) {
			thread.join( 500 );
		}
	}

	private void clearMemory( @NotNull Memory memory ) {
		for ( int i = 0; i < memory.getCapacity(); i++ ) {
			memory.write( i, new Word( 0 ) );
		}
	}
}
