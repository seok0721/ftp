package server;

public enum FTPDataType {

	ASCII("A"),
	IMAGE("I"),
	EBCDIC("E");

	private final String value;

	private FTPDataType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}