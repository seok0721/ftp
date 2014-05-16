package localhost.ftp.command;

public enum AccessControlCommands {

	USER_NAME("USER"),
	PASSWORD("PASS"),
	ACCOUNT("ACCT"),
	CHANGE_WORKING_DIRECTORY("CWD"),
	CHANGE_TO_PARENT_DIRECTORY("CDUP"),
	STRUCTURE_MOUNT("SMNT"),
	REINITIALIZE("REIN"),
	LOGOUT("QUIT");

	private String value;

	private AccessControlCommands(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}