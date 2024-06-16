package kiraNeccesaryLibs;

import java.io.*;
import java.nio.file.DirectoryIteratorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

public class KiraTxtIni {

	/**
	 * Adjustes you path, so that sth like "..\" is considered folder up and ".\" means root folder (usually not necessary, 
	 * as they will be treated as relative path if no drive is mentioned like "C:" or something like that.\\
	 * E.g. We are in root folder "C:\f\f2\XX\p" and use as path="..\..\hello" we will get "C:\f\f2\hello" as a result path.
	 * @param path the path containing relative stuff like ..\ or .\ etc.
	 * @return the absolute path leading to mentioned directory
	 */
	
	public static String relativePathToAbsolutePath(String path) {
		
		//get the absolute path and simultaneously adjust all "/" and "\" to the correct notation depending on OS.
		File f = new File(path);
		path = f.getAbsolutePath();
		
		//remove the ".\" or "./" as we treat every string relative anyway ^^ 
		path=path.replace(File.separator+"."+File.separator,File.separator);
		
		System.out.println(path);
		
//		System.out.println(path);
		
		//remove the folder which comes in front of the ".." --> ../../XY = go back two folders upwards and then into folder XY
		while(path.contains("..")) {
			int index = path.indexOf("..");
			String temp = path.substring(0,index-1);
//			System.out.println(temp);
			temp = temp.substring(0,temp.lastIndexOf(File.separator));
//			System.out.println(temp);
			path=temp+path.substring(index+2);
//			System.out.println("XXX "+path);
		}
//		System.out.println(path);
		return path;
	}
	
	
	
	
	//txt Reader2.0 (ArrayList)-----------------------------------------------------------------------------------------------------------
	
	public static ArrayList<String> txtReader (File f){		//input: Path als String; returns: alle Zeilen des Txt-Dokuments als String[]
		return txtReader(f.getAbsolutePath());
	}
	
	public static ArrayList<String> txtReader (String spath){		//input: Path als String; returns: alle Zeilen des Txt-Dokuments als String[]
		try{
			File f = new File(spath);
			
			FileReader fr = new FileReader(f.getAbsolutePath());
			BufferedReader br = new BufferedReader(fr);

			String cl = "";
			ArrayList<String> reader = new ArrayList<String>(1000);
			reader.add("");											//erste Zeile ist frei --> erster eintrag in zeile.get(1) & letzte Zeile = null
			int ensureCapacity = 1;
			while(cl != null)
			{
				cl=(br.readLine());
				reader.add(cl);
				if(ensureCapacity>=1000)
				{
					ensureCapacity=1;
					reader.ensureCapacity(reader.size()+1000);
				}
				ensureCapacity++;
			}
			br.close();
			
			//remove first empty line and last (NULL)
			reader.remove(0);
			reader.remove(reader.size()-1);
			
			return reader;
		}catch(IOException x)
		{
			System.out.println("path:\""+spath+"\"");
			System.out.println("txtReader Fehler:"+x);
			return null;
		}
	}
	
	//------------------------------------------------------------------------------------------------------------------------------------
	
	public static String iniReader (File file, String search) throws IOException{		//input: Path als File; returns: alle Zeilen des Txt-Dokuments als String[]
			return iniReader(file.getAbsolutePath(), search);
		}
	/**
	 * 
	 * @param spath
	 * @param search
	 * @return null if no Property was found
	 * @throws IOException
	 */
	public static String iniReader (String spath, String search) throws IOException{		//input: Path als String; returns: alle Zeilen des Txt-Dokuments als String[]
	Properties props = iniReader(spath);
	String found = props.getProperty(search);
	
    return found;
	}
	
	public static Properties iniReader(File spath) throws IOException{
		return iniReader(spath.getAbsoluteFile().toString());
	}
	public static Properties iniReader(String spath) throws IOException{
		//do not ask me why I need to make it this complicated, but if the path is not found it will stop here and will not even tell me where it happened :/
		//so this redundant try-cath phrase is necessary!!!!!!!
		try {
			
			String x = StringManipulator.stringListTOString(txtReader(spath), false, true);
			Properties props = new Properties();
			props.load(new StringReader(x.replace("\\","\\\\")));
			
			return props;
		} catch (Exception e) {
//			e.printStackTrace();
			throw new IOException();
		}
		
	}

	
	//------------------------------------------------------------------------------------------------------------------------------------
	//TxtWriter
	
	public static void txtWriter(String spath, ArrayList<String> content){
		txtWriter(spath, content, true);
	}

	public static boolean txtWriter(String spath, ArrayList<String> content, boolean startWithNewLine){ //input s=file path, content=zeile die eingefuegt werden soll
		try {
			
			File f = new File(spath);
			
			// if file doesnt exists, then create it
			if (!f.exists()) {
				f.createNewFile();
				System.out.println("created File at "+spath);
				startWithNewLine=false;
			}
 
			FileWriter fw = new FileWriter(f.getAbsolutePath(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			System.out.println(content);
			for (int i = 0; i < content.size(); i++) {
				if (!startWithNewLine && i == 0) {
				} else {
					bw.newLine();
				}

				bw.write(content.get(i));
			}

			bw.close();

			// System.out.println("Done");
			return true;
		} catch (IOException x) {
			System.out.print("txtWriter Fehler: ");
			x.printStackTrace();
			return false;
		}
	}

	// ------------------------------------------------------------------------------------------------------------------------------------

	public static void txtWriter(String spath, String content){
		txtWriter(spath, content, true);
	}

	// input s=file path, content=zeile die eingefuegt werden soll
	public static void txtWriter(String spath, String content, boolean startWithNewLine) {
		ArrayList<String> temp = new ArrayList<String>();
		temp.add(content);
		txtWriter(spath, temp, startWithNewLine);

	}

}
