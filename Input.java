/********************* Input class *********************
 *  Authors: CAP'N Jyym, Zachary Coffman
 *  
 *  Used to simulate reading in input from file(s)
 *  and simulate processes appearing in system.
 *  (they then are the responsibility of the Scheduler)
 *******************************************************/

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class Input{
	// Relevant system variables that will be part of final project
	private static Process processes[] = new Process[60];
	private static LinkedList<Process> WaitingList;
	private static ArrayList<Process> NewList;
	private final static String filepath = "InputFile.txt"; // relative filepath of input file to be read in
	private static int nextInputTime = -1;
	private static boolean changeOccured = false;
	private static int sys_time = 0;
	
	// Shell main to set conditions for input to run
	// Can be used to test the input
	public static void main(String[] args){
		int runs = 20002;
		WaitingList = new LinkedList<Process>();
		NewList = new ArrayList<Process>();
		
		inputFromFile();
		while (sys_time < runs){
			// while is needed in case 2+ processes should be input on same clock tick
			while (sys_time == nextInputTime){
				System.out.print("input called @ " + sys_time);
				input();
			}
			
			// increment necessary system vars
			sys_time++;
		}
	}
	
	// input of Processes from the input file
	private static void inputFromFile(){
		File processInputFile = new File(filepath);

		try {
			Scanner scan = new Scanner(processInputFile);
			System.out.println("Process Input File found.  Attempting to read from it.");

			int i, creationTime, count = 0;
			String line;
			Process current;
			
			while (scan.hasNextLine()) { // still has more lines to read
				// create process based on line
				line = scan.nextLine();
				if (line != null && line.length() != 0 && line.charAt(0) != '#'){ // if line is not a comment
					current = new Process(line);
					
					// insert the process into WaitingList, ordering the smallest creationTimes first
					// so that the processes that enter the system first are at the top of the list
					creationTime = current.CB().getCreationTime();
					for(i=0; i<WaitingList.size(); i++){
						if (creationTime < WaitingList.get(i).CB().getCreationTime())
							break;
					}
					WaitingList.add(i, current);
					processes[count++] = current;
					
					if (count >= 60)
						break;
				}
			}
			scan.close();
			
			// initialize nextInputTime to the first time for a process to be input
			if (WaitingList.size() > 0)
				nextInputTime = WaitingList.get(0).CB().getCreationTime();
		}
		catch (java.io.FileNotFoundException FNFE){
			System.out.println("Process Input File not found @ " + 
				processInputFile.getAbsolutePath());
			System.exit(0);
		}
	}
	
	// input to be used in final project
	private static void input(){
		// Pops the next Process to be input off waitingList
		// Sets next Process state to "New" and adds to NewList
		if (WaitingList.size() > 0){
			Process next = WaitingList.pop();
			next.CB().setState(Process.PCB.NEW);
			NewList.add(next);
			changeOccured = true;
			
			// find out the next processes input time (if any remain)
			if (WaitingList.size() > 0)
				nextInputTime = WaitingList.peek().CB().getCreationTime();
		}
		if (WaitingList.size() <= 0)
			nextInputTime = Integer.MAX_VALUE;
		
		System.out.println("; Next call should be @ " + nextInputTime);
	}
}
