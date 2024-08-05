package com.github.javarar.animal.faces;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class AnimalFacesPipeline {
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {

        AnimalFacesPipeline animalFacesPipeline = new AnimalFacesPipeline();
        animalFacesPipeline.processImages();
    }

    public void processImages() throws IOException {

        ClassLoader classLoader = AnimalFacesPipeline.class.getClassLoader();
        URL resource = classLoader.getResource("images");

        try (Stream<Path> paths = Files.list(Paths.get(resource.getPath()))) {
            paths.forEach(path -> {
                Path outputDir = path.getParent().resolve("processed");
                try {
                    if (!Files.exists(outputDir)) {
                        Files.createDirectory(outputDir);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                executorService.submit(new ProcessImageTask(path));
            });
        }

        executorService.shutdown();
    }

    public static class ProcessImageTask implements Runnable {

        private final Path imagePath;

        public ProcessImageTask(Path imagePath) {
            this.imagePath = imagePath;
        }

        @Override
        public void run() {
            System.out.println("Начало обработки файла " + imagePath.getFileName().toString());
            try {
                BufferedImage grayScale = ImageIO.read(imagePath.toFile());

                ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
                op.filter(grayScale, grayScale);

                Path outputDir = imagePath.getParent().resolve("processed");

                Path outputFilePath = outputDir.resolve(imagePath.getFileName());
                ImageIO.write(grayScale, "jpg", outputFilePath.toFile());

                System.out.println("Завершение обработки файла " + imagePath.getFileName().toString());
            } catch (Exception exception) {
                System.out.println("Не удалось обработать файл " + imagePath.getFileName().toString());
                exception.printStackTrace();
            }
        }
    }
}
