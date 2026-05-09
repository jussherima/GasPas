# 🛡️ GASPAS (Gérer ASses PASswords)

**A beautifully simple, incredibly fast, completely offline CLI Password Manager.**

![GASPAS TUI Banner](https://via.placeholder.com/800x400.png?text=Add+a+Screenshot+of+GASPAS+Main+Screen+Here)

GASPAS is a terminal-based password manager built for developers and power users who love staying in their CLI. Built with Java and Lanterna, it features a blazingly fast Terminal User Interface (TUI) inspired by the best CLI tools, military-grade encryption, and zero cloud dependency. 

Your secrets never leave your machine.

---

## ✨ Features

- **🚀 Keyboard-First TUI:** Completely mouse-free experience. Navigate, search, copy, and edit with VIM-like speed using intuitive keyboard shortcuts.
- **🔒 Zero-Knowledge Encryption:** Data is encrypted locally using **AES-256-GCM** derived from your Master Password via **PBKDF2**.
- **📋 Instant Clipboard Copy:** Copy passwords securely to your clipboard with a single keystroke (`[c]`), without ever revealing them on screen.
- **🙈 Visual Masking:** Passwords are obfuscated (`••••••••••••`) by default. Toggle visibility at will to protect against shoulder surfing.
- **⚡ Real-time Search:** Instantly filter through your platforms as you type.
- **🔑 Built-in Generator:** Generate cryptographically strong, random passwords on the fly.
- **💾 Local SQLite Storage:** Simple, portable, and entirely offline database (`~/.gaspas/gaspas.db`).

## 📸 Screenshots

*(Replace these placeholder links with actual screenshots of your application in action)*

| Main Platform List | Field Editor & Actions |
| :---: | :---: |
| ![Main Screen](https://via.placeholder.com/400x250.png?text=Screenshot:+Platform+List) | ![Detail Screen](https://via.placeholder.com/400x250.png?text=Screenshot:+Detail+View) |
| *Fast, fuzzy searching across all platforms* | *Masked fields, instant copy, and inline editing* |

## 🚀 Getting Started

### Prerequisites
- **Java 17** or higher
- **Maven** (for building from source)
- A terminal emulator

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/gaspas.git
   cd gaspas
   ```

2. Build the executable Fat-JAR:
   ```bash
   mvn clean package -DskipTests
   ```

3. Run the application:
   ```bash
   java -jar target/gaspas-1.0.0.jar
   ```

*(Optional: Add an alias to your `~/.bashrc` or `~/.zshrc` for global access!)*
```bash
alias gaspas="java -jar /path/to/gaspas/target/gaspas-1.0.0.jar"
```

## 🎮 Keyboard Shortcuts

### Global / List View
- `[a]` - Add a new platform
- `[/]` - Search platforms
- `[Enter]` - Open the selected platform
- `[↑] / [↓]` - Navigate list
- `[d]` - Delete selected platform
- `[q]` - Quit application

### Detail View (Inside a Platform)
- `[c]` - Copy selected value to clipboard
- `[v]` or `[Enter]` - Toggle password visibility
- `[e]` - Edit the currently selected field
- `[a]` - Add a new field (type/value pair)
- `[g]` - Generate a strong password for the selected field
- `[d]` - Delete the selected field
- `[x]` - Delete the entire platform
- `[Esc]` - Go back to the main list

## 🔐 Security Architecture

GASPAS uses industry-standard encryption protocols to ensure your data remains safe:
1. **Master Password:** You set this on first launch. It is never stored.
2. **Key Derivation:** We use `PBKDF2WithHmacSHA256` with a high iteration count and salt to derive a 256-bit encryption key.
3. **Data Encryption:** Your platform configurations and passwords are encrypted using `AES/GCM/NoPadding` before being saved to the local SQLite database.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](https://github.com/your-username/gaspas/issues).

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

Distributed under the MIT License. See `LICENSE` for more information.

---
*Built with ❤️ for the terminal.*
# GasPas
