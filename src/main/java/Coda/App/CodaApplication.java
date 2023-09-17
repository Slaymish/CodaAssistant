package Coda.App;

import java.io.File;

public class CodaApplication {

	public static void main(String[] args) {
		CodaApplication app = new CodaApplication();

		WebPageService<File,File> blenderFarm = app.buildBlenderFarm();
		blenderFarm.runService(new File("test.blend"));

	}

	public WebPageService<File,File> buildBlenderFarm() {
		return (WebPageService<File, File>) new WebPageServiceBuilder<File,File>()
				.setTitle("Blender Farm")
				.setDescription("A simple blender farm")
				.setVersion("0.0.1")
				.setAuthor("Coda")
				.setLicense("MIT")
				.setService(this::runBlenderFarm)
				.build();
	}

	private Object runBlenderFarm(Object o) {
		if (o instanceof File) {
			return runBlenderFarm((File) o);
		}
		return null;
	}

	/**
	 * Run blender farm.
	 *
	 * @param blendFile .blend file
	 * @return
	 */
	private File runBlenderFarm(File blendFile) {
		if (blendFile == null) {
			return null;
		}

		if (!blendFile.exists()) {
			System.out.println("File " + blendFile.getAbsolutePath() + " does not exist.");
			return null;
		}

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return null;
			}

			System.out.println("Running blender farm on " + blendFile.getAbsolutePath());
		}

	}
}


