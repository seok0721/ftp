package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class FTPServerDataTransferProcess {

	private Socket socket;
	private FTPEnvironment environment;

	public FTPEnvironment getEnvironment() {
		return environment;
	}

	public void setEnvironment(FTPEnvironment environment) {
		this.environment = environment;
	}

	public void open(String host, int port) throws Exception {
		if(socket != null && socket.isConnected()) {
			throw new Exception("Already socket is bound.");
		}

		try {
			socket = new Socket(host, port);
			System.out.println(String.format("%s:%d - Success to connect client DTP", host, port));
		} catch(Exception e) {
			System.err.println(String.format("%s:%d - Failure to connect client DTP", host, port));
		}
	}

	public void execute(FTPCommand ftpCommand) {
		if(ftpCommand == null) {
			System.err.println("Unknown command.");
			return;
		}

		switch(ftpCommand) {
		case NAME_LIST:
			listDirectoryContents(("ls " + environment.getWorkingDirectory()).split(" "));
			break;

		case LIST:
			listDirectoryContents(("ls -al " + environment.getWorkingDirectory()).split(" "));
			break;

		default:
			break;
		}
	}

	public void close() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch(IOException e) {}
	}

	public void listDirectoryContents(String[] argv) {
		if(argv == null || argv.length == 0) {
			throw new NullPointerException("Arguments must not be null.");
		}

		String[] envp = {"LANG=en_US.UTF-8"};

		try {
			DataInputStream in = new DataInputStream(Runtime.getRuntime().exec(argv, envp).getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			byte[] buf = new byte[4096];
			int len = 0;

			StringBuffer buffer = new StringBuffer();

			while((len = in.read(buf)) > 0) {
				buffer.append(new String(buf, 0, len));
			}

			out.writeBytes(buffer.toString().replace("\n", "\r\n"));
		} catch(Exception e) {

		}
	}

}