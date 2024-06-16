package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.Scanner;

import kiraNeccesaryLibs.KiraTxtIni;

/**
 * Background class where important global variables are stored and accessed on various parts of the program.
 */
public class Vars {
	/**
	 * Fallback port if none was specified in the settings file
	 */
	private final static int DEFAULT_PORT = 80;
	/**
	 * Port at which the server is running
	 */
	private static int port = DEFAULT_PORT;

	/**
	 * Root directory of the system. Used to locate external files like HTML, css etc. files
	 */
	public static final String ROOT_DIR = System.getProperty("user.dir");	//legt den startpfad in den Root Ordner von Kira, endet nicht mit '\' !!!

	/**
	 * The filename of the html page which should be submitted when a client connects with the server
	 * <br>Default is "index.html"
	 * @see RemoteExplorer#HTML_PATH
	 */
	private static String INDEX_FILE_NAME = "index.html";


	/**
	 * The path from the root where your html files are located. In case it is the regular root, it will be ""(empty), otherwise e.g. in folder KWS it is "/KWS".
	 */
	private static String HTML_PATH = ROOT_DIR+"/KWS";


	/** Properties, for loading mime types (file extensions) into */
	private static Properties mimeTypes = new Properties();
	/**
	 * Loading of the user settings as specified in the settings.properties file. 
	 */
	private static Properties properties = new Properties();
	/**
	 * true is recommended; will not delete the file when requested, but will create a folder in the root of the file to be manually removed later by the admin
	 */
	public static boolean safeDelete = true; 
	/**
	 * The name of the folder which should act as the trash bin (More infos in the readme)
	 */
	static final String TRASH_FOLDER = "TRASH";


	/**
	 * reads the Txt file and returns it as a String
	 * @param f the Path to your file
	 * @return the txt file content as a string or null if the path does not exist or is unreadable etc.
	 */
	private static String readTXTFile(File f) {
		String ret = "";
		 try {
		      Scanner myReader = new Scanner(f);
		      while (myReader.hasNextLine()) {
		    	  ret += myReader.nextLine()+"\n";
		      }
		      myReader.close();
		    } catch (FileNotFoundException e) {
		      System.out.println("setting.properties file not found.");
		      e.printStackTrace();
		      return null;
		    }
		 return ret;
	}
	
	
	//load static properties
	static {
		try {
			System.out.println("loading mime properties: "+ROOT_DIR+File.separator+"mime.properties");
			FileReader frmt = new FileReader(ROOT_DIR+File.separator+"mime.properties");
			File settings = new File(ROOT_DIR+File.separator+"settings.properties");
//			FileReader frs = new FileReader(settings);
			mimeTypes.load(frmt);
			String settingsStr = readTXTFile(settings);

			if(settingsStr != null) {//if the settings properties file is not found, dont event bother reading from it

				//the properties.load() function can not read single "\" elements, as it is a special character. replacing it to "//" will return a normal "/"
				StringReader frs = new StringReader(settingsStr.replace("\\", "\\\\"));
				properties.load(frs);

				//load custom server port
				String portx = properties.getProperty("port");
				if(portx != null && !portx.isEmpty()) {
					try{
						setPort(Integer.parseInt(portx));
						System.out.println("setting port to: "+getPort());}
					catch(java.lang.ExceptionInInitializerError e) {
						System.err.println(portx+" is not a valid port number!!! (can not read and parse it to a integer)");}
				}
				
				//load folder where HTML, css etc files are stored
				String fol = properties.getProperty("htmlFolder");
				if(fol != null && !fol.isEmpty()) {
					setHtmlFolderPath(KiraTxtIni.relativePathToAbsolutePath(fol)); //if the Path is set with "\" instead of "/"
					System.out.println("setting html path to: "+getHtmlFolderPath());}
				
				//load index file (default is index.html)
				String index = properties.getProperty("indexFile");
				if(index != null && !index.isEmpty()) {
					setIndexFileName(index);
					System.out.println("setting Index File to: "+getIndexFileName());}

			}
		} catch (IOException e) {
			System.err.println("could not load mime type file mime.properties in "+ROOT_DIR);
			e.printStackTrace();
		}
	}



	//---Setter/Getter Methods------

	protected static int getPort() {return port;}
	protected static void setPort(int port) {Vars.port = port;}

	protected static String getIndexFileName() {return INDEX_FILE_NAME;}
	protected static void setIndexFileName(String dEF_NAME) {INDEX_FILE_NAME = dEF_NAME;}

	protected static String getHtmlFolderPath() {return HTML_PATH;}
	protected static void setHtmlFolderPath(String path) {HTML_PATH = path;}

	/**
	 * Will try to identify the mime type for the given extension (e.g. ".gif"-->image/gif) as stated in the mime.properties file.
	 * @param type the extension where the mime type should be identified for 
	 * @return the mime type for the corresponding extension
	 */
	public static String getMimeType(String type) {return mimeTypes.getProperty(type);}
	/**
	 * Same as {@link #getMimeType(String)}, but also includes a default value which will be returned if no match has been found.
	 * @param type the extension of which to retrieve the mime type of
	 * @param dflt default value which will be returned if no match has been found
	 * @return the identified mime type if found or the stated default mime type otherwise
	 * @see Vars#getMimeType(String)
	 */
	public static String getMimeType(String type, String dflt) {return mimeTypes.getProperty(type, dflt);}
}
