package yeezus.gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import yeezus.Yeezus;
import yeezus.cpu.CPU;
import yeezus.driver.CPUSchedulingPolicy;
import yeezus.driver.Driver;
import yeezus.memory.Memory;
import yeezus.pcb.TaskManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class GUI extends Application implements Updatable {

	private MemoryPane memoryPane;
	private TaskManagerPane taskManagerPane;
	private CPUsPane cpusPane;
	private CPUSchedulingPolicy schedulingPolicy = CPUSchedulingPolicy.FCFS;
	private ThreadCount threadCount = ThreadCount.NONE;
	private boolean debugging = false;
	private Color backgroundColor = Color.NAVY;
	private Color paneColor = Color.LIGHTGRAY;
	private Color frameColor = Color.DARKGRAY;
	private Yeezus yeezus;
	private boolean isSet = false;

	@Override public void start( Stage primaryStage ) throws IOException {
		Pane contentPane = new Pane();

		// Background
		javafx.scene.shape.Rectangle background = new Rectangle();
		background.setFill( this.backgroundColor );
		background.layoutXProperty().setValue( 0 );
		background.layoutYProperty().setValue( 0 );
		background.widthProperty().bind( contentPane.widthProperty() );
		background.heightProperty().bind( contentPane.heightProperty() );
		contentPane.getChildren().add( background );

		// Memory
		this.memoryPane = new MemoryPane( new Memory( 2048 ), new Memory( 1024 ), this.paneColor, this.frameColor,
				new Memory( 16 ) );
		this.memoryPane.layoutXProperty().setValue( 5 );
		this.memoryPane.layoutYProperty().setValue( 5 );
		this.memoryPane.prefWidthProperty().setValue( 500 );
		this.memoryPane.prefHeightProperty().setValue( 400 );
		contentPane.getChildren().add( this.memoryPane );

		// PCBs
		this.taskManagerPane = new TaskManagerPane( this.paneColor, this.frameColor );
		this.taskManagerPane.layoutXProperty()
				.bind( this.memoryPane.layoutXProperty().add( this.memoryPane.widthProperty() ).add( 5 ) );
		this.taskManagerPane.layoutYProperty().bind( this.memoryPane.layoutYProperty() );
		this.taskManagerPane.prefWidthProperty().setValue( 215 );
		this.taskManagerPane.prefHeightProperty().bind( this.memoryPane.heightProperty() );
		contentPane.getChildren().add( this.taskManagerPane );

		// CPUs
		this.cpusPane = new CPUsPane( this.paneColor, this.frameColor );
		this.cpusPane.layoutXProperty().bind( this.taskManagerPane.layoutXProperty() );
		this.cpusPane.layoutYProperty()
				.bind( this.taskManagerPane.layoutYProperty().add( this.taskManagerPane.heightProperty() ).add( 5 ) );
		this.cpusPane.prefWidthProperty().bind( this.taskManagerPane.widthProperty() );
		this.cpusPane.prefHeightProperty()
				.bind( contentPane.heightProperty().subtract( this.taskManagerPane.heightProperty() ).subtract( 15 ) );
		contentPane.getChildren().add( this.cpusPane );

		// Control Pane
		ControlPane controlPanel = new ControlPane( this.paneColor, this.frameColor );
		controlPanel.layoutXProperty().bind( this.memoryPane.layoutXProperty() );
		controlPanel.layoutYProperty().bind( this.cpusPane.layoutYProperty() );
		controlPanel.prefWidthProperty().bind( this.memoryPane.widthProperty() );
		controlPanel.prefHeightProperty().bind( this.cpusPane.heightProperty() );
		contentPane.getChildren().add( controlPanel );

		// Display
		Scene scene = new Scene( contentPane, 720, 650 );
		primaryStage.setScene( scene );
		Image image = new Image( new FileInputStream( new File( URLDecoder
				.decode( this.getClass().getClassLoader().getResource( "Kanye.png" ).getFile(), "UTF-8" ) ) ) );
		primaryStage.getIcons().add( image );
		primaryStage.setResizable( false );
		primaryStage.show();
	}

	private void reset() {
		TaskManager.INSTANCE.reset();
		CPU.reset();
		Driver.reset();
	}

	private void execute() throws Exception {
		if ( !this.isSet ) {
			setup();
		}
		this.yeezus.run();
	}

	private void setup() throws Exception {
		reset();
		this.isSet = true;
		this.yeezus = new Yeezus( this.threadCount.count, this.schedulingPolicy, 2048, 1024, 100, 16, this.debugging );
		this.memoryPane.setMemory( this.yeezus.getDisk(), this.yeezus.getRAM(), this.yeezus.getRegisters() );
		this.cpusPane.setCPUs( this.yeezus.getCPUs() );
	}

	@Override public void update() {
		this.memoryPane.update();
		this.taskManagerPane.update();
		this.cpusPane.update();
	}

	private enum ThreadCount {
		NONE( 0, "None" ), ONE( 1, "1" ), FOUR( 4, "4" );

		private int count;
		private String text;

		ThreadCount( int count, String text ) {
			this.count = count;
			this.text = text;
		}

		@Override public String toString() {
			return this.text;
		}

		public int getCount() {
			return this.count;
		}
	}

	private class ControlPane extends FramedPane {

		ControlPane( Color backgroundColor, Color frameColor ) {
			super( backgroundColor, frameColor );
			Button executeButton = new Button( "Execute" );
			Button setupButton = new Button( "Setup" );
			Button cancelButton = new Button( "Cancel" );

			// Schedule Label
			Label scheduleLabel = new Label( "Scheduling Policy:" );
			scheduleLabel.layoutXProperty().setValue( 10 );
			scheduleLabel.layoutYProperty().setValue( 10 );
			this.getChildren().add( scheduleLabel );

			// CPU Label
			Label cpuLabel = new Label( "# of CPUs:" );
			cpuLabel.layoutXProperty().bind( scheduleLabel.layoutXProperty() );
			cpuLabel.layoutYProperty()
					.bind( scheduleLabel.layoutYProperty().add( scheduleLabel.heightProperty() ).add( 10 ) );
			this.getChildren().add( cpuLabel );

			// Debug Label
			Label debugLabel = new Label( "Debugging:" );
			debugLabel.layoutXProperty().bind( cpuLabel.layoutXProperty() );
			debugLabel.layoutYProperty().bind( cpuLabel.layoutYProperty().add( cpuLabel.heightProperty() ).add( 10 ) );
			this.getChildren().add( debugLabel );

			// Schedule Selector
			ObservableList<CPUSchedulingPolicy> policyList = FXCollections
					.observableArrayList( CPUSchedulingPolicy.values() );
			ComboBox<CPUSchedulingPolicy> policyComboBox = new ComboBox<>( policyList );
			policyComboBox.layoutXProperty()
					.bind( scheduleLabel.layoutXProperty().add( scheduleLabel.widthProperty().add( 25 ) ) );
			policyComboBox.layoutYProperty()
					.bind( scheduleLabel.layoutYProperty().add( scheduleLabel.heightProperty().divide( 2 ) )
							.subtract( policyComboBox.heightProperty().divide( 2 ) ) );
			policyComboBox.setValue( policyList.get( 0 ) );
			policyComboBox.setOnAction( e -> {
				GUI.this.schedulingPolicy = policyComboBox.getValue();
				GUI.this.isSet = false;
				setupButton.setVisible( true );
			} );
			this.getChildren().add( policyComboBox );

			// CPU Selector
			ObservableList<ThreadCount> cpuList = FXCollections.observableArrayList( ThreadCount.values() );
			ComboBox<ThreadCount> cpuComboBox = new ComboBox<>( cpuList );
			cpuComboBox.layoutXProperty().bind( policyComboBox.layoutXProperty().add( policyComboBox.widthProperty() )
					.subtract( cpuComboBox.widthProperty() ) );
			cpuComboBox.layoutYProperty().bind( cpuLabel.layoutYProperty().add( cpuLabel.heightProperty().divide( 2 ) )
					.subtract( cpuComboBox.heightProperty().divide( 2 ) ) );
			cpuComboBox.setValue( cpuList.get( 0 ) );
			cpuComboBox.setOnAction( e -> {
				GUI.this.threadCount = cpuComboBox.getValue();
				GUI.this.isSet = false;
				setupButton.setVisible( true );
			} );
			this.getChildren().add( cpuComboBox );

			Label debugStatusLabel = new Label( "Off" );
			debugStatusLabel.setTextFill( Color.RED );

			// Debugger Check
			CheckBox debugCheckbox = new CheckBox();
			debugCheckbox.layoutXProperty().bind( cpuComboBox.layoutXProperty().add( cpuComboBox.widthProperty() )
					.subtract( debugCheckbox.widthProperty() ) );
			debugCheckbox.layoutYProperty()
					.bind( debugLabel.layoutYProperty().add( debugLabel.heightProperty().divide( 2 ) )
							.subtract( debugCheckbox.heightProperty().divide( 2 ) ) );
			debugCheckbox.setOnAction( e -> {
				GUI.this.debugging = debugCheckbox.isSelected();
				debugStatusLabel.setText( ( GUI.this.debugging ? "On" : "Off" ) );
				debugStatusLabel.setTextFill( ( GUI.this.debugging ? Color.GREEN : Color.RED ) );
				GUI.this.isSet = false;
				setupButton.setVisible( true );
			} );
			this.getChildren().add( debugCheckbox );

			// Debug Status Label
			debugStatusLabel.layoutXProperty()
					.bind( debugCheckbox.layoutXProperty().subtract( debugStatusLabel.widthProperty() ).subtract( 5 ) );
			debugStatusLabel.layoutYProperty().bind( debugLabel.layoutYProperty() );
			this.getChildren().add( debugStatusLabel );

			/*
			// Output Label
			Label outputLabel = new Label( "Output:" );
			outputLabel.layoutXProperty().bind( debugLabel.layoutXProperty() );
			outputLabel.layoutYProperty()
					.bind( debugLabel.layoutYProperty().add( debugLabel.heightProperty() ).add( 10 ) );
			this.getChildren().add( outputLabel );

			// Output Line
			Line outputLine = new Line();
			outputLine.startXProperty().bind( outputLabel.layoutXProperty() );
			outputLine.startYProperty().bind( outputLabel.layoutYProperty().add( outputLabel.heightProperty() ) );
			outputLine.endXProperty().bind( this.widthProperty().subtract( 10 ) );
			outputLine.endYProperty().bind( outputLine.startYProperty() );
			this.getChildren().add( outputLine );

			// Completion Time
			*/

			// Execute Button
			executeButton.layoutXProperty()
					.bind( setupButton.layoutXProperty().subtract( executeButton.widthProperty() ).subtract( 10 ) );
			executeButton.layoutYProperty().bind( setupButton.layoutYProperty() );
			executeButton.setOnAction( e -> {
				try {
					if ( GUI.this.debugging ) {
						cancelButton.setVisible( true );
					}
					GUI.this.execute();
					GUI.this.update();
				} catch ( Exception e1 ) {
					e1.printStackTrace(); // TODO Display pop-up
				}
			} );
			this.getChildren().add( executeButton );

			// Setup button
			setupButton.layoutXProperty()
					.bind( this.widthProperty().subtract( setupButton.widthProperty() ).subtract( 10 ) );
			setupButton.layoutYProperty().setValue( 10 );
			setupButton.setOnAction( e -> {
				try {
					GUI.this.setup();
					setupButton.setVisible( false );
				} catch ( Exception e1 ) {
					e1.printStackTrace(); // TODO Display pop-up
				}
			} );
			this.getChildren().add( setupButton );

			// Cancel button
			cancelButton.layoutXProperty().bind( executeButton.layoutXProperty().add( executeButton.widthProperty() )
					.subtract( cancelButton.widthProperty() ) );
			cancelButton.layoutYProperty()
					.bind( setupButton.layoutYProperty().add( setupButton.heightProperty() ).add( 5 ) );
			cancelButton.setOnAction( e -> {
				try {
					cancelButton.setVisible( false );
				} catch ( Exception e1 ) {
					e1.printStackTrace(); // TODO Display pop-up
				}
			} );
			cancelButton.setVisible( false );
			this.getChildren().add( cancelButton );

			// Print button
			Button printButton = new Button( "Print" );
			printButton.layoutXProperty()
					.bind( this.widthProperty().subtract( printButton.widthProperty() ).subtract( 10 ) );
			printButton.layoutYProperty()
					.bind( setupButton.layoutYProperty().add( setupButton.heightProperty() ).add( 5 ) );
			printButton.setOnAction( e -> {
				try {
					GUI.this.yeezus.printData();
					printButton.setVisible( false );
				} catch ( Exception e1 ) {
					e1.printStackTrace(); // TODO Display pop-up
				}
			} );
			printButton.setVisible( false );
			this.getChildren().add( printButton );
		}
	}
}
