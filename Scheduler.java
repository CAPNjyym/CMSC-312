/************************* Scheduler class *************************
 * Author: CAP'N Jyym
 * Shell class to test changes to the scheduler() method
 * main() method can be tweaked to simulate different situations
 * 
 * scheduler() evaluates all Processes in "New" and places the best
 * Process (based on a HRRN-style score) into the "Ready" state
 * (it then is the responsibility of the Dispatcher)
 *******************************************************************/

import java.util.ArrayList;

public class Scheduler{
	// Relevant system variables that will be part of final project
	private static int sys_time = 0;
	private static int RAM[] = new int[16];
	private static ArrayList<Process> NewList, ReadyList;
	private static boolean changeOccured = false;
	
	// Shell main to set conditions for scheduler to run
	// Can be used to test the scheduler
	public static void main(String[] args){
		int runs = 100, i, j, time, id; // number of runthrus
		Process p;
		
		NewList = new ArrayList<Process>();
		ReadyList = new ArrayList<Process>();
		
		for (i=0; i<16; i++)
			RAM[i] = 0;
		initRAM();
		importList();
		
		while (--runs >= 0){
			sys_time++;
			scheduler();
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
				}
				p.incrementTime();
			}
		}
	}

	private static void importList(){
		int arr[] = new int[3];
		
		// size, time, etc
		NewList.add(new Process());//5,  60, arr)); // id = 1
		NewList.add(new Process());//6, 950, arr)); // id = 2
		NewList.add(new Process());//1,  20, arr)); // id = 3
		NewList.add(new Process());//2, 200, arr)); // id = 4
		NewList.add(new Process());//3,  30, arr)); // id = 5
		NewList.add(new Process());//8,  10, arr)); // id = 6
	}
	
	private static void initRAM(){
		int i;
		
		for (i=0; i<16; i++){
			RAM[i] = 0;
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
				if (score > maxScore){
					maxScore = score;
					next = i;
					nextSize = size;
				}
			}
		}
		
		if (next >= 0 && next < NewList.size() && nextSize <= maxContRAM){
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
			return (changeOccured = true);
		}
		return false;
	}
}
