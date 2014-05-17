package server;

public class FTPMain {

	public static void main(String[] args) throws Exception {
		printSystemEnvironment();

		Thread serverPI = new Thread(new FTPServerProtocolInterpreter());
		serverPI.start();
		serverPI.join();
	}

	private static void printSystemEnvironment() {
		for(String name : System.getenv().keySet()) {
			System.out.println(name + ": " + System.getenv(name));
		}
	}
}