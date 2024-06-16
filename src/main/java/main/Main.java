package main;

import java.io.File;
import java.io.IOException;

import kiraNeccesaryLibs.LogWriter;
/**
 * Main class that serves as an entry point to the whole program.
 * Loads the server as well as the GUI to access its functions.
 */
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
		//starting the server backend
		RemoteExplorer ser = new RemoteExplorer();
		//Starting the server frontend
		GUI gui = new GUI(ser);
		
	}
	
}
