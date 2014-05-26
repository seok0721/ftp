package localhost;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

public class Environment {

	private Stack<String> stack = new Stack<String>();
	private String workingDirectory = "/";

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String path) throws IllegalAccessException, IOException {
		String[] split = path.split("/");

		if(path.charAt(0) == '/') {
			workingDirectory = path;
			return;
		}

		StringBuffer buffer = new StringBuffer();

		if("..".equals(path.substring(0, 2))) {
			String[] pathArray = path.split("/");

			for(int i = 0; i < pathArray.length; i++) {
				if(i < pathArray.length - 1) {
					buffer.append("/").append(pathArray[i]);
				}
			}
			buffer.append(path.substring(2, path.length()));
		} else if (path.charAt(0) == '.') {

		} else {
			
		}

		File file = new File(String.format("%s/%s", workingDirectory, path));

		if(!file.exists()) {
			throw new IllegalAccessException("Path not exists.");
		}

		if(!file.isDirectory()) {
			throw new IllegalAccessException("Path is not directory.");
		}

		System.out.println(file.getName());
		System.out.println(file.getParent());

		workingDirectory = String.format("%s/%s", file.getParent(), file.getName());
	}
}
