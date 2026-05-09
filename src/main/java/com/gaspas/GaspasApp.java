package com.gaspas;

import com.gaspas.crypto.CryptoManager;
import com.gaspas.db.DatabaseManager;
import com.gaspas.service.PlatformService;
import com.gaspas.ui.MainWindow;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class GaspasApp {
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.gaspas";
    private static final String VERIFY_FILE = CONFIG_DIR + "/verify.enc";

    public static void main(String[] args) {
        try {
            CryptoManager crypto = new CryptoManager();
            DatabaseManager db = new DatabaseManager();

            boolean firstRun = !Files.exists(Paths.get(VERIFY_FILE));

            if (firstRun) {
                System.out.println("=== GASPAS - Premiere utilisation ===");
                String p1 = readPassword("Definissez votre master password: ");
                if (p1 == null || p1.isEmpty()) { System.out.println("Abandonne."); return; }
                String p2 = readPassword("Confirmez le master password: ");
                if (!p1.equals(p2)) { System.out.println("Les mots de passe ne correspondent pas."); return; }

                crypto.deriveKey(p1.toCharArray());
                Files.createDirectories(Paths.get(CONFIG_DIR));
                String token = crypto.encrypt("GASPAS_OK");
                Files.writeString(Paths.get(VERIFY_FILE), token);
                System.out.println("Master password enregistre.");
            } else {
                String password = readPassword("Master password: ");
                if (password == null || password.isEmpty()) { System.out.println("Abandonne."); return; }

                crypto.deriveKey(password.toCharArray());

                try {
                    String token = Files.readString(Paths.get(VERIFY_FILE));
                    String decrypted = crypto.decrypt(token);
                    if (!"GASPAS_OK".equals(decrypted)) {
                        System.out.println("Master password incorrect.");
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("Master password incorrect.");
                    return;
                }
            }

            db.initialize();

            PlatformService platformService = new PlatformService(db, crypto);

            Terminal terminal = new DefaultTerminalFactory().createTerminal();
            Screen screen = new TerminalScreen(terminal);

            MainWindow window = new MainWindow(screen, platformService);
            try {
                window.run();
            } finally {
                screen.close();
                db.close();
            }
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword(prompt);
            return pwd != null ? new String(pwd) : null;
        }
        System.out.print(prompt);
        System.out.flush();
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
}
