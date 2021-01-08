/**********************************************************
 * Tester class to prove Process.java class works properly
 **********************************************************/

import java.util.Scanner;

public class Tester{
	// Given: Number 1 - 18 or none
	// If number, then execute that test case.
	// If none or 0, then execute all test cases.
	public static void main(String[] args){
		try{
			int testNum;
			if (args.length < 1){
				Scanner input = new Scanner(System.in);
				System.out.println("Enter the test case you wish to test (1-18) or 0 to do all test cases.");
				testNum = input.nextInt();
				input.close();
			}
			else
				testNum = Integer.parseInt(args[0]);
			
			if (testNum < 0 || testNum > 18) // out of range? run ALL tests
				testNum = 0;
			test(testNum);
		}
		catch (Exception e){
			test(0);
		}
	}
	
	private static void test(int testNum){
		boolean breaks = true;
		Process p;
		String input;
		
		switch (testNum){
			case 0: // indicates all should be tested
				breaks = false;
			
			case 1: // creates a "dummy" process by using the default constructor
				p = new Process();
				print(1, "", p.toString());
				if (breaks)
					break;
			case 2: // creates a simple Process with no I/Os, no field is incorrect
				input = "1;00001;010;0";
				p = new Process(input);
				print(2, input, p.toString());
				if (breaks)
					break;
			case 3: // creates a simple Process with 1 I/O, no field is incorrect
				input = "1;00001;010;1;5;25";
				p = new Process(input);
				print(3, input, p.toString() + " | I/O @ " + p.CB().getNextIOTime() + " | length = " + p.CB().getNextIOLength());
				if (breaks)
					break;
			
			case 4: // creates a Process, but because the "size" field is out of range it is reset to be in range
				input = "-1;00001;010;0";
				p = new Process(input);
				print(4, input, p.toString());
				if (breaks)
					break;
			case 5: // creates a Process, but because the "size" field is out of range it is reset to be in range
				input = "20;00001;010;0";
				p = new Process(input);
				print(5, input, p.toString());
				if (breaks)
					break;
			
			case 6: // creates a Process, but because the "creationTime" field is out of range, it creates a random creationTime
				input = "3;00000;100;0";
				p = new Process(input);
				print(6, input, p.toString() + " | Creation Time = " + p.CB().getCreationTime());
				if (breaks)
					break;
			
			case 7: // creates a Process, but because the "total time" field is out of range it is reset to be in range
				input = "1;00001;008;0";
				p = new Process(input);
				print(7, input, p.toString());
				if (breaks)
					break;
			case 8: // creates a Process, but because the "total time" field is out of range it is reset to be in range
				input = "8;00001;999;0";
				p = new Process(input);
				print(8, input, p.toString());
				if (breaks)
					break;

			case 9: // creates a Process, but because the "total I/Os" field is out of range it is reset to be in range
				input = "1;00001;010;-4";
				p = new Process(input);
				print(9, input, p.toString());
				if (breaks)
					break;
			case 10: // creates a Process, but because the "total I/Os" field is out of range it is reset to be in range
				// Note that in this case, all I/Os after the 5th are left off, because a Process may only have 5 I/Os
				input = "1;00001;010;14;1;25;2;25;3;25;4;25;5;25;6;25;junk data that's not read";
				p = new Process(input);
				print(10, input, p.toString() + " | First I/O @ " + p.CB().getNextIOTime() + " | length = " + p.CB().getNextIOLength());
				if (breaks)
					break;
			
			case 11: // creates a Process, but because there aren't enough fields to satisfy
				// the given "total I/Os" field (5) resets the I/Os to assume 0 I/O requests
				input = "1;00001;010;5;1;1;25";
				p = new Process(input);
				print(11, input, p.toString());
				if (breaks)
					break;
			case 12: // creates a Process, but because there are more I/O request fields than
				// the given "total I/Os" field (1), it doesn't read that data to be a part of this Process
				input = "1;00001;010;1;1;25;2;25;3;25;4;25;5;25";
				p = new Process(input);
				print(12, input, p.toString() + " | I/O @ " + p.CB().getNextIOTime() + " | length = " + p.CB().getNextIOLength());
				if (breaks)
					break;
			
			case 13: // creates a Process, but because the "I/O start time" field is out of range it is reset to be in range
				input = "1;00001;010;1;-5;25";
				p = new Process(input);
				print(13, input, p.toString() + " | I/O @ " + p.CB().getNextIOTime() + " | length = " + p.CB().getNextIOLength());
				if (breaks)
					break;
			case 14: // creates a Process, but because the "I/O start time" field
				// is greater than the process run time, it is reset to be in range
				input = "1;00001;010;1;100;25";
				p = new Process(input);
				print(14, input, p.toString() + " | I/O @ " + p.CB().getNextIOTime() + " | length = " + p.CB().getNextIOLength());
				if (breaks)
					break;
			
			case 15: // creates a Process, but because the "I/O length" field is out of range it is reset to be in range
				input = "1;00001;010;1;5;10";
				p = new Process(input);
				print(15, input, p.toString() + " | I/O @ " + p.CB().getNextIOTime() + " | length = " + p.CB().getNextIOLength());
				if (breaks)
					break;
			case 16: // creates a Process, but because the "I/O length" field is out of range it is reset to be in range
				input = "1;00001;010;1;5;100";
				p = new Process(input);
				print(16, input, p.toString() + " | I/O @ " + p.CB().getNextIOTime() + " | length = " + p.CB().getNextIOLength());
				if (breaks)
					break;
			
			case 17: // creates a Process, but because the I/Os are not listed in order
				// (from first to last), they are reordered to be first to last
				input = "1;00001;010;3;5;50;3;40;1;30";
				p = new Process(input);
				print(17, input, p.toString() + " | First I/O @ " + p.CB().getNextIOTime() + " | length = " + p.CB().getNextIOLength());
				if (breaks)
					break;
			
			case 18: // creates a Process and outputs it, then and changes its state, increments its current time,
				// and completes one of its first I/O to show the changes that occur in the output
				input = "3;00001;100;3;10;30;50;50;70;40";
				p = new Process(input);
				print(18, input, p.toString() + " | First I/O @ " + p.CB().getNextIOTime() + " | length = " + p.CB().getNextIOLength());
				
				p.CB().setState(p.CB().RUNNING);
				for (int i=0; i<10; i++)
					p.incrementTime();
				p.CB().satisfyIO();
				
				System.out.println(p + " | Next I/O @ " + p.CB().getNextIOTime() + " | length = " + p.CB().getNextIOLength());
				if (breaks)
					break;
		}
	}
	
	private static void print(int testCase, String input, String output){
		System.out.println();
		System.out.println("Case " + testCase + ": Input = " + input);
		System.out.println(output);
	}
}
