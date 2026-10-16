package io;

import model.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PathReader {
    public static List<Path> readPaths(String filePath) {
        List<Path> paths = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filePath), "UTF-8")) {

            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 3) {
                    throw new IllegalArgumentException("Invalid path row: " + line);
                }

                String fromLocationId = parts[0].trim();
                String toLocationId = parts[1].trim();
                double weight = Double.parseDouble(parts[2].trim());

                Path path = new Path(fromLocationId, toLocationId, weight);

                paths.add(path);
            }

        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Cannot read dataset: " + filePath, e);
        }

        return paths;
    }
}
