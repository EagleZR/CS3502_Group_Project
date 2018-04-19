package yeezus;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.usermodel.*;
import yeezus.cpu.CPU;
import yeezus.driver.CPUSchedulingPolicy;
import yeezus.driver.Driver;
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

public class Main {

	// System Variables
	private final static int NUM_CPUS = 1;
	private final static CPUSchedulingPolicy POLICY = CPUSchedulingPolicy.Priority;

	// Memory Data
	private final static int DISK_SIZE = 2048;
	private final static int RAM_SIZE = 1024;
	private final static int CACHE_SIZE = 20;
	private final static int REGISTER_SIZE = 16;

	// Instance variables
	private final int numCPUs;
	private final CPUSchedulingPolicy policy;
	private final int diskSize;
	private final int ramSize;
	private final int cacheSize;
	private final int registerSize;

	public Main( int numCPUs, CPUSchedulingPolicy policy, int diskSize, int ramSize, int cacheSize, int registerSize ) {
		this.numCPUs = numCPUs;
		this.policy = policy;
		this.diskSize = diskSize;
		this.ramSize = ramSize;
		this.cacheSize = cacheSize;
		this.registerSize = registerSize;

		Memory disk;
		Driver driver;

		try {
			// Initialize memory
			disk = new Memory( this.diskSize );

			// Initialize and create Driver
			Driver.loadFile( disk, new File( ( URLDecoder.decode(
					Objects.requireNonNull( Main.class.getClassLoader().getResource( "Program-File.txt" ) ).getFile(),
					"UTF-8" ) ) ) );
			driver = new Driver( this.numCPUs, disk, this.registerSize, this.cacheSize, this.ramSize, this.policy );
		} catch ( Exception e ) {
			System.err.println( "An exception occurred in system initialization." );
			e.printStackTrace();
			return;
		}

		// Log start time
		long startTime = System.nanoTime();

		try {
			driver.run();
		} catch ( Exception e ) {
			System.err.println( "An exception occurred in system Execution." );
			e.printStackTrace();
			driver.dumpData();
			return;
		}
		// Log end time
		long endTime = System.nanoTime();

		System.out.println( "The system completed in " + ( endTime - startTime ) / 1000000 + " milliseconds." );

		// Print out the disk
		try {
			File output = new File( "output/Output_File.txt" );
			if ( !output.exists() && !output.createNewFile() ) {
				throw new Exception( "The output file could not be created." ); // idk how else to exit a try block
			}
			PrintStream out = new PrintStream( new FileOutputStream( output ) );
			for ( PCB pcb : TaskManager.INSTANCE ) {
				out.println( "****Job " + pcb.getPID() + "****" );
				// Instructions
				out.println( "Job " + pcb.getPID() + " Instructions:" );
				for ( int i = 0; i < pcb.getInstructionsLength(); i++ ) {
					out.println( disk.read( pcb.getInstructionDiskAddress() + i ) );
				}
				out.println();

				// Input Buffer
				out.println( "Job " + pcb.getPID() + " Input Buffer:" );
				for ( int i = 0; i < pcb.getInputBufferLength(); i++ ) {
					out.println( disk.read( pcb.getInputBufferDiskAddress() + i ) );
				}
				out.println();

				// Output Buffer
				out.println( "Job " + pcb.getPID() + " Output Buffer:" );
				for ( int i = 0; i < pcb.getOutputBufferLength(); i++ ) {
					out.println( disk.read( pcb.getOutputBufferDiskAddress() + i ) );
				}
				out.println();

				// Temp Buffer
				out.println( "Job " + pcb.getPID() + " Temp Buffer:" );
				for ( int i = 0; i < pcb.getTempBufferLength(); i++ ) {
					out.println( disk.read( pcb.getTempBufferDiskAddress() + i ) );
				}
				out.println( "______________________\n" );
			}
		} catch ( Exception e ) {
			System.err.println( "An exception occurred while writing to the output file." );
			e.printStackTrace();
		}

		// Print out runtime information
		try {
			excelPrint( driver );
		} catch ( Exception e ) {
			System.err.println( "An exception occurred while printing the process data." );
			e.printStackTrace();
		}
	}

	public static void main( String[] args ) {
		int[] cpuSet = { 1, 4 };
		for ( CPUSchedulingPolicy policy : CPUSchedulingPolicy.values() ) {
			for ( int numCPUs : cpuSet ) {
				new Main( numCPUs, policy, DISK_SIZE, RAM_SIZE, CACHE_SIZE, REGISTER_SIZE );
				CPU.reset();
				TaskManager.INSTANCE.reset();
				Driver.reset();
			}
		}
	}

	private void excelPrint( Driver driver ) throws Exception {
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
					Objects.requireNonNull( Main.class.getClassLoader().getResource( "template.xlsx" ) ).getFile(),
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
			XSSFCell cpuCell = row.createCell( x + 6, CellType.NUMERIC );
			XSSFCell cacheCell = row.createCell( x + 7, CellType.NUMERIC );
			XSSFCell ramCell = row.createCell( x + 8, CellType.NUMERIC );
			XSSFCell numPageFaultCell = row.createCell( x + 9, CellType.NUMERIC );
			XSSFCell pageFaultTimeCell = row.createCell( x + 10, CellType.NUMERIC );

			pidCell.setCellValue( TaskManager.INSTANCE.getPCB( i ).getPID() );
			waitCell.setCellValue( TaskManager.INSTANCE.getPCB( i ).getElapsedWaitTime() / timeConverter );
			runCell.setCellValue( TaskManager.INSTANCE.getPCB( i ).getElapsedRunTime() / timeConverter );
			totalCell.setCellFormula( "$B$" + ( i + 1 ) + "+$C$" + ( i + 1 ) );
			executeCell.setCellValue( TaskManager.INSTANCE.getPCB( i ).getExecutionCount() );
			ioCell.setCellValue( TaskManager.INSTANCE.getPCB( i ).getNumIO() );
			cpuCell.setCellValue( TaskManager.INSTANCE.getPCB( i ).getCPUID() );
			cacheCell.setCellValue( 1 );
			ramCell.setCellValue( ( (double) TaskManager.INSTANCE.getPCB( i ).getRAMUsed() ) / this.ramSize );
			numPageFaultCell.setCellValue( TaskManager.INSTANCE.getPCB( i ).getNumPageFaults() );
			pageFaultTimeCell.setCellValue( TaskManager.INSTANCE.getPCB( i ).getAveragePageFaultServicingTime() );
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
			row.createCell( x + 7, CellType.FORMULA ).setCellFormula( "AVERAGE($H$2:$H$31)" );
			row.createCell( x + 8, CellType.FORMULA ).setCellFormula( "AVERAGE($I$2:$I$31)" );
			row.createCell( x + 9, CellType.FORMULA ).setCellFormula( "AVERAGE($J$2:$J$31)" );
			row.createCell( x + 10, CellType.FORMULA ).setCellFormula( "AVERAGE($K$2:$K$31)" );
		}

		table.setCellReferences( new AreaReference( "A1:K32", SpreadsheetVersion.EXCEL2007 ) );

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
		x = table.getStartColIndex();
		long[] busyTimes = driver.getExecuteTimes();
		long[] idleTimes = driver.getIdleTimes();

		for ( int i = 0; i < ( this.numCPUs == 0 ? 1 : this.numCPUs ); i++ ) {
			// Write to cells
			XSSFRow row = sheet.getRow( table.getStartRowIndex() + i + 1 );
			XSSFCell cpuidCell = row.createCell( x, CellType.NUMERIC );
			XSSFCell busyCell = row.createCell( x + 1, CellType.NUMERIC );
			XSSFCell idleCell = row.createCell( x + 2, CellType.NUMERIC );
			XSSFCell numProcessesCell = row.createCell( x + 3, CellType.NUMERIC );

			cpuidCell.setCellValue( i );
			busyCell.setCellValue( busyTimes[i] / timeConverter );
			idleCell.setCellValue( idleTimes[i] / timeConverter );
			numProcessesCell.setCellValue( driver.getProcPerCPU() );
		}

		// Write average rows
		{ // To limit scope
			XSSFRow row = sheet.getRow( table.getStartRowIndex() + 1 + ( this.numCPUs == 0 ? 1 : this.numCPUs ) );
			row.createCell( x, CellType.STRING ).setCellValue( "Average" );
			row.createCell( x + 1, CellType.FORMULA )
					.setCellFormula( "AVERAGE($N$2:$N$" + ( 1 + ( this.numCPUs == 0 ? 1 : this.numCPUs ) ) + ")" );
			row.createCell( x + 2, CellType.FORMULA )
					.setCellFormula( "AVERAGE($O$2:$O$" + ( 1 + ( this.numCPUs == 0 ? 1 : this.numCPUs ) ) + ")" );
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

		table.setCellReferences( new AreaReference( "M1:O" + ( this.numCPUs + 2 ), SpreadsheetVersion.EXCEL2007 ) );

		// Write to file and close
		FileOutputStream outputStream = new FileOutputStream( outputFile );
		wb.write( outputStream );
		outputStream.close();
	}

	private void consolePrint( Driver driver ) {
		System.out.println( "CPU Execute Times (ms): " );
		for ( int i = 0; i < NUM_CPUS; i++ ) {
			System.out.println( "CPU " + i + " execute time: " + ( driver.getExecuteTimes()[i] / 1000000 ) );
			System.out.println( "CPU " + i + " idle time: " + ( driver.getIdleTimes()[i] / 1000000 ) );
		}
		System.out.println( "\nProcess Information: " );
		for ( PCB pcb : TaskManager.INSTANCE ) {
			System.out.println(
					"Process: " + pcb.getPID() + "\nWait Time (ms): " + ( pcb.getElapsedWaitTime() / 1000000 )
							+ "\nRun Time (ms): " + ( pcb.getElapsedRunTime() / 1000000 ) + " \nCompletion time (ms): "
							+ ( ( pcb.getElapsedRunTime() + pcb.getElapsedWaitTime() ) / 1000000 )
							+ "\nExecution Count: " + pcb.getExecutionCount() + "\n" + "IO Count: " + pcb.getNumIO()
							+ "\n" );
		}
		System.out.println( driver.getProcPerCPU() );
	}
}
