package server;

import java.util.Iterator;
import java.util.Stack;

public class FTPEnvironment {

	private Stack<String> stack = new Stack<String>();

	public String getWorkingDirectory() {
		if(stack.size() == 0) {
			return "/";
		}

		StringBuffer buffer = new StringBuffer();

		for(Iterator<String> iter = stack.iterator(); iter.hasNext();) {
			buffer.append("/").append(iter.next());
		}

		return buffer.toString();
	}

	public void setWorkingDirectory(String path) {
		String[] pathArray = path.split("/");

		if(pathArray.length == 0 || pathArray[0].length() == 0) {
			stack.clear();

			for(int i = 1; i < pathArray.length; i++) {
				stack.push(pathArray[i]);
			}

			return;
		}

		for(int i = 0; i < pathArray.length; i++) {
			switch(pathArray[i]) {
			case ".":
				continue;
			case "..":
				stack.pop();
				break;
			default:
				stack.push(pathArray[i]);
				break;
			}
		}
	}
}