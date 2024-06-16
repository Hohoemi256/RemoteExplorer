package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.Scanner;

import kiraNeccesaryLibs.KiraTxtIni;

public class Vars {
	private final static int DEFAULT_PORT = 8080;
	private static int port = DEFAULT_PORT;

	public static final String ROOT_DIR = System.getProperty("user.dir");	//legt den startpfad in den Root Ordner von Kira, endet nicht mit '\' !!!

	/**
	 * The filename of the html page which should be submitted when a client connects with the server
	 * <br>Default is "index.html"
	 * @see WebServer#HTML_PATH
	 */
	private static String INDEX_FILE_NAME = "index.html";


	/**
	 * The path from the root where your html files are located. In case it is the regular root, it will be ""(empty), otherwise e.g. in folder KWS it is "/KWS".
	 */
	private static String HTML_PATH = ROOT_DIR+"/KWS";


	/** A Properties, for loading mime types (file extensions) into */
	private static Properties mimeTypes = new Properties();
	private static Properties properties = new Properties();
	public static boolean safeDelete = true; //true is recommended; will not delete the file when requested, but will create a folder in the root of the file to be manually removed later by the admin
	static final String TRASH_FOLDER = "TRASH"; //The name of the folder which should act as the trash bin (More infos in the readme)


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
	
	
	//load static props
	static {
		try {
			System.out.println(ROOT_DIR+File.separator+"mime.properties");
			FileReader frmt = new FileReader(ROOT_DIR+File.separator+"mime.properties");
			File settings = new File(ROOT_DIR+File.separator+"settings.properties");
//			FileReader frs = new FileReader(settings);
			mimeTypes.load(frmt);
			String settingsStr = readTXTFile(settings);

			if(settingsStr != null) {//if the settings properties file is not found, dont event bother reading from it
			
				//the properties.load() function can not read single "\" elements, as it is a special character. replacing it to "//" will return a normal "/"
			StringReader frs = new StringReader(settingsStr.replace("\\", "\\\\"));
			properties.load(frs);

			String portx = properties.getProperty("port");
			System.out.println(portx);
			if(portx != null && !portx.isEmpty()) {
				try{
					setPort(Integer.parseInt(portx));
					System.out.println("setting port to: "+getPort());}
				catch(java.lang.ExceptionInInitializerError e) {
					System.err.println(portx+" is not a valid port number!!! (can not read and parse it to a integer)");}
			}

			String fol = properties.getProperty("htmlFolder");
			if(fol != null && !fol.isEmpty()) {
				setHtmlFolderPath(KiraTxtIni.relativePathToAbsolutePath(fol)); //if the Path is set with "\" instead of "/"
				System.out.println("setting html path to: "+getHtmlFolderPath());}

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



	//---Methods

	protected static int getPort() {return port;}
	protected static void setPort(int port) {Vars.port = port;}

	protected static String getIndexFileName() {return INDEX_FILE_NAME;}
	protected static void setIndexFileName(String dEF_NAME) {INDEX_FILE_NAME = dEF_NAME;}

	protected static String getHtmlFolderPath() {return HTML_PATH;}
	protected static void setHtmlFolderPath(String path) {HTML_PATH = path;}


	public static String getMimeType(String type) {return mimeTypes.getProperty(type);}
	public static String getMimeType(String type, String dflt) {return mimeTypes.getProperty(type, dflt);}
}
