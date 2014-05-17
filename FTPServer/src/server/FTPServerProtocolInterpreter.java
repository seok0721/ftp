package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class FTPServerProtocolInterpreter implements Runnable {

	private Set<InetSocketAddress> serverPISet = new HashSet<InetSocketAddress>();
	private static ServerSocket server;
	private int port;

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

						createServerDTP(host, port);

						break;

						/* Informational commands */
					case SYSTEM:
						sendCode(215, System.getProperty("os.name", "unknown"));
						break;

						/* File action commands */
					case NAME_LIST:
						sendCode(150, "GoGo");

						PipedWriter pipeWriter = new PipedWriter();
						PipedReader pipeReader = new PipedReader();

						// String[] argv = { "ls", "-al" };
						// String[] envp = { "LANG=en_US.UTF-8" };
/*
						String[] argv = { "dir" };
						String[] envp = { };

						try {
							BufferedReader listReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(argv, envp).getInputStream()));
							String line = null;

							while((line = listReader.readLine()) != null) {
								writer.write(line);
							}
						} catch(IOException e) {
							e.printStackTrace();
						}
*/
						sendCode(250, "Closing data connection. Requested file action successful (for example, file transfer or file abort).");

						//		                  125, 150
						//		                     226, 250
						//		                     425, 426, 451
						//		                  450
						//		                  500, 501, 502, 421, 530
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

			/*
		Thread thread = new Thread(new Runnable() {
			Environment env = new Environment();

			final BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

			final DataInputStream in = new DataInputStream(client.getInputStream());
			final DataOutputStream out = new DataOutputStream(client.getOutputStream());

			@Override
			public void run() {
				Socket socket = null;

				reply (220, "Service ready for new user.");

				try {
					String line = null;
					File file = null;

					while((line = reader.readLine()) != null) {
						System.out.println("LINE: " + line);
						String token[] = line.split(" ");
						byte[] buf = new byte[4096];

						switch(token[0]) {
						case "SYST": // Reference: http://www.iana.org/assignments/operating-system-names/operating-system-names.xhtml
							reply(215, "Linux");
							break;
						case "QUIT":
							reply(221, "Logged out if appropriate.");
							break;
						case "PWD":
							// reply(257, "\"PATHNAME\" created.");
							reply(257, env.getWorkingDirectory());
							break;
						case "CWD":
							try {
								env.setWorkingDirectory(token[1]);
								reply(250, "Requested file action okay, completed.");
							} catch(Exception e) {
								reply(550, "Requested action not taken. File unavailable (e.g., file not found, no access).");
							}
							break;
						case "LIST":
							reply(150, "File status okay; about to open data connection.");
							StringBuffer buffer = new StringBuffer();
							// String[] argv = {"/system/bin/ls", "-al", pathname};
							String[] argv = {"ls", "-al", env.getWorkingDirectory()};
							String[] envp = {"LANG=en_US.UTF-8"};
							DataInputStream in = new DataInputStream(Runtime.getRuntime().exec(argv, envp).getInputStream());
							DataOutputStream out = new DataOutputStream(socket.getOutputStream());
							int read = 0;
							while((read = in.read(buf)) > 0) {
								buffer.append(new String(buf, 0, read).replace("\n", "\r\n"));
							}
							System.out.println(buffer.toString());
							out.writeBytes(buffer.toString());
							//							out.writeBytes(CoreUtils.getDirectoryContents(new File(pathname)));
							reply(226, "Closing data connection. Requested file action successful (for example, file transfer or file abort).");
							if(socket != null && !socket.isClosed()) {
								socket.close();
							}
							break;
						case "PORT":
							reply(200, "Command okay.");
							String[] hostPort = line.split(" ")[1].split(",");
							String host = new StringBuffer()
							.append(hostPort[0]).append(".")
							.append(hostPort[1]).append(".")
							.append(hostPort[2]).append(".")
							.append(hostPort[3]).toString();
							Integer port = (Integer.parseInt(hostPort[4]) << 8) | Integer.parseInt(hostPort[5]);

							try {
								socket = new Socket(host, port);
							} catch(Exception e) {
								e.printStackTrace();
							}
							System.out.println(host + " " + port);
							break;
						case "RETR":
							if(token[1].charAt(0) == '/') {
								file = new File(token[1]);
							} else {
								file = new File(env.getWorkingDirectory() + "/" + token[1]);
							}

							if(!file.exists()) {
								reply(550, "Requested action not taken. File unavailable (e.g., file not found, no access).");
								break;
							} else if(!file.canRead()) {
								reply(450, "Requested file action not taken. File unavailable (e.g., file busy).");
								break;
							}

							reply(125, "Data connection already open; transfer starting.");
							FileInputStream fis = new FileInputStream(file);
							DataInputStream dis = new DataInputStream(fis);
							DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
							int len = 0;
							long size = file.length();
							System.out.println("File Size: " + size + " bytes");
							while(size > 0) {
								System.out.println(size);
								//								len = dis.read(buf, 0, size > buf.length ? buf.length : (int)size);
								len = fis.read(buf, 0, size > buf.length ? buf.length : (int)size);
								/*
								for(int i = 0; i < len - 1; i++) {
									if(i != len -1 && buf[i] == '\r' && buf[i + 1] != '\n') {
										System.arraycopy(buf, i + 1, buf, i, buf.length - (i + 1));
										buf[buf.length - 1] = 0;
									}
								}
			 * /
								dos.write(buf, 0, len);
								size -= len;
							}
							dos.write(0);
							dos.flush();
							reply(226, "Closing data connection. Requested file action successful (for example, file transfer or file abort).");
							if(socket != null && !socket.isClosed()) {
								socket.close();
							}
							break;
						case "TYPE":
							reply(200, "Command okay.");
							break;
						default:
							System.out.println(line);
							break;
						}
					}
					System.out.println("End of Connection.");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if(!client.isClosed()) {
						try {
							client.close();
						} catch(Exception e) {};
					}
				}
			}

			private void reply(int code, String... format) {
				StringBuffer debug = new StringBuffer();

				try {
					debug.append(String.format("%d ", code));
					writer.append(String.format("%d ", code));

					if(format.length > 0) {
						debug.append(String.format(format[0], (Object[])Arrays.copyOfRange(format, 1, format.length)));
						writer.append(String.format(format[0], (Object[])Arrays.copyOfRange(format, 1, format.length)));
					}

					debug.append(" \r\n");
					writer.append(" \r\n");

					System.out.print(debug.toString());
					writer.flush();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});

		thread.run();
		thread.join();
			 */
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

		private void sendCode(int code) {
			sendCode(code, null);
		}

		private FTPServerDataTransferProcess createServerDTP(String host, int port) {
			FTPServerDataTransferProcess serverDTP = null;

			try {
				serverDTP = new FTPServerDataTransferProcess(host, port);

				Thread thread = new Thread(serverDTP);
				thread.start();

				sendCode(200, "OK.");
			} catch(IOException e) {
				sendCode(500, e.getMessage());
			}

			return serverDTP;
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