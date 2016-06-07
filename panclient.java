import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class panclient extends JApplet
{
	int portNum;
	Random rand = new Random();
	int min = 1024, max = 65535;
	Socket socket = null;
	BufferedReader in = null;
	PrintWriter out = null;
	SocketInput socketReader = new SocketInput();
	Thread sockReader;
	public boolean sendMsg = false;
	int interval = 2000;
	ActionListener buttonPress = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			btnDisconnect.doClick();
		}
	};
	javax.swing.Timer delayedClose;
		

	Container cp = getContentPane();
	JButton 
		btnConnect = new JButton("Connect"),
		btnDisconnect = new JButton("Disconnect"),
		btnEnd = new JButton("End"),
		btnClear = new JButton("Clear Chat");
	JCheckBox
		chkActor = new JCheckBox("Check for Panelist");
	JTextField
		txtPort = new JTextField(Integer.toString(rand.nextInt((max-min)+1)+min), 8),
		txtHost = new JTextField("localhost", 15);
	JLabel 
		lblPort = new JLabel("   Port #: "),
		lblHost = new JLabel("   Hostname: ");
	JTextArea 
		txtOutput = new JTextArea(15, 50),
		txtInput = new JTextArea(2, 35);
	JPanel 
		jpText = new JPanel(),
		jpButtons = new JPanel(),
		jpInput = new JPanel(),
		jpBottom = new JPanel(),
		jpAll = new JPanel();
		
		
	public void init()
	{
		// Listener for Connect button
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (chkActor.isSelected()) {
					if (connect(true)) {
						sockReader = new Thread(socketReader);
						sockReader.start();
						btnDisconnect.setEnabled(true);
						btnConnect.setEnabled(false);
						txtPort.setBackground(new Color(200, 200, 200));
						txtPort.setEditable(false);
						txtHost.setBackground(new Color(200, 200, 200));
						txtHost.setEditable(false);
						sendMsg = true;
					}
				}
				else {
					//txtOutputPrintln("Trying to connect as audience");
					if (connect(false)) {
						sockReader = new Thread(socketReader);
						sockReader.start();
						btnDisconnect.setEnabled(true);
						btnConnect.setEnabled(false);
						txtInput.setBackground(new Color(200, 200, 200));
						//txtInput.setEditable(false);
						txtPort.setBackground(new Color(200, 200, 200));
						txtPort.setEditable(false);
						txtHost.setBackground(new Color(200, 200, 200));
						txtHost.setEditable(false);
					}
				}
			}
		});
		// Listener for Disconnect button
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try {
					socket.setSoTimeout(1000);
					socket.close();
				}
				catch (IOException er) {
					System.out.println("Error in closing socket: "+er);
				}
				
				try {
					sockReader.join();
				}
				catch (InterruptedException er) {
					System.out.println("Error in joining: "+er);
				}
				txtOutputPrintln("Disconnected from Server");
				socket = null;
				in = null;
				out = null;
				btnDisconnect.setEnabled(false);
				btnConnect.setEnabled(true);
				txtInput.setBackground(new Color(200, 255, 200));
				txtInput.setEditable(true);
				txtPort.setBackground(new Color(255, 255, 200));
				txtPort.setEditable(true);
				txtHost.setBackground(new Color(255, 255, 200));
				txtHost.setEditable(true);
			}
		});
		// Listener for End button
		btnEnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				int answer = JOptionPane.showConfirmDialog(
							cp, "Are you sure you want to end?", "Are you sure?", 
							JOptionPane.YES_NO_OPTION);
							
				if (answer == JOptionPane.YES_OPTION) {
					if (socket != null) {
						try {
							socket.close();
						}
						catch (IOException er) {
							System.out.println("Error in closing socket: "+er);
						}
				
						try {
							sockReader.join();
						}
						catch (InterruptedException er) {
							System.out.println("Error in joining: "+er);
						}
						txtOutputPrintln("Disconnected from Server");
					}
					System.exit(0);
				}
			}
		});
		// Listener for Clear button
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				txtOutput.setText("");
			}
		});
		
		// Listener for when the enter button is pressed in the text input area
		KeyListener keyListener = new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER) {
					enterPressed();
				}
			}
			public void keyReleased(KeyEvent e) {
			}
			public void keyTyped(KeyEvent e) {
			}
		};
		
		// Settings for buttons and text fields
		chkActor.setMnemonic(KeyEvent.VK_A);
		chkActor.setSelected(false);
		
		btnClear.setMnemonic(KeyEvent.VK_C);
		
		btnDisconnect.setEnabled(false);
		
		txtInput.addKeyListener(keyListener);
		txtInput.setBackground(new Color(200, 255, 200));
		txtInput.setFont(new Font("Serif", Font.PLAIN, 14));
		txtInput.setLineWrap(true);
		txtInput.setWrapStyleWord(true);
		
		txtPort.setBackground(new Color(255, 255, 200));
		txtHost.setBackground(new Color(255, 255, 200));
		
		txtOutput.setBackground(new Color(175, 175, 255));
		txtOutput.setEditable(false);
		txtOutput.setFont(new Font("Serif", Font.BOLD, 14));
		txtOutput.setLineWrap(true);
		txtOutput.setWrapStyleWord(true);
		
		// Using JPanels to organize and keep objects together
		jpButtons.setLayout(new BoxLayout(jpButtons, BoxLayout.LINE_AXIS));
		jpButtons.setBorder(new TitledBorder("Connection Settings"));
		jpButtons.add(lblHost);
		jpButtons.add(txtHost);
		jpButtons.add(lblPort);
		jpButtons.add(txtPort);
		jpButtons.add(chkActor);
		jpButtons.add(btnConnect);
		jpButtons.add(btnDisconnect);
		jpText.setBorder(new TitledBorder("Messages"));
		jpText.add(new JScrollPane(txtOutput));
		jpInput.setLayout(new BoxLayout(jpInput, BoxLayout.LINE_AXIS));
		jpInput.setBorder(new TitledBorder("Input"));
		jpInput.add(txtInput);
		jpInput.add(btnClear);
		jpBottom.setLayout(new BoxLayout(jpBottom, BoxLayout.LINE_AXIS));
		jpBottom.add(jpInput);
		jpBottom.add(btnEnd);
		jpAll.setLayout(new BoxLayout(jpAll, BoxLayout.PAGE_AXIS));
		jpAll.add(jpButtons);
		jpAll.add(jpText);
		jpAll.add(jpBottom);
		
		cp.setLayout(new FlowLayout());
		cp.add(jpAll);
	}
	
	public void enterPressed()
	{
		// If Enter pressed, send text in txtInput through socket
		if (out == null) {
			JOptionPane.showMessageDialog(
					cp, "Please connect to a server.", "Connection Error", 
					JOptionPane.WARNING_MESSAGE);
		}
		else {
			if(sendMsg)
				out.println(txtInput.getText());
			else{
				txtInput.setText("WAIT YOUR TURN!!!!");
			}
			

		}
		txtInput.setText(null);
		txtInput.moveCaretPosition(0);
		txtInput.setCaretPosition(0);
	}
	
	// Used by input listener thread to close socket after listener is gone. Could not
	// call button press from inside thread because button press also joins thread.
	public void delayedDisconnect() {
		delayedClose = new javax.swing.Timer(interval, buttonPress);
		delayedClose.setRepeats(false);
		delayedClose.start();
	}
	
	// For sending text to output window
	public void txtOutputPrintln(String str)
	{
		//txtOutput.setText(str + "\n" + txtOutput.getText());
		txtOutput.setText(txtOutput.getText() + str + "\n");
		txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
	}
	
	// Connection to server
	public boolean connect(boolean actor)
	{
		boolean connected = false;
		int var = 0;
		if (!actor)
			var = 1;

		// Try connecting to server
		try {
			socket = new Socket(txtHost.getText(), (Integer.parseInt(txtPort.getText())+var));
			if (actor) 
				txtOutputPrintln("Connected as Panelist: " + socket.toString());
			else
				txtOutputPrintln("Connected as Audience: " + socket.toString());
			
			// Create resources for new connection
			in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream())), true);
			
			// Send user name as first message
			out.println(System.getProperty("user.name"));
			connected = true;
		}
		catch(IOException e) {
			txtOutputPrintln("Could not connect to server.");
		}
		return connected;
	}
	
	// Thread for listening on the socket for input
	class SocketInput implements Runnable
	{
		String msg;
		int blankInput = 0;
		
		public void run()
		{
			while (true) {
				try {
					msg = in.readLine();
					if (msg != null) {
						blankInput = 0;
						//txtOutputPrintln("Studd")
						sendMsg = true; 
						txtOutputPrintln(msg);
					}
					else if (blankInput > 20) {
						txtOutputPrintln("Oops, something's wrong");
						txtOutputPrintln("Closing connection");
						delayedDisconnect();
						break;
					}
					else {
						blankInput++;
					}
				}
				catch (IOException e) {
					break;
				}
			}
		}
	}
		
	public static void main(String[] args)
	{
		Console.run(new panclient(), 800, 450);
	}
} ///:~
