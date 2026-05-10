import java.time.LocalDateTime;

public class WeatherData {
    private final double temperature;
    private final double humidity;
    private final double windSpeed;
    private final String condition;
    private final LocalDateTime timestamp;

    public WeatherData(double temperature, double humidity, double windSpeed, String condition, LocalDateTime timestamp) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public String getCondition() {
        return condition;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s] Temp: %.1f°C, Humidity: %.1f%%, Wind: %.1f km/h, Condition: %s",
                timestamp, temperature, humidity, windSpeed, condition);
    }
}
