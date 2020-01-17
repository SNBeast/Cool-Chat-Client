import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
public class Server {
	private ServerSocket serverSocket;
	private ArrayList<ConnectedClient> clients = new ArrayList<ConnectedClient>();
	public Server (int port, int maxClients) {
		try {
			serverSocket = new ServerSocket(port, maxClients);
			System.out.println("Cool Chat Server v1.0\nHosting at address " + InetAddress.getLocalHost().getHostAddress() + " and port " + port + " with a maximum of " + maxClients + " clients.");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		new ServerShell(this);
		while (true) {
			try {
				clients.add(new ConnectedClient(serverSocket.accept()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void sendMessage (Message message) {
		for (int i = 0; i < clients.size(); i++) {
			clients.get(i).sendMessageTo(message);
		}
	}
	private boolean checkName (String name) {
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).name.toLowerCase().equals(name.toLowerCase())) {
				return false;
			}
		}
		return true;
	}
	private class ServerShell extends Thread {
		private Scanner s = new Scanner(System.in);
		private String command;
		private ServerShell (Server server) {
			System.out.print("/");
			start();
		}
		public void run () {
			while (true) {
				if (s.hasNextLine()) {
					command = s.nextLine();
					System.out.println(command);
					if (command.equals("exit")) {
						try {
							serverSocket.close();
						} catch (IOException e) {e.printStackTrace();}
						System.exit(0);
					}
				}
			}
		}
	}
	private class ConnectedClient extends Thread {
		private Socket socket;
		private ObjectOutputStream out;
		private ObjectInputStream in;
		private String name = "";
		public ConnectedClient (Socket s) {
			socket = s;
			try {
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
				clients.remove(this);
			}
			start();
		}
		public void sendMessageTo (Message m) {
			try {
				out.writeObject(m);
				out.flush();
			} catch (IOException e) {
				clients.remove(this);
			}
		}
		public void run () {
			while (true) {
				try {
					Message item = (Message)in.readObject();
					if (item != null) {
						if (item.type() == Message.message) {
							if (!name.equals("")) {
								sendMessage(new Message(Message.message, name + ": " + (String)item.contents()));
							}
							else {
								clients.remove(this);
								socket.close();
								break;
							}
						}
						else if (item.type() == Message.name) {
							if (name.equals("")) {
								if (checkName((String)item.contents())) {
									name = (String)(item.contents());
									out.writeObject(new Message(Message.name, true));
									sendMessage(new Message(Message.message, name + " has joined."));
								}
								else {
									out.writeObject(new Message(Message.name, false));
								}
								out.flush();
							}
							else {
								clients.remove(this);
								socket.close();
								break;
							}
						}
					}
				} catch (Exception e) {
					clients.remove(this);
					try {
						socket.close();
					} catch (Exception ex) {
						socket = null;
					}
					break;
				}
			}
		}
	}
}