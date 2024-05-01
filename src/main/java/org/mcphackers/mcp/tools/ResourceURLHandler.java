package org.mcphackers.mcp.tools;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class ResourceURLHandler extends URLStreamHandler {

	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		URL resourceUrl = getClass().getResource(u.getPath());
		if(resourceUrl == null) {
			throw new IOException("The resource '" + u.getPath() + "' does not exist.");
		}
		return resourceUrl.openConnection();
	}
}
