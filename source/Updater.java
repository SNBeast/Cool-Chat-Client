import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
public class Updater {
	private String link = "https://github.com/SNBeast/Cool-Chat-Client/releases/latest/download/";
	private InputStream in;
	private File version;
	private File contents;
	private String s = "";
	private String option;
	private BufferedReader reader;
	public Updater () {
		try {
			download("version.txt");
			version = new File("version.txt");
			reader = new BufferedReader(new FileReader(version));
			if (Double.parseDouble(reader.readLine()) != Client.version) {
				download("contents.txt");
				contents = new File("contents.txt");
				reader = new BufferedReader(new FileReader(contents));
				while ((s = reader.readLine()) != null) download(s);
				reader.close();
				contents.delete();
			}
			version.delete();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("\nReport to me ASAP");
		}
		while (true) {
			option = JOptionPane.showInputDialog("Do you want to run Server or Client?");
			if (option.toLowerCase().equals("server")) {
				int port = Integer.parseInt(JOptionPane.showInputDialog("Enter the port to use (between 1024 and 65535)."));
				int maxClients = Integer.parseInt(JOptionPane.showInputDialog("Enter the maximum clients to accept."));
				new Server(port, maxClients);
				break;
			}
			else if (option.toLowerCase().equals("client")) {
				new Client();
				break;
			}
		}
	}
	public void download (String fileName) {
		try {
			if (fileName.indexOf(".") == -1) new File(fileName).mkdirs();
			else {
				in = URI.create(link + fileName).toURL().openStream();
				Files.copy(in, Paths.get(fileName));
			}
		} catch (FileAlreadyExistsException e) {
			new File(fileName).delete();
			download(fileName);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("\nThe update cannot be run. Check internet conditions. If fine, report to me ASAP");
			System.exit(1);
		}
	}
	public static void main (String[] args) {
		new Updater();
	}
}