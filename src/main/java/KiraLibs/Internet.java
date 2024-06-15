package KiraLibs;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import systemIndependet.OS;
import systemIndependet.ThreadLinkinValues;

public class Internet {

	//----------------------------------------------------------------------------------------------------

	public Internet() {}


	//These adresses start with the given three nubers. Use them to check what type of IP you currently encounter
	private static final String LOOPBACK_PREFIX = "127.";
	private static final String LOCALE_IP_PREFIX = "192.168.";
	private static final String LINK_LOCALE_IP_PREFIX = "169.";
	
	//Addresses in the range 224.xxx.xxx.xxx through 239.xxx.xxx.xxx are multicast addresses.
	//Rest are IPV6 or IPV4 Adresses
	
	
    private static final int BUFFER_SIZE = 4096;
    
    /**
     * Downloads a file fromClient a URL
     * @param fileURL HTTP URL of the file to be downloaded
     * @param saveDir path of the directory to save the file
     * @throws IOException
     */
    public static void downloadFile(String fileURL, String saveDir)
            throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
 
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();
 
            if (disposition != null) {
                // extracts file name fromClient header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name fromClient URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }
 
            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);
 
            // opens input stream fromClient the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;
             
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
 
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            outputStream.close();
            inputStream.close();
 
            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }
    
	/**
	 * send a parameter to an URL via POST Script
	 * @param url the URL to you file ("http://something-somewhere.net/aFolder/anExample.php"
	 * @param postParameters the POST Parameter ("myAge=34&myName=Jack" --> sends the POST Parameter myName and myAge)
	 * @return the Response fromClient the Website 
	 * @throws IOException 
	 * @throws Exception
	 */
	public static String sendPOST(String url, String postParameters) throws IOException  {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		// Send post request
		con.setDoOutput(true);
		
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(postParameters);
		wr.flush();
		wr.close();

//		int responseCode = con.getResponseCode();
//		System.out.println("\nSending 'POST' request to URL : " + url);
//		System.out.println("Post parameters : " + postParameters);
//		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		//print result
//		System.out.println(response.toString());
		return response.toString();
	}
	
	
	
	

	
	  public static String getIPV4() 
	  {
		  String IP ="";

		  try{
			  URL whatismyip = new URL("http://checkip.amazonaws.com");
			  BufferedReader in = new BufferedReader(new InputStreamReader(
					  whatismyip.openStream()));

			  IP = in.readLine(); //you get the IP as a String
			  in.close();

			  //falls die Seite down ist oder was wei� ich was, versuche eine andere Seite
			  if(IP.contains("\n") || !IP.contains("."))
			  {

				  whatismyip = new URL("http://checkip.dyndns.org:8245/");
				  BufferedReader br = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

				  IP = br.readLine(); //IP as a String
				  br.close();

				  IP = IP.replace("<html><head><title>Current IP Check</title></head><body>Current IP Address: ", "");
				  IP = IP.replace("</body></html>", "");

			  }
			  return IP;

		  } catch (IOException e) {
			  e.printStackTrace();
			  return null;
		  }
	  }
	  

	    /**
	     * Returns MAC address of the given interface name.
	     * @param interfaceName eth0, wlan0 or NULL=use first interface 
	     * @return  mac address or empty string
	     */
	  public static ArrayList<String> getMACAddresses() {
		  ArrayList<String> macs = new ArrayList<String>();
		  try {
			  List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			  for (NetworkInterface intf : interfaces) {
				  byte[] mac = intf.getHardwareAddress();
				  if (mac!=null)
				  {
					  StringBuilder buf = new StringBuilder();
					  for (int idx=0; idx<mac.length; idx++)
					  {
						  buf.append(String.format("%02X:", mac[idx]));
					  }
					  if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
					  {
//						  System.out.println(intf.getInetAddresses().toString());
						  if(intf.getInetAddresses().nextElement().toString().startsWith("/192.168.0."))
						  {
//							  System.out.println("Mac hacken"+intf.getInetAddresses().nextElement());
//							  System.out.println(buf.toString());
							  macs.add(buf.toString());
						  }
					  }
				  }
			  }
		  } catch (Exception ex) { 
		  } // for now eat exceptions
		  System.out.println("Return mac exception ");
		  return macs;
	  }
	    
	  /**
	   * Sucht nach den lokalen IPs auf dem PC, indem alle Adressen aller netzwerk Interfaces abgefragt werden und getestet
	   * wird, ob diese eine lokale adresse ist. Am Ende sollte pro verbundenes Netzwerk interface (Netzwerk Adapter/Karte etc) eine IP bereit stehen.
	   * @return lokalen IPs des Computers or null if nothing found (because you aren't connected for example)
	   */
	  public static HashSet <String> getLocaleIPs()
	  {
		  HashSet <String> ret = new HashSet <String>();

		  try {
		  for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
		  {
			  NetworkInterface intf = en.nextElement();

			  for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
			  {
				  InetAddress inetAddress = enumIpAddr.nextElement();

//				  					  System.out.println(inetAddress.getHostAddress());
//				  					  System.out.println(inetAddress.isSiteLocalAddress());

				  if(!ret.contains(inetAddress.getHostAddress()) && inetAddress.isSiteLocalAddress()) {
//					  System.out.println("add");
					  ret.add(inetAddress.getHostAddress());
				  }
			  }
		  } 

		  if(!ret.isEmpty()) {
			  return ret;
		  }
		  
	  }catch (SocketException e){
		e.printStackTrace();
	}

		  try {
			  ret.add(Inet4Address.getLocalHost().getHostAddress());
			  //			  ret.add(InetAddress.getLocalHost().getHostAddress());
			  return ret;
		  } catch (UnknownHostException e) {}



		  return null;
	  }
	  
	  /**
	   * Returns your locale IP Adress as stated via {@link InetAddress#getLocalHost()} or null if not found or you are not connected to the internet.
	   * Might have issues when you are connected to several network interfaces.
	   * In case of doubt try to use {@link #getLocaleIPs()}, as maybe your locale IP is listed there.
	   * @return local IP of this PC or null if nothing found (because you arent connected for example)
	   */
	  public static String getLocaleIP()
	  {
		  try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		  return null;
		  
//		  
//		  //Search for locale IPs that start with 192.168.*
//		  HashSet <String> IPs = getLocaleIPs();
//		  
//		  for(String cIP : IPs) {
//					  if (cIP.startsWith(LOCALE_IP_PREFIX)) {return cIP;}
//		  }
//		  
//		  System.out.println(IPs);
//		  
//		  //Try some other methods, maybe its hidden here
//		  String localhost = null;
//		  String inet4localhost = null;
//		  
//		  try {
//			  localhost= InetAddress.getLocalHost().getHostAddress();	
//			  System.out.println(localhost);	
//			  if (localhost.startsWith(LOCALE_IP_PREFIX)) {return localhost;}
//		  } catch (UnknownHostException e) {}
//		  
//		  
//		  try {
//			  inet4localhost=Inet4Address.getLocalHost().getHostAddress();
//			  System.out.println(Inet4Address.getLocalHost().getHostAddress());
//			  if (inet4localhost.startsWith(LOCALE_IP_PREFIX)) {return inet4localhost;}
//		  } catch (UnknownHostException e) {}
//		  
//		  return null;
	  }
	  

		public static void main(String argv[]) throws Exception {
			  System.out.println("You are on OS: "+OS.currentOS());
			  System.out.println("your IPV6 adress is: "+getIPV6());
			  System.out.println("your locale IP is: "+getLocaleIP());
			  System.out.println("Your MAC addres is: "+getMACAddresses());
		}
	  /**
	   * Sucht nach einer IP mit der Adresse 192.168.0.* und gibt den zugehoerigen IPV6 Wert zurueck.
	   * @return die IPV6 Adresse des Computers oder null
	   */
	  public static String getIPV6 () //Code unterscheided sich von getLocaleIP nur in der 1 bei intf.getInterfaceAdresses().get(1/0)
	  {
		  try{
			  for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
			  {
				  NetworkInterface intf = en.nextElement();
				  for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
				  {
					  InetAddress inetAddress = enumIpAddr.nextElement();
					  if (!inetAddress.isLoopbackAddress()) 
					  {
						  if(intf.getInterfaceAddresses().size()>1)
						  {
							  String IPV6 = intf.getInterfaceAddresses().get(1).toString();
							  if(IPV6.contains("/64 [null]"))
							  {
								  return IPV6.substring(1, IPV6.indexOf("/64 [null]"));
							  }
						  }
					  }
				  }
			  }
		  } catch (SocketException e) {
			  e.printStackTrace();
		  }
		  return null;
	  }
	  
	  /**
	   * return only the IP of the socket, without any / or stuff like that, so for example '192.168.0.2'
	   * @param sock the socket to obtain the IP fromClient
	   * @return the IP of the socket
	   */
	  
	  public static String getSocketIP(Socket sock)
	  {
		  return sock.getInetAddress().getHostAddress();
	  }
	  
	  /**
	   * 
	   * @param sock The socket to analyze
	   * @return the port, at which the socket is bound to
	   */
	  public static int getSocketPort(Socket sock){
		  return sock.getLocalPort();
	  }

	/**
	 * @return the first locale network body (most cases '192.168.0.') or null if no one is registered or can be found.<br>
	 * Remember, if more than one network adapters are present, you might want to check the IPs of all the adapters using {@link #getLocaleIPs()}
	 */
	  public static String getLocaleNetworkBody(){
		  HashSet<String> tt = Internet.getLocaleIPs();
		  
		  if(tt != null){
			  String localeIP = tt.iterator().next();
			  String localeNetworkBody = localeIP.substring(0, localeIP.lastIndexOf(".")+1);	//= bei mir 192.168.0.
			  return localeNetworkBody;
		  }
		  return null;		
	  }

	  /**
	   * Tells you the name of the remote PC
	   * @param sock
	   * @return the host name of the remote PC
	   */
	public static String getSocketHostName(Socket sock) {
		return sock.getInetAddress().getHostName();
	}
	
	
	
	/**
	 * Tells you the name of your PC. As far as I read, this is not a perfect solution, but works until now.
	 * @return the host name of your PC
	 * @throws UnknownHostException 
	 */
	public static String getLocalHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}

	/**
	 * Tries to establish a connection to the stated adress and port. Unlike the regular creation of a {@link Socket},
	 * this method allowes you to specify a timeout where when passed, the connection will be seen as failed and this method will return null.
	 * The advantage of the timeout is, that the regular {@link Socket} invoking blocks when it does not find anything sometimes.
	 * @param iP
	 * @param port
	 * @param timeout
	 * @return the socket if sucessfully connected or null
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static Socket bindSocket(String IP, int port, int timeout) throws IOException {
		
		
		final ThreadLinkinValues ret = new ThreadLinkinValues();
		
		Thread observer = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(timeout);
				} catch (InterruptedException e) {}
				
			}
		});
		
		Thread creator = new Thread(new Runnable() {
			
			@Override
			public void run() {
					try {
						Socket sok = new Socket(IP, port);
						System.out.println("Binding of Socket sucessful");
						ret.setObject(sok);
						observer.interrupt();
					} catch (IOException e) {
						ret.setObject(e);
						observer.interrupt();
					}
			}
		});
		
		
		
		creator.start();
		observer.start();
		
		try {
			observer.join();
		} catch (InterruptedException e) {
		}
		
		if(ret.getObject() instanceof IOException) {
			throw (IOException) ret.getObject();
		}
		if(ret.getObject() == null) {
			throw new UnknownHostException("Timeout exceeded. No connection to host possible.");
		}
		
		return (Socket) ret.getObject();
		
	}
	
	
	
	
	  
	  
}
