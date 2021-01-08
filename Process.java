/********************** Process & PCB classes **********************
 *  Authors: CAP'N Jyym, Paul Morck, Stephen Turner, Zachary Coffman
 *  
 *  PCB is nested inside Process.
 *  This is done so that you cannot create a standalone PCB.
 *  The PCB is a part of the Process.
 *******************************************************************/
public class Process{
	private static short id_counter = 0;
	private static final String whitespace = "                 "; // Used for toString() output
	
	// private variables
	private short tot_time;	// = 10 - 950 (cycles), total time to complete this process
	private short cur_time;	// = 10 - 950 (cycles), <= tot_time, time the process is currently at
	private PCB cb;			// The PCB, see below
	
	/****** CONSTRUCTORS ******/
	// Default constructor: should be unused
	public Process(){
		tot_time = 10;
		cur_time = 0;
		cb = new PCB();
	}
	// Primary constructor: likely will be most used
	public Process(String params){
		byte size, tIOs;
		short cT, tt, IOstartTimes[], IOlengthTimes[];
		
		// Splits the given string into a series of strings for parsing data.
		String vars[] = params.split(";");
		
		/* For every variable that will be read in:
		 *	Attempt to parse the variable from the string
		 *	If the variable is out of bounds, then reset it to be in bounds
		 * If there is an Exception thrown (usually because the variable cannot be parsed):
		 *	Set the variable to be a safe value.
		 */
		
		try{ // size
			size = Byte.parseByte(vars[0]);
			if (size < 1)
				size = 1;
			else if (size > 8)
				size = 8;
		}
		catch (Exception e){
			size = 1;
		}
		
		try{ // creationTime
			cT = Short.parseShort(vars[1]);
			if (cT < 1)
				cT = (short) (1 + (Math.random() * 20000));
		}
		catch (Exception e){
			cT = (short) (1 + (Math.random() * 20000));
		}
		
		try{ // tot_time
			tt = Short.parseShort(vars[2]);
			if (tt < 10)
				tt = 10;
			else if (tt > 950)
				tt = 950;
		}
		catch (Exception e){
			tt = 10;
		}
		
		try { // total_ios, io start time array, and io length array
			tIOs = Byte.parseByte(vars[3]);
			if (tIOs < 0)
				tIOs = 0;
			else if (tIOs > 5)
				tIOs = 5;
			
			// if there are ios, then read them in, and insert them into their arrays (sorted)
			if (tIOs > 0){
				int i, j, index;
				short nextIOstartTime, nextIOlengthTime;
				IOstartTimes = new short[tIOs];
				IOlengthTimes = new short[tIOs];
				
				for (i=0; i<tIOs; i++){
					nextIOstartTime = Short.parseShort(vars[2*i + 4]);
					nextIOlengthTime = Short.parseShort(vars[2*i + 5]);
					
					// if nextIOstartTime is out of bounds, put it in bounds
					if (nextIOstartTime <= 0){
						while (nextIOstartTime <= 0){
							nextIOstartTime += tt;
						}
						if (nextIOstartTime >= tt){
							nextIOstartTime = (short) i;
						}
					}
					else if (nextIOstartTime >= tt){
						while (nextIOstartTime >= tt){
							nextIOstartTime -= tt;
						}
						if (nextIOstartTime <= 0){
							nextIOstartTime = (short) (tt - i - 1);
						}
					}
					
					// if nextIOlengthTime is out of bounds, put it in bounds
					if (nextIOlengthTime < 25){
						nextIOlengthTime = 25;
					}
					else if (nextIOlengthTime > 50){
						nextIOlengthTime = 50;
					}
					
					// find where the nextIOtimes belong in the IOtime arrays 
					for (index=0; index<i; index++){
						if (nextIOstartTime < IOstartTimes[index])
							break;
					}
					
					// insert them there, bumping as necessary
					for (j=i; j>index; j--){
						IOstartTimes[j] = IOstartTimes[j - 1];
						IOlengthTimes[j] = IOstartTimes[j - 1];
					}
					IOstartTimes[index] = nextIOstartTime;
					IOlengthTimes[index] = nextIOlengthTime;
				}
			}
			else{
				IOstartTimes = null;
				IOlengthTimes = null;
			}
		}
		catch (Exception e){
			tIOs = 0;
			IOstartTimes = null;
			IOlengthTimes = null;
		}
		
		tot_time = tt;
		cur_time = 0;
		cb = new PCB(size, cT, tIOs, IOstartTimes, IOlengthTimes);
	}
	
	// Getter methods
	public PCB CB(){
		return cb;
	}
	public int getTotalTime(){
		return (int) tot_time;
	}
	public int getCurrentTime(){
		return (int) cur_time;
	}
	public int getRemainingTime(){
		return (int) (tot_time - cur_time);
	}
	
	// increments the cur_time (current time) by 1
	public void incrementTime(){
		cur_time++;
	}
	
	public String toString(){
		String s = " " + cb.id;							// Adds the ID
		
		s += whitespace.substring(s.length() - 2, 15);	// Adds space so string is 17 length
		s += cb.stateText[cb.state];					// Adds the state
		
		s += whitespace.substring(s.length() - 18, 11);	// Adds space so string is 31 length
		s += cb.size + " MB";							// Adds the size
		
		s += whitespace.substring(s.length() - 32, 13);	// Adds space so string is 45 length
		s += tot_time;									// Adds the total cpu time needed to complete
		
		s += whitespace.substring(s.length() - 46, 17);	// Adds space so string is 63 length
		s += cur_time;									// Adds the cpu time already used
		
		s += whitespace.substring(s.length() - 64, 17);	// Adds space so string is 78 length
		s += cb.ios_satisfied;							// Adds the # of satisfied io requests
		
		s += whitespace.substring(s.length() - 79, 17);	// Adds space so string is 95 length
		s += cb.total_ios - cb.ios_satisfied;			// Adds the # of outstanding io requests
		
		return s;
	}
	
	public class PCB{
		// public constants
		public static final int NEW = 1, READY = 2, RUNNING = 3, BLOCKED = 4, EXIT = 5;
		private final String stateText[] = {"NIS", "New", "Ready", "Running", "Blocked", "Exit"};
		// NOTE: NIS means "Not in System"
		
		// private variables
		private short id;				// = 1+, increments with each new process
		private byte size;				// = 1 - 8 (Mb)
		private byte state;				// = 0 - 5, represents state process is in, see getState() for details
		private byte total_ios;			// = 0 - 5, number of I/Os that are needed
		private byte ios_satisfied;     // = 0 - 5, number of I/Os that have been satisfied
		private short creationTime;		// = 1+, time when the Process is created and enters the system
		private short io_start_times[];	// = 10 - 950 (cycles), array length = total_ios, start times of I/Os needed
		private short io_lengths[];		// = 1+, length of I/Os needed
		
		/****** CONSTRUCTORS ******/
		// Each is called from its respective Process constructor
		private PCB(){
			id = ++id_counter;
			size = 1;
			state = 0;
			total_ios = 0;
			creationTime = (short) (1 + (Math.random() * 20000));
			io_start_times = null;
			io_lengths = null;
		}
		private PCB(byte varSize, short cT, byte tIOs, short[] IOstartTimes, short[] IOlengthTimes){
			id = ++id_counter;
			size = (byte) varSize;
			state = 0;
			total_ios = (byte) tIOs;
			creationTime = cT; // (short) (1 + (Math.random() * 20000));
			io_start_times = IOstartTimes;
			io_lengths = IOlengthTimes;
		}
		
		// getter methods
		public int getID(){
			return id;
		}
		public int getSize(){
			return size;
		}
		public int getState(){
			return state;
		}
		public int getCreationTime(){
			return creationTime;
		}
		public int getTotalIOs(){
			return total_ios;
		}
		public int getIosSatisfied(){
			return ios_satisfied;
		}
		public int getNextIOTime(){
			if (io_start_times != null && ios_satisfied < io_start_times.length)
				return io_start_times[ios_satisfied];
			else
				return -1;
		}
		public int getNextIOLength(){
			if (io_lengths != null  && ios_satisfied < io_lengths.length)
				return io_lengths[ios_satisfied];
			else
				return -1;
		}
		public boolean hasMoreIOs(){
			return (ios_satisfied < total_ios);
		}
		
		// setter methods (where applicable)
		public void setState(int newState){
			state = (byte) newState;
		}
		public void setCTime(int newTime){
			creationTime = (short) newTime;
		}
		
		public void satisfyIO(){
			ios_satisfied++;
		}
		public boolean incrementIO(){
			if (io_lengths != null && ios_satisfied < total_ios){
				io_lengths[ios_satisfied]--;
				if (io_lengths[ios_satisfied] == 0){
					ios_satisfied++;
					return true;
				}
			}
			return false;
		}
	}
}

