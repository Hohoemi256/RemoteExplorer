package main;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import image.ImageUtils;

//import image.ImageUtils;

public abstract class Tray implements MouseListener, ActionListener{

	//TODO make this an extended form of the KiraTray, to have one as a basic Tray class and extend it as needed
	
	private PopupMenu popup = new PopupMenu();

	private Image Icon;
	private TrayIcon trayIcon;
	private final SystemTray tray = SystemTray.getSystemTray();

	// Create a pop-up menu components
	public MenuItem exitItem = new MenuItem("Exit");

	public final static String EXIT_ACTION = "exit";


	public Tray(File icon) {

		try {
			//Check the SystemTray is supported
			if (!SystemTray.isSupported()) {
				System.out.println("SystemTray is not supported");
				return;
			}

			Icon = ImageUtils.setWhiteTransperant(ImageUtils.resize(ImageIO.read(icon), 16,16));
			trayIcon = new TrayIcon(Icon, "KWS", popup);

			//Commands----------------------------------------------------------------------------
			exitItem.setActionCommand(EXIT_ACTION);
			
			//Tray
			exitItem.addActionListener(this);	
			trayIcon.addMouseListener(this);

			//Add components to pop-up menu-------------------------------------------------------

			popup.add(exitItem);

			trayIcon.setPopupMenu(popup);

			tray.add(trayIcon);

		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Add elements to the Tray Icon Menu by adding it to the popup menu. 
	 * The menu will contain the exit element from the start
	 * @return the popupMenu of the tray Icon
	 */
	public PopupMenu getPopupMenu() {return popup;}

	/**
	 * Gets called when a left click was performed on the Tray Icon, most of the time indicating, that the window should be restored
	 */
	public abstract void restore();
	/**
	 * a press on the exit button in the popupmenu, requesting the termination of the program
	 */
	public abstract void exitProgram();
	
	@Override
	public void mouseClicked(MouseEvent e) {
		System.out.println("click");
		//To Front if minimized
		if(e.getSource().toString().startsWith("java.awt.TrayIco")
				&& SwingUtilities.isLeftMouseButton(e)){
			//GUI to front
			restore();
//			popMenu.setVisible(false);
			
		}			
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(EXIT_ACTION)){
			exitProgram();
}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
}



