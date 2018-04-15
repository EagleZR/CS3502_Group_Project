package yeezus.driver;
/**
 * The loader of the OS. Takes a txt file and grabs the PCB attributes, the info about the buffers, and sends
 * instructions to drive.
 * <p>
 * @author Jessica Brummel
 **/

import yeezus.DuplicateIDException;
import yeezus.memory.*;
import yeezus.pcb.TaskManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Loader {

	private int pid, instructionsLength, priority, inputBuffSize, outputBuffSize, tempBuffSize;
	private int startInstructionAddress;
	private int currAddress = 0;
	private Memory disk;
	private File programFile;
	private TaskManager processList;

	Loader( TaskManager processList, File programFile, Memory disk )
			throws InvalidAddressException, DuplicateIDException, InvalidWordException, IOException {
		this.programFile = programFile;
		this.processList = processList;
		this.disk = disk;
		scanFile();

	}

	public void scanFile() throws IOException, InvalidWordException, InvalidAddressException, DuplicateIDException {

		BufferedReader buffReader = new BufferedReader( new FileReader( this.programFile ) );
		String currentLine = buffReader.readLine();
		String subLine;

		while ( currentLine != null ) {
			if ( currentLine.contains( "//" ) ) {
				if ( currentLine.contains( "JOB" ) ) {
					// Skip to the beginning of a new page
					while ( this.currAddress % MMU.FRAME_SIZE != 0 ) {
						this.currAddress++; // Messy, but it works
					}

					subLine = currentLine.substring( 7 ); // grabs new substring

					int space = subLine.indexOf( ' ' );//finds the god damn space
					this.pid = Integer.decode( "0x" + subLine.substring( 0, space ) ); //grabs teh first fuckin letter

					subLine = subLine.substring( space + 1 ); // creates a new line

					space = subLine.indexOf( ' ' ); //grabs the next spaceboy
					this.instructionsLength = Integer
							.decode( "0x" + subLine.substring( 0, space ) ); //grabs the fuckin number

					subLine = subLine.substring( space + 1 ); //grabs the line

					this.priority = Integer.decode( "0x" + subLine ); //grabs priority

					this.startInstructionAddress = this.currAddress;

				}
				if ( currentLine.contains( "Data" ) ) {
					subLine = currentLine.substring( 8 );

					int space = subLine.indexOf( ' ' );
					this.inputBuffSize = Integer.decode( "0x" + subLine.substring( 0, space ) );

					subLine = subLine.substring( space + 1 );

					space = subLine.indexOf( ' ' );
					this.outputBuffSize = Integer.decode( "0x" + subLine.substring( 0, space ) );

					subLine = subLine.substring( space + 1 );

					this.tempBuffSize = Integer.decode( "0x" + subLine );

				}
				if ( currentLine.contains( "END" ) ) {
					this.processList.addPCB( this.pid, this.startInstructionAddress, this.instructionsLength,
							this.inputBuffSize, this.outputBuffSize, this.tempBuffSize, this.priority );
				}

			} else {

				if ( currentLine != null && !currentLine.equals( "" ) ) {
					this.disk.write( this.currAddress, new Word( currentLine ) );
					this.currAddress++;
				}

			}
			currentLine = buffReader.readLine();
		}

	}

}

//Job 1 17 2
// pid, number of words in instructions, priority number

//Data 14 C C  input buffer, output buffer, temp buffer
