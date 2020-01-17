import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
public class Client implements Runnable, KeyListener {
	public static final double version = 0.91;
	private JFrame frame = new JFrame("Cool Chat Client");
	private Container canvas = frame.getContentPane();
	private JTextArea text = new JTextArea();
	private JTextArea display = new JTextArea();
	private JScrollPane scrollpane = new JScrollPane(display);
	private String name = "";
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Thread t;
	private boolean astoggle = true;
	public Client () {
		text.addKeyListener(this);
		display.setLineWrap(true);
		display.setWrapStyleWord(true);
		display.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		display.setSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		display.setEditable(false);
		scrollpane.setPreferredSize(new Dimension(500, 600));
		canvas.setLayout(new BorderLayout());
		canvas.add(scrollpane, BorderLayout.CENTER);
		canvas.add(text, BorderLayout.SOUTH);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new Closer());
		String address = JOptionPane.showInputDialog("What is the IP of the server are you connecting to?");
		int port = Integer.parseInt(JOptionPane.showInputDialog("What port are you connecting on?\nMust be between 1024 and 65535"));
		frame.setVisible(true);
		try {
			socket = new Socket(InetAddress.getByName(address), port);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			nameCycle:
			while (true) {
				name = JOptionPane.showInputDialog("What's your name?");
				if (!name.equals("") && name.matches("^[a-zA-Z0-9]*$")) {
					out.writeObject(new Message(Message.name, name));
					out.flush();
					while (true) {
						try {
							Message item = (Message)in.readObject();
							if (item != null) {
								if (item.type() == Message.name) {
									if ((Boolean)item.contents()) break nameCycle;
									else break;
								}
							}
						} catch (Exception e) {
							System.out.println("Server unreachable");
							System.exit(1);
						}
					} 
				}
			}
		} catch (IOException e) {
			System.out.println("Server unreachable");
			System.exit(1);
		}
		t = new Thread(this);
		t.run();
	}
	public void run () {
		while (true) {
			try {
				Message item = (Message)in.readObject();
				if (item != null) {
					if (item.type() == Message.message) {
						display.append((String)item.contents() + "\n");
						if (astoggle) scrollpane.getVerticalScrollBar().setValue(scrollpane.getVerticalScrollBar().getMaximum());
					}
				}
			} catch (EOFException e) {
				display.append("Server has closed.");
				break;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void keyPressed (KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			try {
				String s = text.getText();
				if (s.charAt(0) == '!') {
					if (s.equals("!help")) {
						display.append("Commands:\n-!help: lists this command list\n!astoggle: toggles autoscrolling");
					}
					else if (s.equals("!astoggle")) {
						astoggle ^= true;
					}
				}
				else {
					out.writeObject(new Message(Message.message, s));
					out.flush();
				}
				text.setText("");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			e.consume();
		}
	}
	public void keyTyped (KeyEvent e) {}
	public void keyReleased (KeyEvent e) {}
	private class Closer extends WindowAdapter {
		public void windowClosing (WindowEvent w) {
			try {
				out.writeObject(new Message(Message.message, " has left."));
				out.flush();
				socket.close();
				System.exit(0);
			} catch (IOException e) {}
		}
	}
}