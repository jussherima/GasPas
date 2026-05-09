package com.gaspas.ui;

import com.gaspas.model.Field;
import com.gaspas.model.Platform;
import com.gaspas.service.PasswordGenerator;
import com.gaspas.service.PlatformService;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainWindow {

    private enum ViewState { LIST, DETAIL, SEARCH }

    private final Screen screen;
    private final PlatformService platformService;
    private final PasswordGenerator passwordGenerator = new PasswordGenerator();

    private final AtomicBoolean dirty = new AtomicBoolean(true);
    private volatile boolean running = true;

    private ViewState viewState = ViewState.LIST;
    private List<String> platforms = new ArrayList<>();
    private List<String> filteredPlatforms = new ArrayList<>();
    private int selectedIndex = 0;
    private String statusMessage = "Bienvenue dans GASPAS.";

    // Detail view state
    private Platform currentPlatform = null;
    private int detailFieldIndex = 0;
    private final Set<Integer> revealedFields = new HashSet<>();

    // Search state
    private String searchQuery = "";

    public MainWindow(Screen screen, PlatformService platformService) {
        this.screen = screen;
        this.platformService = platformService;
    }

    public void run() throws IOException {
        screen.startScreen();
        screen.setCursorPosition(null);
        refreshPlatforms();
        try {
            while (running) {
                if (dirty.compareAndSet(true, false)) {
                    render();
                }
                KeyStroke key = screen.pollInput();
                if (key != null) {
                    handleKey(key);
                    dirty.set(true);
                } else {
                    try { Thread.sleep(80); } catch (InterruptedException ignored) {}
                }
            }
        } finally {
            screen.stopScreen();
        }
    }

    private void refreshPlatforms() {
        try {
            platforms = platformService.searchPlatforms(null);
            filteredPlatforms = new ArrayList<>(platforms);
        } catch (Exception e) {
            statusMessage = "Erreur: " + e.getMessage();
        }
    }

    // ─── Key handling ───

    private void handleKey(KeyStroke key) throws IOException {
        if (key.getKeyType() == KeyType.EOF) { running = false; return; }

        switch (viewState) {
            case LIST -> handleListKey(key);
            case DETAIL -> handleDetailKey(key);
            case SEARCH -> handleSearchKey(key);
        }
    }

    private void handleListKey(KeyStroke key) throws IOException {
        Character ch = key.getCharacter();
        if (ch != null) {
            switch (Character.toLowerCase(ch)) {
                case 'q' -> { running = false; return; }
                case 'a' -> { promptCreatePlatform(); return; }
                case 'd' -> { deleteSelectedPlatform(); return; }
                case '/' -> { enterSearch(); return; }
                case 'g' -> { generateAndShowPassword(); return; }
                default -> {}
            }
        }
        if (key.getKeyType() == KeyType.ArrowUp) {
            if (!platforms.isEmpty()) selectedIndex = Math.max(0, selectedIndex - 1);
        } else if (key.getKeyType() == KeyType.ArrowDown) {
            if (!platforms.isEmpty()) selectedIndex = Math.min(platforms.size() - 1, selectedIndex + 1);
        } else if (key.getKeyType() == KeyType.Enter) {
            openSelectedPlatform();
        }
    }

    private void handleDetailKey(KeyStroke key) throws IOException {
        Character ch = key.getCharacter();
        if (ch != null) {
            switch (Character.toLowerCase(ch)) {
                case 'e' -> { editSelectedField(); return; }
                case 'a' -> { addFieldToCurrentPlatform(); return; }
                case 'd' -> { deleteSelectedField(); return; }
                case 'x' -> { deleteCurrentPlatform(); return; } // changed from 'd' to 'x' to avoid conflict with field delete
                case 'g' -> { generatePassForSelectedField(); return; }
                case 'v' -> { toggleFieldVisibility(); return; }
                case 'c' -> { copySelectedField(); return; }
                default -> {}
            }
        }
        if (key.getKeyType() == KeyType.Escape) {
            viewState = ViewState.LIST;
            currentPlatform = null;
        } else if (key.getKeyType() == KeyType.ArrowUp) {
            if (currentPlatform != null && !currentPlatform.getFields().isEmpty())
                detailFieldIndex = Math.max(0, detailFieldIndex - 1);
        } else if (key.getKeyType() == KeyType.ArrowDown) {
            if (currentPlatform != null && !currentPlatform.getFields().isEmpty())
                detailFieldIndex = Math.min(currentPlatform.getFields().size() - 1, detailFieldIndex + 1);
        } else if (key.getKeyType() == KeyType.Enter) {
            toggleFieldVisibility();
        }
    }

    private void handleSearchKey(KeyStroke key) throws IOException {
        if (key.getKeyType() == KeyType.Escape) {
            viewState = ViewState.LIST;
            searchQuery = "";
            refreshPlatforms();
            return;
        }
        if (key.getKeyType() == KeyType.Enter) {
            if (!filteredPlatforms.isEmpty() && selectedIndex < filteredPlatforms.size()) {
                String name = filteredPlatforms.get(selectedIndex);
                openPlatform(name);
            }
            return;
        }
        if (key.getKeyType() == KeyType.ArrowUp) {
            if (!filteredPlatforms.isEmpty()) selectedIndex = Math.max(0, selectedIndex - 1);
            return;
        }
        if (key.getKeyType() == KeyType.ArrowDown) {
            if (!filteredPlatforms.isEmpty()) selectedIndex = Math.min(filteredPlatforms.size() - 1, selectedIndex + 1);
            return;
        }
        if (key.getKeyType() == KeyType.Backspace) {
            if (!searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                updateSearch();
            }
            return;
        }
        if (key.getCharacter() != null) {
            searchQuery += key.getCharacter();
            updateSearch();
        }
    }

    private void updateSearch() {
        try {
            filteredPlatforms = platformService.searchPlatforms(searchQuery);
            selectedIndex = 0;
        } catch (Exception e) {
            statusMessage = "Erreur: " + e.getMessage();
        }
    }

    // ─── Actions ───

    private void openSelectedPlatform() {
        if (platforms.isEmpty()) return;
        if (selectedIndex < 0 || selectedIndex >= platforms.size()) return;
        openPlatform(platforms.get(selectedIndex));
    }

    private void openPlatform(String name) {
        try {
            currentPlatform = platformService.getPlatform(name);
            if (currentPlatform != null) {
                viewState = ViewState.DETAIL;
                detailFieldIndex = 0;
                revealedFields.clear();
                statusMessage = "Plateforme: " + name;
            }
        } catch (Exception e) {
            statusMessage = "Erreur: " + e.getMessage();
        }
    }

    private void enterSearch() {
        viewState = ViewState.SEARCH;
        searchQuery = "";
        selectedIndex = 0;
        filteredPlatforms = new ArrayList<>(platforms);
    }

    private void promptCreatePlatform() throws IOException {
        String name = readLineModal("Nom de la plateforme: ");
        if (name == null || name.isBlank()) return;
        name = name.trim();

        try {
            if (platformService.platformExists(name)) {
                statusMessage = "Cette plateforme existe deja.";
                return;
            }
        } catch (Exception e) {
            statusMessage = "Erreur: " + e.getMessage();
            return;
        }

        List<Field> fields = new ArrayList<>();
        while (true) {
            String type = readLineModal("Type (vide pour finir): ");
            if (type == null || type.isBlank()) break;
            String value = readLineModal("Valeur pour '" + type.trim() + "': ");
            if (value == null) break;
            fields.add(new Field(type.trim(), value));
        }

        try {
            platformService.createPlatform(name, fields);
            refreshPlatforms();
            statusMessage = "Plateforme '" + name + "' creee.";
        } catch (Exception e) {
            statusMessage = "Erreur: " + e.getMessage();
        }
    }

    private void addFieldToCurrentPlatform() throws IOException {
        if (currentPlatform == null) return;
        
        String type = readLineModal("Nouveau type: ");
        if (type == null || type.isBlank()) return;
        
        String value = readLineModal("Valeur pour '" + type.trim() + "': ");
        if (value == null) return;
        
        List<Field> fields = new ArrayList<>(currentPlatform.getFields());
        fields.add(new Field(type.trim(), value));
        
        try {
            platformService.updatePlatform(currentPlatform.getName(), fields);
            currentPlatform = platformService.getPlatform(currentPlatform.getName());
            detailFieldIndex = fields.size() - 1; // Select the new field
            statusMessage = "Champ ajoute.";
        } catch (Exception e) {
            statusMessage = "Erreur: " + e.getMessage();
        }
    }

    private void editSelectedField() throws IOException {
        if (currentPlatform == null || currentPlatform.getFields().isEmpty() || detailFieldIndex < 0 || detailFieldIndex >= currentPlatform.getFields().size()) return;
        
        Field f = currentPlatform.getFields().get(detailFieldIndex);
        String newType = readLineModal("Nouveau type [" + f.getType() + "]: ");
        if (newType == null) return;
        if (newType.isBlank()) newType = f.getType();
        
        String newValue = readLineModal("Nouvelle valeur [" + f.getValue() + "]: ");
        if (newValue == null) return;
        if (newValue.isEmpty()) newValue = f.getValue(); // allow empty strings if explicitly typed, but empty enter means keep old
        // Actually, let's say if they just hit enter, keep old. If they want to clear, they can't with this simple UI, but usually they want to update.
        if (newValue.isBlank() && !f.getValue().isBlank()) newValue = f.getValue(); // slight compromise

        List<Field> fields = new ArrayList<>(currentPlatform.getFields());
        fields.set(detailFieldIndex, new Field(newType.trim(), newValue));
        
        try {
            platformService.updatePlatform(currentPlatform.getName(), fields);
            currentPlatform = platformService.getPlatform(currentPlatform.getName());
            statusMessage = "Champ mis a jour.";
        } catch (Exception e) {
            statusMessage = "Erreur: " + e.getMessage();
        }
    }

    private void deleteSelectedField() throws IOException {
        if (currentPlatform == null || currentPlatform.getFields().isEmpty() || detailFieldIndex < 0 || detailFieldIndex >= currentPlatform.getFields().size()) return;
        
        if (!confirmModal("Supprimer ce champ ?")) {
            statusMessage = "Suppression annulee.";
            return;
        }
        
        List<Field> fields = new ArrayList<>(currentPlatform.getFields());
        fields.remove(detailFieldIndex);
        
        try {
            platformService.updatePlatform(currentPlatform.getName(), fields);
            currentPlatform = platformService.getPlatform(currentPlatform.getName());
            if (detailFieldIndex >= fields.size()) detailFieldIndex = Math.max(0, fields.size() - 1);
            statusMessage = "Champ supprime.";
        } catch (Exception e) {
            statusMessage = "Erreur: " + e.getMessage();
        }
    }

    private void generatePassForSelectedField() throws IOException {
        if (currentPlatform == null || currentPlatform.getFields().isEmpty() || detailFieldIndex < 0 || detailFieldIndex >= currentPlatform.getFields().size()) return;
        
        String lenStr = readLineModal("Longueur (defaut 16): ");
        if (lenStr == null) return; // cancelled
        
        int len = 16;
        if (!lenStr.isBlank()) {
            try { len = Integer.parseInt(lenStr.trim()); } catch (NumberFormatException ignored) {}
        }
        
        String password = passwordGenerator.generateStrong(Math.max(8, Math.min(len, 128)));
        
        List<Field> fields = new ArrayList<>(currentPlatform.getFields());
        Field f = fields.get(detailFieldIndex);
        fields.set(detailFieldIndex, new Field(f.getType(), password));
        
        try {
            platformService.updatePlatform(currentPlatform.getName(), fields);
            currentPlatform = platformService.getPlatform(currentPlatform.getName());
            statusMessage = "Password genere et sauvegarde.";
        } catch (Exception e) {
            statusMessage = "Erreur: " + e.getMessage();
        }
    }

    private void toggleFieldVisibility() {
        if (currentPlatform == null || currentPlatform.getFields().isEmpty()) return;
        if (revealedFields.contains(detailFieldIndex)) {
            revealedFields.remove(detailFieldIndex);
        } else {
            revealedFields.add(detailFieldIndex);
        }
    }

    private void copySelectedField() {
        if (currentPlatform == null || currentPlatform.getFields().isEmpty() || detailFieldIndex < 0 || detailFieldIndex >= currentPlatform.getFields().size()) return;
        String text = currentPlatform.getFields().get(detailFieldIndex).getValue();
        
        try {
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(text), null);
            statusMessage = "Copie dans le presse-papier !";
            return;
        } catch (Exception e) {
            // Fallback for headless environments
            String os = System.getProperty("os.name").toLowerCase();
            try {
                ProcessBuilder pb;
                if (os.contains("mac")) {
                    pb = new ProcessBuilder("pbcopy");
                } else if (os.contains("win")) {
                    pb = new ProcessBuilder("clip");
                } else {
                    if (System.getenv("WAYLAND_DISPLAY") != null) {
                        pb = new ProcessBuilder("wl-copy");
                    } else {
                        pb = new ProcessBuilder("xclip", "-selection", "clipboard");
                    }
                }
                Process p = pb.start();
                p.getOutputStream().write(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                p.getOutputStream().close();
                p.waitFor();
                statusMessage = "Copie dans le presse-papier (cli) !";
            } catch (Exception ex) {
                statusMessage = "Impossible de copier: " + ex.getMessage();
            }
        }
    }

    private boolean confirmModal(String prompt) throws IOException {
        String res = readLineModal(prompt + " [y/N]: ");
        return res != null && res.trim().equalsIgnoreCase("y");
    }

    private void deleteSelectedPlatform() throws IOException {
        if (platforms.isEmpty()) return;
        if (selectedIndex < 0 || selectedIndex >= platforms.size()) return;
        String name = platforms.get(selectedIndex);
        
        if (!confirmModal("Supprimer la plateforme '" + name + "' ?")) {
            statusMessage = "Suppression annulee.";
            return;
        }
        
        try {
            platformService.deletePlatform(name);
            refreshPlatforms();
            if (selectedIndex >= platforms.size()) selectedIndex = Math.max(0, platforms.size() - 1);
            statusMessage = "'" + name + "' supprimee.";
        } catch (Exception e) {
            statusMessage = "Erreur: " + e.getMessage();
        }
    }

    private void deleteCurrentPlatform() throws IOException {
        if (currentPlatform == null) return;
        String name = currentPlatform.getName();
        
        if (!confirmModal("Supprimer la plateforme '" + name + "' ?")) {
            statusMessage = "Suppression annulee.";
            return;
        }
        
        try {
            platformService.deletePlatform(name);
            refreshPlatforms();
            currentPlatform = null;
            viewState = ViewState.LIST;
            if (selectedIndex >= platforms.size()) selectedIndex = Math.max(0, platforms.size() - 1);
            statusMessage = "'" + name + "' supprimee.";
        } catch (Exception e) {
            statusMessage = "Erreur: " + e.getMessage();
        }
    }

    private void generateAndShowPassword() throws IOException {
        String lenStr = readLineModal("Longueur (defaut 16): ");
        int len = 16;
        if (lenStr != null && !lenStr.isBlank()) {
            try { len = Integer.parseInt(lenStr.trim()); } catch (NumberFormatException ignored) {}
        }
        String password = passwordGenerator.generateStrong(Math.max(8, Math.min(len, 128)));
        statusMessage = "Password genere: " + password;
    }

    // ─── Rendering ───

    private void render() throws IOException {
        screen.doResizeIfNecessary();
        screen.clear();
        TerminalSize size = screen.getTerminalSize();
        TextGraphics g = screen.newTextGraphics();

        int w = size.getColumns();
        int h = size.getRows();

        drawBorder(g, 0, 0, w, h, "");

        switch (viewState) {
            case LIST -> renderListView(g, w, h);
            case DETAIL -> renderDetailView(g, w, h);
            case SEARCH -> renderSearchView(g, w, h);
        }

        screen.refresh();
    }

    private void renderListView(TextGraphics g, int w, int h) {
        int bannerHeight = Banner.HEIGHT + 1;
        int helpHeight = 3;
        int statsHeight = 3;

        int bannerY = 1;
        int sep1Y = bannerY + bannerHeight;
        int listY = sep1Y + 1;
        int sep3Y = h - helpHeight - 1;
        int statsY = sep3Y - statsHeight;
        int sep2Y = statsY - 1;
        int listHeight = sep2Y - listY;

        renderBanner(g, 1, bannerY, w - 2);
        drawHorizontal(g, 0, sep1Y, w);
        renderPlatformList(g, 1, listY, w - 2, listHeight, platforms);
        drawHorizontal(g, 0, sep2Y, w);
        renderStats(g, 1, statsY, w - 2);
        drawHorizontal(g, 0, sep3Y, w);
        renderListHelp(g, 1, h - helpHeight, w - 2);
    }

    private void renderDetailView(TextGraphics g, int w, int h) {
        if (currentPlatform == null) return;

        int helpHeight = 3;
        int titleY = 1;
        int sep1Y = 2;
        int fieldsY = 3;
        int sep2Y = h - helpHeight - 1;
        int fieldsHeight = sep2Y - fieldsY;

        // Title
        String title = "=== " + currentPlatform.getName() + " ===";
        g.setForegroundColor(TextColor.ANSI.CYAN_BRIGHT);
        g.putString(centerX(1, w - 2, title.length()), titleY, title, SGR.BOLD);

        drawHorizontal(g, 0, sep1Y, w);

        // Fields
        List<Field> fields = currentPlatform.getFields();
        if (fields.isEmpty()) {
            g.setForegroundColor(TextColor.ANSI.WHITE);
            g.putString(2, fieldsY + 1, "(aucun champ)");
        } else {
            g.setForegroundColor(TextColor.ANSI.WHITE);
            g.putString(2, fieldsY, padRight("TYPE", 22) + "VALEUR", SGR.BOLD);
            g.setForegroundColor(TextColor.ANSI.BLACK_BRIGHT);
            g.putString(2, fieldsY + 1, padRight("", w - 4).replace(' ', '·'));

            int maxVisible = fieldsHeight - 2;
            int start = Math.max(0, Math.min(detailFieldIndex - maxVisible / 2, fields.size() - maxVisible));
            if (start < 0) start = 0;

            for (int i = 0; i < maxVisible && start + i < fields.size(); i++) {
                Field f = fields.get(start + i);
                int actualIndex = start + i;
                boolean isSelected = actualIndex == detailFieldIndex;
                boolean isRevealed = revealedFields.contains(actualIndex);
                String displayValue = isRevealed ? f.getValue() : "••••••••••••";
                String line = padRight(f.getType(), 22) + displayValue;

                if (isSelected) {
                    g.setForegroundColor(TextColor.ANSI.BLACK);
                    g.setBackgroundColor(TextColor.ANSI.WHITE);
                    g.putString(2, fieldsY + 2 + i, padRight("▶ " + line, w - 4));
                } else {
                    g.setForegroundColor(TextColor.ANSI.GREEN_BRIGHT);
                    g.setBackgroundColor(TextColor.ANSI.DEFAULT);
                    g.putString(2, fieldsY + 2 + i, "  " + line);
                }
            }
            g.setBackgroundColor(TextColor.ANSI.DEFAULT);
        }

        drawHorizontal(g, 0, sep2Y, w);

        // Help
        g.setForegroundColor(TextColor.ANSI.BLACK_BRIGHT);
        g.putString(2, h - helpHeight, "[a] add   [e] edit   [d] del   [v/enter] toggle visible");
        g.putString(2, h - helpHeight + 1, "[g] gen pass   [c] copy pass   [x] del platform   [esc] retour");
    }

    private void renderSearchView(TextGraphics g, int w, int h) {
        int helpHeight = 2;
        int titleY = 1;
        int sep1Y = 2;
        int inputY = 3;
        int sep2Y = 5;
        int listY = 6;
        int sep3Y = h - helpHeight - 1;
        int listHeight = sep3Y - listY;

        // Title
        g.setForegroundColor(TextColor.ANSI.CYAN_BRIGHT);
        g.putString(2, titleY, "=== Recherche ===", SGR.BOLD);

        drawHorizontal(g, 0, sep1Y, w);

        // Search input
        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.putString(2, inputY, "Recherche: ");
        g.setForegroundColor(TextColor.ANSI.YELLOW);
        g.putString(13, inputY, searchQuery + "█");

        g.setForegroundColor(TextColor.ANSI.BLACK_BRIGHT);
        g.putString(2, inputY + 1, filteredPlatforms.size() + " resultat(s)");

        drawHorizontal(g, 0, sep2Y, w);

        // Results list
        renderPlatformList(g, 1, listY, w - 2, listHeight, filteredPlatforms);

        drawHorizontal(g, 0, sep3Y, w);

        // Help
        g.setForegroundColor(TextColor.ANSI.BLACK_BRIGHT);
        g.putString(2, h - helpHeight, "[enter] ouvrir   [↑↓] nav   [esc] retour");
    }

    private void renderBanner(TextGraphics g, int x, int y, int w) {
        int bx = centerX(x, w, Banner.width());
        g.setForegroundColor(TextColor.ANSI.CYAN_BRIGHT);
        Banner.draw(g, bx, y);
        g.setForegroundColor(TextColor.ANSI.BLACK_BRIGHT);
        String tag = Banner.tagline();
        g.putString(centerX(x, w, tag.length()), y + Banner.HEIGHT, tag);
    }

    private void renderPlatformList(TextGraphics g, int x, int y, int w, int h, List<String> list) {
        g.setForegroundColor(TextColor.ANSI.WHITE_BRIGHT);
        g.putString(x, y, "Plateformes" + (viewState == ViewState.LIST ? " ◀" : ""), SGR.BOLD);

        if (list.isEmpty()) {
            g.setForegroundColor(TextColor.ANSI.WHITE);
            g.putString(x, y + 2, "(aucune — appuie sur [a] pour en ajouter)");
            return;
        }

        int max = h - 1;
        int start = Math.max(0, Math.min(selectedIndex - max / 2, list.size() - max));
        if (start < 0) start = 0;
        for (int i = 0; i < max && start + i < list.size(); i++) {
            String name = list.get(start + i);
            boolean isSelected = (start + i) == selectedIndex;

            String marker = isSelected ? "▶ " : "  ";
            String line = marker + "■ " + name;

            if (isSelected) {
                g.setForegroundColor(TextColor.ANSI.BLACK);
                g.setBackgroundColor(TextColor.ANSI.WHITE);
            } else {
                g.setForegroundColor(TextColor.ANSI.WHITE);
                g.setBackgroundColor(TextColor.ANSI.DEFAULT);
            }
            g.putString(x, y + 1 + i, padRight(line, w));
        }
        g.setBackgroundColor(TextColor.ANSI.DEFAULT);
    }

    private void renderStats(TextGraphics g, int x, int y, int w) {
        g.setForegroundColor(TextColor.ANSI.CYAN);
        g.putString(x, y, platforms.size() + " plateforme(s) enregistree(s)");
        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.putString(x, y + 1, truncate(statusMessage, w));
    }

    private void renderListHelp(TextGraphics g, int x, int y, int w) {
        g.setForegroundColor(TextColor.ANSI.BLACK_BRIGHT);
        g.putString(x, y,     "[a] ajouter   [enter] ouvrir   [d] supprimer   [/] rechercher");
        g.putString(x, y + 1, "[g] gen password   [↑↓] naviguer   [q] quitter");
    }

    // ─── Drawing helpers ───

    private void drawBorder(TextGraphics g, int x, int y, int w, int h, String title) {
        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.drawLine(x, y, x + w - 1, y, '─');
        g.drawLine(x, y + h - 1, x + w - 1, y + h - 1, '─');
        g.drawLine(x, y, x, y + h - 1, '│');
        g.drawLine(x + w - 1, y, x + w - 1, y + h - 1, '│');
        g.setCharacter(x, y, '┌');
        g.setCharacter(x + w - 1, y, '┐');
        g.setCharacter(x, y + h - 1, '└');
        g.setCharacter(x + w - 1, y + h - 1, '┘');
        if (!title.isEmpty()) g.putString(x + 2, y, title, SGR.BOLD);
    }

    private void drawHorizontal(TextGraphics g, int x, int y, int w) {
        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.drawLine(x + 1, y, x + w - 2, y, '─');
        g.setCharacter(x, y, '├');
        g.setCharacter(x + w - 1, y, '┤');
    }

    private int centerX(int x, int w, int strLen) {
        return x + Math.max(0, (w - strLen) / 2);
    }

    private String truncate(String s, int max) {
        if (max <= 0) return "";
        return s.length() <= max ? s : s.substring(0, Math.max(0, max - 1)) + "…";
    }

    private String padRight(String s, int w) {
        if (s.length() >= w) return s.substring(0, w);
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < w) sb.append(' ');
        return sb.toString();
    }

    // ─── Modal text input ───

    private String readLineModal(String prompt) throws IOException {
        TerminalSize size = screen.getTerminalSize();
        int boxW = Math.min(size.getColumns() - 4, 60);
        int boxH = 5;
        int x = (size.getColumns() - boxW) / 2;
        int y = (size.getRows() - boxH) / 2;

        TextGraphics g = screen.newTextGraphics();
        g.setBackgroundColor(TextColor.ANSI.BLACK);
        for (int i = 0; i < boxH; i++) {
            g.putString(x, y + i, padRight("", boxW));
        }
        drawBorder(g, x, y, boxW, boxH, " Saisie ");
        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.putString(x + 2, y + 1, truncate(prompt, boxW - 4));
        screen.refresh();

        StringBuilder sb = new StringBuilder();
        int inputX = x + 2;
        int inputY = y + 2;
        int maxLen = boxW - 4;
        screen.setCursorPosition(new TerminalPosition(inputX, inputY));

        while (true) {
            KeyStroke k = screen.readInput();
            if (k.getKeyType() == KeyType.Escape) {
                screen.setCursorPosition(null);
                dirty.set(true);
                return null;
            }
            if (k.getKeyType() == KeyType.Enter) {
                screen.setCursorPosition(null);
                dirty.set(true);
                return sb.toString();
            }
            if (k.getKeyType() == KeyType.Backspace && sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            } else if (k.getCharacter() != null && sb.length() < maxLen) {
                sb.append(k.getCharacter());
            }
            g.setForegroundColor(TextColor.ANSI.YELLOW);
            g.putString(x + 2, inputY, padRight(sb.toString(), maxLen));
            screen.setCursorPosition(new TerminalPosition(inputX + Math.min(sb.length(), maxLen), inputY));
            screen.refresh();
        }
    }
}
