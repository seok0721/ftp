package server;

public class FTPMain {

	public static void main(String[] args) throws Exception {
		Thread serverPI = new Thread(new FTPServerProtocolInterpreter());
		serverPI.start();
		serverPI.join();
	}
}