package yeezus;

import com.sun.istack.internal.NotNull;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.usermodel.*;
import yeezus.cpu.CPU;
import yeezus.driver.*;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

public class Yeezus implements Runnable {

	// Memory Data
	private final static int DISK_SIZE = 2048;
	private final static int RAM_SIZE = 1024;
	private final static int CACHE_SIZE = 100;
	private final static int REGISTER_SIZE = 16;

	// Instance variables
	private final int numCPUs;
	private final CPUSchedulingPolicy policy;
	private final boolean debugging;
	private boolean firstTime = true;
	private boolean systemFinished;
	private long startTime, endTime;

	private Memory disk;
	private Memory RAM;
	private Memory[] registers;
	private AbstractDriver driver;

	public Yeezus( @NotNull CPUSchedulingPolicy policy, int diskSize, int ramSize, int cacheSize, int registerSize,
			boolean debugging ) throws Exception {
		this( 0, policy, diskSize, ramSize, cacheSize, registerSize, debugging );
	}

	public Yeezus( int numCPUs, @NotNull CPUSchedulingPolicy policy, int diskSize, int ramSize, int cacheSize,
			int registerSize ) throws Exception {
		this( numCPUs, policy, diskSize, ramSize, cacheSize, registerSize, false );
	}

	public Yeezus( @NotNull CPUSchedulingPolicy policy, int diskSize, int ramSize, int cacheSize, int registerSize )
			throws Exception {
		this( 0, policy, diskSize, ramSize, cacheSize, registerSize, false );
	}

	public Yeezus( int numCPUs, @NotNull CPUSchedulingPolicy policy, int diskSize, int ramSize, int cacheSize,
			int registerSize, boolean debugging ) throws Exception {
		this.numCPUs = numCPUs;
		this.policy = policy;
		this.debugging = debugging;

		try {
			// Initialize memory
			this.disk = new Memory( diskSize );

			// Initialize and create Driver
			Driver.loadFile( this.disk, new File( ( URLDecoder.decode(
					Objects.requireNonNull( Yeezus.class.getClassLoader().getResource( "Program-File.txt" ) ).getFile(),
					"UTF-8" ) ) ) );
			if ( debugging ) {
				DebuggingDriver driver = new DebuggingDriver( this.disk, registerSize, cacheSize, ramSize,
						this.policy );
				this.driver = driver;
				this.registers = new Memory[1];
				this.registers[0] = driver.getRegisters();
			} else if ( this.numCPUs == 0 ) {
				Driver driver = new Driver( this.disk, registerSize, cacheSize, ramSize, this.policy );
				this.driver = driver;
				this.registers = new Memory[1];
				this.registers[0] = driver.getRegisters();
			} else {
				ThreadedDriver driver = new ThreadedDriver( this.numCPUs, this.disk, registerSize, cacheSize, ramSize,
						this.policy );
				this.driver = driver;
				this.registers = driver.getRegisters();
			}

			this.RAM = this.driver.getRAM();
		} catch ( Exception e ) {
			System.err.println( "An exception occurred in system initialization." );
			e.printStackTrace();
			throw e;
		}
	}

	public static void main( String[] args ) {
		int[] cpuSet = { 0, 1, 4 };
		for ( CPUSchedulingPolicy policy : CPUSchedulingPolicy.values() ) {
			for ( int numCPUs : cpuSet ) {
				try {
					Yeezus system = new Yeezus( numCPUs, policy, DISK_SIZE, RAM_SIZE, CACHE_SIZE, REGISTER_SIZE,
							false );
					system.run();
					system.printData();
					CPU.reset();
					TaskManager.INSTANCE.reset();
					Driver.reset();
				} catch ( Exception e ) {
					System.out.println( "There was an issue trying to run system " + policy + "-" + numCPUs );
					e.printStackTrace();
				}
			}
		}
	}

	public void run() {
		if ( !this.systemFinished ) {
			if ( this.debugging ) {
				debugRun();
			} else {
				continuousRun();
			}
		}
	}

	private void continuousRun() {
		// Log start time
		this.startTime = System.nanoTime();

		try {
			this.driver.run();
			this.systemFinished = true;
		} catch ( Exception e ) {
			System.err.println( "An exception occurred in system Execution." );
			e.printStackTrace();
			this.driver.dumpData();
			this.systemFinished = true;
			return;
		}
		// Log end time
		this.endTime = System.nanoTime();

		System.out
				.println( "The system completed in " + ( this.endTime - this.startTime ) / 1000000 + " milliseconds." );
	}

	public void printData() {
		// Print out the disk
		try {
			File output = new File( "output/" + this.policy + "_" + this.numCPUs + "_Output_File.txt" );
			if ( !output.exists() && !output.createNewFile() ) {
				throw new Exception( "The output file could not be created." ); // idk how else to exit a try block
			}
			PrintStream out = new PrintStream( new FileOutputStream( output ) );
			for ( PCB pcb : TaskManager.INSTANCE ) {
				out.println( "****Job " + pcb.getPID() + "****" );
				// Instructions
				out.println( "Job " + pcb.getPID() + " Instructions:" );
				for ( int i = 0; i < pcb.getInstructionsLength(); i++ ) {
					out.println( this.disk.read( pcb.getInstructionDiskAddress() + i ) );
				}
				out.println();

				// Input Buffer
				out.println( "Job " + pcb.getPID() + " Input Buffer:" );
				for ( int i = 0; i < pcb.getInputBufferLength(); i++ ) {
					out.println( this.disk.read( pcb.getInputBufferDiskAddress() + i ) );
				}
				out.println();

				// Output Buffer
				out.println( "Job " + pcb.getPID() + " Output Buffer:" );
				for ( int i = 0; i < pcb.getOutputBufferLength(); i++ ) {
					out.println( this.disk.read( pcb.getOutputBufferDiskAddress() + i ) );
				}
				out.println();

				// Temp Buffer
				out.println( "Job " + pcb.getPID() + " Temp Buffer:" );
				for ( int i = 0; i < pcb.getTempBufferLength(); i++ ) {
					out.println( this.disk.read( pcb.getTempBufferDiskAddress() + i ) );
				}
				out.println( "______________________\n" );
			}
		} catch ( Exception e ) {
			System.err.println( "An exception occurred while writing to the output file." );
			e.printStackTrace();
		}

		// Print out runtime information
		try {
			excelPrint( this.driver );
		} catch ( Exception e ) {
			System.err.println( "An exception occurred while printing the process data to the Excel file." );
			e.printStackTrace();
			try {
				consolePrint( this.driver );
			} catch ( Exception e1 ) {
				System.out.println(
						"An exception occurred while printing the process data to the Excel file. The runtime data cannot be printed." );
				e1.printStackTrace();
			}
		}
	}

	private void debugRun() {
		if ( this.firstTime ) {
			// Log start time
			this.startTime = System.nanoTime();
			this.firstTime = false;
		}
		if ( !this.systemFinished ) {
			try {
				this.driver.run();
				this.systemFinished = ( (DebuggingDriver) this.driver ).isFinished();
			} catch ( Exception e ) {
				System.err.println( "An exception occurred in system Execution." );
				e.printStackTrace();
				this.driver.dumpData();
				this.systemFinished = true;
			}
		} else {
			// Log end time
			this.endTime = System.nanoTime();

			System.out.println(
					"The system completed in " + ( this.endTime - this.startTime ) / 1000000 + " milliseconds." );
		}
	}

	private void excelPrint( @NotNull AbstractDriver driver ) throws Exception {
		// https://poi.apache.org/spreadsheet/quick-guide.html#NewWorkbook
		// https://www.callicoder.com/java-read-excel-file-apache-poi/
		XSSFWorkbook wb;
		File outputFile = new File( "output/data.xlsx" );
		String sheetName = this.policy + "-" + this.numCPUs;
		String processTableName = "Process_Table";
		String cpuTableName = "CPU_Table";
		int timeConverter = 1000000; // Set to turn nanoseconds into milliseconds

		// If the file doesn't exist, go ahead and create it, copying the template
		if ( !outputFile.exists() ) {
			outputFile.getParentFile().mkdirs();
			Files.copy( new File( URLDecoder.decode(
					Objects.requireNonNull( Yeezus.class.getClassLoader().getResource( "template.xlsx" ) ).getFile(),
					"UTF-8" ) ).toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.COPY_ATTRIBUTES );
			outputFile.setWritable( true );
		}

		Workbook temp = WorkbookFactory.create( new FileInputStream( outputFile ) );

		if ( temp.getClass() == XSSFWorkbook.class ) {
			wb = (XSSFWorkbook) temp;
		} else {
			System.out.println( "The Excel sheet could not be used." );
			consolePrint( driver );
			return;
		}

		// Check if a sheet exists for this policy/num_cpu run
		XSSFSheet sheet = null;
		boolean hasSheet = false;
		for ( Sheet currSheet : wb ) {
			if ( sheetName.equals( currSheet.getSheetName() ) && currSheet.getClass() == XSSFSheet.class ) {
				hasSheet = true;
				sheet = (XSSFSheet) currSheet;
				break;
			}
		}

		// If the sheet doesn't exist, create the sheet
		if ( !hasSheet ) {
			// https://stackoverflow.com/questions/21942056/is-it-possible-to-change-sheet-name-with-apache-poi-ms-excel-java-android
			sheet = wb.cloneSheet( 0 );
			wb.setSheetName( wb.getSheetIndex( sheet ), sheetName );
		}

		// Just check again to see if it's still null
		if ( sheet == null ) {
			System.out.println( "There was an issue in creating or selecting the sheet." );
			consolePrint( driver );
			return;
		}

		// Write Process Information
		List<XSSFTable> tables = sheet.getTables();
		XSSFTable table = null;
		for ( XSSFTable currTable : tables ) {
			if ( currTable.getName().equals( processTableName ) ) {
				table = currTable;
				break;
			}
		}

		// Just check again to see if it's still null
		if ( table == null ) {
			System.out.println( "There was an issue in creating or selecting the process table." );
			consolePrint( driver );
			return;
		}

		// Write data
		int x = table.getStartColIndex();
		for ( int i = 1; i <= TaskManager.INSTANCE.size(); i++ ) {
			// Write to cells
			XSSFRow row = sheet.createRow( table.getStartRowIndex() + i );
			XSSFCell pidCell = row.createCell( x, CellType.NUMERIC );
			XSSFCell waitCell = row.createCell( x + 1, CellType.NUMERIC );
			XSSFCell runCell = row.createCell( x + 2, CellType.NUMERIC );
			XSSFCell totalCell = row.createCell( x + 3, CellType.FORMULA );
			XSSFCell executeCell = row.createCell( x + 4, CellType.NUMERIC );
			XSSFCell ioCell = row.createCell( x + 5, CellType.NUMERIC );

			pidCell.setCellValue( TaskManager.INSTANCE.getPCB( i ).getPID() );
			waitCell.setCellValue( TaskManager.INSTANCE.getPCB( i ).getElapsedWaitTime() / timeConverter );
			runCell.setCellValue( TaskManager.INSTANCE.getPCB( i ).getElapsedRunTime() / timeConverter );
			totalCell.setCellFormula( "$B$" + ( i + 1 ) + "+$C$" + ( i + 1 ) );
			executeCell.setCellValue( TaskManager.INSTANCE.getPCB( i ).getExecutionCount() );
			ioCell.setCellValue( TaskManager.INSTANCE.getPCB( i ).getNumIO() );
		}

		// Write average rows
		{ // To limit scope
			XSSFRow row = sheet.createRow( table.getEndRowIndex() );
			row.createCell( x, CellType.STRING ).setCellValue( "Average" );
			row.createCell( x + 1, CellType.FORMULA ).setCellFormula( "AVERAGE($B$2:$B$31)" );
			row.createCell( x + 2, CellType.FORMULA ).setCellFormula( "AVERAGE($C$2:$C$31)" );
			row.createCell( x + 3, CellType.FORMULA ).setCellFormula( "AVERAGE($D$2:$D$31)" );
			row.createCell( x + 4, CellType.FORMULA ).setCellFormula( "AVERAGE($E$2:$E$31)" );
			row.createCell( x + 5, CellType.FORMULA ).setCellFormula( "AVERAGE($F$2:$F$31)" );
		}

		table.setCellReferences( new AreaReference( "A1:F32", SpreadsheetVersion.EXCEL2007 ) );

		// Write CPU Information
		table = null;
		for ( XSSFTable currTable : tables ) {
			if ( currTable.getName().equals( cpuTableName ) ) {
				table = currTable;
				break;
			}
		}

		// Just check again to see if it's still null
		if ( table == null ) {
			System.out.println( "There was an issue in creating or selecting the cpu table." );
			consolePrint( driver );
			return;
		}

		// Write data
		if ( driver.getClass() == ThreadedDriver.class ) {
			x = table.getStartColIndex();
			long[] busyTimes = ( (ThreadedDriver) driver ).getExecuteTimes();
			long[] idleTimes = ( (ThreadedDriver) driver ).getIdleTimes();

			for ( int i = 0; i < this.numCPUs; i++ ) {
				// Write to cells
				XSSFRow row = sheet.getRow( table.getStartRowIndex() + i + 1 );
				XSSFCell cpuidCell = row.createCell( x, CellType.NUMERIC );
				XSSFCell busyCell = row.createCell( x + 1, CellType.NUMERIC );
				XSSFCell idleCell = row.createCell( x + 2, CellType.NUMERIC );

				cpuidCell.setCellValue( i );
				busyCell.setCellValue( busyTimes[i] / timeConverter );
				idleCell.setCellValue( idleTimes[i] / timeConverter );
			}

			// Write average rows
			{ // To limit scope
				XSSFRow row = sheet.getRow( table.getStartRowIndex() + 1 + this.numCPUs );
				row.createCell( x, CellType.STRING ).setCellValue( "Average" );
				row.createCell( x + 1, CellType.FORMULA ).setCellFormula( "AVERAGE($B$2:$B$31)" );
				row.createCell( x + 2, CellType.FORMULA ).setCellFormula( "AVERAGE($C$2:$C$31)" );
			}

			for ( int i = 0; i < 15; i++ ) { // To clear out some old data
				// Write to cells
				XSSFRow row = sheet.getRow( table.getStartRowIndex() + this.numCPUs + i + 2 );
				XSSFCell cpuidCell = row.createCell( x, CellType.STRING );
				XSSFCell busyCell = row.createCell( x + 1, CellType.STRING );
				XSSFCell idleCell = row.createCell( x + 2, CellType.STRING );

				cpuidCell.setCellValue( "" );
				busyCell.setCellValue( "" );
				idleCell.setCellValue( "" );
			}

			table.setCellReferences( new AreaReference( "H1:J" + ( this.numCPUs + 2 ), SpreadsheetVersion.EXCEL2007 ) );
		}

		// Write to file and close
		FileOutputStream outputStream = new FileOutputStream( outputFile );
		wb.write( outputStream );
		outputStream.close();
	}

	private void consolePrint( @NotNull AbstractDriver driver ) {
		if ( driver.getClass() == ThreadedDriver.class ) {
			System.out.println( "CPU Execute Times (ms): " );
			for ( int i = 0; i < this.numCPUs; i++ ) {
				System.out.println( "CPU " + i + " execute time: " + ( ( (ThreadedDriver) driver ).getExecuteTimes()[i]
						/ 1000000 ) );
				System.out.println(
						"CPU " + i + " idle time: " + ( ( (ThreadedDriver) driver ).getIdleTimes()[i] / 1000000 ) );
			}
			System.out.println( "\nProcess Information: " );
			for ( PCB pcb : TaskManager.INSTANCE ) {
				System.out.println(
						"Process: " + pcb.getPID() + "\nWait Time (ms): " + ( pcb.getElapsedWaitTime() / 1000000 )
								+ "\nRun Time (ms): " + ( pcb.getElapsedRunTime() / 1000000 )
								+ " \nCompletion time (ms): " + ( ( pcb.getElapsedRunTime() + pcb.getElapsedWaitTime() )
								/ 1000000 ) + "\nExecution Count: " + pcb.getExecutionCount() + "\n" + "IO Count: "
								+ pcb.getNumIO() + "\n" );
			}
			System.out.println( ( (ThreadedDriver) driver ).getProcPerCPU() );
		}
	}

	public Memory getDisk() {
		return this.disk;
	}

	public Memory getRAM() {
		return this.RAM;
	}

	public Memory[] getRegisters() {
		return this.registers;
	}

	public CPU[] getCPUs() {
		return this.driver.getCPUs();
	}
}
