# PyParkour

## 🇵🇱 Opis Projektu (Polish Description)

**PyParkour** to zaawansowany system parkour dla serwerów Minecraft, stworzony w Javie (na podstawie analizy plików `pom.xml` i `Main.java` jest to projekt Java/Maven, a nie Python, jak sugeruje nazwa).

Plugin oferuje:
*   Kompleksowy system zarządzania parkourem.
*   Integrację z PlaceholderAPI.
*   Wbudowane komendy dla graczy i administratorów.
*   Zarządzanie statystykami graczy (`StatsManager.java`).

### Wymagania
*   Serwer Minecraft (np. Spigot/Paper)
*   Java Development Kit (JDK)
*   Maven
*   PlaceholderAPI (wymagane do działania)

### Komendy
*   `/parkour help` - Wyświetla pomoc.

### Uprawnienia (Permissions)
| Uprawnienie | Opis | Domyślne |
| :--- | :--- | :--- |
| `pyparkour.use` | Pozwala na granie i komendy gracza (help, wyjście) | `true` |
| `pyparkour.admin` | Pozwala na budowanie i przeładowanie (reload) | `op` |

### Licencja
Projekt jest udostępniony na licencji **MIT**. Szczegóły znajdują się w pliku [LICENSE](LICENSE).

---

## 🇬🇧 Project Description (English Description)

**PyParkour** is an advanced parkour system for Minecraft servers. Based on the file analysis (`pom.xml` and `Main.java`), this is a Java/Maven project, despite the "Py" prefix in the name.

The plugin features:
*   A comprehensive parkour management system.
*   Integration with PlaceholderAPI.
*   Built-in commands for players and administrators.
*   Player statistics management (`StatsManager.java`).

### Requirements
*   Minecraft Server (e.g., Spigot/Paper)
*   Java Development Kit (JDK)
*   Maven
*   PlaceholderAPI (required dependency)

### Commands
*   `/parkour help` - Displays help.

### Permissions
| Permission | Description | Default |
| :--- | :--- | :--- |
| `pyparkour.use` | Allows playing and player commands (help, exit) | `true` |
| `pyparkour.admin` | Allows building and reloading | `op` |

### License
The project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.
