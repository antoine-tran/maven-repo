package tuan;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * A simple Web application launcher using the embedded Jetty Web server
 * @author tuan
 *
 */
public class JettyWebAppLauncher {

	public static void main(String[] args) throws Exception {
		Server server = new Server(Integer.parseInt(args[0]));
		WebAppContext handler = new WebAppContext();
		handler.setContextPath("/");
		handler.setWar(args[1]);
		server.setHandler(handler);
        server.start();
        server.join();
	}

}
