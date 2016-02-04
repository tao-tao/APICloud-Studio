package com.apicloud.navigator.handlers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class CustomerLoaderHelpHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {

		String url = "http://docs.apicloud.com/APICloud/%E6%8A%80%E6%9C%AF%E4%B8%93%E9%A2%98/Custom_Loader";
		
		Desktop desktop = Desktop.getDesktop();
		if ((Desktop.isDesktopSupported())
				&& (desktop.isSupported(Desktop.Action.BROWSE))) {
				URI uri = new URI(url);
				desktop.browse(uri);
		}
		}
			 catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return null;

}
	}
