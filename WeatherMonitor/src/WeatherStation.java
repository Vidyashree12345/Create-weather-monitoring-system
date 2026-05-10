import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

public class WeatherStation {
    private final String stationName;
    private final ArrayList<WeatherData> history;
    private final Random random;

    public WeatherStation(String stationName) {
        this.stationName = stationName;
        this.history = new ArrayList<>();
        this.random = new Random();
    }

    public void recordReading(double temp, double humidity, double windSpeed, String condition) {
        WeatherData reading = new WeatherData(temp, humidity, windSpeed, condition, LocalDateTime.now());
        history.add(reading);
    }

    public WeatherData getCurrentReading() {
        if (history.isEmpty()) {
            return null;
        }
        return history.get(history.size() - 1);
    }

    public ArrayList<WeatherData> getHistory() {
        return new ArrayList<>(history);
    }

    public double getAverageTemperature() {
        if (history.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (WeatherData data : history) {
            sum += data.getTemperature();
        }
        return sum / history.size();
    }

    public void simulateRealTimeUpdates(int count, int delaySeconds) {
        String[] conditions = {"Sunny", "Cloudy", "Rainy", "Stormy", "Clear"};
        
        for (int i = 0; i < count; i++) {
            double temp = 15 + (35 - 15) * random.nextDouble(); // 15 to 35
            double humidity = 30 + (90 - 30) * random.nextDouble(); // 30% to 90%
            double windSpeed = 0 + (50 - 0) * random.nextDouble(); // 0 to 50 km/h
            String condition = conditions[random.nextInt(conditions.length)];

            recordReading(temp, humidity, windSpeed, condition);
            System.out.println("Recorded new reading for " + stationName + ": " + getCurrentReading());

            if (i < count - 1) {
                try {
                    Thread.sleep(delaySeconds * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Simulation interrupted.");
                    break;
                }
            }
        }
    }

    public void fetchRealWeatherData(String city) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String encodedCity = java.net.URLEncoder.encode(city, java.nio.charset.StandardCharsets.UTF_8);
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://wttr.in/" + encodedCity + "?format=%25t,%25h,%25w,%25C"))
                    .build();
java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
String responseBody = response.body().trim();

// Format returned by wttr.in: +30°C,48%,E13km/h,Clear
String[] parts = responseBody.split(",");
if (parts.length == 4) {
    // Extract only numbers and dots/negatives for temp, humidity, wind
    double temp = Double.parseDouble(parts[0].replaceAll("[^\\d.-]", ""));
    double humidity = Double.parseDouble(parts[1].replaceAll("[^\\d.-]", ""));
    double wind = Double.parseDouble(parts[2].replaceAll("[^\\d.-]", ""));
    String condition = parts[3].trim();


                recordReading(temp, humidity, wind, condition);
                System.out.println("Live data fetched for " + city + ": " + getCurrentReading());
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch live weather for " + city + ": " + e.getMessage());
        }
    }

    public String getStationName() {
        return stationName;
    }

    public void saveHistoryToFile(String filePath) {
        try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
            for (WeatherData data : history) {
                writer.write(String.format("%s,%.2f,%.2f,%.2f,%s\n",
                        data.getTimestamp(),
                        data.getTemperature(),
                        data.getHumidity(),
                        data.getWindSpeed(),
                        data.getCondition()));
            }
            System.out.println("History saved to " + filePath);
        } catch (java.io.IOException e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
    }

    public void loadHistoryFromFile(String filePath) {
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            System.out.println("No history file found at " + filePath + ". Starting fresh.");
            return;
        }

        try (java.util.Scanner fileScanner = new java.util.Scanner(file)) {
            int count = 0;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    try {
                        java.time.LocalDateTime ts = java.time.LocalDateTime.parse(parts[0]);
                        double temp = Double.parseDouble(parts[1]);
                        double humidity = Double.parseDouble(parts[2]);
                        double wind = Double.parseDouble(parts[3]);
                        String condition = parts[4];
                        
                        history.add(new WeatherData(temp, humidity, wind, condition, ts));
                        count++;
                    } catch (Exception e) {
                        System.err.println("Skipping malformed line: " + line);
                    }
                }
            }
            System.out.println("Loaded " + count + " records from " + filePath);
        } catch (java.io.FileNotFoundException e) {
            System.err.println("History file not found: " + e.getMessage());
        }
    }
}
