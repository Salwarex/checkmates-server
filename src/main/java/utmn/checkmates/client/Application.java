package utmn.checkmates.client;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Application {
    private static String SERVER_HOST;
    private static int SERVER_PORT;
    private static final int SOCKET_TIMEOUT = 10000;

    // Сокет сессии (для stateful-запросов)
    private static Socket sessionSocket;
    private static OutputStream sessionOut;
    private static InputStream sessionIn;

    private static final Scanner scanner = new Scanner(System.in);
    private static final Gson gson = new Gson();

    // Состояние сессии
    private static boolean isSessionEstablished = false;
    private static int currentSessionId = -1;
    private static int currentClientId = -1;

    public static void main(String[] args) {
        if (!promptConnectionSettings()) {
            System.err.println("❌ Не удалось задать параметры подключения.");
            return;
        }

        debug("[MAIN] Запуск клиента...");
        debug("[MAIN] Сервер: " + SERVER_HOST + ":" + SERVER_PORT);

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
                    if (isSessionEstablished) {
                        debug("[SESSION] Сброс состояния сессии из-за ошибки");
                        resetSessionState();
                    }
                }

                System.out.println("\n" + "─".repeat(60) + "\n");
            }
        } finally {
            closeSessionSocket();
        }
    }

    /**
     * Запрос хоста и порта через консоль
     */
    private static boolean promptConnectionSettings() {
        System.out.println("""
                
╔════════════════════════════════════════════════════╗
║  ♟️  CHESS CLIENT — НАСТРОЙКА ПОДКЛЮЧЕНИЯ  ♟️      ║
╚════════════════════════════════════════════════════╝
            """);

        while (true) {
            System.out.print("📡 Хост сервера [по умолчанию: localhost]: ");
            String hostInput = scanner.nextLine().trim();
            SERVER_HOST = hostInput.isEmpty() ? "localhost" : hostInput;
            if (!SERVER_HOST.isEmpty()) break;
        }

        while (true) {
            System.out.print("🔌 Порт сервера [по умолчанию: 8228]: ");
            String portInput = scanner.nextLine().trim();
            if (portInput.isEmpty()) {
                SERVER_PORT = 8228;
                break;
            }
            try {
                int port = Integer.parseInt(portInput);
                if (port < 1 || port > 65535) {
                    System.out.println("❌ Порт должен быть в диапазоне 1-65535");
                    continue;
                }
                SERVER_PORT = port;
                break;
            } catch (NumberFormatException e) {
                System.out.println("❌ Введите корректное число");
            }
        }

        System.out.println("\n✅ Подключение: " + SERVER_HOST + ":" + SERVER_PORT + "\n");
        return true;
    }

    // ==================== УПРАВЛЕНИЕ СОКЕТАМИ ====================

    private static Socket createEphemeralSocket() throws IOException {
        Socket s = new Socket(SERVER_HOST, SERVER_PORT);
        s.setSoTimeout(SOCKET_TIMEOUT);
        debug("[SOCKET] Временный сокет: " + s.getRemoteSocketAddress());
        return s;
    }

    private static boolean openSessionSocket() {
        try {
            sessionSocket = new Socket(SERVER_HOST, SERVER_PORT);
            sessionSocket.setSoTimeout(SOCKET_TIMEOUT);
            sessionOut = sessionSocket.getOutputStream();
            sessionIn = sessionSocket.getInputStream();
            debug("[SOCKET] Сессия: подключено к " + sessionSocket.getRemoteSocketAddress());
            return true;
        } catch (IOException e) {
            debug("[ERROR] " + e.getMessage());
            return false;
        }
    }

    private static void closeSessionSocket() {
        try {
            if (sessionSocket != null && !sessionSocket.isClosed()) {
                sessionSocket.close();
                debug("[SOCKET] Сокет сессии закрыт");
            }
        } catch (IOException e) {
            debug("[WARN] " + e.getMessage());
        }
    }

    private static void resetSessionState() {
        isSessionEstablished = false;
        currentSessionId = -1;
        currentClientId = -1;
        closeSessionSocket();
    }

    /**
     * 🔥 Читаем строку из InputStream до '\n' вручную — без BufferedReader!
     * Это предотвращает рассинхронизацию буферов при смешанном чтении байтов и строк.
     */
    private static String readLineRaw(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') break;
            baos.write(b);
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * Отправка через временный сокет (stateless-запросы)
     */
    private static void sendEphemeral(byte messageType, JsonObject payload, String label) throws IOException {
        Socket tempSocket = null;
        try {
            tempSocket = createEphemeralSocket();
            OutputStream out = tempSocket.getOutputStream();
            InputStream in = tempSocket.getInputStream();

            String jsonPayload = gson.toJson(payload);
            debug("[SEND] %s [EPHEMERAL] | type=%d | JSON: %s".formatted(label, messageType, jsonPayload));

            out.write(messageType);
            out.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            out.write('\n');
            out.flush();

            int responseByte = in.read();
            if (responseByte == -1) {
                System.out.println("⚠️ Сервер закрыл соединение (EOF)");
                return;
            }
            byte responseType = (byte) responseByte;

            String responseJson = readLineRaw(in);
            if (responseJson == null || responseJson.isEmpty()) {
                System.out.println("⚠️ Получен пустой ответ");
                return;
            }

            debug("[RECV] type=%d | JSON: %s".formatted(responseType, responseJson));
            printResponse(responseType, responseJson, label + " [EPHEMERAL]");

        } finally {
            if (tempSocket != null && !tempSocket.isClosed()) {
                tempSocket.close();
                debug("[SOCKET] Временный сокет закрыт");
            }
        }
    }

    /**
     * Отправка через сокет сессии (stateful-запросы)
     */
    private static void sendViaSession(byte messageType, JsonObject payload, String label) throws IOException {
        if (!isSessionEstablished || sessionSocket == null || sessionSocket.isClosed()) {
            throw new IllegalStateException("❌ Сессия не установлена. Сначала выполните SERVER_CONNECTION (пункт 4)");
        }

        String jsonPayload = gson.toJson(payload);
        debug("[SEND] %s [SESSION] | type=%d | JSON: %s".formatted(label, messageType, jsonPayload));

        sessionOut.write(messageType);
        sessionOut.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        sessionOut.write('\n');
        sessionOut.flush();

        int responseByte = sessionIn.read();
        if (responseByte == -1) {
            System.out.println("⚠️ Сервер закрыл соединение (EOF)");
            resetSessionState();
            return;
        }
        byte responseType = (byte) responseByte;

        String responseJson = readLineRaw(sessionIn);
        if (responseJson == null || responseJson.isEmpty()) {
            System.out.println("⚠️ Получен пустой ответ");
            resetSessionState();
            return;
        }

        debug("[RECV] type=%d | JSON: %s".formatted(responseType, responseJson));
        printResponse(responseType, responseJson, label + " [SESSION]");
    }

    // ==================== МЕНЮ ====================
    private static void showMenu() {
        String sessionStatus = isSessionEstablished
                ? "🟢 Сессия: #%d (вы: #%d)".formatted(currentSessionId, currentClientId)
                : "🔴 Сессия: не установлена";

        System.out.println("""
                
╔════════════════════════════════════════════════════╗
║  ♟️  CHESS CLIENT — ВЫБОР ЗАПРОСА (C2S)  ♟️        ║
╚════════════════════════════════════════════════════╝
   %s

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

Ваш выбор > """.formatted(sessionStatus));
    }

    // ==================== ОБРАБОТКА ВЫБОРА ====================
    private static void processChoice(String choice) throws IOException {
        byte type;
        JsonObject payload;
        String label;

        switch (choice) {
            case "1" -> {
                type = 0;
                payload = new JsonObject();
                label = "PING_REQUEST";
                if (isSessionEstablished) {
                    debug("[PING] Используем сокет сессии");
                    sendViaSession(type, payload, label);
                } else {
                    debug("[PING] Сессии нет — используем временный сокет");
                    sendEphemeral(type, payload, label);
                }
            }
            case "2" -> {
                type = 9;
                payload = new JsonObject();
                label = "SESSIONS_LIST_REQUEST";
                sendEphemeral(type, payload, label);
            }
            case "3" -> {
                type = 10;
                payload = new JsonObject();
                label = "CREATE_SESSION";
                sendEphemeral(type, payload, label);
            }
            case "4" -> {
                type = 1;
                label = "SERVER_CONNECTION";
                payload = new JsonObject();

                System.out.println("\n📋 SERVER_CONNECTION (S0001) — параметры:");
                System.out.println("   • session_id (int) — ID сессии");
                System.out.println("   • player_name (string) — ваше имя");

                int sessionId = readInt("session_id");
                String playerName = readString("player_name");
                payload.addProperty("session_id", sessionId);
                payload.addProperty("player_name", playerName);

                // 🔧 ИСПОЛЬЗУЕМ sessionSocket, а не ephemeral!
                if (!openSessionSocket()) {
                    throw new IOException("Не удалось открыть сокет сессии");
                }

                String jsonPayload = gson.toJson(payload);
                debug("[SEND] %s [SESSION] | type=%d | JSON: %s".formatted(label, type, jsonPayload));

                sessionOut.write(type);
                sessionOut.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                sessionOut.write('\n');
                sessionOut.flush();

                int responseByte = sessionIn.read();
                if (responseByte == -1) {
                    System.out.println("⚠️ Сервер закрыл соединение");
                    resetSessionState();
                    return;
                }
                byte responseType = (byte) responseByte;
                String responseJson = readLineRaw(sessionIn);

                if (responseJson == null || responseJson.isEmpty()) {
                    System.out.println("⚠️ Получен пустой ответ");
                    resetSessionState();
                    return;
                }

                debug("[RECV] type=%d | JSON: %s".formatted(responseType, responseJson));
                printResponse(responseType, responseJson, label + " [SESSION]");

                if (responseType == 1) {
                    JsonObject resp = gson.fromJson(responseJson, JsonObject.class);
                    if (resp.has("client_id")) {
                        currentClientId = resp.get("client_id").getAsInt();
                        currentSessionId = sessionId;
                        isSessionEstablished = true; // 🔥 Сессия установлена, сокет остаётся открытым!
                        debug("[SESSION] ✅ Сессия установлена: #%d, клиент #%d".formatted(currentSessionId, currentClientId));
                        System.out.println("🎉 Вы подключены к сессии #%d как клиент #%d".formatted(currentSessionId, currentClientId));
                    }
                }
                // 🔥 НЕ ЗАКРЫВАЕМ sessionSocket! Он нужен для дальнейшей коммуникации
            }
            case "5" -> {
                if (!isSessionEstablished) throw new IllegalStateException("❌ Сначала установите сессию (пункт 4)");
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
                sendViaSession(type, payload, label);
            }
            case "6" -> {
                type = 8;
                label = "DISCONNECTION";
                payload = new JsonObject();
                System.out.println("\n📋 DISCONNECTION (S1000) — параметры:");
                System.out.println("   • session_id (int)");
                System.out.println("   • client_id (int)");
                payload.addProperty("session_id", readInt("session_id"));
                payload.addProperty("client_id", readInt("client_id"));

                if (isSessionEstablished) {
                    sendViaSession(type, payload, label);
                    resetSessionState();
                    System.out.println("👋 Вы отключились от сессии");
                } else {
                    sendEphemeral(type, payload, label);
                }
            }
            case "7" -> {
                if (!isSessionEstablished) throw new IllegalStateException("❌ Сначала установите сессию (пункт 4)");
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
                sendViaSession(type, payload, label);
            }
            case "8" -> {
                if (!isSessionEstablished) throw new IllegalStateException("❌ Сначала установите сессию (пункт 4)");
                type = 5;
                label = "RESIGN";
                payload = new JsonObject();
                System.out.println("\n📋 RESIGN (S0101) — параметры:");
                System.out.println("   • session_id (int)");
                System.out.println("   • client_id (int)");
                payload.addProperty("session_id", readInt("session_id"));
                payload.addProperty("client_id", readInt("client_id"));
                sendViaSession(type, payload, label);
            }
            case "9" -> {
                if (!isSessionEstablished) throw new IllegalStateException("❌ Сначала установите сессию (пункт 4)");
                type = 6;
                label = "DRAW_OFFER";
                payload = new JsonObject();
                System.out.println("\n📋 DRAW_OFFER (S0110) — параметры:");
                System.out.println("   • session_id (int)");
                System.out.println("   • client_id (int)");
                payload.addProperty("session_id", readInt("session_id"));
                payload.addProperty("client_id", readInt("client_id"));
                sendViaSession(type, payload, label);
            }
            case "10" -> {
                if (!isSessionEstablished) throw new IllegalStateException("❌ Сначала установите сессию (пункт 4)");
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
                sendViaSession(type, payload, label);
            }
            case "99" -> {
                System.out.print("\nВведите тип сообщения (0-15): ");
                type = readByte("type");
                System.out.print("Введите JSON-параметры: ");
                String json = scanner.nextLine().trim();
                payload = gson.fromJson(json, JsonObject.class);

                if (isSessionEstablished && type != 0 && type != 3 && type != 9 && type != 10) {
                    sendViaSession(type, payload, "MANUAL");
                } else {
                    sendEphemeral(type, payload, "MANUAL");
                }
            }
            default -> throw new IllegalArgumentException("Неизвестная команда: " + choice);
        }
    }

    // ==================== ВВОД ПАРАМЕТРОВ ====================
    private static int readInt(String paramName) {
        while (true) {
            System.out.print("   " + paramName + " (int) > ");
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("   ❌ Введите целое число"); }
        }
    }

    private static byte readByte(String paramName) {
        while (true) {
            System.out.print("   " + paramName + " (byte, -128..127) > ");
            try {
                int val = Integer.parseInt(scanner.nextLine().trim());
                if (val < -128 || val > 127) { System.out.println("   ❌ Значение вне диапазона byte"); continue; }
                return (byte) val;
            } catch (NumberFormatException e) { System.out.println("   ❌ Введите число"); }
        }
    }

    private static boolean readBool(String paramName) {
        while (true) {
            System.out.print("   " + paramName + " (true/false) > ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("true") || input.equals("1") || input.equals("да")) return true;
            else if (input.equals("false") || input.equals("0") || input.equals("нет")) return false;
            System.out.println("   ❌ Введите true/false, 1/0 или да/нет");
        }
    }

    private static String readString(String paramName) {
        System.out.print("   " + paramName + " (string) > ");
        return scanner.nextLine().trim();
    }

    // ==================== ВЫВОД ОТВЕТА ====================
    private static void printResponse(byte type, String json, String requestLabel) {
        String typeName = switch (type) {
            case 0 -> "PING_RESPONSE"; case 1 -> "CLIENT_CONNECTION"; case 2 -> "OPPONENT_UPDATE";
            case 3 -> "GAME_START"; case 4 -> "GAME_UPDATE"; case 5 -> "ILLEGAL_MOVE";
            case 6 -> "DRAW_DECISION"; case 7 -> "GAME_OVER"; case 8 -> "PAUSE";
            case 9 -> "SESSIONS_LIST_RESPONSE"; case 10 -> "CREATE_SESSION_RESPONSE";
            case 15 -> "EXCEPTION"; default -> "UNKNOWN(" + type + ")";
        };
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
        if (json.length() <= 44) return json + " ".repeat(44 - json.length());
        StringBuilder sb = new StringBuilder();
        String[] parts = json.split("(?<=\\}),|(?<=\\]),");
        int lineLen = 0;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (lineLen + part.length() > 44) { sb.append("\n  "); lineLen = 2; }
            sb.append(part);
            if (i < parts.length - 1) sb.append(",");
            lineLen += part.length() + 1;
        }
        String lastLine = sb.toString().substring(Math.max(0, sb.lastIndexOf("\n  ") + 3));
        if (lastLine.length() < 44) sb.append(" ".repeat(44 - lastLine.length()));
        return sb.toString();
    }

    private static void debug(String message) {
        String ts = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        System.err.println("[" + ts + " DEBUG] " + message);
    }
}