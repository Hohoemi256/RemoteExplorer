package plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class Loader implements PluginManager{

	private static List<PluginInterface> loadedplugins = new ArrayList<PluginInterface>();
	private static final String PLUGIN_DIR = "plugins";
	
	/**
	 * loads all plugins
	 */
	public void start(){
		  File[] files = new File(PLUGIN_DIR).listFiles();
		  for(File f : files)
			try {
				loadedplugins = loadPlugin(f);
			} catch (Exception e) {
				e.printStackTrace();
			}
		  for(PluginInterface pi : loadedplugins)
		    pi.start();
		}


	private void loadPluginOLD(File f) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {

//		//Erzeugen des JAR-Objekts
//		JarFile file = new JarFile(f);
//		//Laden der MANIFEST.MF
//		Manifest manifest = file.getManifest();
//		// auslesen der Attribute aus der Manifest
//		Attributes attrib = manifest.getMainAttributes();
//		// holen der Mainclass aus den Attributen
//		String main = attrib.getValue(Attributes.Name.MAIN_CLASS);
//		
//		
//		
//		// laden der Klasse mit dem File als URL und der Mainclass
//		Class cl = new URLClassLoader(new URL[]{f.toURI().toURL()}).loadClass(main);
//		// holen der Interfaces die die Klasse impementiert
//		Class[] interfaces = cl.getInterfaces();
//		// Durchlaufen durch die Interfaces der Klasse und nachsehn ob es das passende Plugin implementiert
//		boolean isplugin = false;
//		for(int y = 0; y < interfaces.length && !isplugin; y++)
//		  if(interfaces[y].getName().equals("net.byte_welt.wiki.PluginInterface"))
//		    isplugin = true;
//		if(isplugin){
//			
////		  PluginInterface plugin = (PluginInterface) cl.newInstance();
//			PluginInterface plugin = (PluginInterface) cl.getDeclaredConstructor().newInstance() ;
//			cl.cast(PluginInterface.class);
//		  loadedplugins.add(plugin);
//		}
	}
	
	/**
	 * loads all classes and creates a new object of which inherit the interface {@link PluginInterface}
	 * @param f
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private static List<PluginInterface> loadPlugin(File f) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		List<PluginInterface> ret = new ArrayList<PluginInterface>();

		URLClassLoader loader = new URLClassLoader(new URL[]{f.toURI().toURL()});
		List<PluginInterface> classes = new ArrayList<PluginInterface>();
		JarInputStream jaris = new JarInputStream(new FileInputStream(f));
		JarEntry ent = null;
		while ((ent = jaris.getNextJarEntry()) != null) {
			if (ent.getName().toLowerCase().endsWith(".class")) {
				try {
					Class<?> cls = loader.loadClass(ent.getName().substring(0, ent.getName().length() - 6).replace('/', '.'));
					if (isPluginClass(cls)) {
						ret .add((PluginInterface) cls.getDeclaredConstructor().newInstance());
//	        	        loadedplugins.add((Class<PluginInterface>)cls);
					}
				}
				catch (ClassNotFoundException e) {
					System.err.println("Can't load Class " + ent.getName());
					e.printStackTrace();
				}
			}
		}
		jaris.close();
		loader.close();
		return classes;
	}

	
	private static boolean isPluginClass(Class<?> cls) {
		  for (Class<?> i : cls.getInterfaces()) {
		    if (i.equals(PluginInterface.class)) {
		      return true;
		    }
		  }
		  return false;
		}
	
	

	  
}
