# GASPAS

**A fast, offline, terminal-based password manager.**


*(Liste des plateformes)*
<img width="886" height="698" alt="plateforme_list" src="https://github.com/user-attachments/assets/fe09b9be-cf73-4d02-91b3-458c4b9b34bc" />

![GASPAS Detail](images/detail.png)
*(Édition des mots de passe)*
<img width="886" height="698" alt="plateform_data" src="https://github.com/user-attachments/assets/22deeb8e-46e5-418d-b69a-dd18a9d3ff55" />


## Pourquoi GASPAS ?

GASPAS (Gérer ASses PASswords) est fait pour ceux qui vivent dans le terminal. Pas de cloud, pas d'interface web lourde. Vos mots de passe sont chiffrés localement et accessibles au clavier en un clin d'œil.

## Fonctionnalités

*   **100% Clavier :** Navigation rapide avec des raccourcis intuitifs (`/` pour chercher, `c` pour copier).
*   **Chiffrement Local :** AES-256-GCM via un Master Password (PBKDF2). Les données sont dans `~/.gaspas/gaspas.db`.
*   **Sécurité Visuelle :** Les mots de passe sont masqués (`••••••`). Touche `v` pour révéler.
*   **Copie Rapide :** Copie directe dans le presse-papier de l'OS.
*   **Générateur Intégré :** Créez des mots de passe forts directement depuis l'application (`g`).

## Installation

Nécessite **Java 17+** et **Maven**.

```bash
git clone https://github.com/votre-nom/gaspas.git
cd gaspas
mvn clean package -DskipTests
```

## Utilisation

Lancez l'application via le JAR généré :

```bash
java -jar target/gaspas-1.0.0.jar
```

### Raccourcis principaux

**Liste des plateformes :**
*   `[a]` : Ajouter une plateforme
*   `[/]` : Chercher
*   `[Entrée]` : Ouvrir la plateforme

**Dans une plateforme :**
*   `[c]` : Copier la valeur
*   `[v]` ou `[Entrée]` : Révéler / Masquer
*   `[e]` : Éditer le champ
*   `[a]` : Ajouter un champ
*   `[g]` : Générer un mot de passe
*   `[d]` : Supprimer le champ
*   `[x]` : Supprimer la plateforme entière
*   `[Esc]` : Retour à la liste

## Licence

MIT.
