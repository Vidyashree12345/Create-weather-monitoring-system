import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class WeatherWebServer {
    private final WeatherStation station;
    private HttpServer server;

    public WeatherWebServer(WeatherStation station) {
        this.station = station;
    }

    public void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            
            // Serve the modern Web UI
            server.createContext("/", new WebUIHandler());
            
            // API Endpoints
            server.createContext("/api/current", new CurrentWeatherHandler());
            server.createContext("/api/history", new HistoryHandler());
            server.createContext("/api/fetch", new FetchCityHandler());
            
            server.setExecutor(null);
            server.start();
            System.out.println("Web Application running at http://localhost:" + port);
        } catch (IOException e) {
            System.err.println("Failed to start web server: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void sendResponse(HttpExchange exchange, String content, String contentType) throws IOException {
        byte[] responseBytes = content.getBytes();
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    private String toJson(WeatherData d) {
        if (d == null) return "null";
        return String.format(
            "{\"timestamp\":\"%s\",\"temperature\":%.2f,\"humidity\":%.2f,\"windSpeed\":%.2f,\"condition\":\"%s\"}",
            d.getTimestamp().toString(), d.getTemperature(), d.getHumidity(), d.getWindSpeed(), d.getCondition()
        );
    }

    private class WebUIHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Weather Monitoring System</title>\n" +
                "    <script src=\"https://cdn.tailwindcss.com\"></script>\n" +
                "    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css\">\n" +
                "</head>\n" +
                "<body class=\"bg-slate-50 min-h-screen font-sans\">\n" +
                "    <div class=\"max-w-6xl mx-auto p-4 md:p-8\">\n" +
                "        <header class=\"flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8\">\n" +
                "            <h1 class=\"text-3xl font-bold text-slate-800\">Weather<span class=\"text-blue-600\">Monitor</span></h1>\n" +
                "            \n" +
                "            <div class=\"flex items-center gap-4\">\n" +
                "                <div class=\"relative flex-1 md:w-64\">\n" +
                "                    <input type=\"text\" id=\"city-search\" placeholder=\"Search city...\" \n" +
                "                           class=\"w-full px-4 py-2 rounded-full border border-slate-200 focus:outline-none focus:ring-2 focus:ring-blue-500 shadow-sm\">\n" +
                "                    <button onclick=\"searchCity()\" class=\"absolute right-3 top-2 text-slate-400 hover:text-blue-500\">\n" +
                "                        <i class=\"fas fa-search\"></i>\n" +
                "                    </button>\n" +
                "                </div>\n" +
                "                \n" +
                "                <div class=\"flex items-center space-x-2 bg-white px-4 py-2 rounded-full shadow-sm border border-slate-100\">\n" +
                "                    <span class=\"relative flex h-3 w-3\">\n" +
                "                        <span class=\"animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75\"></span>\n" +
                "                        <span class=\"relative inline-flex rounded-full h-3 w-3 bg-green-500\"></span>\n" +
                "                    </span>\n" +
                "                    <span class=\"text-sm font-medium text-slate-600 uppercase tracking-wider\">Live</span>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </header>\n" +
                "\n" +
                "        <!-- Current Weather Card -->\n" +
                "        <div id=\"current-weather\" class=\"bg-white rounded-3xl p-8 shadow-xl shadow-slate-200/50 mb-8 border border-slate-100\">\n" +
                "            <div class=\"flex flex-col md:flex-row md:items-center justify-between gap-8\">\n" +
                "                <div>\n" +
                "                    <h2 class=\"text-xl font-semibold text-slate-500 mb-1\">Current Reading</h2>\n" +
                "                    <p id=\"display-city\" class=\"text-4xl font-bold text-slate-900 mb-4 tracking-tight\">" + station.getStationName() + "</p>\n" +
                "                    <p id=\"last-updated\" class=\"text-sm font-medium text-slate-400 uppercase tracking-widest\"></p>\n" +
                "                </div>\n" +
                "                <div class=\"flex items-center gap-6\">\n" +
                "                    <div id=\"weather-icon\" class=\"text-7xl text-blue-500\"></div>\n" +
                "                    <div>\n" +
                "                        <p id=\"temp\" class=\"text-7xl font-black text-slate-900 leading-none\">--</p>\n" +
                "                        <p id=\"condition\" class=\"text-2xl font-medium text-slate-600 mt-2 capitalize tracking-wide\">--</p>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"grid grid-cols-2 md:grid-cols-2 gap-4 mt-10 border-t pt-8 border-slate-50\">\n" +
                "                <div class=\"bg-slate-50 p-6 rounded-2xl flex items-center gap-4 transition-all hover:bg-slate-100\">\n" +
                "                    <div class=\"w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center text-blue-600 shadow-sm\">\n" +
                "                        <i class=\"fas fa-droplet text-xl\"></i>\n" +
                "                    </div>\n" +
                "                    <div>\n" +
                "                        <p class=\"text-xs font-bold text-slate-400 uppercase tracking-widest\">Humidity</p>\n" +
                "                        <p id=\"humidity\" class=\"text-2xl font-bold text-slate-800\">--</p>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "                <div class=\"bg-slate-50 p-6 rounded-2xl flex items-center gap-4 transition-all hover:bg-slate-100\">\n" +
                "                    <div class=\"w-12 h-12 bg-indigo-100 rounded-xl flex items-center justify-center text-indigo-600 shadow-sm\">\n" +
                "                        <i class=\"fas fa-wind text-xl\"></i>\n" +
                "                    </div>\n" +
                "                    <div>\n" +
                "                        <p class=\"text-xs font-bold text-slate-400 uppercase tracking-widest\">Wind Speed</p>\n" +
                "                        <p id=\"wind\" class=\"text-2xl font-bold text-slate-800\">--</p>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "\n" +
                "        <!-- History Table -->\n" +
                "        <div class=\"bg-white rounded-3xl p-8 shadow-xl shadow-slate-200/50 border border-slate-100\">\n" +
                "            <div class=\"flex justify-between items-center mb-6\">\n" +
                "                <h3 class=\"text-2xl font-bold text-slate-900\">Activity Feed</h3>\n" +
                "                <span class=\"text-xs font-bold text-slate-400 bg-slate-50 px-3 py-1 rounded-full\">Last 10 Records</span>\n" +
                "            </div>\n" +
                "            <div class=\"overflow-x-auto\">\n" +
                "                <table class=\"w-full text-left border-separate border-spacing-y-2\">\n" +
                "                    <thead>\n" +
                "                        <tr class=\"text-slate-400 text-xs font-black uppercase tracking-widest\">\n" +
                "                            <th class=\"pb-4 px-4\">Timestamp</th>\n" +
                "                            <th class=\"pb-4 px-4\">Condition</th>\n" +
                "                            <th class=\"pb-4 px-4 text-center\">Temp</th>\n" +
                "                            <th class=\"pb-4 px-4 text-center\">Humidity</th>\n" +
                "                            <th class=\"pb-4 px-4 text-center\">Wind</th>\n" +
                "                        </tr>\n" +
                "                    </thead>\n" +
                "                    <tbody id=\"history-body\" class=\"text-slate-600\">\n" +
                "                        <!-- Rows injected by JS -->\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "\n" +
                "    <script>\n" +
                "        function getIcon(condition) {\n" +
                "            const c = condition.toLowerCase();\n" +
                "            if (c.includes('sun')) return 'fa-sun text-yellow-500';\n" +
                "            if (c.includes('rain')) return 'fa-cloud-showers-heavy text-blue-400';\n" +
                "            if (c.includes('cloud')) return 'fa-cloud text-slate-400';\n" +
                "            if (c.includes('storm')) return 'fa-bolt text-indigo-500';\n" +
                "            if (c.includes('clear')) return 'fa-moon text-blue-300';\n" +
                "            return 'fa-cloud-sun text-orange-400';\n" +
                "        }\n" +
                "\n" +
                "        async function searchCity() {\n" +
                "            const cityInput = document.getElementById('city-search');\n" +
                "            const city = cityInput.value.trim();\n" +
                "            if (!city) return;\n" +
                "            \n" +
                "            document.getElementById('display-city').innerText = city;\n" +
                "            document.getElementById('temp').innerText = '...';\n" +
                "            \n" +
                "            try {\n" +
                "                await fetch(`/api/fetch?city=${encodeURIComponent(city)}`);\n" +
                "                refreshData();\n" +
                "                cityInput.value = '';\n" +
                "            } catch (e) { console.error('Search error:', e); }\n" +
                "        }\n" +
                "\n" +
                "        document.getElementById('city-search').addEventListener('keypress', (e) => {\n" +
                "            if (e.key === 'Enter') searchCity();\n" +
                "        });\n" +
                "\n" +
                "        async function refreshData() {\n" +
                "            try {\n" +
                "                const curRes = await fetch('/api/current');\n" +
                "                const cur = await curRes.json();\n" +
                "                \n" +
                "                if (cur && cur.temperature !== undefined) {\n" +
                "                    document.getElementById('temp').innerText = Math.round(cur.temperature) + '°';\n" +
                "                    document.getElementById('condition').innerText = cur.condition;\n" +
                "                    document.getElementById('humidity').innerText = Math.round(cur.humidity) + '%';\n" +
                "                    document.getElementById('wind').innerText = cur.windSpeed + ' km/h';\n" +
                "                    document.getElementById('last-updated').innerText = 'Last reading: ' + new Date(cur.timestamp).toLocaleTimeString();\n" +
                "                    document.getElementById('weather-icon').innerHTML = `<i class=\"fas ${getIcon(cur.condition)}\"></i>`;\n" +
                "                }\n" +
                "\n" +
                "                const histRes = await fetch('/api/history');\n" +
                "                const hist = await histRes.json();\n" +
                "                const body = document.getElementById('history-body');\n" +
                "                body.innerHTML = '';\n" +
                "                \n" +
                "                [...hist].reverse().slice(0, 10).forEach(d => {\n" +
                "                    const row = `<tr class=\"hover:bg-slate-50 transition-colors bg-white shadow-sm\">\n" +
                "                        <td class=\"py-4 px-4 rounded-l-2xl font-semibold text-slate-500 text-xs\">${new Date(d.timestamp).toLocaleString([], {month: 'short', day: 'numeric', hour: '2-digit', minute:'2-digit'})}</td>\n" +
                "                        <td class=\"py-4 px-4\"><span class=\"capitalize bg-blue-50 px-4 py-1.5 rounded-full text-[10px] font-black tracking-widest text-blue-600 border border-blue-100\">${d.condition}</span></td>\n" +
                "                        <td class=\"py-4 px-4 font-black text-slate-800 text-center\">${d.temperature.toFixed(1)}°C</td>\n" +
                "                        <td class=\"py-4 px-4 text-center font-bold text-slate-500\">${d.humidity.toFixed(0)}%</td>\n" +
                "                        <td class=\"py-4 px-4 rounded-r-2xl text-center font-bold text-slate-500\">${d.windSpeed.toFixed(1)}</td>\n" +
                "                    </tr>`;\n" +
                "                    body.innerHTML += row;\n" +
                "                });\n" +
                "            } catch (e) { console.error('Fetch error:', e); }\n" +
                "        }\n" +
                "\n" +
                "        refreshData();\n" +
                "        setInterval(refreshData, 3000);\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
            sendResponse(exchange, html, "text/html");
        }
    }

    private class FetchCityHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.startsWith("city=")) {
                String city = java.net.URLDecoder.decode(query.substring(5), java.nio.charset.StandardCharsets.UTF_8);
                station.fetchRealWeatherData(city);
            }
            sendResponse(exchange, "{\"status\":\"ok\"}", "application/json");
        }
    }

    private class CurrentWeatherHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            WeatherData current = station.getCurrentReading();
            sendResponse(exchange, current == null ? "{}" : toJson(current), "application/json");
        }
    }

    private class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            ArrayList<WeatherData> history = station.getHistory();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < history.size(); i++) {
                sb.append(toJson(history.get(i)));
                if (i < history.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendResponse(exchange, sb.toString(), "application/json");
        }
    }
}
