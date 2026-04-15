package utmn.checkmates.client;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Application {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8228;
    private static final int SOCKET_TIMEOUT = 10000;

    private static Socket socket;
    private static OutputStream out;
    private static InputStream in;
    private static BufferedReader reader;
    private static final Scanner scanner = new Scanner(System.in);
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        debug("[MAIN] Запуск клиента...");

        if (!connectToServer()) {
            System.err.println("❌ Не удалось подключиться к серверу.");
            return;
        }

        try {
            while (true) {
                showMenu();
                String choice = scanner.nextLine().trim().toLowerCase();

                if (choice.equals("0") || choice.equals("exit") || choice.equals("quit") || choice.equals("выход")) {
                    debug("[MAIN] Завершение работы");
                    break;
                }

                try {
                    processChoice(choice);
                } catch (IllegalArgumentException e) {
                    System.err.println("❌ Ошибка: " + e.getMessage());
                } catch (SocketTimeoutException e) {
                    System.err.println("⏱ Таймаут ответа от сервера");
                } catch (IOException e) {
                    System.err.println("🔌 Ошибка соединения: " + e.getMessage());
                    if (!reconnect()) break;
                }

                System.out.println("\n" + "─".repeat(60) + "\n");
            }
        } finally {
            closeConnection();
        }
    }

    // ==================== ПОДКЛЮЧЕНИЕ ====================

    private static boolean connectToServer() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            socket.setSoTimeout(SOCKET_TIMEOUT);
            out = socket.getOutputStream();
            in = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            debug("[SOCKET] Подключено: " + socket.getRemoteSocketAddress());
            return true;
        } catch (IOException e) {
            debug("[ERROR] " + e.getMessage());
            return false;
        }
    }

    private static boolean reconnect() {
        debug("[RECONNECT] Попытка переподключения...");
        closeConnection();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        return connectToServer();
    }

    private static void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                debug("[SOCKET] Соединение закрыто");
            }
        } catch (IOException e) {
            debug("[WARN] " + e.getMessage());
        }
    }

    // ==================== МЕНЮ ====================

    private static void showMenu() {
        System.out.println("""
                
╔════════════════════════════════════════════════════╗
║  ♟️  CHESS CLIENT — ВЫБОР ЗАПРОСА (C2S)  ♟️        ║
╚════════════════════════════════════════════════════╝

┌─ СИСТЕМНЫЕ ──────────────────────────────────────┐
│ 0  │ Выход из клиента                            │
│ 1  │ PING_REQUEST          (S0000) — проверка   │
│ 2  │ SESSIONS_LIST_REQUEST (S1001) — список комнат │
│ 3  │ CREATE_SESSION        (S1010) — создать сессию │
└──────────────────────────────────────────────────┘

┌─ ПОДКЛЮЧЕНИЕ ────────────────────────────────────┐
│ 4  │ SERVER_CONNECTION  (S0001) — войти в сессию │
│ 5  │ PLAYER_UPDATE      (S0010) — статус игрока │
│ 6  │ DISCONNECTION      (S1000) — отключиться   │
└──────────────────────────────────────────────────┘

┌─ ИГРА ───────────────────────────────────────────┐
│ 7  │ MOVE           (S0100) — сделать ход        │
│ 8  │ RESIGN         (S0101) — сдаться            │
│ 9  │ DRAW_OFFER     (S0110) — предложить ничью   │
│ 10 │ DRAW_RESPONSE  (S0111) — ответ на ничью     │
└──────────────────────────────────────────────────┘

┌─ ОТЛАДКА ────────────────────────────────────────┐
│ 99 │ Ввести тип и JSON вручную                   │
└──────────────────────────────────────────────────┘

Ваш выбор > """);
    }

    // ==================== ОБРАБОТКА ВЫБОРА ====================

    private static void processChoice(String choice) throws IOException {
        byte type;
        JsonObject payload;
        String label;

        switch (choice) {
            case "1" -> { // PING_REQUEST (S0000) — нет параметров
                type = 0;
                payload = new JsonObject();
                label = "PING_REQUEST";
                sendAndReceive(type, payload, label);
            }

            case "2" -> { // SESSIONS_LIST_REQUEST (S1001) — нет параметров
                type = 9;
                payload = new JsonObject();
                label = "SESSIONS_LIST_REQUEST";
                sendAndReceive(type, payload, label);
            }

            case "3" -> { // CREATE_SESSION (S1010) — нет параметров
                type = 10;
                payload = new JsonObject();
                label = "CREATE_SESSION";
                sendAndReceive(type, payload, label);
            }

            case "4" -> { // SERVER_CONNECTION (S0001)
                type = 1;
                label = "SERVER_CONNECTION";
                payload = new JsonObject();

                System.out.println("\n📋 SERVER_CONNECTION (S0001) — параметры:");
                System.out.println("   • session_id (int) — ID сессии");
                System.out.println("   • player_name (string) — ваше имя");

                payload.addProperty("session_id", readInt("session_id"));
                payload.addProperty("player_name", readString("player_name"));

                sendAndReceive(type, payload, label);
            }

            case "5" -> { // PLAYER_UPDATE (S0010)
                type = 2;
                label = "PLAYER_UPDATE";
                payload = new JsonObject();

                System.out.println("\n📋 PLAYER_UPDATE (S0010) — параметры:");
                System.out.println("   • session_id (int)");
                System.out.println("   • client_id (int)");
                System.out.println("   • ready (bool) — готовность к игре");

                payload.addProperty("session_id", readInt("session_id"));
                payload.addProperty("client_id", readInt("client_id"));
                payload.addProperty("ready", readBool("ready"));

                sendAndReceive(type, payload, label);
            }

            case "6" -> { // DISCONNECTION (S1000)
                type = 8;
                label = "DISCONNECTION";
                payload = new JsonObject();

                System.out.println("\n📋 DISCONNECTION (S1000) — параметры:");
                System.out.println("   • session_id (int)");
                System.out.println("   • client_id (int)");

                payload.addProperty("session_id", readInt("session_id"));
                payload.addProperty("client_id", readInt("client_id"));

                sendAndReceive(type, payload, label);
            }

            case "7" -> { // MOVE (S0100)
                type = 4;
                label = "MOVE";
                payload = new JsonObject();

                System.out.println("\n📋 MOVE (S0100) — параметры:");
                System.out.println("   • session_id (int)");
                System.out.println("   • client_id (int)");
                System.out.println("   • from (byte) — позиция фигуры (0-63)");
                System.out.println("   • to (byte) — целевая позиция (0-63)");
                System.out.println("\n💡 Кодирование позиции:");
                System.out.println("   1. Координаты (x, y) от 1 до 8");
                System.out.println("   2. Перевод в 0-based: (x-1, y-1)");
                System.out.println("   3. Bin: 3 бита X + 3 бита Y = 6 бит");
                System.out.println("   Пример: (6,8) → (5,7) → 101+111 → 101111₂ = 47");

                payload.addProperty("session_id", readInt("session_id"));
                payload.addProperty("client_id", readInt("client_id"));
                payload.addProperty("from", readByte("from"));
                payload.addProperty("to", readByte("to"));

                sendAndReceive(type, payload, label);
            }

            case "8" -> { // RESIGN (S0101)
                type = 5;
                label = "RESIGN";
                payload = new JsonObject();

                System.out.println("\n📋 RESIGN (S0101) — параметры:");
                System.out.println("   • session_id (int)");
                System.out.println("   • client_id (int)");

                payload.addProperty("session_id", readInt("session_id"));
                payload.addProperty("client_id", readInt("client_id"));

                sendAndReceive(type, payload, label);
            }

            case "9" -> { // DRAW_OFFER (S0110)
                type = 6;
                label = "DRAW_OFFER";
                payload = new JsonObject();

                System.out.println("\n📋 DRAW_OFFER (S0110) — параметры:");
                System.out.println("   • session_id (int)");
                System.out.println("   • client_id (int)");

                payload.addProperty("session_id", readInt("session_id"));
                payload.addProperty("client_id", readInt("client_id"));

                sendAndReceive(type, payload, label);
            }

            case "10" -> { // DRAW_RESPONSE (S0111)
                type = 7;
                label = "DRAW_RESPONSE";
                payload = new JsonObject();

                System.out.println("\n📋 DRAW_RESPONSE (S0111) — параметры:");
                System.out.println("   • session_id (int)");
                System.out.println("   • client_id (int)");
                System.out.println("   • agree (bool) — принять ничью?");

                payload.addProperty("session_id", readInt("session_id"));
                payload.addProperty("client_id", readInt("client_id"));
                payload.addProperty("agree", readBool("agree"));

                sendAndReceive(type, payload, label);
            }

            case "99" -> { // Ручной ввод
                System.out.print("\nВведите тип сообщения (0-15): ");
                type = readByte("type");
                System.out.print("Введите JSON-параметры: ");
                String json = scanner.nextLine().trim();
                payload = gson.fromJson(json, JsonObject.class);
                sendAndReceive(type, payload, "MANUAL");
            }

            default -> throw new IllegalArgumentException("Неизвестная команда: " + choice);
        }
    }

    // ==================== ВВОД ПАРАМЕТРОВ ====================

    private static int readInt(String paramName) {
        while (true) {
            System.out.print("   " + paramName + " (int) > ");
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("   ❌ Введите целое число");
            }
        }
    }

    private static byte readByte(String paramName) {
        while (true) {
            System.out.print("   " + paramName + " (byte, -128..127) > ");
            try {
                int val = Integer.parseInt(scanner.nextLine().trim());
                if (val < -128 || val > 127) {
                    System.out.println("   ❌ Значение вне диапазона byte");
                    continue;
                }
                return (byte) val;
            } catch (NumberFormatException e) {
                System.out.println("   ❌ Введите число");
            }
        }
    }

    private static boolean readBool(String paramName) {
        while (true) {
            System.out.print("   " + paramName + " (true/false) > ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("true") || input.equals("1") || input.equals("да")) {
                return true;
            } else if (input.equals("false") || input.equals("0") || input.equals("нет")) {
                return false;
            }
            System.out.println("   ❌ Введите true/false, 1/0 или да/нет");
        }
    }

    private static String readString(String paramName) {
        System.out.print("   " + paramName + " (string) > ");
        return scanner.nextLine().trim();
    }

    // ==================== ОТПРАВКА / ПОЛУЧЕНИЕ ====================

    private static void sendAndReceive(byte messageType, JsonObject payload, String label) throws IOException {
        String jsonPayload = gson.toJson(payload);

        // === ОТПРАВКА ===
        debug("[SEND] %s | type=%d | JSON: %s".formatted(label, messageType, jsonPayload));

        out.write(messageType);  // 1 байт типа
        out.write(jsonPayload.getBytes(StandardCharsets.UTF_8));  // JSON
        out.write('\n');  // терминатор строки для readLine()
        out.flush();

        debug("[SEND] Отправлено. Ожидание ответа...");

        // === ПРИЁМ ===
        int responseByte = in.read();
        if (responseByte == -1) {
            System.out.println("⚠️ Сервер закрыл соединение (EOF)");
            return;
        }
        byte responseType = (byte) responseByte;

        String responseJson = reader.readLine();
        if (responseJson == null) {
            System.out.println("⚠️ Получен null вместо ответа");
            return;
        }

        debug("[RECV] type=%d | JSON: %s".formatted(responseType, responseJson));

        // === ВЫВОД ОТВЕТА ===
        printResponse(responseType, responseJson, label);
    }

    private static void printResponse(byte type, String json, String requestLabel) {
        String typeName = switch (type) {
            case 0 -> "PING_RESPONSE";
            case 1 -> "CLIENT_CONNECTION";
            case 2 -> "OPPONENT_UPDATE";
            case 3 -> "GAME_START";
            case 4 -> "GAME_UPDATE";
            case 5 -> "ILLEGAL_MOVE";
            case 6 -> "DRAW_DECISION";
            case 7 -> "GAME_OVER";
            case 8 -> "PAUSE";
            case 9 -> "SESSIONS_LIST_RESPONSE";
            case 10 -> "CREATE_SESSION_RESPONSE";
            case 15 -> "EXCEPTION";
            default -> "UNKNOWN(" + type + ")";
        };

        // Форматируем JSON для красивого вывода
        String formattedJson = formatJson(json);

        System.out.println("""
                
┌────────────────────────────────────────────────┐
│  📥 ОТВЕТ СЕРВЕРА  (запрос: %-16s) │
├────────────────────────────────────────────────┤
│  Тип сообщения: %-23s │
│  Код (byte): %-26d │
├────────────────────────────────────────────────┤
│  JSON:                                         │
│  %-46s │
└────────────────────────────────────────────────┘
                """.formatted(requestLabel, typeName, type, formattedJson));
    }

    private static String formatJson(String json) {
        // Простое форматирование: перенос длинных строк
        if (json.length() <= 44) {
            return json + " ".repeat(44 - json.length());
        }
        // Разбиваем по ключам для читаемости
        StringBuilder sb = new StringBuilder();
        String[] parts = json.split("(?<=\\}),|(?<=\\]),");
        int lineLen = 0;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (lineLen + part.length() > 44) {
                sb.append("\n  ");
                lineLen = 2;
            }
            sb.append(part);
            if (i < parts.length - 1) sb.append(",");
            lineLen += part.length() + 1;
        }
        // Дополняем последнюю строку пробелами
        String lastLine = sb.toString().substring(sb.lastIndexOf("\n  ") + 3);
        if (lastLine.length() < 44) {
            sb.append(" ".repeat(44 - lastLine.length()));
        }
        return sb.toString();
    }

    // ==================== УТИЛИТЫ ====================

    private static void debug(String message) {
        String ts = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        System.err.println("[" + ts + " DEBUG] " + message);
    }
}