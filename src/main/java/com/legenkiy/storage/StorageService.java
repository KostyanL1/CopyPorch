package com.legenkiy.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.legenkiy.exception.StorageException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class StorageService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final DateTimeFormatter FILE_TIMESTAMP = new DateTimeFormatterBuilder()
            .appendInstant(9).toFormatter();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path directory;
    private Path currentFile;

    public StorageService(Properties properties) throws IOException {
        this.directory = Path.of(properties.getProperty("PATH"));
        Files.createDirectories(directory);
        List<Path> files = processFileList();
        this.currentFile = files.isEmpty() ? newHistoryFile() : files.getFirst();
    }

    public synchronized void saveTextToHistory(String text) throws IOException {
        String json = objectMapper.writeValueAsString(Map.of("text", text));
        byte[] entry = (json + "\n").getBytes(StandardCharsets.UTF_8);

        if (entry.length > MAX_BYTES) {
            throw new StorageException("The text size exceeds the maximum allowed for saving.");
        }
        processSaving(entry);

    }

    public synchronized String fetchFiles(int numberOfPage) throws IOException {
            List<Path> files = processFileList();
            if (numberOfPage < 1 || numberOfPage > files.size()) return "";
            return Files.readString(files.get(numberOfPage - 1));
    }

    private void processSaving(byte[] entry) throws IOException {
        while (true) {
            Path file = currentFile;

            long size = Files.exists(file) ? Files.size(file) : 0;

            if (size + entry.length <= MAX_BYTES) {
                Files.write(file, entry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                break;
            }
            currentFile = newHistoryFile();
        }
    }

    private Path newHistoryFile() {
        String timestamp = FILE_TIMESTAMP.format(Instant.now()).replace(':', '-');
        return directory.resolve(timestamp + ".jsonl");
    }

    private List<Path> processFileList() throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".jsonl"))
                    .sorted(Comparator.reverseOrder())
                    .toList();
        }
    }

}
