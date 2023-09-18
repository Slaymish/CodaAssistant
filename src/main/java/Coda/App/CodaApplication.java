package Coda.App;

import org.junit.jupiter.api.parallel.Resources;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class CodaApplication {
	private List<WebPageService> services;
	private static WebServer server;

	public CodaApplication() {
		this.services = new ArrayList<>();
	}

	/**
	 * Build a blender farm service.
	 *
	 * @return the blender farm service
	 */
	public WebPageService buildBlenderFarm() {
		return new WebPageServiceBuilder<>()
				.setTitle("Blender Farm")
				.setDescription("A simple blender farm")
				.setVersion("0.0.1")
				.setAuthor("Hamish Burke")
				.setLicense("MIT")
				.setService(BlenderFarm::renderFrame)
				.build();
	}

	public WebPageService buildAdderService(){
		return new WebPageServiceBuilder<>()
				.setTitle("Adder")
				.setDescription("A simple adder that'll add 10 to the input")
				.setVersion("0.0.1")
				.setAuthor("Hamish Burke")
				.setLicense("MIT")
				.setService((Object i) -> {
					if (i == null) {
						return null;
					}
					if (i instanceof Integer) {
						return (Integer) i + 10;
					}
					return i;
				})
				.build();
	}

	/**
	 * Main method.
	 *
	 * @param args IP and port
	 */
	public static void main(String[] args) {
		String ip;
		int port;

		if (args.length > 0) {
			ip = args[0];
			port = Integer.parseInt(args[1]);
		} else {
			ip = "localhost";
			port = 80;
		}

		CodaApplication app = new CodaApplication();

		// Setup web server
		server = new WebServer(ip, port, app);
		app.addService(app.buildBlenderFarm());
		app.addService(app.buildAdderService());


		// Start server
		server.start(port);
	}


	/**
	 * Add a service to the application.
	 *
	 * @param codaService the service to add
	 */
	private void addService(WebPageService<?, ?> codaService) {
		this.services.add(codaService);
		server.addServiceEndpoint(codaService);
	}

	/**
	 * Get the services.
	 *
	 * @return Unmodifiable list of services
	 */
	public List<WebPageService> getServices() {
		return Collections.unmodifiableList(this.services);
	}
}


