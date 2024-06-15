package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import internet.Internet;
import systemIndependet.KiraTxtIni;

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
public class WebServer extends Vars{

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
	//	public static String HTML_CODE = "<html>\r\n" + 
	//		"<body>\r\n" + 
	//		"\r\n" + 
	//		"<h1>My First Heading2</h1>\r\n" + 
	//		"<p>My first paragraph.</p>\r\n" + 
	//		"\r\n" + 
	//		"</body>\r\n" + 
	//		"</html>";



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
	public WebServer(int port) throws IOException {
	}

	/**
	 * Starts a server with the default port of 8080
	 * @throws IOException
	 */
	public WebServer() throws IOException {
		//		init();
	}

	public static void main (String [] args) throws Exception, Throwable{
		WebServer ser = new WebServer();
		ser.startServer();
		Thread.sleep(10000000);
		ser.stopServer();
		System.out.println("end of code");
	}

	//	private void init() {
	//		HTML_CODE="";
	//		for(String s : KiraTxtIni.txtReader("index.html")) {
	//			HTML_CODE+=s;
	//		}
	//		System.out.println(HTML_CODE);
	//	}


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
					System.out.println("waiting");
					final Socket remote = server.accept();
					// remote is now the connected socket
					System.out.println("Connection accepted from " +
							remote.getInetAddress());
					
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
					}else	//irgendwie kaputt gegangen (noch nie vorgekommen)
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




	private void processRequest(Socket remote) throws IOException {
		//make sure it uses UTF_8 as decoding like in the javascript file, as otherwise our escape characters will get auto transformed back
		InputStreamReader isr = new InputStreamReader(
				remote.getInputStream(), StandardCharsets.UTF_8);
		
		BufferedReader in = new BufferedReader(isr);
		//		PrintWriter out = new PrintWriter(remote.getOutputStream());
		PrintStream out = new PrintStream(remote.getOutputStream());

		HashMap<String, String> requestMap = readIncomming(in);
		
		if(requestMap==null) {System.out.println("Socket died or timeout");return;}

		String[] sa =  requestMap.get(HEADER).split(" ");
		String rqCode = sa[0];
		String rqName = sa[1];
		String rqHttpVer = sa[2];
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
		System.out.println("all sent :)");

	}

	
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
		System.out.println("1");
		request = in.readLine();
		System.out.println("2");
		if (request == null || request.length() == 0) {
			// No point nattering: the sock died, nobody will hear
			// us if we scream into cyberspace... Could log it though.
			System.err.println("Request error: "+request);
			return null;
		}

		System.out.println("start reading incomming transmission");

		//save the first line as the "header", where GET, POST etc are declared
		HashMap<String, String> ret = new HashMap<String, String>();
		ret.put(HEADER, request);

		// Read headers, up to the null line before the body,
		// so the body can be read directly if it's a POST.
		String hdrLine;
		while ((hdrLine = in.readLine()) != null &&
				hdrLine.length() != 0) {
			int ix;
			if ((ix=hdrLine.indexOf(':')) != -1) {
				String hdrName = hdrLine.substring(0, ix);
				String hdrValue = hdrLine.substring(ix+1).trim();
				//					Debug.println("hdr", hdrName+","+hdrValue);
				ret.put(hdrName, hdrValue);
//				System.out.println(hdrValue);
			} else {
				System.err.println("INVALID HEADER: " + hdrLine);
			}
		}
		System.out.println("header reading done");
		System.out.println("request: "+request);
		
//		
//		//TEST
		
//		if(request.equalsIgnoreCase("POST /upload?path=my%20wine%20meme.jpg HTTP/1.1")) {
//			
//		int t = 0;
//		String tl = "";
//		System.out.println("start read ooooooooooooooooooooooooooOOO");
//		while () != null &&
//				hdrLine.length() != 0) {
//			System.out.println("reading line");
//			tl+=hdrLine;
//			t++;
//			System.out.println(hdrLine);
//		}
//		}
//		
//		
//		//TEST ENDE
		//------------------------------------------
		return ret;
	}


	/** Sends an error response, by number, hopefully localized. */
	static void errorResponse(int errNum, String errMsg, PrintStream out) {
		// Check for localized messages
		String response;

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

	/** Processes one file request */
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



	void doDirList(String rqName, File dir, boolean justAHead, PrintStream os) {
		os.println("HTTP/1.0 200 directory found");
		os.println("Content-type: text/html");
		os.println("Date: " + new Date().toString());
		os.println("");
		if (justAHead)
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

	/** Send one file, given a File object. */
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

	/** Send one file, given the filename and contents.
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

	protected static String guessMime(String fn) {
		String lcname = fn.toLowerCase();

		//check "makefile" file case
		int extenStartsAt = lcname.lastIndexOf('.');
		if (extenStartsAt<0) {
			if (fn.equalsIgnoreCase("makefile"))
				return "text/plain";
			return UNKNOWN;
		}

		//search mime
		String exten = lcname.substring(extenStartsAt);
		String guess = getMimeType(exten, UNKNOWN);

		return guess;
	}

	public boolean isRunning() {
		return isRunning;
	}
}
