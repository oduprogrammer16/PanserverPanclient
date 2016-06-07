import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class panserver extends JApplet
{
	public java.util.List<PanelistHandler> panelistsList = new ArrayList<PanelistHandler>();
	public java.util.List<AudienceHandler> audienceList = new ArrayList<AudienceHandler>();
	public boolean audienceEnabled = false; 

	private PanelistListener panelistListener;
	private AudienceListener audienceListener;

	public ListUpdater listUpdater;
	private Thread listUpdaterThread; // Thread to update all the connected clients. 
	
	int portNum; // Port number for the server. 
	Random rand = new Random();

	int min = 1024; // Minimum server connection. 
	int max = 65535; // Maximum server connection. 
	
	// Timer used for checking on connections
	int interval = 2000;
	ActionListener update = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			listChanged();
		}
	};
	javax.swing.Timer updateTimer = new javax.swing.Timer(interval, update);

	// Buttons to control the server. 
	JButton btnStart = new JButton("Start Server"); // Start server button. 
	JButton btnCloseConn = new JButton("Close Server"); // Close server button. 
	JButton btnClear = new JButton("Clear Outputs"); // Button to clear outputs.
	JButton btnAllowAud = new JButton("Allow Audience to Speak"); // Button to allow audience to speak. 
	
	// Textfield to display server connection information 
	JTextField txtPort = new JTextField(Integer.toString(rand.nextInt((max-min)+1)+min), 8);
	
	
	JLabel lblPort = new JLabel("Port #"); // Label for port information.
	
	// Text areas 
	JTextArea txtpanelist = new JTextArea(8, 45); // Text Area for panelists connection information.
	JTextArea txtAud = new JTextArea(8, 45); // Text area to display audience connection information.
	JTextArea txtList = new JTextArea(8, 45); // Text area for status information 
	

	// JPanels 
	JPanel jpPort = new JPanel(); // Panel to contain port information.
	JPanel jppanelist = new JPanel(); // Panel to contain panelist information. 
	JPanel jpAud = new JPanel(); // Panel to contain audience information. 
	JPanel jpList = new JPanel(); // Panel to contain a list. 
	JPanel jpButtons = new JPanel(); // Panel to contain buttons. 
	JPanel jpAll = new JPanel(); // Panel to contain everything in the gui. 


	Container cp = getContentPane(); // Content pane 
		
	public void init()
	{
		panelistListener = new PanelistListener();
		
		audienceListener = new AudienceListener();
		listUpdater = new ListUpdater();
		listUpdaterThread = new Thread(listUpdater);
		
		// Start button listener(to start listening for clients. )
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				txtpanelistOutput("Starting panelist Handler");
				txtAudOutput("Starting Audience Handler");
				
				panelistListener.start();
				audienceListener.start(); 
				listUpdaterThread.start();
				updateTimer.start();
				
				btnStart.setEnabled(false);
				btnCloseConn.setEnabled(true);
				txtPort.setBackground(new Color(200, 200, 200));
				txtPort.setEditable(false);
			}
		});

		// Close Server button listener(to terminate the server).
		btnCloseConn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				int answer = JOptionPane.showConfirmDialog(
							cp, "Are you sure you want to end?", "Are you sure?", 
							JOptionPane.YES_NO_OPTION);
							
				if (answer == JOptionPane.YES_OPTION) {			
					closeConnections();
					try {
						panelistListener.join();
						//audHandler.join();
						audienceListener.join();
						updateTimer.stop();
					}
					catch (InterruptedException er) {
						System.out.println("Main thread join interrupted: "+er);
					}
					System.exit(0);
				}
			}
		});

		// Action listener to allow the audience to speak. 
		btnAllowAud.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int answer = JOptionPane.showConfirmDialog(
							cp, "Are you sure that you want to allow the audience to speak?", "Are you sure?", 
							JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION) {	
					allowAudienceMembersToSpeak(); 
					
					btnAllowAud.setEnabled(false);
				}
				
			}
		});

		// Clear button listener 
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				txtpanelist.setText("");
				txtAud.setText("");
			}
		});
		
		// Settings for text fields and buttons
		txtpanelist.setBackground(new Color(255, 167, 187));
		txtpanelist.setFont(new Font("Serif", Font.PLAIN, 14));
		txtpanelist.setLineWrap(true);
		txtpanelist.setWrapStyleWord(true);
		txtpanelist.setEditable(false);
		
		// Settings for audience text area
		txtAud.setBackground(new Color(142, 253, 216));
		txtAud.setFont(new Font("Serif", Font.PLAIN, 14));
		txtAud.setLineWrap(true);
		txtAud.setWrapStyleWord(true);
		txtAud.setEditable(false);
		
		// Settings for member list. 
		txtList.setBackground(new Color(243, 252, 117));
		txtList.setFont(new Font("Serif", Font.PLAIN, 14));
		txtList.setLineWrap(true);
		txtList.setWrapStyleWord(true);
		txtList.setEditable(false);
		
		txtPort.setBackground(new Color(255, 255, 200));
		txtPort.setEditable(true);
		
		btnCloseConn.setEnabled(false);	
		
		// Used JPanels to organize objects and keep them together during resize
		jpPort.add(lblPort);
		jpPort.add(txtPort);

		// Set a boarder for the panelist output. 
		jppanelist.setBorder(new TitledBorder("Panelist Output"));
		jppanelist.add(new JScrollPane(txtpanelist));

		// Set a boarder and title for the audience output. 
		jpAud.setBorder(new TitledBorder("Audience Output"));
		jpAud.add(new JScrollPane(txtAud));

		// Set a boarder for the member list. 
		jpList.setBorder(new TitledBorder("Member List"));
		jpList.add(new JScrollPane(txtList));

		// Add the buttons 
		jpButtons.add(btnStart);
		jpButtons.add(btnClear);
		jpButtons.add(btnAllowAud);
		jpButtons.add(btnCloseConn);

		// Add all the gui elements to the gui.
		jpAll.setLayout(new BoxLayout(jpAll, BoxLayout.PAGE_AXIS));
		jpAll.add(jpPort);
		jpAll.add(jppanelist);
		jpAll.add(jpAud);
		jpAll.add(jpList);
		jpAll.add(jpButtons);
		
		cp.setLayout(new FlowLayout());
		cp.add(jpAll);
	}
	

	// Checks people in chat and updates the list.
	class ListUpdater implements Runnable
	{
		boolean listChanged = false;
		boolean socketGood = false;
		String msg;
		Stack<PanelistHandler> actorRem = new Stack<PanelistHandler>();
		Stack<AudienceHandler> audRem = new Stack<AudienceHandler>(); 
		public void run()
		{
			while (true) {
				synchronized (this) {
					try {
						// Wait here until notify()
						wait();
					}
					catch (InterruptedException e) {
						System.out.println("ListUpdater waiting interrupted");
					}
					txtListClear();
					
					// Print list of actors
					txtListOutput("panelists: ");
					for (int i = 0; i < panelistsList.size(); i++) {
						// If actor is done (connection closed), add to list to be removed
						if (!panelistsList.get(i).done) {
							txtListOutput(
									panelistsList.get(i).sock.getInetAddress().toString()+", "+
									Integer.toString(panelistsList.get(i).sock.getPort())+", "+
									panelistsList.get(i).name);
						}
						else
							actorRem.push(panelistsList.get(i));
					}
					// Remove actors from list that are done (connections closed)
					while (!actorRem.empty()) {
						panelistsList.remove(actorRem.pop());
					}
					
					////////////////////////////////////////////
					// Print list of Audience members
					txtListOutput("Audience: ");
					for (int i = 0; i < audienceList.size(); i++) {
						// If actor is done (connection closed), add to list to be removed
						if (!audienceList.get(i).done) {
							txtListOutput(
									audienceList.get(i).sock.getInetAddress().toString()+", "+
									Integer.toString(audienceList.get(i).sock.getPort())+", "+
									audienceList.get(i).name);
						}
						else
							audRem.push(audienceList.get(i));
					}
					// Remove audience from list that are done (connections closed)
					while (!audRem.empty()) {
						audienceList.remove(audRem.pop());
					}
					
				}
			}
		}
	}
	
	// Leave this alone 
	// Wake up the list updater from wait()
	public void listChanged()
	{
		synchronized (this.listUpdater) {
			listUpdater.notify();
		}
	}
	
	/**
	 * Sends a message to all the audience members telling them that they can speak. 
	 */
	public void allowAudienceMembersToSpeak(){
		String msg = "Panserver: Audience may now speak!"; 
		audienceEnabled = true; 
		for(int i = 0; i < audienceList.size(); i++){
			audienceList.get(i).out.println(msg); 
		}
		for(int i = 0; i < panelistsList.size(); i++){
			panelistsList.get(i).out.println(msg); 
		}
	}

	/**
	 * Terminates sends a message to all of the connections informing them that the discussion is over.
	 */
	public void closeConnections()
	{
		String msg = "Good bye everyone. Panel is Over.";
		for (int i = 0; i < panelistsList.size(); i++)
			panelistsList.get(i).out.println(msg);
		for (int i = 0; i < audienceList.size(); i++)
			audienceList.get(i).out.println(msg);
	
		for (int i = 0; i < panelistsList.size(); i++) {
			try {
				panelistsList.get(i).setDone(true);
				panelistsList.get(i).sock.close();
			}
			catch (IOException e) {
				System.out.println("panelist socket close error: "+e);
			}
		}
		
		for(int i = 0; i < audienceList.size(); i++){
			try{
				audienceList.get(i).setDone(true); 
				audienceList.get(i).sock.close(); 
			}
			catch (IOException e) {
				System.out.println("audience socket close error: "+e);
			}
		}

		// Close panelist socket.
		try{
			panelistListener.serverSock.close();
		}
		catch (IOException e) {
			System.out.println("Panelist server socket close error: "+e);
		}
		
		// Close audience socket.
		try{
			audienceListener.serverSock.close(); 
		}
		catch (IOException e) {
			System.out.println("Audience server socket close error: "+e);
		}
		
		
	}
	
	/**
	 * Clears the list of members.
	 */
	public void txtListClear()
	{
		txtList.setText("");
	}

	/**
	 * Sets the adds output to the members list. 
	 * @param str A string to add to the member list text area.
	 */
	public void txtListOutput(String str)
	{
		txtList.setText(txtList.getText() + str + "\n");
		txtList.setCaretPosition(txtList.getDocument().getLength());
	}
	
	/**
	 * Sets the adds output to the panelist list. 
	 * @param str A string to add to the member list text area.
	 */
	public void txtpanelistOutput(String str)
	{
		txtpanelist.setText(txtpanelist.getText() + str + "\n");
		txtpanelist.setCaretPosition(txtpanelist.getDocument().getLength());
	}

	/**
	 * Sets the adds output to the audience list. 
	 * @param str A string to add to the member list text area.
	 */
	public void txtAudOutput(String str)
	{
		txtAud.setText(txtAud.getText() + str + "\n");
		txtAud.setCaretPosition(txtAud.getDocument().getLength());
	}
	
	/**
	 * Handels the panelist thread. 
	 */
	class PanelistListener extends Thread
	{
		public java.util.List<Thread> panelistThreads = new ArrayList<Thread>();
		ServerSocket serverSock;
		Socket socketpanelist;
		
		public PanelistListener()
		{
		}
		
		public void run()
		{
			try {
				serverSock = new ServerSocket(Integer.parseInt(txtPort.getText()));
				txtpanelistOutput("Started listener: " + serverSock.toString());
				
				// Listen for new connections from actors
				while(true) {
					txtpanelistOutput("Listening for panelists");
					socketpanelist = serverSock.accept();
					
					// Create resources for new connection
					BufferedReader in = 
							new BufferedReader(
							new InputStreamReader(socketpanelist.getInputStream()));
					PrintWriter out = 
							new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(socketpanelist.getOutputStream())), true);
					
					// Create new actor/thread and add to lists, start thread
					PanelistHandler panelist = new PanelistHandler(socketpanelist, in, out);
					Thread panelistThread = new Thread(panelist);
					panelistThread.start();
					panelistThreads.add(panelistThread);
					panelistsList.add(panelist);
				}
			}
			catch(IOException e) {
			}
			
			// If broken out of loop, join all threads and exit
			try {
				for (int i = 0; i < panelistThreads.size(); i++) {
					panelistThreads.get(i).join();
				}
			}
			catch (InterruptedException e) {
				System.out.println("panelist threads join interrupt: "+e);
			}
		}
	}
	
	/**
	 * Handles the socket for the panelists. 
	 */
	class PanelistHandler implements Runnable
	{
		Socket sock;  
		BufferedReader in; 
		PrintWriter out;
		String name = null;
		String msgInput;
		int blankInput = 0;
		boolean done = false;
		
		/**
		 * Constructor for Panelist Handler. 
		 * @param sock The socket for the connected panelist. 
		 * @param in The stream going in. 
		 * @param out 
		 */
		public PanelistHandler(Socket sock, BufferedReader in, PrintWriter out)
		{
			this.sock = sock;
			this.in = in;
			this.out = out;
		}
		
		public void setDone(boolean val) {
			this.done = val;
		}
		
		public void run()
		{
			// Get name from actor
			try {
				String newName = in.readLine();
				this.name = newName;
				txtpanelistOutput("panelist connected: "+sock.toString()+", "+name);
			}
			catch (IOException e) {
				System.out.println("panelist nameSet error");
			}
			
			// Update list of actors
			listChanged();
			
			// Start loop
			while(true)
			{
				// Wait for input
				try {
					msgInput = in.readLine();
				}
				catch (IOException e) {
					txtpanelistOutput("Connection Closed to "+name);
					break;
				}
				
				// Send input to all actors and audience
				if (msgInput != null && msgInput.length() > 0) {
					blankInput = 0;
					for (int i = 0; i < panelistsList.size(); i++)
						panelistsList.get(i).out.println(msgInput);
					for (int i = 0; i < audienceList.size(); i++)
						audienceList.get(i).out.println(msgInput);
				}
				// If client leaves ungracefully, close connection
				else if (blankInput > 20) {
					txtpanelistOutput(name+" left the chat, closing connection");
					try {
						sock.close();
					}
					catch (IOException e) {
						System.out.println("Error in closing panelist socket");
					}
					break;
				}
				else {
					blankInput++;
				}
			}
			done = true;
			listChanged();
		}
	}
	
	class AudienceListener extends Thread{
		public java.util.List<Thread> audienceThreads = new ArrayList<Thread>(); 
		ServerSocket serverSock; 
		Socket socketAudience; 
		public AudienceListener(){}

		public void run(){
			try{
				serverSock = new ServerSocket(Integer.parseInt(txtPort.getText())+1);
				txtAudOutput("Started listener: " + serverSock.toString());
				// Listen for new connections from actors
				while(true) {
					txtAudOutput("Listening for actors");
					socketAudience = serverSock.accept();
					
					// Create resources for new connection
					BufferedReader in = 
							new BufferedReader(
							new InputStreamReader(socketAudience.getInputStream()));
					PrintWriter out = 
							new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(socketAudience.getOutputStream())), true);
					
					// Create new actor/thread and add to lists, start thread
					AudienceHandler audience = new AudienceHandler(socketAudience, in, out);
					Thread audienceThread = new Thread(audience);
					audienceThread.start();
					audienceThreads.add(audienceThread);
					audienceList.add(audience);
				}
			}
			catch(IOException e) {
			}

			try {
				for (int i = 0; i < audienceThreads.size(); i++) {
					audienceThreads.get(i).join();
				}
			}
			catch (InterruptedException e) {
				System.out.println("Actor threads join interrupt: "+e);
			}

		}
	}
	
	class AudienceHandler implements Runnable
	{
		Socket sock;
		BufferedReader in;
		PrintWriter out;
		String name = null;
		String msgInput;
		int blankInput = 0;
		boolean done = false;
		boolean canSpeak = false; 
		
		public AudienceHandler(Socket sock, BufferedReader in, PrintWriter out)
		{
			this.sock = sock;
			this.in = in;
			this.out = out;
		}
		
		public void setCanSpeak(boolean v){
			canSpeak = v; 
		}

		public void setDone(boolean val) {
			this.done = val;
		}
		
		public void run()
		{
			// Get name from actor
			try {
				String newName = in.readLine();
				this.name = newName;
				txtAudOutput("Audience connected: "+sock.toString()+", "+name);
			}
			catch (IOException e) {
				System.out.println("Audience nameSet error");
			}
			
			// Update list of audience members 
			listChanged();
			
			// Start loop
			while(true)
			{
				// Wait for input
				try {
					msgInput = in.readLine();
				}
				catch (IOException e) {
					txtAudOutput("Connection Closed to "+name);
					break;
				}
				
				// Send input to all actors and audience
				if (msgInput != null && msgInput.length() > 0) {
					blankInput = 0;
					for (int i = 0; i < panelistsList.size(); i++)
						panelistsList.get(i).out.println(msgInput);
					
					for (int i = 0; i < audienceList.size(); i++)
						audienceList.get(i).out.println(msgInput);
				}
				// If client leaves ungracefully, close connection
				else if (blankInput > 20) {
					txtAudOutput(name+" left the chat, closing connection");
					try {
						sock.close();
					}
					catch (IOException e) {
						System.out.println("Error in closing actor socket");
					}
					break;
				}
				else {
					blankInput++;
				}
			}
			done = true;
			listChanged();
		}
	}

	
	// Leave alone 
	// Holds information for each audience member
	class SocketInfo
	{
		Socket sock;
		BufferedReader in;
		PrintWriter out;
		String name = null;
		boolean done = false;
		public SocketInfo(Socket sock, BufferedReader in, PrintWriter out)
		{
			this.sock = sock;
			this.in = in;
			this.out = out;
		}
		public void setName(String name)
		{
			this.name = name;
		}
	}
	
	public static void main(String[] args)
	{
		Console.run(new panserver(), 700, 650);
	}
} ///:~


































