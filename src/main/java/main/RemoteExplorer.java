package main;

import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import kiraNeccesaryLibs.Internet;
import kiraNeccesaryLibs.LogWriter;


/**
 * If using the port 80, you dont have to specify the port in your webbrowser.
 * Example if you use port 8888, then to reach the site you have to go to "localhost:8888"
 * but if you chose port 80, you can just type "localhost" and hit enter.<br>
 * You can declare the port in the Port in the constructor, otherwise it will be default setted to Port 8080<br><br>
 * 
 * If you connect to the server the first time, the index.html page will be transmitted which is located in you root folder.<br>
 * You can use AJAX with this server. To call special responses which you want to declare, use the KWS as the
 * request code instead of HEAD, GET, POST etc.
 * calling xhttp.open("KWS", "test", true); will give you in return the String "Test erfolgreich" as a demo expample.<br>
 * @author Hohoemi256
 *
 */
public class RemoteExplorer extends Vars{

	private final static String HEADER = "header";
	protected static final String RQ_INVALID = "INVALID", RQ_GET = "GET", RQ_HEAD = "HEAD",
			RQ_POST = "POST", RQ_KWS = "KWS"; 
	//	private HashMap<String, String> map = new HashMap<String, String>();


	//	protected final static String HTML_PATH = "";

	private boolean isRunning = false;
	/**
	 * If enabled loads files only once and puts the bytes into a internal hash table ({@link #cache}, so the next time the same object is loaded, it will use the cached file.
	 * Makes the sending faster, but also ignores changes made during that time to the original file. So if your files are changing while the server is running, you might want to set this value to false.
	 */
	private boolean useCache = false;


	/** The Hashtable used to cache all URLs, images etc. we've read (otherwise we would always e.g. read the bytes of an image which is requested, but like this we can just send the cached bytes directly (faster)).
	 * Static, shared by all instances of Handler (one Handler per request;
	 * this is probably quite inefficient, but simple. Need ThreadPool).
	 * Note that Hashtable methods *are* synchronized.
	 */
	private static Hashtable cache = new Hashtable();

	static {
		cache.put("", "<html><body><b>Unknown server error</b>".getBytes());
	}


	//	private ArrayList<String> requestSequence = new ArrayList<String>();


	/**
	 * To connect to this server, you have to create it and call {@link #startServer()}, which
	 * will create a new Thread for listening to incomming requests.
	 * @param port the port on which the server should run
	 * @throws IOException
	 */
	public RemoteExplorer(int port) throws IOException {
		Vars.setPort(port);
	}

	/**
	 * Starts a server with the default port as stated in {@link Vars#getPort()}
	 * @throws IOException
	 */
	public RemoteExplorer() throws IOException {
		//		init();
	}

	/**
	 * Example of how to initialize and start the server. 
	 * @param args
	 * @throws Exception
	 * @throws Throwable
	 */
	public static void main (String [] args) throws Exception, Throwable{
		RemoteExplorer ser = new RemoteExplorer();
		ser.startServer();
		Thread.sleep(10000000);
		ser.stopServer();
		System.out.println("end of code");
	}


	/**
	 * Starts the the server by starting a new Thread for it
	 * @return the Thread, on which the server is running
	 */
	public Thread startServer(){
		Thread ret = new Thread(new Runnable() {

			public void run() {
				runServer();
			}
		});
		ret.start();
		return ret;
	}

	/**
	 * disconnects all clients and shuts down this server
	 */
	public void stopServer(){
		System.out.println("shutting down Server ");
		try {
			server.close();
		} catch (IOException e) {
			System.out.println("WHAT THE FCK I CANT CLOSE THIS DAMN SERVERSOCKET!!!!");
		}
	}

	private ServerSocket server;
	/**
	 * Starts the server which was declared in the constructor
	 */
	private synchronized void runServer(){

		if(isRunning()) {return;}
		try {
			server = new ServerSocket(getPort());
			System.out.println("starting server at IP: "+Internet.getLocaleIP()+":"+Vars.getPort());
			isRunning=true;
			while (true) {

				try {
					// wait for a connection
					System.out.println("waiting for connection of client to server...");
					final Socket remote = server.accept();
					// remote is now the connected socket
					System.out.println("Connection accepted from " +
							remote.getInetAddress());
					
					//Start a new Thread to handle all requests from the newly connected client
					Thread handle = new Thread(new Runnable() {

						public void run() {
							try {
								processRequest(remote);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
					handle.start();
					
				} catch (IOException e) { //restart or exit
					if(server.isClosed())
					{
						System.out.println("server shutdown");
						break;
					}else	//irgendwie kaputt gegangen (noch nie vorgekommen, aber nur für den Fall der Faelle)
					{	
						server= new ServerSocket(getPort());
						System.out.println("restart server");
					}
				}
			}
			server.close();
			isRunning=false;
			System.out.println("exit");
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

	}



/**
 * Processes all requests that come from the connected Socket. Usually this should be someone who connects to the server via a webbrowser.
 * Sends, depending on the request, html data ({@link #RQ_GET},{@link #RQ_HEAD} or other informations like some information which is enquired by an ajax request ({@link #RQ_KWS}, {@link #RQ_POST}).
 * @param remote the client which is connected
 * @throws IOException
 */
	private void processRequest(Socket remote) throws IOException {
		//make sure it uses UTF_8 as decoding like in the javascript file, as otherwise our escape characters will get auto transformed back
		InputStreamReader isr = new InputStreamReader(
				remote.getInputStream(), StandardCharsets.UTF_8);
		
		BufferedReader in = new BufferedReader(isr);
		//		PrintWriter out = new PrintWriter(remote.getOutputStream());
		PrintStream out = new PrintStream(remote.getOutputStream());
		
		//Read the request from the client
		HashMap<String, String> requestMap = readIncomming(in);
		
		if(requestMap==null) {System.out.println("Socket died or timeout");return;}

		String[] sa =  requestMap.get(HEADER).split(" "); //extract infos by splitting the HEADER string at spaces
		String rqCode = sa[0]; //request code to identify what the client want exactly
		String rqName = sa[1]; //optional or extending information that is delivered afterwards to tell the command in more detail
		String rqHttpVer = sa[2]; //What http version is used
		System.out.println("Request: Command: \"" + rqCode +
				"\"; file or command: \"" + rqName + "\"; version: " + rqHttpVer);


		//put response in output stream

		//only specific KiraWebServerReqests
		if(rqCode.equalsIgnoreCase(RQ_KWS) ) {
			KWSmethods.handleAJAX(rqName.substring(1), out, null);
			//regular stuff like getting the html files
		}else if(rqCode.equalsIgnoreCase(RQ_POST)){
			System.out.println("POST fuer mich , Post fuer mich, Post fuer mich da freu ich mich!");
//			readPOSTBody(in);
			KWSmethods.handleAJAX(rqName.substring(1), out, in);

		}else if (rqCode.equalsIgnoreCase(RQ_GET)
				|| rqCode.equalsIgnoreCase(RQ_HEAD))  	{
			processFile(rqName, rqCode.equalsIgnoreCase(RQ_HEAD), out , requestMap);
		}else {
			//Error 404
			System.err.println("could not handly that request: "+rqCode);
			errorResponse(400, "invalid method: " + rqCode, out);
			return;
		}

		//after everything is written flush that shit to the client
		out.flush();
		//and close the connection for being able to connect to a new socket
		remote.close();
		System.out.println("sent all data to client!");

	}

	@Deprecated
	private String readPOSTBody(BufferedReader in) {

		System.out.println("POST!");
		try {
			System.out.println(in.read());
			
//			int cc;
//			while ((cc = in.read()) != 0) {
//				System.out.println((char)cc);
//		}
			
			System.out.println("end of stream?");
			String body = in.readLine();
			System.out.println("not stuck");
			System.out.println(body);
			
			if (body == null || body.length() == 0) {
				return null;
			}
			
			System.out.println("post not null");
			
			String hdrLine;
			while ((hdrLine = in.readLine()) != null &&	hdrLine.length() != 0) {
					body+=hdrLine;
			}
			
			System.out.println("finshed body reading");
			System.out.println(body);
			return body;
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Reads the next request from the client as soon as available.
	 * The generated {@link HashMap} contains all informations of the request, that is to date the header, commands and parameter/additional information.
	 * @param in the {@link BufferedReader} linked to the {@link Socket} of the client
	 * @return a HashMap containing all informations of the request.
	 * @throws IOException
	 */
	private HashMap<String, String> readIncomming(BufferedReader in) throws IOException {
		System.out.println("start read");

		for(int x=0;x<10;x++) {
			if(in.ready()) {break;}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(!in.ready()) {System.err.println("not ready to read even after waiting 1 sek, continue anyways");
//		return null;
		}

		//check if the socket did not die halfway
		String request = ".";
		LogWriter.printlnDebug("Reading next line...");
		request = in.readLine();
		if (request == null || request.length() == 0) {
			// No point nattering: the sock died, nobody will hear
			// us if we scream into cyberspace... Could log it though.
			System.err.println("Request error: "+request);
			return null;
		}

		LogWriter.printlnDebug("Read line. Start processing incomming transmission");

		//save the first line as the "header", where GET, POST etc are declared
		HashMap<String, String> ret = new HashMap<String, String>();
		ret.put(HEADER, request);

		// Read headers, up to the null line before the body,
		// so the body can be read directly if it's a POST.
		String hdrLine;
		while ((hdrLine = in.readLine()) != null &&
				hdrLine.length() != 0) {
			int ix;
			//extract the hdrName and hdrValue (=command and optional information) if present in the header (determined by having a ":" in the header string)
			if ((ix=hdrLine.indexOf(':')) != -1) {
				//Extract the command
				String hdrName = hdrLine.substring(0, ix);
				//Extract the optional further parameter and informations
				String hdrValue = hdrLine.substring(ix+1).trim();
				//					Debug.println("hdr", hdrName+","+hdrValue);
				ret.put(hdrName, hdrValue);
			} else {
				System.err.println("INVALID HEADER: " + hdrLine);
			}
		}
		System.out.println("header reading done");
		System.out.println("request: "+request);
		
		return ret;
	}


	/** Sends an error response, in HTML style. Error can be defined by number (e.g.404) where the according interpretation and response is sent.
	 * In case the error number is not found it will send the error message stated by you as.
	 *  
	 * @param errNum the error indicated by number.
	 * @param errMsg backup error message to send in case no adequate error message is found for the error number
	 * @param out the Stream to the client where to print the error message.
	 */
	static void errorResponse(int errNum, String errMsg, PrintStream out) {
		// Check for localized messages
		String response;

		//Retrieve the error response in according to the error number (e.g. 404 --> Not found)
		try { 
			ResourceBundle messages = ResourceBundle.getBundle("errors");
			response = messages.getString(Integer.toString(errNum)); }
		catch (MissingResourceException e) { response=errMsg; }

		// Generate and send the response
		out.println("HTTP/1.0 " + errNum + " " + response);
		out.println("Content-type: text/html");
		out.println();
		out.println("<html>");
		out.println("<head><title>Error " + errNum + "--" + response +
				"</title></head>");
		out.println("<h1>" + errNum + " " + response + "</h1>");
	}

	/** Processes one file request 
	 * 
	 * @param rqName
	 * @param headerOnly
	 * @param os
	 * @param map
	 * @throws IOException
	 */
	void processFile(String rqName, boolean headerOnly, PrintStream os, HashMap<String,String> map) throws IOException {
		rqName = getHtmlFolderPath()+rqName;
		File f;
		byte[] content = null;
		Object o = cache.get(rqName);
		if (useCache && o != null && o instanceof byte[]) {
			content = (byte[])o;
			System.out.println("Using cached file " + rqName);
			sendFile(rqName, headerOnly, content, os);
		} else if ((f = new File(rqName)).isDirectory()) {
			// Directory with index.html? Process it.
			File index = new File(f, getIndexFileName());
			if (index.isFile()) {
				processFile(rqName + getIndexFileName(), index, headerOnly, os);
				return;
			}
			else {
				// Directory? Do not cache; always make up dir list.
				System.out.println("DIRECTORY FOUND");
				doDirList(rqName, f, headerOnly, os);
			}
		} else if (f.canRead()) {
			// REGULAR FILE
			processFile(rqName, f, headerOnly, os);
		}else {
			System.err.println("File not found: "+rqName);
			errorResponse(404, "File not found", os);
		}
	}


/**
 * Generates a HTML code on the fly which lists the contents of the directory stated
 * @param rqName
 * @param dir the directory to explore
 * @param headerOnly if you just want to send the head without any acutal content
 * @param os the output stream to the client
 */
	void doDirList(String rqName, File dir, boolean headerOnly, PrintStream os) {
		os.println("HTTP/1.0 200 directory found");
		os.println("Content-type: text/html");
		os.println("Date: " + new Date().toString());
		os.println("");
		if (headerOnly)
			return;
		os.println("<HTML>");
		os.println("<TITLE>Contents of directory " + rqName + "</TITLE>");
		os.println("<H1>Contents of directory " + rqName + "</H1>");
		String fl[] = dir.list();
		Arrays.sort(fl);
		for (int i=0; i<fl.length; i++)
			os.println("<br/><a href=\"" + rqName + File.separator + fl[i] + "\">" +
					"<img align='center' border='0' src=\"/images/file.jpg\">" +
					' ' + fl[i] + "</a>");
	}

	/** Send one file, given a File object. Will convert the file into bytes and then send it via {@link #sendFile(String, boolean, byte[], PrintStream)}.
	 * 
	 * @param rqName
	 * @param f the file to send for the client and hence download
	 * @param headerOnly true if you just want to send the header without the actual file bytes
	 * @param os the output stream towards the client
	 * @throws IOException
	 */
	void processFile(String rqName, File f, boolean headerOnly, PrintStream os) throws IOException {
		System.out.println("Loading file " + rqName);
		InputStream in = new FileInputStream(f);
		byte c_content[] = new byte[(int)f.length()];
		// Single large read, should be fast.
		in.read(c_content);
		if(useCache) {
			cache.put(rqName, c_content);
		}
		sendFile(rqName, headerOnly, c_content, os);
		in.close();
	}

	/** Send one file as a byte stream to download, given the filename and contents.
	 * For specifying a whole file rather than bytes, use {@link #processFile(String, File, boolean, PrintStream)}
	 * @param justHead - if true, send heading and return.
	 */
	static void sendFile(String fname, boolean justHead, byte[] content, PrintStream os) throws IOException {
		os.println("HTTP/1.0 200 Here's your file");
		os.println("Content-type: " + guessMime(fname));
		os.println("Content-length: " + content.length);
		os.println();//ends the header

		if (justHead)
			return;
		os.write(content);
	}


	/** The type for unguessable files */
	final static String UNKNOWN = "unknown/unknown";

	/**
	 * Tries to identify the mime type for the given filename. Will extract the extension info and then search for the mime type which are defined in the 
	 * mime.properties file.
	 * @param filename
	 * @return the mime String if found to be included in your HTTP process
	 */
	protected static String guessMime(String filename) {
		String lcname = filename.toLowerCase();

		//check "makefile" file case
		int extenStartsAt = lcname.lastIndexOf('.');
		if (extenStartsAt<0) {
			if (filename.equalsIgnoreCase("makefile"))
				return "text/plain";
			return UNKNOWN;
		}

		//search mime
		String exten = lcname.substring(extenStartsAt);
		String guess = getMimeType(exten, UNKNOWN);

		return guess;
	}

	/**
	 * 
	 * @return True if the server is runing, false otherwise
	 */
	public boolean isRunning() {
		return isRunning;
	}
}
