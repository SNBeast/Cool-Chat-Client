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
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
public class Client implements Runnable, KeyListener {
	private JFrame frame = new JFrame();
	private Container canvas = frame.getContentPane();
	private JTextArea text = new JTextArea();
	private JTextArea display = new JTextArea();
	private String name = "";
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Thread t;
	public Client () {
		text.addKeyListener(this);
		display.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		display.setPreferredSize(new Dimension(500, 600));
		display.setEditable(false);
		canvas.setLayout(new BorderLayout());
		canvas.add(display, BorderLayout.CENTER);
		canvas.add(text, BorderLayout.SOUTH);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new Closer());
		frame.setTitle("Connecting");
		while (true) {
			name = JOptionPane.showInputDialog("What's your name?");
			if (!name.equals("")) {
				break;
			}
		}
		String address = JOptionPane.showInputDialog("What is the IP of the server are you connecting to?");
		int port = Integer.parseInt(JOptionPane.showInputDialog("What port are you connecting on?\nMust be between 1024 and 65535"));
		frame.setVisible(true);
		try {
			socket = new Socket(InetAddress.getByName(address), port);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			out.writeObject(new Message(Message.message, name + " has joined."));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		frame.setTitle("Cool Chat Client");
		t = new Thread(this);
		t.run();
	}
	public static void main (String[] args) {
		new Client();
	}
	public void run () {
		while (true) {
			try {
				Message item = (Message)in.readObject();
				if (item != null) {
					if (item.type() == Message.message) {
						display.append((String)item.contents() + "\n");
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
				while (s.indexOf("\\n") != -1) {
					s = s.substring(0, s.indexOf("\\n")) + "\n" + name + ": " + s.substring(s.indexOf("\\n") + 2);
				}
				out.writeObject(new Message(Message.message, name + ": " + s));
				out.flush();
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
				out.writeObject(new Message(Message.message, name + " has left."));
				out.flush();
				socket.close();
				System.exit(0);
			} catch (IOException e) {}
		}
	}
}