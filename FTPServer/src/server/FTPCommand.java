package server;

public enum FTPCommand {

	/* AccessControlCommands */
	USER_NAME("USER"),
	PASSWORD("PASS"),
	ACCOUNT("ACCT"),
	CHANGE_WORKING_DIRECTORY("CWD"),
	CHANGE_TO_PARENT_DIRECTORY("CDUP"),
	STRUCTURE_MOUNT("SMNT"),
	REINITIALIZE("REIN"),
	LOGOUT("QUIT"),

	/* TransferParameterCommands */
	DATA_PORT("PORT"),
	PASSIVE("PASV"),
	REPRESENTATION_TYPE("TYPE"),
	FILE_STRUCTURE("STRU"),
	TRANSFER_MODE("MODE"),

	/* FTPServiceCommands */
	RETRIEVE("RETR"),
	STORE("STOR"),
	STORE_UNIQUE ("STOU"),
	APPEND("APPE"),
	ALLOCATE("ALLO"),
	RESTART("REST"),
	RENAME_FROM("RNFR"),
	RENAME_TO("RNTO"),
	ABORT("ABOR"),
	DELETE("DELE"),
	REMOVE_DIRECTORY ("RMD"),
	MAKE_DIRECTORY ("MKD"),
	PRINT_WORKING_DIRECTORY("PWD"),
	LIST("LIST"),
	NAME_LIST("NLST"),
	SITE_PARAMETERS("SITE"),
	SYSTEM("SYST"),
	STATUS("STAT"),
	HELP("HELP"),
	NOOP("NOOP");

	private final String command;
	private String argument;

	private FTPCommand(String value) {
		this.command = value;
	}

	public String getCommand() {
		return command;
	}

	public void setArgument(String arguments) {
		this.argument = arguments;
	}

	public String getArgument() {
		return argument;
	}
}