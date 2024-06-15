package main;

import java.awt.Desktop;
import java.awt.image.ReplicateScaleFilter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Handles the ajax request from the client.
 * <br>Main method, which gets called from outside is {@link #handleAJAX(String, PrintStream)}.
 * The KWS_CODES needed for a successful interpretation are stated in the {@link KWS_CODES} and also in the Javascript file KWSscript.js which is transmitted to the client.
 * <br>Parameters of a command have to follow a "?" after the {@link KWS_CODES} as the command
 * and the parameters itself need to be in the form ("parameter=value") to be correctly interpreted.
 * @author Yari9
 * @see KWS_CODES
 * @see #getParameters(String)
 * @see #handleAJAX(String, PrintStream)
 */
public class KWSmethods {


	/**
	 * containes all the commands and essential attributes/Strings for communicating with the client. 
	 * Use the value of these codes when transmitting an ajax request to the server to get a answer.
	 * The KWS_CODES are defined in the JAVA, as well as in the Javascript file with the same values and linked to a var (or enum) to
	 * correctly identify them
	 * @author Yari9
	 *
	 */
	public enum KWS_CODES {


		UNDEFINED("unknown"),
		KWS_RESPONSE("KWS-Response"),

		SUCCESS("success"),
		FAIL("fail"),

		//action codes
		GET_DIR_CONTENT("getDirContent"),
		DOWNLOAD("download"), 
		UPLOAD("upload"), 
		COPY_PASTE("copyPaste"),
		DELETE("delete"),
		RENAME("rename"),
		MKDIR("mkdir"),
		MOVE("move"),

		//parameter codes
		PATH("path"),
		NAME("name"),
		SOURCE("source"),
		TARGET("target"),
		FILE_SIZE("filesize");


		/**
		 *Stores the value
		 */
		public final String codeString;

		/**
		 *Constructor
		 */
		private KWS_CODES(final String selectedModul){
			this.codeString = selectedModul;
		}

		/**
		 * get the KWS_CODE depending on the string corresponding with the string value		
		 * @param i the value of the KWS_Code object you want to obtain
		 * @return the corresponding KWS_CODE object or {@link KWS_CODES#UNDEFINED} if not existent
		 */
		public static KWS_CODES getKWSCode(String i) {
			for(KWS_CODES x : KWS_CODES.values())
			{
				if(x.codeString.toLowerCase().equals(i.toLowerCase()))
				{
					return x;
				}
			}
			return UNDEFINED;
		}
	}







	/**
	 * Handles an Ajax request and depending on the command it will send the corresponding response (call the correct method for further processing))
	 * <br>The requests should consist of a command which are coded by the {@link KWS_CODES} as String. 
	 * Optional parameters are sent by attaching a "?" and the parameters needed for the command to further execute the corresponding method succesfully.<br>
	 * e.g.:<br>
	 * handleAJAX("getDirContent?dir=C:/myPath/anotherPath", printerObject)<br>
	 * this will call the {@link #getDirContent(String)} method and the parameter C:/myPath/anotherPath is transmitted as the directory parameter from which to get the elements in this directory.
	 * @param request the command with optional parameters
	 * @param out the print stream to write to
	 * @param br the bufferedReader, which was used previously to read the header. Neccesarry if you want to read the body of a post request
	 * @return true if the request was succesfully handled and an answer sent.
	 * @see #getParameters(String)
	 */
	static public boolean handleAJAX(String request, PrintStream out, BufferedReader br) {
		if(request.equalsIgnoreCase("test")) {
			//header
			out.println("HTTP/1.0 200 Here's your file");
			out.println("Content-type: text/html");
			out.println();
			//header ended and now for the actual message:
			out.println("TEST ERFOLGREICH");
			return true;
		}




		String command = request;
		HashMap<KWS_CODES, String> parameters;

		if(request.contains("?")) {
			command = decodeEscapeCharacters(request.substring(0, request.indexOf("?")));
		}

		parameters = getParameters(request);

		System.out.println("------new request \""+KWS_CODES.getKWSCode(command)+"\" being handled----------");
		System.out.println("command: "+command+" ("+KWS_CODES.getKWSCode(command)+")");
		System.out.println("parameters: "+parameters);
		System.out.println("body present: "+br!=null);

		switch(KWS_CODES.getKWSCode(command)) {
		case GET_DIR_CONTENT:
			String answer=ResponseMethods.getDirContent(parameters.get(KWS_CODES.PATH));
			if(answer == null) {break;}

			sendSucess(out);
			sendText(answer, out);
			System.out.println("get dir answer sent");
			return true;

		case DOWNLOAD:
			//to download many files, the paths need to be separated by a "|" character
			String dlRequest = parameters.get(KWS_CODES.PATH);

			byte[] fileBytes;
			String filename;

			//for downloading a folder, zipping them for easier transmission
			File zipLocation = new File("downloadRequest.zip");
			//delete previous zip file if still present somehow
			ResponseMethods.delete(zipLocation);

			//TODO IF WISHED FOR:
			//Add the possibillity to download more than one file simultaneously by adding a "|" between the file paths
			//Not neccessary, as right now, multiple files will be downloaded one at a time
			if(dlRequest.contains("|") || new File(dlRequest).isDirectory()) {
				System.out.println("many files requested; zipping files and sending it");

				ArrayList<File> dls = new ArrayList<File>();
				
				//download multiple files
				if(dlRequest.contains("|")) {
				for(String cs : dlRequest.split("|")) {
					dls.add(new File(cs));
				}
				
				try {
				zipFile(dls, zipLocation);
			} catch (IOException e) {
				e.printStackTrace();
			}
				//download Directory
				}else if(new File(dlRequest).isDirectory()) {
//					try {
//			        String sourceFile = dlRequest;
//			        FileOutputStream fos;
//						fos = new FileOutputStream(zipperLocation);
//			        ZipOutputStream zipOut = new ZipOutputStream(fos);
//			        File fileToZip = new File(sourceFile);
//
//			        zipFileT(fileToZip, fileToZip.getName(), zipOut);
//			        zipOut.close();
//			        fos.close();
//					
//					
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
			        
					try {
						zipFile(new File(dlRequest), zipLocation);
					} catch (IOException e) {
						e.printStackTrace();
					}
//						zipFolder(new File(dlRequest).toPath(), zipperLocation.toPath());
//					dls.add(new File(dlRequest));
				}else {
					System.err.println("WTF is with these parameters, I do not know how to download that!: "+dlRequest);
				}


				
				
				fileBytes=ResponseMethods.getFileBytes(zipLocation);
				filename = zipLocation.getName();
				
				//download single file
			}else {
				System.out.println("sending file to download: "+dlRequest);
				//change depending if lot of files and zip them or send single file
				fileBytes=ResponseMethods.getFileBytes(new File(dlRequest));
				filename = new File(dlRequest).getName();
			}

			System.out.println("file length: "+fileBytes.length);
			if(fileBytes.length > 1) { //=^ nothing went wrong and bytes are there to write
				try {
					sendSucess(out, false, KWS_CODES.NAME.codeString+": "+filename);
					ResponseMethods.sendFile(fileBytes, out);

					//in case we created a zip file clean up the temporary file afterwards
					ResponseMethods.delete(zipLocation);
					return true;
				} catch (IOException e) {
					System.err.println("could not send file for download");
					e.printStackTrace();
				}
			}
			break;

		case UPLOAD:
			System.out.println("accepting file upload from client");
			String body = readPOSTbody(br);
			if(ResponseMethods.uploadFile(parameters.get(KWS_CODES.PATH), Integer.parseInt(parameters.get(KWS_CODES.FILE_SIZE)), body)) {
				sendSucess(out);
				return true;
			}			
			break;

		case COPY_PASTE:
			System.out.println("Copy file: "+parameters.get(KWS_CODES.SOURCE));
			if(ResponseMethods.copyPaste(new File(parameters.get(KWS_CODES.SOURCE)),new File(parameters.get(KWS_CODES.TARGET)))) {
				sendSucess(out);
				return true;
			}
			break;

		case DELETE:
			System.out.println("DELETING FILE: "+parameters.get(KWS_CODES.PATH));
			if(parameters.get(KWS_CODES.PATH) != null && ResponseMethods.delete(new File(parameters.get(KWS_CODES.PATH)))) {
				sendSucess(out);
				return true;
			}else {
				System.err.println("Null Pointer for deleting File parameter");
			}
			break;

		case RENAME:
			System.out.println("Renaming file");
			File nf = new File(adjustDirParameter(parameters.get(KWS_CODES.PATH)));
			if(ResponseMethods.rename(nf, parameters.get(KWS_CODES.NAME))) {
				sendSucess(out);
				return true;
			}
			break;

		case MKDIR:
			File mkf = new File(adjustDirParameter(parameters.get(KWS_CODES.PATH)));
			if(ResponseMethods.mkdir(mkf, parameters.get(KWS_CODES.NAME))) {
				sendSucess(out);
				return true;
			}
			break;

		case MOVE:
			File source = new File(parameters.get(KWS_CODES.SOURCE));
			File target = new File(parameters.get(KWS_CODES.TARGET));
			if(ResponseMethods.moveTo(source, target, false)) {
				sendSucess(out);
				return true;
			}
			break;


		default:
			System.err.println("sorry, no KWS code found for "+command);
		}

		System.out.println("FALSE RESPONSE, Sorry T_T");
		sendFail(out);
		return false;
	}

	/**
	 * Extends the {@link KWSmethods#handleAJAX(String, PrintStream, BufferedReader)} method to more specific sub methods.
	 */
	private static class ResponseMethods {
	
	

			/**
			 * Gets information about the files and directories, currently located in the directory, stated in the parameter.
			 * These informations will then be converted in a XML tree, which is read and interpreted by javascript on the client side.
			 * @param dir the directory, which content should be fetched
			 * @return a String containing these information in form of a XML script, interpreted on the client side by javascript
			 */
			public static String getDirContent(String dir) {
				if(dir.equalsIgnoreCase("null")) {
					return null;
				}
	
				dir = adjustDirParameter(dir);
	
				String XMLdir = adjustXMLString(dir);
	
				//create XML String containing all the infos about the current path
				String ret = "<root path=\""+XMLdir+"\">";
				System.out.println("dir to explore "+dir);
	
				//get Drive lists if path is... well... empty and does not contain a drive letter
				if(dir.equals("")) {
					for (File xx : File.listRoots()) {
						ret+=FOLDER_OPEN;
						ret+=adjustXMLString(xx.getPath().substring(0,xx.getPath().length()-1));
						ret+=FOLDER_CLOSE;
					}
				}else {
					File[] directories = new File(dir).listFiles();
	
					System.out.println("Files in current dir found: "+directories.length);
	
					//directories == null means no directories found
					if(directories != null) {
						for (File xx : directories) {
							if(xx.isFile()) {
								ret+=FILE_OPEN;
								ret+=adjustXMLString(xx.getName());
								ret+=FILE_CLOSE;
							}else {
								ret+=FOLDER_OPEN;
								ret+=adjustXMLString(xx.getName());
								ret+=FOLDER_CLOSE;
							}
						}
					}
				}
				ret+="</root>";
				System.out.println(ret);
				return ret;
			}
	
	
			/**
			 * Sends the bytes of a file down the stream. Small code, but written for easier overlook
			 * @param b the bytes of the stream
			 * @param out the PrintStream to which the bytes should be written to
			 * @throws IOException
			 */
			public static void sendFile(byte[] b, PrintStream out) throws IOException {
	
				//			sendHeader(out,
				//					KWS_CODES.KWS_RESPONSE.codeString+": "+KWS_CODES.DOWNLOAD.codeString,
				//					KWS_CODES.NAME.codeString+": "+filename);
	
				//write file bytes to body
				out.write(b);
			}
	
	
			/**
			 * Renames a file to a new name
			 * @param f The file or directory which name should be changed
			 * @param newName the new name of the file or directory
			 * @return true if renaming was successful
			 */
			public static boolean rename(File f, String newName) {
				String parentDir = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf("\\")+1);
				File rf = new File(parentDir+newName);
				return f.renameTo(rf);
			}
	
	
			/**
			 * creates a directory at the stated path with the new folder stated
			 * @param parentDir
			 * @param folderName
			 * @return
			 */
			public static boolean mkdir(File parentDir, String folderName) {
	
				File newDir = new File(parentDir+File.separator+folderName);
				System.out.println("sads");
				System.out.println(newDir);
				return newDir.mkdir();
			}
	
			/**
			 * Deletes a file by moving it to the recycle bin or immediately removing it when not supported
			 * @param f
			 * @return
			 */
			public static boolean delete(File f) {
				if(!f.exists()) {return false;}
	
				try {
	
					if(Desktop.isDesktopSupported()) {
						Desktop.getDesktop().moveToTrash(f);
						System.out.println("move to trash");
					}else {
	
						if(f.isDirectory()) {
							for(File tf : f.listFiles()) {
								delete(tf);
							}
						}
						
						//will not delete the file when safeDelete is true, but will create a folder in the root of the file to be manually removed later by the admin
						if(Vars.safeDelete) {
							File nf = new File(f.getCanonicalPath().substring(0, 1)+":"+File.separator+Vars.TRASH_FOLDER+File.separator+f.getAbsolutePath());
							System.out.println(f.getCanonicalPath().substring(0, 1));
							System.out.println(f.getAbsolutePath());
							System.out.println(f.getCanonicalPath().substring(0, 1)+":"+File.separator+Vars.TRASH_FOLDER+File.separator+f.getAbsolutePath());
							System.out.println(nf);
							
							moveTo(f, f, false);
						}else{
							//immediate bye-bye file
							f.delete();
						}
					}
					return true;
				}catch(Exception e) {
					e.printStackTrace();
					return false;
				}
			}
	
			/**
			 * reads and returns the bytes of the file as a byte Array or an empty Array if file not present
			 * @param f
			 * @return
			 */
			public static byte[] getFileBytes(File f) {
	
				if(!f.exists()) {return new byte[0];}
	
				try {
					InputStream in = new FileInputStream(f);
					byte c_content[] = new byte[(int)f.length()];
					in.read(c_content);
					in.close();
	
					System.out.println(f.length());
					return c_content;
				} catch (IOException e) {
					e.printStackTrace();
					return new byte[0];
				}
			}
	
			/**
			 * Handles the AJAX request of uploading a file to the Server. 
			 * @param path the path of the file (including its name)
			 * @param fileSize the size of the file to compare it with what was actually recieved
			 * @param body the POST body as a String
			 * @return true if the file was successfully saved and false otherwise
			 * @see #readPOSTbody(BufferedReader)
			 */
			public static boolean uploadFile(String path, int fileSize, String body) {
				System.out.println("start reading POST body");
	
				//as there is always this "data Form:text/plain blablabla base64," before the actual data, remove this part
				String replaced = body.substring(body.indexOf("base64,")+7);
				//			System.out.println("Base64 String:"+replaced);
	
				Decoder decoder = Base64.getDecoder();
				byte[] replacedbytes = decoder.decode(replaced);
				System.out.println("total file bytes read : reported file size from client = "+body.length()+" : "+fileSize);
	
				return saveFile(new File(path), replacedbytes);
			}
	
	
			/**
			 * Saves a file by overwriting the existing bytes of a file with the new ones.
			 * @param file The file of which the bytes should be overwritten
			 * @param replacedbytes the bytes which are gonna be the new content of the file
			 * @return true if writing was successful
			 */
			private static boolean saveFile(File file, byte[] replacedbytes) {
				try {
					//create File and if file already exists, adjust name by adding a index behind
					file = adjustFileIfDuplicate(file);
	
					file.createNewFile();
	
					FileOutputStream outFile;
					outFile = new FileOutputStream(file);
					outFile.write(replacedbytes, 0, replacedbytes.length);
					outFile.close();
	
					System.out.println("finished file writing");
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}
	
	
	/**
	 * Copys one file to another location. Will automatically change the name a bit (adding a number)
	 * if the destination file already exists
	 * @param fromPath the File element which should be copied
	 * @param toPath the location of the new File (not just the destination directory, but also with the file ending)
	 * @return true if copy was successful
	 */
			public static boolean copyPaste(File fromPath, File toPath) {
				System.out.println("copy \""+fromPath.getAbsolutePath()+"\" to \""+toPath.getAbsolutePath()+"\"");
	
				if(fromPath.isFile()) {
					byte[] b = getFileBytes(fromPath);
					return saveFile(toPath, b);
				}else {
					//If is a directory create the new directory at the destination and copy every containing files and folders
					File cd = fromPath;
					File dest = adjustFileIfDuplicate(toPath);
	
					//should not happen, but just in case
					if(!dest.mkdir()) {
						return false;
					}
	
					//copy each file inside the directory to its new place
					for(File cf : cd.listFiles()) {
						copyPaste(cf, new File(dest.getAbsolutePath()+File.separator+cf.getName()));
					}
					return true;
				}
			}
	
			/**
			 * moves a file/folder to the target folder. Also copies empty all empty folders contained within.
			 * @param source the file or folder to move
			 * @param target the target folder to move to
			 * @param overwriteExisting if true will overwrite any existing file, that is at the target location, false will cancel the movement
			 * @return true if the moving of all elements were successful
			 */
			public static boolean moveTo(File source, File target, boolean overwriteExisting) {
				System.out.println("move "+source.getAbsolutePath()+" to "+target.getAbsolutePath());
				//create directory in target folder if the element to copy is a folder
				File newTarget = new File(target.getAbsolutePath()+File.separator+source.getName());
				
				
//					TODO GET IT FROM MAVE, SO IT IS USABLE
//				    FileUtils.copyDirectory(source, target);
				
				return false;
				
			}
			
			
			
			/**
			 * Works fine, but the newer one is much shorter and probably less error prone than my patchup
			 * @param source
			 * @param target
			 * @param overwriteExisting
			 * @return
			 */
			public static boolean moveToOLD(File source, File target, boolean overwriteExisting) {
				System.out.println("move "+source.getAbsolutePath()+" to "+target.getAbsolutePath());
				//create directory in target folder if the element to copy is a folder
				File newTarget = new File(target.getAbsolutePath()+File.separator+source.getName());
				if(source.isDirectory()) {
					System.out.println(newTarget);
	
					boolean xxx = mkdir(target, source.getName());
					System.out.println(xxx);
					//				
					if((newTarget.exists() && !newTarget.isDirectory()) || !newTarget.exists()) {
						System.out.println("created new dir as not existent yet");
					}
	
					System.out.println("file moving?");
					//move all underlying files and folders
					for(File cf : source.listFiles()) {
						System.out.println("moving underlying file "+cf);
						moveTo(cf, newTarget, overwriteExisting);
					}
	
					//delete folder at the end, as the folder was created not moved
					if(source.list().length==0) {
						System.out.println("empty list, continue delete");
						delete(source);
					}else {
						System.err.println("why is it not empty that shitty dir???");
						System.err.println(new ArrayList<File>(Arrays.asList(source)));
					}
					return true;
				}
	
				//TODO: check for if Atomic moving or the other one. Also what to do if file exists already in destination (overwrite or tell the client)
				//file moving
				System.out.println("file moving");
				System.out.println(newTarget);
				System.out.println(newTarget.exists());
				try {
					if(!newTarget.exists() || newTarget.exists() && overwriteExisting) {
						Path ret = Files.move(source.toPath(), newTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
						System.out.println("ret erfolgreich>??? "+ret);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}
	
		}



	/**
	 * Given the nature of URL not containing special characters, like " ","&" etc. these characters are decoded like "%20" for " "(SPACE) for example.
	 * To change them back to their original characters, this method is used.
	 * @param request The encoded String (containing "%20", "%24" etc)
	 * @return the decoded String (containing " ", "&" etc.)
	 */
	private static String decodeEscapeCharacters(String request) {
		//adjust url specific character back to normal
		request = request.replaceAll("%20", " ");
		request = request.replaceAll("%24", "&");
		request = request.replaceAll("%3F", "?");
		request = request.replaceAll("%3D", "=");

		request = request.replace("%22", "\"");
		request = request.replace("%27", "'");
		return request;
	}

	/**
	 * Sends a header with the Information, that an error occurred on this side, 
	 * or that the request could not be fulfilled, and no further information will be sent 
	 * regarding that request by sending an empty body.
	 * @param out the PrintStream (client) to write this message to
	 * @see #sendSucess(PrintStream)
	 */
	private static void sendFail(PrintStream out) {
		sendHeader(out, KWS_CODES.KWS_RESPONSE.codeString+": "+KWS_CODES.FAIL.codeString);
		out.println(); //send empty body
	}

	/**
	 * ={@link #sendSucess(PrintStream, boolean, String...)}
	 * @param out
	 */
	private static void sendSucess(PrintStream out) {sendSucess(out, true);}

	/**
	 * Sends the information to the client, that the requested operation was successfully performed.
	 * If needed, additional parameters, such as an answer can be stated in the header.
	 * If the body of the answer is not empty, for example if you want to prepare the client to await something after this message (header), set the boolean to false
	 * and flush the rest of your stuff afterwards. Otherwise this answer will send an empty body and the client will stop listening and mark this operation as complete.
	 * @param out the PrintStream (client) to write to
	 * @param sendEmptyBody TRUE will mark this answer as complete and the client will stop listening on that port. FALSE will give you the opportunity to manually flush some additional
	 * information down the stream in form of the body before closing.
	 * @param additionalHeaderLines gives you the opportunity to flush some small pieces of information as headers with it. For large chunks of informations or stuff like file bytes, use the body instead.
	 */
	private static void sendSucess(PrintStream out, boolean sendEmptyBody, String ...additionalHeaderLines) {

		additionalHeaderLines = Arrays.copyOf(additionalHeaderLines, additionalHeaderLines.length + 1);
		additionalHeaderLines[additionalHeaderLines.length - 1] = KWS_CODES.KWS_RESPONSE.codeString+": "+KWS_CODES.SUCCESS.codeString; // Assign 40 to the last element
		sendHeader(out, additionalHeaderLines);

		if(sendEmptyBody) {
			System.out.println("sending empty body");
			out.println(); //send empty body
		}
	}

	/**
	 * send a standard beginning header with "HTTP/1.0 200", your header Parameters as stated and an empty line, to mark the end of the header
	 * @param out the {@link PrintStream} of the client to write to
	 * @param headerLines lines you want to include in your header
	 */
	private static void sendHeader(PrintStream out, String ...headerLines) {
		out.println("HTTP/1.0 200 Here's your file");
		for(String chl : headerLines) {
			out.println(chl);
		}
		out.println();//ends the header

	}

	/**
	 * Sends a text down the PrintStream
	 * @param answer the text to flush
	 * @param out the PrintStream to write to
	 */
	private static void sendText(String answer, PrintStream out) {
		if(answer == null) {
			WebServer.errorResponse(404, "File not found", out);
			return;
		}


		//		sendHeader(out, KWS_CODES.KWS_RESPONSE.codeString+": "+KWS_CODES.GET_DIR_CONTENT.codeString);

		//		out.println("HTTP/1.0 200 Here's your file");
		//		out.println(KWS_RESPONSE+": "+KWS_CODES.GET_DIR_CONTENT.selectedModul);
		//		out.println();//ends header

		Iterator<String> it = answer.lines().iterator();
		while(it.hasNext()) {
			out.println(it.next());
		}
	}


	/**
	 * gets the parameters of the command as a HashMap with the parameter names as elements and their values.
	 * the command can be passed raw with the "?" or only the parameters
	 * <br>e.g. "getDirContent?dir=C:/Pather/onePathe" or "dir=C:/Pather/onePathe"
	 * will both return a Hash map with the element "dir" and its value "C:/Pather/onePathe"
	 * <br>Make sure your Command does not have a "?" in its name, as it marks the start of the parameter list.<br>
	 * also make sure your parameters do not contain the "&" element and a "=" element is only acceptable for the parameter value, but not for the parameter name.
	 * @param command the command to get the values from
	 * @return a hash map with the parameters and their values
	 */
	private static HashMap<KWS_CODES, String> getParameters(String command) {
		HashMap<KWS_CODES, String> ret = new HashMap<KWS_CODES, String>();

		//If there is a "?", make sure the "?" comes before any "=", which marks the beginning of the parameters.
		//if the "?" comes after the "=", it means, the "?" is part of a parameter value or elements name
		if(command.contains("?") && command.indexOf("=")>command.indexOf("?")) {
			command=command.substring(command.indexOf("?")+1);
			System.out.println("adjusting parameter string to: "+command);
		}

		String[] ss = command.split("&");
		for(String cs : ss) {
			if(cs.contains("=")) {//just making sure it really contains the "=" attribute (error catching)
				String [] t = cs.split("=");
				if(t.length==1) {//if no expression comes after the "=" (error catching)
					ret.put(KWS_CODES.getKWSCode(decodeEscapeCharacters(t[0])), ""); 
				}else {
					ret.put(KWS_CODES.getKWSCode(decodeEscapeCharacters(t[0])), decodeEscapeCharacters(t[1])); 
				}
			}
		}

		return ret;
	}

	private final static String FOLDER_OPEN = "<folder>";
	private final static String FOLDER_CLOSE= "</folder>";

	private final static String FILE_OPEN= "<file>";
	private final static String FILE_CLOSE= "</file>";




	//----------------------KWS Functions-------------------------

	/**
	 * Just in case someone messed up with the path parameter, sets it to the standard way, by letting it start with the drive letter and not "\" or "/"
	 * @param dir the dir path to adjust
	 * @return the adjusted String
	 */
	private static String adjustDirParameter(String dir) {
		//adjust dir parameter if it start with a "/" or "\" caracter removes it.
		while(dir.startsWith("/") || dir.startsWith("\\")) {
			dir=dir.substring(1);}

		//		if(!dir.endsWith("/")) {
		//			dir+="/";
		//		}

		return dir;
	}


	/**
	 * Adjust the xml string by looking for illegal characters like &,<,>,",' and changes them to the xml appropriate chars &amp, &lt, &gt, &quot, &apos
	 * @param xml
	 * @return
	 */
	private static String adjustXMLString(String xml){
		xml=xml.replace("&", "&amp;");
		xml=xml.replace("<", "&lt;");
		xml=xml.replace(">", "&gt;");
		xml=xml.replace("\"", "&quot;");
		xml=xml.replace("'", "&apos;");

		return xml;
	}

	/**
	 * reads the body of a POST request. As we are using a Buffered Reader, make sure, that when sending a POST body 
	 * for this server to read, attach a "\n\n" at the end of the body!<br>
	 * The first "\n" will mark the end of the line from the last data stream and the second one marks the end of the body, as
	 * we conclude the body is over, if the {@link BufferedReader#readLine()} method returns null or is the length of 0 (which it is by calling "\n\n" for the last row).
	 * @param br The {@link BufferedReader} which was used to read the header of the POST request before
	 * @return the POST body as a String or null if failed to read
	 */
	private static String readPOSTbody(BufferedReader br) {
		StringBuilder buf = new StringBuilder(512);
		String s = "";
		try {
			while ((s = br.readLine()) != null && s.length() != 0) {
				buf.append(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		System.out.println("finished reading POST body");
		return buf.toString();
	}


	/**
	 * Adjustes the file regarding its file/directory name if already existent by adding a "('number')" to it while keeping its extension.
	 * E.g. if "C:/aFile.txt" exists, adjustIfDuplicate(new File("C:/aFile.txt") will return a File with the Absolute Path of "C:/aFile (1).txt".
	 * @param file The file which should be checked
	 * @return the file element with the original path of the file if file does NOT exist, or the adjusted name if already a file with that name at that location exists.
	 */
	private static File adjustFileIfDuplicate(File file) {
		System.out.println("START ADJUST");
		//adjust to attach a number if exists and 
		int nfi = 0; //neccessary to start at 0, as otherwise also those with a "(NUMBER)" Tag will be adjusted, hence for example "test(1).txt" copied will result in "test(2).txt" instead of "test(1)(1).txt"
		while(file.exists()) {
			File oldFile=file;
			String extension = "";
			int pi = file.getAbsolutePath().length();
			
			if(oldFile.isFile()) {
				//get the index where the extension part starts in the file name
				pi = file.getAbsolutePath().lastIndexOf(".");
				extension = file.getAbsolutePath().substring(pi);
			}
			
				//Check if it contains the current number in brackets, else do another round
				System.out.println("TEST:"+file.getName());
				
				//If the same file exists with a "(NUMBER)" like indication do not add a new index, but replace this one with the next higher number ("(1)" --> "(2)")
				if(file.getName().contains("("+nfi+")")) {
					//new name is highly complicated to only replace the last "(NUMBER)" and not every one
					String newName = file.getName().substring(0,file.getName().lastIndexOf(("("+nfi+")"))) //get all of the name except the last number index
							+"("+(nfi+1)+")"+extension; //add new number index and file extension (if its no file the extension string is empty and does not matter
					
					System.out.println("NEW NAME "+newName);
					file = new File(file.getAbsolutePath().replace(file.getName(), newName));
					System.out.println("NF");
				}else {
					file = new File(file.getAbsolutePath().substring(0, pi)+" ("+(nfi+1)+")"+extension); //pi is set to be the whole length at the start and in this case the whole path is used when its a folder
					System.out.println("NF NIIIIIIICHT");
					System.out.println(file.exists());
					System.out.println(nfi);
					System.out.println("X");
				}
				
				System.out.println(oldFile.getName()+" already exists --> new Filename/Folder: "+file.getName());
				
				
				
				
//			}else {
//				TODO SAME WITH FOLDER
//				file = new File(file.getAbsolutePath()+" ("+nfi+")");
//				System.out.println(oldFile.getName()+" already exists --> new Folder: "+file.getName());
//			}
			nfi++;
		}
		System.out.println("END ADJUST with "+file.getName());
		return file;
	}


//private static File adjustFileIfDuplicateWORKS(File file) {
//	System.out.println("START ADJUST");
//	//adjust to attach a number if exists and 
//	int nfi = 0;
//	while(file.exists()) {
//		File oldFile=file;
//		
//		if(file.isFile()) {
//			//get the index where the extension part starts in the file name
//			int pi = file.getAbsolutePath().lastIndexOf(".");
//			String extension = file.getAbsolutePath().substring(pi);
//			
//			//Check if it contains the current number in brackets, else do another round
//			System.out.println("TEST:"+file.getName());
//			
//			//If the same file exists with a "(NUMBER)" like indication do not add a new index, but replace this one with the next higher number ("(1)" --> "(2)")
//			if(file.getName().contains("("+nfi+")")) {
//				//new name is highly complicated to only replace the last "(NUMBER)" and not every one
//				String newName = file.getName().substring(0,file.getName().lastIndexOf(("("+nfi+")"))) //get all of the name except the last number index
//						+"("+(nfi+1)+")"+extension; //add new number index and file extension
//				System.out.println("NEW NAME "+newName);
//				file = new File(file.getAbsolutePath().replace(file.getName(), newName));
//				System.out.println("NF");
//			}else {
//				file = new File(file.getAbsolutePath().substring(0, pi)+" ("+(nfi+1)+")"+extension);
//				System.out.println("NF NIIIIIIICHT");
//				System.out.println(file.exists());
//			}
//			
//			System.out.println(oldFile.getName()+" already exists --> new Filename: "+file.getName());
//		}else {
////			TODO SAME WITH FOLDER
//			file = new File(file.getAbsolutePath()+" ("+nfi+")");
//			System.out.println(oldFile.getName()+" already exists --> new Folder: "+file.getName());
//		}
//		nfi++;
//	}
//	System.out.println("END ADJUST with "+file.getName());
//	return file;
//}
	
	
    /**
     * See {@link #zipFile(ArrayList, File)};
     * @param source
     * @param fileName
     * @throws IOException
     */
    public static void zipFile(File source, File fileName) throws IOException {
    	ArrayList<File> temp = new ArrayList<File>();
    	temp.add(source);
    	zipFile(temp, fileName);
    }
    
    /**
     * Zip the files listed in the ArrayList to the location as stated in the zip file destination parameter.
     * Will zip files as well as all sub directories.
     * @param filesToZip All the files which should be included in the zip file
     * @param zipDestination the location of the zip file which is to be created
     * @throws IOException
     */
	public static void zipFile(ArrayList<File> filesToZip, File zipDestination) throws IOException {
		FileOutputStream fos = new FileOutputStream(zipDestination);
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		        
        for(File cf : filesToZip) {
        	System.out.println("zipping: "+cf);
        zipFileSub(cf, zipDestination.getName(), zipOut);
        }
        
        zipOut.close();
        fos.close();
    }
	
	/**
	 * A sub-method for the zipper. Wrapped seperately, as this piece of code has to call itself if it finds a directory
	 * @param fileToZip The current file, which should be added into the zip archive
	 * @param zipDestination The zip file path which is currently created
	 * @param zipOut the ZipOutputStream, which is currently being worked with
	 * @throws IOException
	 */
	private static void zipFileSub(File fileToZip, String zipDestination, ZipOutputStream zipOut) throws IOException {
      	
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (zipDestination.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(zipDestination));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(zipDestination + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFileSub(childFile, zipDestination + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(zipDestination);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
	}
	


}
