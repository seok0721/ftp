package localhost;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		Thread serverPI = new Thread(new ProtocolInterpretor());
		serverPI.start();
		serverPI.join();
	}
}