/************************************************************
 * Tester class to prove Scheduler.java class works properly
 ************************************************************/

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class SkedTester{
	// Relevant system variables that will be part of final project
	private static int sys_time = 0, runs = 1;
	private static int RAM[] = new int[16];
	private static ArrayList<Process> NewList, ReadyList;
	private static boolean changeOccurred = false;
	private static DecimalFormat DF = new DecimalFormat("0.000");

	// Given: Number 1 - 18 or none
	// If number, then execute that test case.
	// If none or 0, then execute all test cases.
	public static void main(String[] args){
		int x, i, j, time, id, testNum;
		boolean breaks = false;
		Process p;
		
		try{
			if (args.length < 1){
				Scanner input = new Scanner(System.in);
				System.out.println("Enter the test case you wish to test (1-3) or 0 to do all test cases.");
				testNum = input.nextInt();
				input.close();
			}
			else
				testNum = Integer.parseInt(args[0]);
			
			if (testNum < 0 || testNum > 3) // out of range? run ALL tests
				testNum = 0;
		}
		catch (Exception e){
			testNum = 0;
		}
		
		if (testNum != 0)
			breaks = true;
		
		for (x=testNum; x<=3; x++){
			NewList = new ArrayList<Process>();
			ReadyList = new ArrayList<Process>();
			
			for (i=0; i<16; i++)
				RAM[i] = 0;
			importList(x);

			changeOccurred = true;
			while (--runs >= 0){
				sys_time++;
				//if (changeOccurred){
					while(scheduler());
					//changeOccurred = false;
				//}
				
				// This part simulates processor time (not accurately, but good enough for testing)
				for (i=0; i<ReadyList.size(); i++){
					p = ReadyList.get(i);
					time = p.getCurrentTime() + 1;
					if (time >= p.getTotalTime()){ // process finished running
						ReadyList.remove(i);
						i--;
						
						// after removing from ReadyList, reset its RAM allocation
						id = p.CB().getID();
						for (j=0; j<16; j++){
							if (id == RAM[j])
								RAM[j] = 0;
						}
						changeOccured(id, -1);
					}
					p.incrementTime();
				}
			}
			
			if (breaks)
				break;
		}
	}

	private static void importList(int testCase){
		switch (testCase){
			case 1: // Shows that the scheduler prioritizes smaller, faster processes first.
				// It also shows that the scores grow over time, thus prioritizing processes that have waited long.
				runs = 40;
				NewList.add(new Process("5;00001;060;0")); // id = 1
				NewList.add(new Process("6;00001;950;0")); // id = 2
				NewList.add(new Process("1;00001;020;0")); // id = 3
				NewList.add(new Process("2;00001;200;0")); // id = 4
				NewList.add(new Process("3;00001;030;0")); // id = 5
				NewList.add(new Process("8;00001;010;0")); // id = 6
				System.out.println();
				System.out.println("Case 1:");
				break;
			case 2: // Shows that the scheduler will try to add a process that has been waiting
				// a long time instead of a process that just arrived (to prevent starvation).
				runs = 2000;
				NewList.add(new Process("8;00001;900;0")); // id = 1
				NewList.add(new Process("8;00001;900;0")); // id = 2
				NewList.add(new Process("5;00005;948;0")); // id = 3
				NewList.add(new Process("8;00005;950;0")); // id = 4
				NewList.add(new Process("8;00010;950;0")); // id = 5
				NewList.add(new Process("3;01848;100;0")); // id = 6
				System.out.println();
				System.out.println("Case 2:");
				break;
			case 3: // Shows that the a smaller, faster process's score will increase faster than a larger, longer process.
				// This can cause the scheduler to try to allocate RAM to the larger, longer process at first,
				// but then give RAM to the smaller one when its score grows large enough
				runs = 250;
				NewList.add(new Process("8;00001;100;0")); // id = 1
				NewList.add(new Process("8;00001;100;0")); // id = 2
				NewList.add(new Process("8;00001;100;0")); // id = 3
				NewList.add(new Process("8;00001;100;0")); // id = 4
				NewList.add(new Process("8;00010;100;0")); // id = 5
				NewList.add(new Process("4;00090;050;0")); // id = 6
				System.out.println();
				System.out.println("Case 3:");
				break;
		}
	}
	
	// scheduler method to be used in final project
	private static boolean scheduler(){
		Process p = null;
		int i, size = 17, nextSize = 0, waitTime, time, next = -1, contRAM = 0, maxContRAM = 0, startLoc = -1, endLoc = 0, id = -1;
		double score, maxScore = -1.0;
		
		// checks RAM available
		for (i=0; i<16; i++){
			if (RAM[i] == 0){ // RAM block is free
				contRAM++;
			}
			else if (maxContRAM < contRAM){
				maxContRAM = contRAM;
				contRAM = 0;
			}
		}
		if (maxContRAM < contRAM)
			maxContRAM = contRAM;
		
		for (i=0; i<NewList.size(); i++){
			p = NewList.get(i);
			size = p.CB().getSize();
			waitTime = sys_time - p.CB().getCreationTime();
			
			// process will fit in current state of RAM
			if (size <= maxContRAM || waitTime >= 1000){
				time = p.getTotalTime();
				
				// HRRN-style score;  higher score = go first
				// formula is an adaptation on the HRRN in book, but size is taken into account too
				// the 0.4 added to size gives size a stronger weight (it is still less than time)
				score = (1.0 + (5.0 * waitTime) + ((0.4 + size) * time)) / ((0.4 + size) * time);
				if (size <= maxContRAM && score >= 1.0)
					System.out.print("  Process " + p.CB().getID() + ": " + DF.format(score) + " |");
				if (score > maxScore){
					maxScore = score;
					next = i;
					nextSize = size;
				}
			}
		}
		
		if (next >= 0 && next < NewList.size() && nextSize <= maxContRAM){
			System.out.println();
			p = NewList.remove(next);
			id = p.CB().getID();
			size = p.CB().getSize();
			
			// FIRST FIT to find area in RAM free for process
			for (i=0; i<16; i++){
				if (RAM[i] == 0){ // RAM block is free
					if (i - startLoc >= size){ // area found that is large enough
						endLoc = i;
						break;
					}
				}
				else{
					startLoc = i;
				}
			}
			
			// allocate RAM to that process
			for (i=startLoc+1; i<=endLoc; i++){
				RAM[i] = id;
			}
			
			p.CB().setState(2); // puts Process in Ready
			ReadyList.add(p); // add Process into "Ready" list
			
			changeOccured(id, maxScore);
			System.out.println(" (Size: " + size + ", Time: " + p.getRemainingTime()
				+ ", Waited for: " + (sys_time - p.CB().getCreationTime()) + ")");
			return true;
		}
		return false;
	}
	
	// set changeOccured to true (used in final project) and output updates
	private static void changeOccured(int id, double status){
		int i;
		String output = "" + sys_time;
		while (output.length() < 5){
			output = "0" + output;
		}
		output = " @" + output + ", Process " + id + " ";
		if (status == -1)
			output += "has  left";
		else
			output += "allocated";
		output += " RAM: ";
		for (i=0; i<16; i++){
			output += RAM[i];
		}
		if (status != -1)
			output += " || It's score was " + DF.format(status);
		
		changeOccurred = true;
		if (status == -1)
			System.out.println(output);
		else
			System.out.print(output);
	}
}
