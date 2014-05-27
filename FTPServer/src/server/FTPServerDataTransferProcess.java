package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class FTPServerDataTransferProcess {

	private Socket socket;
	private FTPEnvironment env;

	public FTPEnvironment getEnvironment() {
		return env;
	}

	public void setEnvironment(FTPEnvironment environment) {
		this.env = environment;
	}

	public void open(String host, int port) throws Exception {
		if(socket != null && socket.isConnected()) {
			throw new Exception("Socket is already bound.");
		}

		try {
			socket = new Socket(host, port);
			socket.setReuseAddress(true);
			System.out.println(String.format("%s:%d - Success to connect client DTP", host, port));
		} catch(Exception e) {
			System.err.println(String.format("%s:%d - Failure to connect client DTP", host, port));
		}
	}

	public void close() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch(IOException e) {}
	}

	public void execute(FTPCommand ftpCommand) {
		if(ftpCommand == null) {
			System.err.println("Unknown command.");
			return;
		}

		switch(ftpCommand) {
		case NAME_LIST:
			if(ftpCommand.getArgument() == null) {
				ftpCommand.setArgument(env.getWorkingDirectory());
			}

			listDirectoryContents(("ls " + ftpCommand.getArgument()).split(" "));
			break;

		case LIST:
			if(ftpCommand.getArgument() == null) {
				ftpCommand.setArgument(env.getWorkingDirectory());
			}

			listDirectoryContents(("ls -al " + ftpCommand.getArgument()).split(" "));
			break;

		default:
			break;
		}
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

	public void assertFile(FTPCommand command) throws Exception {
		File file = new File(String.format("%s/%s", env.getWorkingDirectory(), command.getArgument()));

		System.out.println(file.getAbsolutePath());
		System.out.println(file.getPath());

		if(!file.exists()) {
			throw new Exception("File not exists.");
		}

		if(!file.isFile()) {
			throw new Exception("File type is not normal file.");
		}
	}

	public void store(FTPCommand command) throws Exception {
		File file = new File(String.format("%s/%s", env.getWorkingDirectory(), command.getArgument()));

		if(file.exists()) {
			throw new Exception("File exists.");
		}

		file.createNewFile();

		FileOutputStream out = null;
		DataInputStream in = null;

		try {
			out = new FileOutputStream(file);
			in = new DataInputStream(socket.getInputStream());

			byte[] buf = new byte[4096];
			int len = 0;

			while((len = in.read(buf, 0, 4096)) > 0) {
				out.write(buf, 0, len);
			}
		} catch(Exception e) {
			System.err.println(e.getMessage());
			throw e;
		} finally {
			try {
				out.close();
			} catch(Exception e) {}

			try {
				in.close();
			} catch(Exception e) {}
		}
	}

	public void retrieve(FTPCommand command) throws Exception {
		File file = new File(String.format("%s/%s", env.getWorkingDirectory(), command.getArgument()));

		FileInputStream in = null;
		DataOutputStream out = null;

		try {
			in = new FileInputStream(file);
			out = new DataOutputStream(socket.getOutputStream());

			byte[] buf = new byte[4096];
			int len = 0;

			while((len = in.read(buf, 0, 4096)) > 0) {
				out.write(buf, 0, len);
			}
		} catch(Exception e) {
			System.err.println(e.getMessage());
			throw e;
		} finally {
			try {
				out.close();
			} catch(Exception e) {}

			try {
				in.close();
			} catch(Exception e) {}
		}
	}
}