package kiraNeccesaryLibs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class LogWriter {

	private static final boolean DEBUGGING = true;
	
	private static File systemLog = new File("System Log.log");
	private static File errorLog = new File("Error Log.log");
	
	private static FileOutputStream fout = null;
	private static FileOutputStream ferr = null;
	
	private static MultiOutputStream multiOut = new MultiOutputStream(System.out, fout);
	private static MultiOutputStream multiErr = new MultiOutputStream(System.err, ferr);
	
	private static PrintStream stdout= new PrintStream(multiOut);
	private static PrintStream stderr= new PrintStream(multiErr);
	
	private static final String DEBUG_STRING = "DEBUG";
	
	public static void main (String [] args) throws IOException{
		try {
//			LogWriter.setLogPath(new File(InputMethode.KIRA_ROOT_DIR+""+File.separator+"log"+File.separator+""));
			LogWriter.setLogPath(new File("loggerXXXXXX"+File.separator+"ONE MORE"));
			LogWriter.start(true);
		} catch (FileNotFoundException | NullPointerException e1) {
			e1.printStackTrace();
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("was geht");
				System.err.println("loooasdada");
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				System.out.println("tt2");
//				System.err.println("t2");
			}
		}).start();

		
		System.out.println(Thread.currentThread().getId());
		System.out.println(Thread.currentThread().getName());
		System.out.println(Thread.currentThread().toString());
		System.out.println(Thread.currentThread().getState());
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		System.out.println("1111");
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
		    System.out.println(ste);
		}
		System.out.println("wsss");
try {
	System.out.println("hey");
	System.err.println("looool");
	throw new Exception("alles noobs hier");
	
} catch (Exception e) {
	e.printStackTrace();
}
		System.out.println("und ende");
		LogWriter.stop();
	}
	
	private static void stop() {
		System.setOut(System.out);
		System.setErr(System.err);		
	}

/**
 * starts the logging to the stated log file
 * @param includeThread will write the stack trace of all println commands called as well next to the statement (for debugging mostly)
 * @throws IOException
 */
	public static void start(boolean includeThread) throws IOException{
			
		if(!errorLog.exists())
			errorLog.createNewFile();
		
		if(!systemLog.exists())
			systemLog.createNewFile();
			
		refreshFileStreams();
		//Puts the regular and our other output streams together
		multiOut = new MultiOutputStream(System.out, fout);
		multiErr = new MultiOutputStream(System.err, ferr, fout);
		
		
		//overwrite all the println methods to adjust the DEBUGGING and Thread names in addition to the actual output
		if(includeThread) {
			stdout = new PrintStream(multiOut) {
				@Override
				public void println(String x) {
					super.println(adjustDebugOutput(x, true));
				}				
				
				@Override
				public void println() {
					super.println(adjustDebugOutput("", true));
				}
				
				@Override
				public void println(boolean x) {
					super.println(adjustDebugOutput(String.valueOf(x), true));
				}
				
				@Override
				public void println(char x) {
					super.println(adjustDebugOutput(String.valueOf(x), true));
				}
				
				@Override
				public void println(char[] x) {
					super.println(adjustDebugOutput(x.toString(), true));
				}
				@Override
				public void println(double x) {
					super.println(adjustDebugOutput(String.valueOf(x), true));
				}
				@Override
				public void println(float x) {
					super.println(adjustDebugOutput(String.valueOf(x), true));
				}
				@Override
				public void println(int x) {
					super.println(adjustDebugOutput(String.valueOf(x), true));
				}
				@Override
				public void println(long x) {
					super.println(adjustDebugOutput(String.valueOf(x), true));
				}
				@Override
				public void println(Object x) {
					super.println(adjustDebugOutput(String.valueOf(x), true));
				}
			};
			
			
			
			stderr = new PrintStream(multiErr) {
				@Override
				public void println(String x) {
					super.println(adjustDebugOutput(x, false));
				}				
				
				@Override
				public void println() {
					super.println(adjustDebugOutput("", false));
				}
				
				@Override
				public void println(boolean x) {
					super.println(adjustDebugOutput(String.valueOf(x), false));
				}
				
				@Override
				public void println(char x) {
					super.println(adjustDebugOutput(String.valueOf(x), false));
				}
				
				@Override
				public void println(char[] x) {
					super.println(adjustDebugOutput(x.toString(), false));
				}
				@Override
				public void println(double x) {
					super.println(adjustDebugOutput(String.valueOf(x), false));
				}
				@Override
				public void println(float x) {
					super.println(adjustDebugOutput(String.valueOf(x), false));
				}
				@Override
				public void println(int x) {
					super.println(adjustDebugOutput(String.valueOf(x), false));
				}
				@Override
				public void println(long x) {
					super.println(adjustDebugOutput(String.valueOf(x), false));
				}
				@Override
				public void println(Object x) {
					super.println(adjustDebugOutput(String.valueOf(x), false));
				}
			};
		}else {
			stdout = new PrintStream(multiOut);
			stderr = new PrintStream(multiErr);
		}
		
		//set the systems output streams to our multi output stream where the original output stream is also located within
		System.setOut(stdout);
		System.setErr(stderr);
	}
	
	/**
	 * Initially, the Thread which shows where the command was comming from is the 3rd one (otherwise it would be the sysout, or this Thread which is irrelevant)
	 * Remember to increase the stack if you put a log catcher in between or decrease it if you remove it
	 */
	private static int ThreadStack = 3;
	
	/**
	 * Adjustst the string which should be printed to include the current thread next to it
	 * @param x the string which to print
	 * @param out is the adjusted output for an out or err stream. Depending on which, the written thread is the same (out) or above (err)
	 * @return the adjusted output
	 */
	private static String adjustDebugOutput(String x, boolean out) {
		int addedSpaces = 100-x.toString().length();
		String add = "";
		while(addedSpaces>0) {
			add+=" ";
			addedSpaces--;
		}

		
		if(x.toString().startsWith(DEBUG_STRING)) {
			//output the 4th stack in the trace (0=getStackTrace(), 1= adjustDebugOutput(), 2=println(), 3=printlnDebug(), 4=The thread where println() or printerr() was called)
			x+=add+"   Thread: "+Thread.currentThread().getName()+" - "+Thread.currentThread().getStackTrace()[ThreadStack+1];
//			x+=add+"   Thread: "+Thread.currentThread().getStackTrace()[4]+" :: "+Arrays.asList(Thread.currentThread().getStackTrace());
		}else {
			//output the 4th stack in the trace (0=getStackTrace(), 1= adjustDebugOutput(), 2=println(), 3=The thread where println() or printerr() was called)
			x+=add+"   Thread: "+Thread.currentThread().getName()+" - "+Thread.currentThread().getStackTrace()[ThreadStack];
//			x+=add+"   Thread: "+Thread.currentThread().getStackTrace()[3]+" :: "+Arrays.asList(Thread.currentThread().getStackTrace());
		}

			return x;
	}
	
	/**
	 * Set the folder where to store the system and error logs (usually something like "Log" in the root folder
	 * @param logFileFolder
	 * @throws FileNotFoundException
	 */
	public static void setLogPath(File logFileFolder) throws FileNotFoundException
	{
		if(!logFileFolder.exists()) {
			System.out.println("Log Folder not existing, creating folder(s)");
			logFileFolder.mkdirs();
		}
		systemLog = new File(logFileFolder+""+File.separator+"System Log.log");
		errorLog = new File(logFileFolder+""+File.separator+"Error Log.log");
		refreshFileStreams();
	}
	
	private static void refreshFileStreams() throws FileNotFoundException
	{
		fout= new FileOutputStream(systemLog);
		ferr= new FileOutputStream(errorLog);
	}
	
	//not used
	@Deprecated
	private static void addOutputStream(OutputStream newOutputStream)
	{
		multiOut.addOutputStream(newOutputStream);
	}
	
	//not used
	@Deprecated
	private static void addErrorOutputStream(OutputStream newOutputStream)
	{
		multiErr.addOutputStream(newOutputStream);
	}

	public static final void printlnDebug(Object o) {printlnDebug(""+o);}
	public static final void printlnDebug(int t) {printlnDebug(""+t);}
	
	public static final void printlnDebug(String string) {
		if(DEBUGGING) {
			System.out.println(DEBUG_STRING+": "+string);
		}
	}

	/**
	 * Increases the Stack count for one. Use it when you use a logger in between to catch all of your sysouts and syserrs
	 */
	public static void increaseThreadStack() {ThreadStack++;}

	/**
	 * Decreases the Stack count for one. Use it when you use remove a logger in between to catch all of your sysouts and syserrs
	 */
	public static void decreaseThreadStack() {ThreadStack--;}

}
