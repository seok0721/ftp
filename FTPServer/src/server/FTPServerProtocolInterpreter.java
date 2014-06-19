package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class FTPServerProtocolInterpreter implements Runnable {

	private ServerSocket server;

	public String getAddress() {
		if(server == null) {
			return null;
		}

		return server.getInetAddress().getHostAddress();
	}

	public Integer getPort() {
		if(server == null) {
			return null;
		}

		return server.getLocalPort();
	}

	@Override
	public void run() {
		Logger.out("Trying to create FTP server protocol interpretor...");

		try {
			server = new ServerSocket(FTPConfiguration.getPort());
			server.setReuseAddress(true);

			Logger.out("Success to create FTP server protocol interpretor.");
		} catch(Exception e) {
			Logger.err("Failure to create FTP server protocol interpretor.");

			System.exit(1);
		}

		while(true) {
			ClientSocket client = null;
			try {
				client = new ClientSocket(server.accept());

				Logger.out(String.format("Success to accept new client. %s:%d",
						client.getSocket().getInetAddress().toString(),
						client.getSocket().getPort()));

				Thread thread = new Thread(client);
				thread.start();
				thread.join();
			} catch(IOException e) {
				Logger.err("Failure to accept new client.");
			} catch(InterruptedException e) {
				Logger.err(String.format("Client thread is interrupted. %s:%d",
						client.getSocket().getInetAddress().toString(),
						client.getSocket().getPort()));
			}
		}
	}

	public class ClientSocket implements Runnable {

		private FTPServerDataTransferProcess serverDTP;
		private FTPEnvironment env = new FTPEnvironment();
		private String renameFile;
		private Socket socket;
		private FTPCommand ftpCommand;
		private BufferedReader reader;
		private BufferedWriter writer;

		public ClientSocket(Socket socket) {
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
						try {
							env.setWorkingDirectory(ftpCommand.getArgument());
							sendCode(250, env.getWorkingDirectory());
						} catch(Exception e) {
							sendCode(550, e.getMessage());
						}

						break;

					case RENAME_FROM:
						renameFile = ftpCommand.getArgument();
						sendCode(350, "Rename from: " + renameFile);
						break;

					case RENAME_TO:
						File sourceFile = new File(String.format("%s/%s", env.getWorkingDirectory(), renameFile));
						File targetFile = new File(String.format("%s/%s", env.getWorkingDirectory(), ftpCommand.getArgument()));

						sourceFile.renameTo(targetFile);

						sendCode(250, "Rename to: " + targetFile.getName());
						break;

					case STORE:
						sendCode(125, null);

						try {
							serverDTP.store(ftpCommand);
							sendCode(226, null);
						} catch(Exception e1) {
							sendCode(550, e1.getMessage());
						} finally {
							serverDTP.close();
						}

						break;

					case LIST:
						sendCode(150, "List.");
						serverDTP.execute(ftpCommand);
						serverDTP.close();
						sendCode(226, "List");
						break;

					case NAME_LIST:
						sendCode(150, "Name List.");
						serverDTP.execute(ftpCommand);
						serverDTP.close();
						sendCode(226, "Name List");
						break;

					case REPRESENTATION_TYPE:
						changeRepresentationType();
						break;

					case RETRIEVE:
						try {
							serverDTP.assertFile(ftpCommand);
							sendCode(125, "File transfer start.");
						} catch(Exception e) {
							sendCode(550, e.getMessage());
							serverDTP.close();
							break;
						}

						try {
							serverDTP.retrieve(ftpCommand);
							sendCode(226, "File transfer success.");
						} catch(Exception e) {
							sendCode(550, e.getMessage());
						} finally {
							serverDTP.close();
						}

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

		private void changeRepresentationType() {
			switch(ftpCommand.getArgument()) {
			case "A":
				env.setFtpDataType(FTPDataType.ASCII);
				sendCode(200, "Transfer mode : Ascii");
				break;
			case "I":
				env.setFtpDataType(FTPDataType.IMAGE);
				sendCode(200, "Transfer mode : Binary");
				break;
			case "E":
				env.setFtpDataType(FTPDataType.EBCDIC);
				sendCode(200, "Transfer mode : Ebcdic");
				break;
			default:
				sendCode(500, "Unknown transfer mode.");
				break;
			}
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
				System.out.println("Receive unknown command.");
				return;
			}

			if(pair.length == 1) {
				ftpCommand.setArgument(null);
				System.out.println("Command: " + pair[0]);
				return;
			}

			if(pair.length == 2) {
				ftpCommand.setArgument(pair[1]);
			} else {
				StringBuffer buffer = new StringBuffer();
				for(String arg : Arrays.copyOfRange(pair, 1, pair.length - 1)) {
					buffer.append(arg).append(" ");
				}
				buffer.append(pair[pair.length - 1]);

				ftpCommand.setArgument(buffer.toString());
			}

			System.out.println("Command: " + pair[0] + ", Arguments: " + ftpCommand.getArgument());
		}
	}
}