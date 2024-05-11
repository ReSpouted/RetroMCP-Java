package org.mcphackers.mcp.tools;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class ResourceURLHandler extends URLStreamHandler {

	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		String path = u.getPath();
		// gradle handles .jar files as dependencies even if they're in resources, so all .jar files will have to be
		// named .jar.resource instead.
		if(path.endsWith(".jar")) {
			path += ".resource";
		}
		URL resourceUrl = getClass().getResource(path);
		if(resourceUrl == null) {
			throw new IOException("The resource '" + u.getPath() + "' does not exist.");
		}
		return resourceUrl.openConnection();
	}
}
