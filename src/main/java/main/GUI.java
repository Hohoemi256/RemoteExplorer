package main;

import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import kiraNeccesaryLibs.Internet;
import kiraNeccesaryLibs.LogWriter;

public class GUI extends JFrame implements ActionListener, ItemListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private JButton startStopServer = new JButton("start server");
	private JLabel stateLabel = new JLabel("Server offline");
	private JLabel ipLabel = new JLabel("IP: XXX.XXX.X.X:PORT");
	public JTextArea logTxt = new JTextArea();
	
	private JButton portButton = new JButton("change port");
	private JTextField portChangeTxt = new JTextField(3);
			
	private JCheckBoxMenuItem showLog = new JCheckBoxMenuItem("show server log");
	public CheckboxMenuItem serverItem = new CheckboxMenuItem("Server Running");
	
	private JPanel mainPanel = new JPanel();
	private JPanel portChangePanel = new JPanel() {
		
//		//Overwrite this method to let the panels be shown at their preffered size. Otherwise the BoyLayout will mess it up and insert stupid spaces between the panels
//		@Override
//		public Dimension getMaximumSize()
//		{
//		    Dimension preferred = getPreferredSize();
//		    Dimension maximum = new Dimension(Integer.MAX_VALUE, (int)preferred.getHeight());
//		 
//		    return maximum;
//		}
	};
	private JPanel buttonPanel = new JPanel();
	private JScrollPane scrollPane = new JScrollPane(logTxt);

	private final static String START_STOP_SERVER = "startStopServer";
	private final static String SERVER_TOGGLE = "toggleServer";
	private final static String CHANGE_PORT = "changePort";
	

	private WebServer serv;
	
	private JFrame self=this;
	
	Tray tr = new Tray(new File("TrayIcon.png")) {
		
		@Override
		public void restore() {
			System.out.println("RESTORE");
			self.setVisible(true);
			self.setExtendedState(Frame.NORMAL);
			self.toFront();			
		}

		@Override
		public void exitProgram() {
			System.exit(0);
		}

	};

	
	
	public GUI(WebServer server) {
		serv = server;
		updateIPlabel();

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBounds(100, 100, 300, 300);
		captureLog(logTxt);

//		tr.exitItem.addActionListener(this);

//		serverItem.addActionListener(this);
		serverItem.setState(false);
		serverItem.setActionCommand(SERVER_TOGGLE);
		serverItem.addItemListener(this);
		tr.getPopupMenu().add(serverItem);
		tr.getPopupMenu().addSeparator();
		
		portButton.setActionCommand(CHANGE_PORT);
		portButton.addActionListener(this);

		showLog.addActionListener(this);
		showLog.setSelected(false);

		startStopServer.setActionCommand(START_STOP_SERVER);
		startStopServer.addActionListener(this);

		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		mainPanel.add(stateLabel);
		mainPanel.add(ipLabel);
		
		portChangePanel.setLayout(new FlowLayout());
		portChangePanel.add(portChangeTxt);
		portChangePanel.add(portButton);
		portChangeTxt.setText(Vars.getPort()+"");
		mainPanel.add(portChangePanel);
		
		
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(startStopServer);
		buttonPanel.add(showLog);
		mainPanel.add(buttonPanel);

		/*
		 * The BorderLayout on the content pane acts as a wrapper to get the boxLayout 
		/* of the main panel to respect the preferred sizes of their components.
		 * Otherwise there will be a lot of stupid space between the portChange and buttonPanel
		 */
		getContentPane().setLayout(new BorderLayout() );
		getContentPane().add(mainPanel, BorderLayout.PAGE_START);
		
//		getContentPane().add(mainPanel);
		//		this.pack();
		this.setVisible(true);
	}

	private void updateIPlabel() {
		HashSet<String> ips = Internet.getLocaleIPs();
		if(ips == null) {
			ipLabel.setText("<html><font color='red'>No network connection</font></html>");
		}
		
		ipLabel.setText("<html>");

		for(String cip : ips) {
			ipLabel.setText(ipLabel.getText()
					+"IP: <font color='green'>"+cip+"</font>:<font color='red'>"+Vars.getPort()+"</font>"
					+"<br>");
		}
		
		ipLabel.setText(ipLabel.getText()+"</html>");
	}
	
	
	private void captureLog(JTextArea log2) {
		final PrintStream old = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		//Necessary to adjust the thread stack from where the sysout etc was called, as otherwise it will always show the GUI as the calling Thread
		LogWriter.increaseThreadStack();
		
		PrintStream ps = new PrintStream(baos) {

			@Override
			public void println() {
				logTxt.append("\n");
				old.println();
				super.println();
			}

			@Override
			public void println(String x) {
				logTxt.append(x+"\n");
				old.println(x);
				super.println(x);
			}

			@Override
			public void println(boolean x) {
				logTxt.append(x+"\n");
				old.println(x);
				super.println(x);
			}

			@Override
			public void println(Object x) {
				logTxt.append(x+"\n");
				old.println(x);
				super.println(x);
			}

			@Override
			public void println(int x) {
				logTxt.append(x+"\n");
				old.println(x);
				super.println(x);
			}

			@Override
			public void println(char x) {
				logTxt.append(x+"\n");
				old.println(x);
				super.println(x);
			}

			@Override
			public void println(double x) {
				logTxt.append(x+"\n");
				old.println(x);
				super.println(x);
			}

			@Override
			public void println(float x) {
				logTxt.append(x+"\n");
				old.println(x);
				super.println(x);
			}

			@Override
			public void println(long x) {
				logTxt.append(x+"\n");
				old.println(x);
				super.println(x);
			}
		};
		System.setOut(ps);
	}




	public void actionPerformed(ActionEvent e) {
		System.out.println("Action Performed");

		if(showLog.isSelected() && !mainPanel.isAncestorOf(scrollPane)) {
			mainPanel.add(scrollPane);
		}else if(!showLog.isSelected() && mainPanel.isAncestorOf(scrollPane)) {
			mainPanel.remove(scrollPane);
		}

		String sw = e.getActionCommand();
		switch (sw) {
		
		case START_STOP_SERVER: case SERVER_TOGGLE:
			if(serv.isRunning()) {
				serv.stopServer();
				startStopServer.setText("start server");
				stateLabel.setText("Server offline");
			}else {
				updateIPlabel();
				startStopServer.setText("stop server");
				stateLabel.setText("Server running");
				serv.startServer();}
			
			serverItem.setState(serv.isRunning());
			break;
			
		case CHANGE_PORT:
			try {
				int x = Integer.parseInt(portChangeTxt.getText());
				Vars.setPort(x);
				if(serv.isRunning()) {
				serv.stopServer();
				serv.startServer();
				}
				updateIPlabel();
			} catch (NumberFormatException e2) {
				System.err.println("No valid port entered. Are you sure this is a number?");
			}
			break;

		}

		this.revalidate();
		//		this.pack();
		buttonPanel.repaint();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		actionPerformed(new ActionEvent(e.getSource(), e.getID(), SERVER_TOGGLE));
	}



}
