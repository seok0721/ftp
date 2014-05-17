package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FTPServerDataTransferProcess implements Runnable {

	private Socket socket;
	private InetSocketAddress inetSocketAddress;
	private FTPCommand ftpCommand;
	private BufferedReader reader;
	private BufferedWriter writer;

	public FTPServerDataTransferProcess(String host, int port) throws IOException {
		System.out.println(String.format("Connect to FTP client data socket: %s %d", host, port));

		socket = new Socket();

		try {
			inetSocketAddress = new InetSocketAddress(host, port);

			socket.connect(inetSocketAddress);

			System.out.println("Success to connect FTP client data socket.");

			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			sendCode(200, "Open data channel.");
		} catch(IOException e) {
			System.err.println("Failure to connect FTP client data socket.");

			closeDataSocket();

			throw e;
		}
	}

	public void open(String host, int port) throws IOException {
		System.out.println(String.format("Connect to FTP client data socket: %s %d", host, port));

		socket = new Socket();

		try {
			inetSocketAddress = new InetSocketAddress(host, port);

			socket.connect(inetSocketAddress);

			System.out.println("Success to connect FTP client data socket.");

			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			sendCode(200, "Open data channel.");
		} catch(IOException e) {
			System.err.println("Failure to connect FTP client data socket.");

			closeDataSocket();

			throw e;
		}
	}

	@Override
	public void run() {
		while(true) {
			try {
				readCommand();
			} catch(IOException e) {
				System.err.println(e.getMessage());
				break;
			}

			if(ftpCommand == null) {
				System.err.println("Unknown command.");
				continue;
			}

			switch(ftpCommand) {
			default:
				sendCode(502, "Command not implemented.");
				break;
			}
		}

		closeDataSocket();
	}

	public InetSocketAddress getInetSocketAddress() {
		return inetSocketAddress;
	}

	private void sendCode(int code, String description) {
		try {
			writer.write(String.format("%d %s\n", code, (description == null) ? "" : description));
			writer.flush();
			System.out.println(String.format("Send Success: %d %s", code, (description == null) ? "" : description));
		} catch(IOException e) {
			System.err.println(String.format("Send Failure: %d %s", code, (description == null) ? "" : description));
		}
	}

	private void closeDataSocket() {
		if(socket != null && !socket.isClosed()) {
			try {
				socket.close();
			} catch(IOException ex) {}
		}

		System.out.println("Close FTP client data socket.");
	}

	private void readCommand() throws IOException {
		String line = null;
		String[] pair = null;

		try {
			line = reader.readLine();

			if(line != null) {
				pair = line.split(" ");
			} else {
				throw new IOException("Client is closed.");
			}

			if(pair.length == 1) {
				System.out.println(String.format("Receive Success: %s", pair[0]));
			} else {
				System.out.println(String.format("Receive Success: %s %s", pair[0], pair[1]));
			}
		} catch(IOException e) {
			if(pair == null) {
				System.err.println("Receive Failure.");
			}

			if(reader == null || !reader.ready()) {
				throw new IOException("Client input stream is not ready status.");
			}

			ftpCommand = null;
			return;
		}

		switch(pair[0]) {
		case "USER":
			ftpCommand = FTPCommand.USER_NAME;
			break;
		case "PASS":
			ftpCommand = FTPCommand.PASSWORD;
			break;
		case "ACCT":
			ftpCommand = FTPCommand.ACCOUNT;
			break;
		case "CWD":
			ftpCommand = FTPCommand.CHANGE_WORKING_DIRECTORY;
			break;
		case "CDUP":
			ftpCommand = FTPCommand.CHANGE_TO_PARENT_DIRECTORY;
			break;
		case "SMNT":
			ftpCommand = FTPCommand.STRUCTURE_MOUNT;
			break;
		case "REIN":
			ftpCommand = FTPCommand.REINITIALIZE;
			break;
		case "QUIT":
			ftpCommand = FTPCommand.LOGOUT;
			break;
		case "PORT":
			ftpCommand = FTPCommand.DATA_PORT;
			break;
		case "PASV":
			ftpCommand = FTPCommand.PASSIVE;
			break;
		case "TYPE":
			ftpCommand = FTPCommand.REPRESENTATION_TYPE;
			break;
		case "STRU":
			ftpCommand = FTPCommand.FILE_STRUCTURE;
			break;
		case "MODE":
			ftpCommand = FTPCommand.TRANSFER_MODE;
			break;
		case "RETR":
			ftpCommand = FTPCommand.RETRIEVE;
			break;
		case "STOR":
			ftpCommand = FTPCommand.STORE;
			break;
		case "STOU":
			ftpCommand = FTPCommand.STORE_UNIQUE;
			break;
		case "APPE":
			ftpCommand = FTPCommand.APPEND;
			break;
		case "ALLO":
			ftpCommand = FTPCommand.ALLOCATE;
			break;
		case "REST":
			ftpCommand = FTPCommand.RESTART;
			break;
		case "RNFR":
			ftpCommand = FTPCommand.RENAME_FROM;
			break;
		case "RNTO":
			ftpCommand = FTPCommand.RENAME_TO;
			break;
		case "ABOR":
			ftpCommand = FTPCommand.ABORT;
			break;
		case "DELE":
			ftpCommand = FTPCommand.DELETE;
			break;
		case "RMD":
			ftpCommand = FTPCommand.REMOVE_DIRECTORY;
			break;
		case "MKD":
			ftpCommand = FTPCommand.MAKE_DIRECTORY;
			break;
		case "PWD":
			ftpCommand = FTPCommand.PRINT_WORKING_DIRECTORY;
			break;
		case "LIST":
			ftpCommand = FTPCommand.LIST;
			break;
		case "NLST":
			ftpCommand = FTPCommand.NAME_LIST;
			break;
		case "SITE":
			ftpCommand = FTPCommand.SITE_PARAMETERS;
			break;
		case "SYST":
			ftpCommand = FTPCommand.SYSTEM;
			break;
		case "STAT":
			ftpCommand = FTPCommand.STATUS;
			break;
		case "HELP":
			ftpCommand = FTPCommand.HELP;
			break;
		case "NOOP":
			ftpCommand = FTPCommand.NOOP;
			break;
		default:
			ftpCommand = null;
			break;
		}

		if(pair.length == 1) {
			System.out.println("Command: " + pair[0]);
		} else {
			ftpCommand.setArgument(pair[1]);

			System.out.println("Command: " + pair[0] + ", Arguments: " + pair[1]);
		}
	}
}