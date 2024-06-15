package plugin;

import java.util.ArrayList;

/**
 * All Methods which the main program is authorized to execute in the plugin
 * @author Yari9
 *
 */
public interface PluginInterface {

	/**
	 * gets called upon loading the plugin
	 * @return
	 */
	  public boolean start();
	  
	  /**
	   * gets called upon closing/shutting down the plugin
	   * @return
	   */
	  public boolean stop();
	  /**
	   * allows the plugin to have limited control over the main Program, by assigning a {@link PluginManager}, which handles these requests
	   * @param manager
	   */
	  public void setPluginManager(PluginManager manager);
	  /**
	   * output a message as Kira (hence in the speech bubble or even via voice)
	   */
	  public void showMessage();
	  /**
	   * Tell the main programm, where the menu button(s) should be located on the popup menu.<br>
	   * Use {@link SwingMenuCreator#
	   * @return
	   */
	  public ArrayList<String> getMenuItems();
	  
}
