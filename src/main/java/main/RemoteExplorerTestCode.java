package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Some random code for testing purpose only
 */
public class RemoteExplorerTestCode {

	  /**
	   * RemoteExplorer constructor.
	   */
	  protected void start() {
	    ServerSocket s;

	    System.out.println("RemoteExplorer starting up on port 80");
	    System.out.println("(press ctrl-c to exit)");
	    try {
	      // create the main server socket
	      s = new ServerSocket(8080);
	    } catch (Exception e) {
	      System.out.println("Error: " + e);
	      return;
	    }

	    System.out.println("Waiting for connection");
	    for (;;) {
	      try {
	        // wait for a connection
	        Socket remote = s.accept();
	        // remote is now the connected socket
	        System.out.println("Connection, sending data.");
	        BufferedReader in = new BufferedReader(new InputStreamReader(
	            remote.getInputStream()));
	        PrintWriter out = new PrintWriter(remote.getOutputStream());

	        // read the data sent. We basically ignore it,
	        // stop reading once a blank line is hit. This
	        // blank line signals the end of the client HTTP
	        // headers.
	        String str = ".";
	        while (!str.equals(""))
	          str = in.readLine();

	        // Send the response
	        // Send the headers
	        out.println("HTTP/1.0 200 OK");
	        out.println("Content-Type: text/html");
	        out.println("Server: Bot");
	        // this blank line signals the end of the headers
	        out.println("");
	        // Send the HTML page
	        out.println("<H1>Welcome to the Ultra Mini-RemoteExplorer</H2>");
	        out.flush();
	        remote.close();
	      } catch (Exception e) {
	        System.out.println("Error: " + e);
	      }
	    }
	  }

	  /**
	   * Start the application.
	   * 
	   * @param args
	   *            Command line parameters are not used.
	   */
	  public static void main(String args[]) {
	    RemoteExplorerTestCode ws = new RemoteExplorerTestCode();
	    ws.start();
	  }
}
