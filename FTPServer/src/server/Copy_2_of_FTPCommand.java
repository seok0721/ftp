package server;

public enum Copy_2_of_FTPCommand {

	/* AccessControlCommands */
	USER_NAME,
	PASSWORD,
	ACCOUNT,
	CHANGE_WORKING_DIRECTORY,
	CHANGE_TO_PARENT_DIRECTORY,
	STRUCTURE_MOUNT,
	REINITIALIZE,
	LOGOUT,

	/* TransferParameterCommands */
	DATA_PORT,
	PASSIVE,
	REPRESENTATION_TYPE,
	FILE_STRUCTURE,
	TRANSFER_MODE,

	/* FTPServiceCommands */
	RETRIEVE,
	STORE,
	STORE_UNIQUE ,
	APPEND,
	ALLOCATE,
	RESTART,
	RENAME_FROM,
	RENAME_TO,
	ABORT,
	DELETE,
	REMOVE_DIRECTORY ,
	MAKE_DIRECTORY ,
	PRINT_WORKING_DIRECTORY,
	LIST,
	NAME_LIST,
	SITE_PARAMETERS,
	SYSTEM,
	STATUS,
	HELP,
	NOOP;

	private String arguments;

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public String getArguments() {
		return arguments;
	}
}