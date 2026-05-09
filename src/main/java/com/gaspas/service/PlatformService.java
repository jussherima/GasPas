package com.gaspas.service;

import com.gaspas.crypto.CryptoManager;
import com.gaspas.db.DatabaseManager;
import com.gaspas.model.Field;
import com.gaspas.model.Platform;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlatformService {
    private final DatabaseManager db;
    private final CryptoManager crypto;
    private final Gson gson = new Gson();

    public PlatformService(DatabaseManager db, CryptoManager crypto) {
        this.db = db;
        this.crypto = crypto;
    }

    public void createPlatform(String name, List<Field> fields) throws Exception {
        String json = gson.toJson(fields);
        String encrypted = crypto.encrypt(json);
        db.insertPlatform(name, encrypted);
    }

    public Platform getPlatform(String name) throws Exception {
        String encrypted = db.getEncryptedData(name);
        if (encrypted == null) return null;
        String json = crypto.decrypt(encrypted);
        Type listType = new TypeToken<ArrayList<Field>>() {}.getType();
        List<Field> fields = gson.fromJson(json, listType);
        Platform platform = new Platform(name, fields);
        return platform;
    }

    public List<String> searchPlatforms(String query) throws Exception {
        List<String> all = db.getAllPlatformNames();
        if (query == null || query.isEmpty()) return all;
        String q = query.toLowerCase();
        return all.stream()
                  .filter(name -> name.toLowerCase().contains(q))
                  .toList();
    }

    public void updatePlatform(String name, List<Field> fields) throws Exception {
        String json = gson.toJson(fields);
        String encrypted = crypto.encrypt(json);
        db.updatePlatform(name, encrypted);
    }

    public void deletePlatform(String name) throws Exception {
        db.deletePlatform(name);
    }

    public boolean platformExists(String name) throws Exception {
        return db.platformExists(name);
    }
}
