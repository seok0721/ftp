package localhost;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(String.valueOf(0xFF00FF00).getBytes().length);
		System.out.println(String.valueOf(0).getBytes().length);
//		Thread serverPI = new Thread(new ProtocolInterpretor());
//		serverPI.start();
//		serverPI.join();
	}
}