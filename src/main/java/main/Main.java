package main;

import java.io.File;
import java.io.IOException;

import kiraNeccesaryLibs.LogWriter;

public class Main {

	
	public static void main (String [] args) throws Exception, Throwable{
		//start the logger
				try {
				LogWriter.setLogPath(new File("Logs"));
				LogWriter.start(true);
				} catch (IOException e) {
					System.err.println("Unable to start the log writer!");
					e.printStackTrace();
				}
				
		System.out.println("starting the server");
		RemoteExplorer ser = new RemoteExplorer();
//		ser.startServer();
		GUI gui = new GUI(ser);
		
//		Thread.sleep(10000000);
		//TODO how to know when to end the server and while(true) to wait here (maybe GUI und oder minimize to tray)
		
//		ser.stopServer();
//		System.out.println("end of code");
	}
	
}
