package localhost;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ProtocolInterpretor implements Runnable {

	private ServerSocket server;
	private int port = 10021;

	public ProtocolInterpretor() {
		System.out.println("Create Protocol Interpretor...");
		try {
			server = new ServerSocket(port);
			server.setReuseAddress(true);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public String getIpAddress() {
		return new String(server.getLocalSocketAddress().toString());
	}

	public String getPort() {
		return String.valueOf(server.getLocalPort());
	}

	@Override
	public void run() {
		while(true) {
			try {
				Socket client = server.accept();
				handleClientSocket(client);
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleClientSocket(final Socket client) throws InterruptedException, IOException {
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
						case "USER":
							reply (230, "User logged in, proceed.");
							// if(token.length == 1) {
							// 	reply(332, "Need account for login.");
							// }

							// username = token[1];
							//reply(331, "User name okay, need password.");

							/* 고정된 사용자 아이디르르 사용할 경우 이 코드 사용
							if(token[1].equals(Constants.USERNAME)) {
								reply(331, "User name okay, need password.");
							} else {
								reply(530, "Not logged in.");
							}
							 */
							break;
						case "PASS":
							reply (230, "User logged in, proceed.");
							/*
							if(token.length == 1) {

							}
							if(username == null) {
								reply(332, "Need account for login.");
							} else {
								reply (230, "User logged in, proceed.");
							}
							 */

							break;
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
								 */
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
					writer.append(" \n");

					System.out.print(debug.toString());
					writer.flush();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});

		thread.run();
		thread.join();
	}
}