package localhost.ftp.command;

public enum TransferParameterCommands {

	DATA_PORT("PORT"),
	PASSIVE("PASV"),
	REPRESENTATION_TYPE("TYPE"),
	FILE_STRUCTURE("STRU"),
	TRANSFER_MODE("MODE");

	private String value;

	private TransferParameterCommands(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}