import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        WeatherStation station = new WeatherStation("Chennai Central");
        station.loadHistoryFromFile("data/weather_history.txt");
        
        WeatherWebServer webServer = new WeatherWebServer(station);
        webServer.start(8080);

        Scanner scanner = new Scanner(System.in);

        // Add 5 sample readings manually
        station.recordReading(32.5, 65.0, 12.0, "Sunny");
        station.recordReading(31.0, 70.0, 15.0, "Cloudy");
        station.recordReading(28.5, 85.0, 20.0, "Rainy");
        station.recordReading(33.0, 60.0, 10.0, "Sunny");
        station.recordReading(30.0, 75.0, 14.0, "Clear");

        boolean exit = false;
        while (!exit) {
            System.out.println("\n--- Weather Monitoring System ---");
            System.out.println("1 - View current weather");
            System.out.println("2 - View history");
            System.out.println("3 - View summary stats");
            System.out.println("4 - Simulate real-time updates (3 readings, 2s apart)");
            System.out.println("5 - Fetch ACTUAL live weather for any city");
            System.out.println("6 - Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    WeatherDisplay.displayCurrent(station.getCurrentReading());
                    break;
                case "2":
                    WeatherDisplay.displayHistory(station.getHistory());
                    break;
                case "3":
                    WeatherDisplay.displaySummary(station);
                    break;
                case "4":
                    System.out.println("\nStarting simulation...");
                    station.simulateRealTimeUpdates(3, 2);
                    System.out.println("Simulation complete.");
                    break;
                case "5":
                    System.out.print("Enter city name (e.g., London, Tokyo, Mumbai): ");
                    String city = scanner.nextLine();
                    System.out.println("\nFetching live data for " + city + "...");
                    station.fetchRealWeatherData(city);
                    break;
                case "6":
                    station.saveHistoryToFile("data/weather_history.txt");
                    webServer.stop();
                    exit = true;
                    System.out.println("Exiting system. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }
}
