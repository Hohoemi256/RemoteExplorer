package plugin;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class SwingMenuCreator {

	public final static String PLUGIN_MENU = "Plugins";
	
	/**
	 * Creates a List, which can be interpreted by the {@link #createMenuItem(ArrayList)} method to create a button on the regarding GUI.
	 * The Parameters can be used freely, whereas the last entry is the buttons name, and the ones before are the Menu components.<br>
	 * E.g.: createMenuDirection(Server, Test, myPlugin) will create a menu item "Server" on the popup menu in the GUI, which has another Menu named Test in it, which again has Menu Item called myPlugin.
	 * <br>Remember, all buttons defined here will be placed in the first menu entry called {@value #PLUGIN_MENU}. If you dont want that, consider using {@link #createMenuDirection(boolean, String...)}.
	 * @param MenuPath The Menu hierarchy, whereas the last entry is the actual button.
	 * @return an ArrayList, which can be interpreted on the main Programm to construct a button where needed
	 */
	public static ArrayList<String> createMenuDirection(String... MenuPath) {
		return createMenuDirection(true, MenuPath);
	}
	
	/**
	 * Creates a List, which can be interpreted by the {@link #createMenuItem(ArrayList)} method to create a button on the regarding GUI.
	 * The Parameters can be used freely, whereas the last entry is the buttons name, and the ones before are the Menu components.<br>
	 * E.g.: createMenuDirection(Server, Test, myPlugin) will create a menu item "Server" on the popup menu in the GUI, which has another Menu named Test in it, which again has Menu Item called myPlugin.
	 * @param MenuPath The Menu hierarchy, whereas the last entry is the actual button.
	 * @param intoPluginMenu whether the whole path should be placed into a common Menu called {@value #PLUGIN_MENU}. Default is true.
	 * @return an ArrayList, which can be interpreted on the main Programm to construct a button where needed
	 */
	public static ArrayList<String> createMenuDirection(boolean intoPluginMenu, String... MenuPath) {
		ArrayList<String> ret = new ArrayList<String>();
		if(intoPluginMenu) {ret.add(PLUGIN_MENU);}
		for(String cs : MenuPath) {
		ret.add(cs);	
		}
		return ret;
	}
	
	
	//Swing-----------------------
	private ArrayList<Component> pluginMenu = new ArrayList<Component>();
	
	
	private void addPluginButton(ArrayList<String> as) {pluginMenu.add(createMenuItem(as));}
	public ArrayList<Component> getPluginButtons() {return pluginMenu;}
	
	/**
	 * creates The Menu Item to add to your JPopupMenu. It contains already all defined sub menus.
	 * @param as the buttons hierarchy, as defined by {@link #createMenuDirection(boolean, String...)}
	 * @return
	 */
	public Component createMenuItem(ArrayList<String> as) {
		JMenu ret = null;
		JMenuItem mi = null;
		
		for(int i=as.size(); i>0;i--) {
			if(i==as.size()) {
				mi = new JMenuItem(as.get(i));
				}
			else if(i==as.size()-1) {
				ret = new JMenu(as.get(i));
				ret.add(mi);
				}
			else {
				JMenu nRet = new JMenu(as.get(i));
				nRet.add(ret);
				ret=nRet;
			}
				
		}
		if(ret==null) {return mi;}
	return ret;
	}
}
