# Weather Monitoring System

A simple Java-based console application that tracks, simulates, and persists weather data for a specific station. This project allows users to record weather readings (temperature, humidity, wind speed, and conditions), calculate average temperatures, and simulate real-time updates with random data. It features a CSV-based persistence system that saves weather history to `data/weather_history.txt` upon exit and restores it on startup.

### File Responsibilities:
- **`WeatherData.java`**: A data model representing a single weather reading with a timestamp.
- **`WeatherStation.java`**: The core logic class that manages weather history, performs calculations, handles file I/O, and runs simulations.
- **`WeatherDisplay.java`**: A utility class with static methods for formatting and printing data to the console.
- **`Main.java`**: The entry point of the application, providing an interactive CLI menu for the user.

### Compilation and Execution:
From the `WeatherMonitor/` root directory, run the following commands:

**1. Compile:**
```bash
javac -d bin src/*.java
```
*(This compiles all files into a `bin` directory for clean organization.)*

**2. Run:**
```bash
java -cp bin Main
```
