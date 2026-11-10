package com.legenkiy.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legenkiy.storage.StorageService;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HistoryService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StorageService storageService;

    public HistoryService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void saveToStorage(String value) {
        try {
            storageService.saveTextToHistory(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JList<String> getListFromHistory() {
        String json = storageService.fetchFiles(1);

        List<String> list = new ArrayList<>();

        Iterator<String> lines = json.lines().iterator();

        while (lines.hasNext()) {
            String line = lines.next();
            if (line.isBlank()) {
                continue;
            }
            try {
                String text = objectMapper.readTree(line).get("text").asText();
                list.add(text);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        DefaultListModel<String> model = new DefaultListModel<>();
        list.forEach(model::addElement);
        return new JList<>(model);
    }
}
