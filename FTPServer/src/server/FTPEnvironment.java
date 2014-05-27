package server;

import java.io.File;
import java.util.Iterator;
import java.util.Stack;

public class FTPEnvironment {

	private Stack<String> workingDirectory = new Stack<String>();
	private FTPDataType ftpDataType = FTPDataType.ASCII;

	public String getWorkingDirectory() {
		if(workingDirectory.size() == 0) {
			return "/";
		}

		StringBuffer buffer = new StringBuffer();

		for(Iterator<String> iter = workingDirectory.iterator(); iter.hasNext();) {
			buffer.append("/").append(iter.next());
		}

		return buffer.toString();
	}

	public void setWorkingDirectory(String path) throws Exception {
		File directory = null;

		if(path.charAt(0) == '/') {
			directory = new File(path);
		} else {
			directory = new File(String.format("%s/%s", getWorkingDirectory(), path));
		}

		if(!directory.exists()) {
			throw new Exception("Directory not exists.");
		}

		if(!directory.isDirectory()) {
			throw new Exception("Paht is not directory.");
		}

		String[] pathArray = path.split("/");

		if(pathArray.length == 0 || pathArray[0].length() == 0) {
			workingDirectory.clear();

			for(int i = 1; i < pathArray.length; i++) {
				workingDirectory.push(pathArray[i]);
			}

			return;
		}

		for(int i = 0; i < pathArray.length; i++) {
			switch(pathArray[i]) {
			case ".":
				continue;
			case "..":
				workingDirectory.pop();
				break;
			default:
				workingDirectory.push(pathArray[i]);
				break;
			}
		}
	}

	public FTPDataType getFtpDataType() {
		return ftpDataType;
	}

	public void setFtpDataType(FTPDataType ftpDataType) {
		this.ftpDataType = ftpDataType;
	}
}