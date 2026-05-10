import java.util.ArrayList;

public class WeatherDisplay {

    public static void displayCurrent(WeatherData data) {
        if (data == null) {
            System.out.println("No weather data available to display.");
            return;
        }
        System.out.println("=========================================");
        System.out.println("           CURRENT WEATHER               ");
        System.out.println("=========================================");
        System.out.println(" Time:      " + data.getTimestamp());
        System.out.println(" Condition: " + data.getCondition());
        System.out.printf(" Temp:      %.1f°C\n", data.getTemperature());
        System.out.printf(" Humidity:  %.1f%%\n", data.getHumidity());
        System.out.printf(" Wind:      %.1f km/h\n", data.getWindSpeed());
        System.out.println("=========================================");
    }

    public static void displayHistory(ArrayList<WeatherData> history) {
        if (history == null || history.isEmpty()) {
            System.out.println("No history recorded yet.");
            return;
        }
        System.out.println("\n--- Weather History ---");
        for (int i = 0; i < history.size(); i++) {
            System.out.println((i + 1) + ". " + history.get(i).toString());
        }
    }

    public static void displaySummary(WeatherStation station) {
        if (station == null) return;
        
        ArrayList<WeatherData> history = station.getHistory();
        System.out.println("\n--- Station Summary ---");
        System.out.println(" Station Name:    " + station.getStationName());
        System.out.println(" Total Readings:  " + history.size());
        System.out.printf(" Average Temp:    %.1f°C\n", station.getAverageTemperature());
        System.out.println("------------------------");
    }
}
