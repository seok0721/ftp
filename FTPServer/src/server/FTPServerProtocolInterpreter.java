package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPServerProtocolInterpreter implements Runnable {

	private static ServerSocket server;
	private int port;
	private FTPEnvironment env = new FTPEnvironment();
	private FTPServerDataTransferProcess serverDTP;

	public FTPServerProtocolInterpreter() {
		port = FTPConfiguration.getPort();
	}

	@Override
	public void run() {
		System.out.println("Create FTP Server Protocol Interpretor...");

		try {
			server = new ServerSocket(port);
		} catch(IOException e) {
			System.err.println("Failure to create server socket.");
			System.exit(1);
		}

		while(true) {
			CommandSocket client = null;

			try {
				client = new CommandSocket(server.accept());

				System.out.println(String.format("New client connect success: %s, %d",
						client.getSocket().getInetAddress().toString(),
						client.getSocket().getPort()));

				Thread thread = new Thread(client);
				thread.start();
				thread.join();
			} catch(IOException e) {
				System.err.println("New client connect failure: " + e.getMessage());
			} catch(InterruptedException e) {
				System.err.println("Client thread is interrupted: " + e.getMessage());
			}

		}
	}

	public FTPServerDataTransferProcess getServerDTP() {
		return serverDTP;
	}

	public void setServerDTP(FTPServerDataTransferProcess serverDTP) {
		this.serverDTP = serverDTP;
	}

	public class CommandSocket implements Runnable {

		private Socket socket;
		private FTPCommand ftpCommand;
		private BufferedReader reader;
		private BufferedWriter writer;

		public CommandSocket(Socket socket) {
			if(socket == null) {
				throw new NullPointerException();
			}

			this.socket = socket;

			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		public Socket getSocket() {
			return socket;
		}

		@Override
		public void run() {
			/* Connection Establishment */
			sendCode(220, "Nice to meet you.");

			FETCH:
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

					/* Login */
					case USER_NAME:
					case PASSWORD:
						sendCode(230, "Welcome guest!");
						break;

						/* Logout */
					case LOGOUT:
						sendCode(221, "See you!");
						break FETCH;

						/* Transfer parameters */
					case DATA_PORT:
						String[] args = ftpCommand.getArgument().split(",");
						String host = String.format("%s.%s.%s.%s", args[0], args[1], args[2], args[3]);
						int port = (Integer.parseInt(args[4]) << 8) | (Integer.parseInt(args[5]));

						serverDTP = new FTPServerDataTransferProcess();
						serverDTP.setEnvironment(env);
						try {
							serverDTP.open(host, port);
							sendCode(200, null);
						} catch(Exception e) {
							sendCode(500, e.getMessage());
						}

						break;

						/* Informational commands */
					case SYSTEM:
						sendCode(215, System.getProperty("os.name", "unknown"));
						break;

						/* File action commands */
					case PRINT_WORKING_DIRECTORY:
						sendCode(257, env.getWorkingDirectory());
						break;

					case CHANGE_WORKING_DIRECTORY:
						env.setWorkingDirectory(ftpCommand.getArgument());
						sendCode(250, env.getWorkingDirectory());
						break;

					case NAME_LIST:
						sendCode(150, "Name List.");
						serverDTP.execute(ftpCommand);
						serverDTP.close();
						sendCode(226, "Name List");
						break;

					case LIST:
						sendCode(150, "List.");
						serverDTP.execute(ftpCommand);
						serverDTP.close();
						sendCode(226, "List");
						break;

					case REPRESENTATION_TYPE:
						// TODO mode change
						sendCode(200, "Transfer Mode.");
						break;

					default:
						sendCode(502, "Command not implemented.");
						break;
					}
				}

			if(socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} catch(IOException e) {}
			}

			System.out.println("Close client socket.");
		}

		private void sendCode(int code, String description) {
			try {
				writer.write(String.format("%d %s\r\n", code, (description == null) ? "" : description));
				writer.flush();
				System.out.println(String.format("Send Success: %d %s", code, (description == null) ? "" : description));
			} catch(IOException e) {
				System.err.println(String.format("Send Failure: %d %s", code, (description == null) ? "" : description));
			}
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
}