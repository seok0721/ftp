package localhost.ftp.command;

public enum FTPServiceCommands {

	RETRIEVE("RETR"),
	STORE("STOR"),
	STORE_UNIQUE ("STOU"),
	APPEND("APPE"), // with create
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

	private String value;

	private FTPServiceCommands(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}