package server;

import java.io.IOException;
import java.util.Properties;

public class FTPConfiguration {

	private static Properties props;
	private static int port = 10021;
	private static int bufferSize = 4096;

	static {
		props = new Properties();
		try {
			props.load(FTPConfiguration.class.getClassLoader().getResourceAsStream("server/config.properties"));
			System.out.println("Success to load config.properties");

			port = Integer.parseInt((String) props.get("port"));
			bufferSize = Integer.parseInt((String) props.get("bufferSize"));
		} catch (IOException e) {
			System.out.println("Failure to load config.properties. Use default setting value.");
		}
	}

	public static int getPort() {
		return port;
	}

	public static int getBufferSize() {
		return bufferSize;
	}

	private FTPConfiguration() {}

}