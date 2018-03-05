package yeezus.driver;
/**
 * The loader of the OS. Takes a txt file and grabs the PCB attributes, the info about the buffers, and
 * sends instructions to drive.
 * <p>
 * author: jessica brummel
 **/

import yeezus.memory.InvalidAddressException;
import yeezus.memory.InvalidWordException;
import yeezus.memory.Memory;
import yeezus.memory.Word;
import yeezus.pcb.DuplicatePIDException;
import yeezus.pcb.PCB;

import yeezus.pcb.TaskManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Loader {

	int pid, instructionsLength, priority, inputBuffSize, outputBuffSize, tempBuffSize;
	ArrayList<Word> instructionList = new ArrayList<Word>();
	ArrayList<Word> memoryList = new ArrayList<Word>();
	ArrayList<PCB> PCBList = new ArrayList<PCB>();
	int startInstructionAddress, startInputBufferAddress, startOutputBufferAddress, startTempBufferAddress;
	int endInstructionAddress, endInputBufferAddress, endOutputBufferAddress;
	int end;
	int currAddress = 0;
	Memory disk;
	File programFile;
	TaskManager processList;

	Loader( TaskManager processList, File programFile, Memory disk )
			throws InvalidAddressException, DuplicatePIDException, InvalidWordException, IOException {
		this.programFile = programFile;
		this.processList = processList;
		this.disk = disk;
		scanFile();

	}

	public void scanFile() throws IOException, InvalidWordException, InvalidAddressException, DuplicatePIDException {

		BufferedReader buffReader = new BufferedReader( new FileReader( programFile ) );
		String currentLine = buffReader.readLine();
		String subLine;

		while ( currentLine != null ) {
			if ( currentLine.contains( "//" ) ) {
				if ( currentLine.contains( "JOB" ) ) {
					System.out.println( "Grabbing the Job" );

					subLine = currentLine.substring( 7 ); // grabs new substring
					System.out.println( "First split: " + subLine ); // prints the new substring for my eyeballs
					int space = subLine.indexOf( ' ' );//finds the god damn space
					pid = Integer.decode( "0x" + subLine.substring( 0, space ) ); //grabs teh first fuckin letter

					subLine = subLine.substring( space + 1 ); // creates a new line
					System.out.println( "Second split: " + subLine ); //prints out this motherfucker
					space = subLine.indexOf( ' ' ); //grabs the next spaceboy
					instructionsLength = Integer
							.decode( "0x" + subLine.substring( 0, space ) ); //grabs the fuckin number

					subLine = subLine.substring( space + 1 ); //grabs the line
					System.out.println( "Grab 3rd num: " + subLine ); //prints out the line
					priority = Integer.decode( "0x" + subLine ); //grabs priority

					startInstructionAddress = currAddress;

				}
				if ( currentLine.contains( "Data" ) ) {
					System.out.println( "Grabbing the data" );
					subLine = currentLine.substring( 8 );
					System.out.println( "This is the first line:" + subLine );
					int space = subLine.indexOf( ' ' );
					inputBuffSize = Integer.decode( "0x" + subLine.substring( 0, space ) );

					subLine = subLine.substring( space + 1 );
					System.out.println( "This is the 2nd line:" + subLine );

					space = subLine.indexOf( ' ' );
					outputBuffSize = Integer.decode( "0x" + subLine.substring( 0, space ) );

					subLine = subLine.substring( space + 1 );
					System.out.println( "This is the third line:" + subLine );
					tempBuffSize = Integer.decode( "0x" + subLine );

					endInstructionAddress = currAddress - 1;
					System.out.println( "endInstructionAddress: " + endInstructionAddress );
					startInputBufferAddress = currAddress;
					System.out.println( "StartInputBufferAddress: " + startInputBufferAddress );
					endInputBufferAddress = currAddress + inputBuffSize - 1;
					System.out.println( "endInputBufferAddress: " + endInputBufferAddress );
					startOutputBufferAddress = endInputBufferAddress + 1;
					System.out.println( "startOutputBufferAddress: " + startOutputBufferAddress );
					endOutputBufferAddress = startOutputBufferAddress + outputBuffSize - 1;
					System.out.println( "endOutputBufferAddress: " + endOutputBufferAddress );
					startTempBufferAddress = endOutputBufferAddress + 1;
					System.out.println( "startTempBufferAddress: " + startTempBufferAddress );

				}
				if ( currentLine.contains( "END" ) ) {
					processList.addPCB( pid, startInstructionAddress, instructionsLength, inputBuffSize, outputBuffSize,
							tempBuffSize, priority );
				}

			} else {

				if ( currentLine != null && !currentLine.equals( "" ) ) {
					disk.write( currAddress, new Word( currentLine ) );
					currAddress++;
				}

			}
			currentLine = buffReader.readLine();
		}

	}

}

//Job 1 17 2
// pid, number of words in instructions, priority number

//Data 14 C C  input buffer, output buffer, temp buffer
