/*************************** Go Team Go! OS ***************************
 * Authors: Paul Morck, Steven Turner, Jyym Culpepper, Zachary Coffman
 * This is the final OS from "Go Team Go!".
 * For details, see the included documentations.
 **********************************************************************/

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

public class gui extends JFrame{
	// Adjustable variables
	private String filepath = "InputFile.txt"; // relative location of input file
	private int delay = 1, defaultDelay = 92; // delay (in milliseconds) and default delay (used when no delay is set)
	private int maxSleepTime = 2000; // maximum amount of time (in milliseconds) the program may sleep for a "quick" state change
	
	/************
	 * GUI items
	 ************/
	private JPanel background;				// JPanel (where all other GUI items are placed)
	private JButton runPause, nextStep;		// Buttons
	private ImageIcon RAMempty, RAMpics[];	// RAM bar images
	
	// Labels, text areas, and scroll panes for the text areas
	private JLabel runningLabel, newLabel, readyLabel, blockedLabel, exitLabel, changeLabel, RAMlabels[], speedLabel, mainLabel;
	private JTextArea runningArea, newArea, readyArea, blockedArea, exitArea, mainArea, changeArea, delayArea;
	private JScrollPane runningPane, newPane, readyPane, blockedPane, exitPane, mainPane, changePane, delayPane;
	
	private static final long serialVersionUID = 1L; // get warnings without it

	/******************
	 * other variables
	 ******************/
	private boolean changeOccurred, newListChanged, RAMChanged, allProcessed;
	private int count, RAM[], nextInputTime, sys_time, processCount;
	private Process processes[], runningProcess;
	private ArrayList<Process> NewList, ReadyList, BlockedList, ExitList;
	private LinkedList<Process> WaitingList;
	
	
	
	// Creates an instance of gui() (starts the program)
	public static void main(String[] args){
		new gui();
	}
	
	// Creates, initializes, and sets up all necessary GUI items,
	// then calls the methods to initialize all necessary system variables.
	// Once everything is ready, show the GUI.
	public gui(){
		int i;
		
		background = new JPanel();
		background.setBounds(800, 800, 200, 100);
		background.setLayout(null);
		add(background);
		
		
		/********************************
		 * Sets properties of the JFrame
		 ********************************/
		setTitle("Go Team Go! OS 2.0X");	// Title of the window
		setSize(800, 640);					// Size of the window
		setBackground(Color.BLACK);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Clicking the "X" at the top-right corner closes the window
		
		
		/******************************************************
		 * Define and initialize Buttons and add to the JPanel
		 ******************************************************/
		// Run/Pause Button: Pause or run the program
		runPause = new JButton("Run/Pause");
		runPause.setBounds(435, 10, 145, 70);
		runPause.setEnabled(true);
		background.add(runPause);
		
		// Next Step Button: Advance to the next step in the program
		nextStep = new JButton("Next Step");
		nextStep.setBounds(610, 10, 145, 70);
		nextStep.setEnabled(true);
		background.add(nextStep);
		
		
		/*****************************************************************
		 * Define and initialize RAM bar ImageIcons and add to the JPanel
		 *****************************************************************/
		RAMempty = new ImageIcon("312Icons/ramEMPTY.jpg");
		RAMpics = new ImageIcon[16];
		for (i=0; i<16; i++)
			RAMpics[i] = new ImageIcon("312Icons/ram" + (i + 1) + ".jpg");
		
		
		/*****************************************************
		 * Define and initialize Labels and add to the JPanel
		 *****************************************************/
		runningLabel = new JLabel("RUNNING");
		runningLabel.setBounds(25, 10, 100, 30);
		background.add(runningLabel);
		
		newLabel = new JLabel("NEW");
		newLabel.setBounds(40, 90, 148, 30);
		background.add(newLabel);
		
		readyLabel = new JLabel("READY");
		readyLabel.setBounds(132, 90, 148, 30);
		background.add(readyLabel);
		
		blockedLabel = new JLabel("BLOCKED");
		blockedLabel.setBounds(225, 90, 148, 30);
		background.add(blockedLabel);
		
		exitLabel = new JLabel("EXIT");
		exitLabel.setBounds(340, 90, 148, 30);
		background.add(exitLabel);
		
		changeLabel = new JLabel("Current Changes:");
		changeLabel.setBounds(420, 90, 148, 30);
		background.add(changeLabel);
		
		speedLabel = new JLabel("Speed:");
		speedLabel.setBounds(132, 10, 100, 30);
		background.add(speedLabel);
		
		mainLabel = new JLabel("PROCESS ID         |       STATE       |       SIZE       |       CPU TIME NEEDED"
				+ "       |       TIME USED       |       I/O'S SATISFIED      |      I/O'S LEFT");
		mainLabel.setBounds(32, 283, 786, 30);
		background.add(mainLabel);
		
		// RAM Labels (they appear directly below RAM bar)
		RAMlabels = new JLabel[16];
		for (i=0; i<16; i++){//Create all ram blocks either empty or full
			RAMlabels[i]=new JLabel("<HTML>Empty<BR/>RAM</HTML>", RAMempty, JLabel.CENTER);
			RAMlabels[i].setVerticalTextPosition(JLabel.BOTTOM);
			RAMlabels[i].setHorizontalTextPosition(JLabel.CENTER);
			RAMlabels[i].setBounds(10 + 48*i, 490, 46, 100);
			background.add(RAMlabels[i]);
		}
		
		
		/*************************************************************************
		 * Define and initialize TextAreas and Scroll Panes and add to the JPanel
		 *************************************************************************/
		Font monoSpace = new Font("monospaced", Font.PLAIN, 12);
		
		runningArea = new JTextArea(2, 2);
		runningArea.setFont(monoSpace);
		runningArea.setEditable(false);
		runningPane = new JScrollPane(runningArea);
		runningPane.setBounds(10, 36, 90, 50);
		background.add(runningPane);
		
		newArea = new JTextArea(5, 10);
		newArea.setFont(monoSpace);
		newArea.setEditable(false);
		newPane = new JScrollPane(newArea);
		newPane.setBounds(10, 113, 90, 160);
		background.add(newPane);
		
		readyArea = new JTextArea(5, 10);
		readyArea.setFont(monoSpace);
		readyArea.setEditable(false);
		readyPane = new JScrollPane(readyArea);
		readyPane.setBounds(110, 113, 90, 160);
		background.add(readyPane);
		
		blockedArea = new JTextArea(5, 10);
		blockedArea.setFont(monoSpace);
		blockedArea.setEditable(false);
		blockedPane = new JScrollPane(blockedArea);
		blockedPane.setBounds(210, 113, 90, 160);
		background.add(blockedPane);
		
		exitArea = new JTextArea(5, 10);
		exitArea.setFont(monoSpace);
		exitArea.setEditable(false);
		exitPane = new JScrollPane(exitArea);
		exitPane.setBounds(310, 113, 90, 160);
		background.add(exitPane);
		
		mainArea = new JTextArea(5, 10);
		mainArea.setFont(monoSpace);
		mainArea.setEditable(false);
		mainPane = new JScrollPane(mainArea);
		mainPane.setBounds(28, 306, 737, 180);
		background.add(mainPane);
		
		changeArea = new JTextArea(1, 1);
		changeArea.setFont(monoSpace);
		changeArea.setEditable(false);
		changePane = new JScrollPane(changeArea);
		changePane.setBounds(410, 113, 362, 160);
		background.add(changePane);
		
		delayArea = new JTextArea(1, 1);
		delayArea.setFont(monoSpace);
		delayPane = new JScrollPane(delayArea);
		delayPane.setBounds(110, 36, 90, 50);
		background.add(delayPane);
		
		
		/*************************************************************************
		 * Initialize variables to be used
		 *************************************************************************/
		initButtons();
		initVariables();
		inputFromFile();
		
		
		// after everything is initialized, show the frame
		setVisible(true);
	}
	
	
	
	// Create the actionListener functions for the buttons
	private void initButtons(){
		// When Run/Pause is clicked, the OS will run, updating the screen after every clock tick
		// The delay between clock ticks is defined by delay.
		runPause.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if (count == 0){ // Starting run cycle
					count = 1;
					delayArea.setEnabled(false);
					nextStep.setEnabled(false);
				}
				else{ // End run cycle
					count = 0;
					delayArea.setEnabled(true);
					nextStep.setEnabled(true);
				}
				
				ActionListener taskPerformer = new ActionListener(){
					public void actionPerformed(ActionEvent evt){
						if (allProcessed) // if program is complete, then stop execution 
							count = 0;
						
						if (count == 1){
							run();
							
							// if a process goes straight from New to Ready or Ready to Running, then delay
							if (runningProcess != null && sys_time - runningProcess.CB().getCreationTime() < 3)
								sleep(11 * delay);
							else
								for (int i=0; i<ReadyList.size(); i++)
									if (sys_time - ReadyList.get(i).CB().getCreationTime() < 2)
										sleep(11 * delay);
							
							update();
						}
						else
							((Timer)evt.getSource()).stop();
						
					}
				};
				
				if (count == 0){
					taskPerformer = null;
				}
				else{
					try{
						delay = Integer.parseInt(delayArea.getText());
					}
					catch (Exception e){
						delay = defaultDelay;
					}
					new Timer(delay, taskPerformer).start();
				}
			}
		}); //End runPause actionListener
		
		// When Next Step is clicked, the OS will run, updating the screen only for events
		// such as a process changing state or a process entering a system.
		nextStep.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				changeOccurred = false;
				while (changeOccurred == false)
					run();
				
				update();
			} 
		}); // End nextStep actionListener
	}
	
	// Initialize variables to be used
	private void initVariables(){
		int i;
		
		// initialize RAM
		RAM = new int[16];
		for (i=0;i<16;i++)
			RAM[i] = 0; // "Block" of RAM, = the id of the process that it is allocated to
		
		// initialize Lists of Processes
		processes   = new Process[60];
		WaitingList = new LinkedList<Process>();// List of Processes yet to enter the system
		NewList     = new ArrayList<Process>(); // List of Processes in New state
		ReadyList   = new ArrayList<Process>(); // List of Processes in Ready state
		BlockedList = new ArrayList<Process>(); // List of Processes in Blocked state
		ExitList    = new ArrayList<Process>(); // List of Processes in Exit state
		
		// initialize other system variables
		sys_time = 0;			// system time, measured in number of clock ticks from beginning
		nextInputTime = -1;		// next system time a process will enter the system
		processCount = 0;		// number of processes 
		changeOccurred = false;	// indicates if there is a change in a Process's state
		newListChanged = false;	// indicates if a Process has been removed from the NewList
		RAMChanged = false;		// indicates if the state of RAM has changed
		allProcessed = false;	// indicates if all Processes have passed through the system
	}
	
	// Access input file, read in Processes from input file and put processes into WaitingList and processes[]
  	private void inputFromFile(){
		File processInputFile = new File(filepath);
		
		try {
			Scanner scan = new Scanner(processInputFile);
			System.out.println("Process Input File found.  Attempting to read from it.");
			
			int i, creationTime;
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
					processes[processCount++] = current;
					
					if (processCount >= 60)
						break;
				}
			}
			scan.close();
			System.out.println(processCount + " processes succesfully read in.");
			
			// initialize nextInputTime
			// to the first time for a process to be input
			if (WaitingList.size() > 0)
				nextInputTime = WaitingList.get(0).CB().getCreationTime();
		}
		catch (java.io.FileNotFoundException FNFE){
			System.out.println("Process Input File not found @ " + 
				processInputFile.getAbsolutePath());
			System.exit(0);
		}
	}
	
	
	
	// Display information to the screen
	private void update(){
		drawRAM(); // update the RAM bar
		
		// Clear all Text Areas
		newArea.setText("");
		runningArea.setText("");
		blockedArea.setText("");
		readyArea.setText("");
		exitArea.setText("");
		mainArea.setText("");
		
		if (!allProcessed){ // If there are still processed to be processed
			int i;
			
			if (runningProcess != null) // Display the running process (if there is one)
				runningArea.append("Process " + runningProcess.CB().getID() + "\n");
			
			for (i=0; i<NewList.size(); i++) // List all processes in New
				newArea.append("Process " + NewList.get(i).CB().getID() + "\n");
			
			for (i=0; i<ReadyList.size(); i++) // List all processes in Ready
				readyArea.append("Process " + ReadyList.get(i).CB().getID() + "\n");
			
			for (i=0; i<BlockedList.size(); i++) // List all processes in Blocked
				blockedArea.append("Process " + BlockedList.get(i).CB().getID() + "\n");
			
			for (i=0; i<ExitList.size(); i++) // List all processes in Exit
				exitArea.append("Process " + ExitList.get(i).CB().getID() + "\n");
			
			for (i=0; i<processes.length; i++) // List all processes in the system in the main Text Area
				if (processes[i] != null && processes[i].CB().getState() > 0)
					mainArea.append(processes[i] + "\n");
		}
		else
			mainArea.append("All processes have run to completion!");
	}
	
	// Draws state of the RAM in the RAM bar
	private void drawRAM(){
		int i, imageIndex;
		
		for (i=0; i<16;){
			if (RAM[i] != 0){ // RAM block is allocated
				imageIndex = i;
				
				// Set all RAM blocks that are allocated to the same process as this block to the same color
				do{
					RAMlabels[i].setIcon(RAMpics[imageIndex]);
					RAMlabels[i].setText("<HTML>Pro: <BR/>" + RAM[i] + "</HTML>");
					i++;
				}
				while (i < 16 && RAM[i-1] == RAM[i]);
			}
			
			else{ // RAM block is empty
				RAMlabels[i].setIcon(RAMempty);
				RAMlabels[i].setText("<HTML>Empty<BR/>RAM</HTML>");
				i++;
			}
		}
	}
	
	// Updates the Changes Occurred text area with the given message
	private void changeOccurred(String message){
		String time = "" + sys_time;
		while (time.length() < 5){
			time = "0" + time;
		}
		
		changeOccurred = true;
		System.out.println(time + ": " + message);
		changeArea.append(message + "\n");
	}
	
	
	
	// Main loop that the program runs through after Run/Pause or Next Step is clicked
	// Calls the methods found below
	public void run(){
		if (!allProcessed){
			if (++sys_time >= nextInputTime)
				input();
			
			if (runningProcess != null)
				processor();

			if (sys_time % 10 == 0)
				dispatcher();
			else if (runningProcess == null && dispatcher());
			else if ((RAMChanged) && sys_time % 10 != 0){
				while(scheduler());
				newListChanged = RAMChanged = false;
			}
			
			if (BlockedList.size() > 0)
				simulateIO();
			if (ExitList.size() > 0)
				handleExit();
			
			if (newListChanged)
				RAMChanged = true;
		}
	}
	
	
	
	// Simulates the next process entering the system
	private void input(){
		// Pops the next Process to be input off waitingList
		// Sets next Process state to "New" and adds to NewList
		if (WaitingList.size() > 0){
			Process next = WaitingList.pop();
			next.CB().setState(Process.PCB.NEW);
			NewList.add(next);
			newListChanged = true;
			changeOccurred("Process " + next.CB().getID() + " has entered the system.");
			
			// find out the next processes input time (if any remain)
			if (WaitingList.size() > 0)
				nextInputTime = WaitingList.peek().CB().getCreationTime();
		}
		if (WaitingList.size() <= 0)
			nextInputTime = Integer.MAX_VALUE;
		
		//System.out.println("Input called @ " + sys_time + "; Next call should be @ " + nextInputTime);
	}
	
	// Simulates the scheduler; picks the next process to put into Ready state
	// Returns true if the scheduler put allocated RAM to a new Process (thus putting it in Ready).
	private boolean scheduler(){
		Process p = null;
		int i, size = 17, nextSize = 0, waitTime, time, next = -1, contRAM = 0,
			maxContRAM = 0, startLoc = 0, endLoc = 0, id = -1;
		double score, maxScore = -1.0;
		String blocks = "(block";
		
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
					if (i - startLoc + 1 >= size){ // area found that is large enough
						endLoc = i;
						break;
					}
				}
				else{
					startLoc = i + 1;
				}
			}
			
			// allocate RAM to that process
			for (i=startLoc; i<=endLoc; i++){
				RAM[i] = id;
			}
			
			p.CB().setState(2); // puts Process in Ready
			ReadyList.add(p); // add Process into "Ready" list
			
			if (startLoc != endLoc)
				blocks += "s " + startLoc + " - " + endLoc + ")";
			else
				blocks += " " + startLoc + ")";
			changeOccurred("Process " + id + " has been allocated RAM " + blocks + ".");
			return true;
		}
		return (newListChanged = RAMChanged = false);
	}
	
	// Simulates the dispatcher; picks the next process to get processor time
	// Returns true if the dispatcher performs a Process Switch, preempting one Process for another.
	private boolean dispatcher(){
		boolean change = false;
		int i, maxIndex = -1;
		float max = (float) -1.0, temp;
		Process p = runningProcess;
		
		if (runningProcess != null) // calculate this running process's HRRN score, if one is running
			max = (((float) p.getRemainingTime() + sys_time - p.CB().getCreationTime()) / p.getTotalTime());
		
		for (i=0; i<ReadyList.size(); i++){ // loop to find highest response ratio of processes in Ready state
			// calculate this process's HRRN score
			p = ReadyList.get(i);
			temp = (((float) p.getRemainingTime() + sys_time - p.CB().getCreationTime()) / p.getTotalTime());
			
			// if this process's HRRN score is best yet, then store this process's score and index
			if (temp > max){
				maxIndex = i;
				max = temp;
				change = true;
			}
		}
		
		if (change){ // if there is a better suited process for the processor to work on
			if (runningProcess != null){ // if there was a process running, then put it in ready
				changeOccurred("Process " + ReadyList.get(maxIndex).CB().getID() + " preempted Process " + runningProcess.CB().getID() + ".");
				runningProcess.CB().setState(2);
				ReadyList.add(runningProcess);
			}
			else // if there was no process running
				changeOccurred("Process " + ReadyList.get(maxIndex).CB().getID() + " starts running (no running process to preempt).");
			
			// put the new process into the running state, remove it from ready state
			ReadyList.get(maxIndex).CB().setState(3);
			runningProcess = ReadyList.remove(maxIndex);
		}
		
		return change;
	}
	
	// Simulates the processor; the Running Process will add one clock tick to its amount of time completed
	// If the Process makes an I/O request or is finished, then it is Blocked or Exits, respectively
	private void processor(){
		if (runningProcess != null)
			runningProcess.incrementTime(); // increment processing time of process (if there is one)
		
		// if a process needs an I/O request, block process
		if ((runningProcess.CB().hasMoreIOs()) && (runningProcess.getCurrentTime() >= runningProcess.CB().getNextIOTime())){
			runningProcess.CB().setState(4);
			BlockedList.add(runningProcess);
			changeOccurred("Process " + runningProcess.CB().getID() + " has been blocked awaiting an I/O request.");
			runningProcess = null;
		}
		else if (runningProcess.getCurrentTime() == runningProcess.getTotalTime()){ // if process is complete
			int i, id;
			
			runningProcess.CB().setState(5); //exit
			ExitList.add(runningProcess);
			runningProcess.CB().setCTime(sys_time); // NOTE HERE
			
			id = runningProcess.CB().getID();
			for(i=0; i<16; i++)
				if (RAM[i] == id)
					RAM[i] = 0;
			RAMChanged = true;
			
			changeOccurred("Process " + id + " has completed processing and is exiting the system.");
			runningProcess = null;
		}
	}
	
	// Simulates Process(es) waiting on I/O requests; decrements each Process's I/O wait time by one clock tick
	private void simulateIO(){
		int i;
		
		for (i=0; i<BlockedList.size(); i++){
			if (BlockedList.get(i).CB().incrementIO()){
				BlockedList.get(i).CB().setState(2);
				changeOccurred("Process " + BlockedList.get(i).CB().getID() + " has completed its I/O request and is unblocked.");
				ReadyList.add(BlockedList.remove(i));
			}
		}
	}
	
	// Handles a Process in the Exit state; holds a process in the Exit state so that it may be seen exiting
	private void handleExit(){
		int i;
		Process p;
		
		for (i=0; i<ExitList.size(); i++){
			if (sys_time - ExitList.get(i).CB().getCreationTime() > 50 || processCount == ExitList.size()){
				p = ExitList.remove(i);
				processes[p.CB().getID() - 1] = null;
				processCount--;
				changeOccurred = true;
			}
		}
		
		if (processCount <= 0){
			allProcessed = true;
			System.out.println();
			changeArea.append("\n");
			changeOccurred("All Processes processed!");
			
			nextStep.setEnabled(false);
			runPause.setEnabled(false);
		}
	}
	
	
	
	// Forces the program to sleep for the specified amount of time (in milliseconds)
	private void sleep(int time){
		if (time > maxSleepTime)
			time = maxSleepTime;
		else if (time < 0)
			time = 0;
	
		try{
			Thread.sleep(time);
		}
		catch (InterruptedException e){}
	}
}
