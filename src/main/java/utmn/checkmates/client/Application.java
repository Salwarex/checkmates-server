package utmn.checkmates.client;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class Application {
    public static void main(String[] args) {
        debug("[MAIN] Запуск клиента...");

        try (Socket socket = new Socket("localhost", 8228)) {
            debug("[SOCKET] Соединение установлено: " + socket.getLocalAddress() + " -> " + socket.getRemoteSocketAddress());
            socket.setSoTimeout(5000); // Увеличил таймаут для отладки

            // === ОТПРАВКА ЗАПРОСА ===
            debug("[SEND] Начинаю отправку пакета...");
            OutputStream out = socket.getOutputStream();

            byte packetType = 0; // PingRequest
            String jsonPayload = "{}";

            debug("[SEND] Тип пакета: " + packetType);
            debug("[SEND] JSON: " + jsonPayload);

            out.write(packetType);
            out.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            out.write('\n'); // <=== КРИТИЧНО: завершитель строки для server.readLine()
            out.flush();

            debug("[SEND] Пакет отправлен. Ожидание ответа...");

            // === ЧТЕНИЕ ОТВЕТА ===
            debug("[RECV] Начинаю чтение ответа...");
            InputStream in = socket.getInputStream();

            // 1. Читаем байт типа пакета
            debug("[RECV] Чтение байта типа...");
            int messageType = in.read();

            if (messageType == -1) {
                debug("[RECV] ⚠️ Получен EOF (-1) — сервер закрыл соединение без ответа");
                System.out.println("Сервер не вернул данных (EOF).");
                return;
            }

            debug("[RECV] ✓ Получен байт типа: " + (byte) messageType + " (0x" + Integer.toHexString(messageType) + ")");

            // 2. Читаем JSON-строку до \n (в том же формате, в котором сервер отправляет)
            debug("[RECV] Чтение JSON-строки (readLine)...");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String responseJson = reader.readLine();

            if (responseJson == null) {
                debug("[RECV] ⚠️ readLine() вернул null — соединение разорвано");
                System.out.println("Сервер не вернул данных (null).");
                return;
            }

            debug("[RECV] ✓ Получена JSON-строка, длина: " + responseJson.length() + " симв.");
            debug("[RECV] Сырой JSON: [" + responseJson + "]");

            // 3. Вывод результата
            System.out.println("\n=== ОТВЕТ СЕРВЕРА ===");
            System.out.println("Тип сообщения (byte): " + (byte) messageType);
            System.out.println("JSON ответ: " + responseJson);
            System.out.println("=====================\n");

            debug("[RECV] Обработка завершена.");

        } catch (SocketTimeoutException e) {
            debug("[ERROR] ⏱ Таймаут сокета: " + e.getMessage());
            System.err.println("Таймаут ожидания ответа от сервера.");
            e.printStackTrace();
        } catch (IOException e) {
            debug("[ERROR] ❌ Ошибка IO: " + e.getMessage());
            System.err.println("Ошибка соединения: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            debug("[ERROR] 💥 Неожиданная ошибка: " + e.getMessage());
            System.err.println("Необработанная ошибка: " + e.getMessage());
            e.printStackTrace();
        }

        debug("[MAIN] Клиент завершил работу.");
    }

    /**
     * Вспомогательный метод для отладки с временной меткой
     */
    private static void debug(String message) {
        String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
        );
        System.err.println("[" + timestamp + " DEBUG-Client] " + message);
    }
}