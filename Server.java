import java.net.*;
import java.io.*;
import java.text.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Server {
	// initialize socket and input stream
	private Socket socket = null;
	private ServerSocket server = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	private BufferedReader inFromClient = null;
	private String request = null;
	private String params = null;
	private String response = null;
	private String header = null;
	private String body = null;

	public Server(int port) throws IOException {
		server = new ServerSocket(port);
	}

	/** Accept incoming connection */
	public void accept() throws IOException {
		socket = server.accept();
		System.out.println("Successfully connected to " + socket.getInetAddress().toString());
	}

	public void parseRequest() throws IOException {
		in = new DataInputStream(socket.getInputStream());
		inFromClient = new BufferedReader(new InputStreamReader(in));
		// GET /time[?zone=x] HTTP/1.1 where x = GMT, EST, or PST
		request = inFromClient.readLine();
		params = request.split(" ")[1]; // get path from header
	}

	public void giveTime() throws Throwable {
		// if valid route
		if (params.equals("/time") || params.equals("/time?zone=all") || params.equals("/time?zone=est")
				|| params.equals("/time?zone=pst")) {
			TimeClient timeClient = new TimeClient("time.nist.gov");
			String dateTime = timeClient.getTime();
			timeClient.close();

			DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			DateFormat outputFormat = new SimpleDateFormat("MM-dd-yyyy'T'KK:mm a");

			dateTime = dateTime.split(" ")[1] + "T" + dateTime.split(" ")[2];
			// need to add '20' for parse to work correctly
			String strDateTime = "20" + dateTime;
			LocalDateTime gmtLDT = LocalDateTime.parse(strDateTime);
			String gmtDateTime = outputFormat.format(inputFormat.parse(strDateTime));

			String estDateTime = OffsetDateTime.of(gmtLDT, ZoneOffset.of("+05:00")).toInstant().toString();
			estDateTime = estDateTime.substring(0, estDateTime.length() - 1);
			estDateTime = outputFormat.format(inputFormat.parse(estDateTime));

			String pstDateTime = OffsetDateTime.of(gmtLDT, ZoneOffset.of("+07:00")).toInstant().toString();
			pstDateTime = pstDateTime.substring(0, pstDateTime.length() - 1);
			pstDateTime = outputFormat.format(inputFormat.parse(pstDateTime));

			String[] gmtArr = gmtDateTime.split("T");
			if (gmtArr[1].substring(0, 1).equals("0"))
				gmtArr[1] = gmtArr[1].substring(1, gmtArr[1].length());
			gmtDateTime = String.join(" ", gmtArr);
			gmtDateTime = gmtDateTime.replace("-", "/");
			gmtDateTime = gmtDateTime.replaceFirst(" ", ", ");

			String[] estArr = estDateTime.split("T");
			if (estArr[1].substring(0, 1).equals("0"))
				estArr[1] = estArr[1].substring(1, estArr[1].length());
			estDateTime = String.join(" ", estArr);
			estDateTime = estDateTime.replace("-", "/");
			estDateTime = estDateTime.replaceFirst(" ", ", ");

			String[] pstArr = pstDateTime.split("T");
			if (pstArr[1].substring(0, 1).equals("0"))
				pstArr[1] = pstArr[1].substring(1, pstArr[1].length());
			pstDateTime = String.join(" ", pstArr);
			pstDateTime = pstDateTime.replace("-", "/");
			pstDateTime = pstDateTime.replaceFirst(" ", ", ");

			if (params.equals("/time?zone=est")) {
				body = "<html><head><title>Output from Server</title></head><body><h1>EST Date/Time: " + estDateTime
						+ "</h1></body></html>";
				header = "HTTP/1.1 200 OK\r\n" + "Host: " + server.getInetAddress() + "\r\n"
						+ "Content-Type: text/html\r\n" + "Content-Length: " + body.length() + "\r\n"
						+ "Connection: close\r\n\r\n";
				response = header + body;
			} else if (params.equals("/time?zone=pst")) {
				body = "<html><head><title>Output from Server</title></head><body><h1>PST Date/Time: " + pstDateTime
						+ "</h1></body></html>";
				header = "HTTP/1.1 200 OK\r\n" + "Host: " + server.getInetAddress() + "\r\n"
						+ "Content-Type: text/html\r\n" + "Content-Length: " + body.length() + "\r\n"
						+ "Connection: close\r\n\r\n";
				response = header + body;
			} else {
				body = "<html><head><title>Output from Server</title></head><body><h1>GMT Date/Time: " + gmtDateTime
						+ "<br/><br/>EST Date/Time: " + estDateTime + "<br/><br/>PST Date/Time: " + pstDateTime
						+ "</h1></body></html>";
				header = "HTTP/1.1 200 OK\r\n" + "Host: " + server.getInetAddress() + "\r\n"
						+ "Content-Type: text/html\r\n" + "Content-Length: " + body.length() + "\r\n"
						+ "Connection: close\r\n\r\n";
				response = header + body;
			}
		} else {
			body = "<html><head><title>Output from Server</title></head><body><h1>Invalid Request</h1></body></html>";
			header = "HTTP/1.1 200 OK\r\n" + "Host: " + server.getInetAddress() + "\r\n" + "Content-Type: text/html\r\n"
					+ "Content-Length: " + body.length() + "\r\n" + "Connection: close\r\n\r\n";
			response = header + body;
		}
		out = new DataOutputStream(socket.getOutputStream());
		out.writeBytes(response);
	}

	public void closeSocket() throws IOException {
		socket.close();
	}

	public static void main(String args[]) throws Throwable {
		int port = 5000;
		Server server = new Server(port);

		System.out.println("Server running on port " + port);
		while (true) {
			// keep running until there is an incoming socket
			server.accept();
			// give time
			server.parseRequest();
			server.giveTime();
			server.closeSocket();
		}
	}
}
