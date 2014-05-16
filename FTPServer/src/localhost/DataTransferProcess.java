package localhost;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DataTransferProcess {

	private ServerSocket server;
	private int port = 20;

	public DataTransferProcess() throws IOException {
		initSocket();

		while(true) {
			Socket client = server.accept();
			client.close();
		}
	}

	private void initSocket() throws IOException {
		server = new ServerSocket(port);
	}
}