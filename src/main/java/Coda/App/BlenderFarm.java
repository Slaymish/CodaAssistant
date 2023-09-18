package Coda.App;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlenderFarm {

    private static Consumer<File> renderConsumer;

    public static Consumer<File> getRenderConsumer() {
        if (renderConsumer == null) {
            renderConsumer = (File f) -> {
                // Render the file
                System.out.println("Rendered file: " + f.getAbsolutePath());
            };
        }
        return renderConsumer;
    }


    /**
     * Render a frame of a blender file.
     *
     * @param object the blender file to render
     * @return
     */
    public static Consumer<File> renderFrame(Object object){
        try{
            File blendFile = checkFile(object);
            new Thread(() -> {
                try {
                    // Render frame
                    System.out.println("Rendering frame: " + blendFile.getAbsolutePath());
                    File outputImage = createBlenderRenderProcess(blendFile);

                    if (outputImage == null) {
                        throw new Exception("Error while rendering frame.");
                    }

                    getRenderConsumer().accept(outputImage);
                } catch (Exception e) {
                    System.out.println("Thread Error: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return getRenderConsumer();
    }

    private static File createBlenderRenderProcess(File blendFile) throws InterruptedException {
        // TODO: Use processbuilder to render the frame
        Thread.sleep(2000); // fixme: remove
        return new File("test.png");
    }


    /**
     * Check if the input is a file and if it exists.
     *
     * @param o the input
     * @return the file
     * @throws Exception
     */
    private static File checkFile(Object o) throws Exception {
        if (!(o instanceof File)) {
            throw new Exception("Input is not a file.");
        }
        File blendFile = (File) o;

        if (!blendFile.exists()) {
            throw new Exception("File " + blendFile.getAbsolutePath() + " does not exist.");
        }

        // Check blendFile
        if (!blendFile.getName().endsWith(".blend")) {
            throw new Exception("File " + blendFile.getAbsolutePath() + " is not a .blend file.");
        }

        return blendFile;
    }
}