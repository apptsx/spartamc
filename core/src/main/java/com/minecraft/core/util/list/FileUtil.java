package com.minecraft.core.util.list;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public class FileUtil {

    public static void copy(final File source, final File destination, Predicate<Path> predicate) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdir();
            }

            String[] files = source.list();

            if (files == null) {
                return;
            }

            for (String file : files) {
                File newSource = new File(source, file);

                if (predicate != null && !predicate.test(newSource.toPath())) {
                    continue;
                }

                File newDestination = new File(destination, file);
                copy(newSource, newDestination, predicate);
            }
        } else {
            try (InputStream inputStream = Files.newInputStream(source.toPath());
                 OutputStream outputStream = Files.newOutputStream(destination.toPath())) {

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    public static boolean delete(final File worldFile) {
        if (worldFile.exists()) {
            File[] files = worldFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        delete(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }

        return worldFile.delete();
    }
}