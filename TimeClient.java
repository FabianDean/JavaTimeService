import java.net.*;
import java.io.*;

public class TimeClient {
	private Socket socket = null;
	private DataInputStream input = null;
	private String currTime = null;
	BufferedReader inFromClient = null;

	public TimeClient(String url) throws UnknownHostException, IOException {
		socket = new Socket(url, 13);
		input = new DataInputStream(socket.getInputStream());
		inFromClient = new BufferedReader(new InputStreamReader(input));
	}

	public String getTime() throws IOException {
		inFromClient.readLine(); // must call twice, otherwise currTime is an empty string
		currTime = inFromClient.readLine();
		return currTime;
	}

	public void close() throws IOException {
		inFromClient.close();
		socket.close();
	}

	public static void main(String args[]) {
		try {
			TimeClient client = new TimeClient("time.nist.gov");
			System.out.println(client.getTime());
			client.close();
			System.out.println("Client successfully closed");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
