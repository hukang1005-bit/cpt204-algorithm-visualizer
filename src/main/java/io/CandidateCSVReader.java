package io;

import model.CandidateLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CandidateCSVReader {
    public static List<CandidateLocation> readCandidates(String filePath) {
        List<CandidateLocation> locations = new ArrayList<>();

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
                if (parts.length < 2) {
                    throw new IllegalArgumentException("Invalid candidate row: " + line);
                }

                String locationId = parts[0].trim();
                int priorityScore = Integer.parseInt(parts[1].trim());

                CandidateLocation location = new CandidateLocation(locationId, priorityScore);

                locations.add(location);
            }

        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Cannot read dataset: " + filePath, e);
        }

        return locations;
    }
}
