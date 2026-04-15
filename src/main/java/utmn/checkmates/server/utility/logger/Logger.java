package utmn.checkmates.server.utility.logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final DateTimeFormatter LOG_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH_mm_ss-dd_MM_yyyy");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Color DEFAULT_COLOR = Color.WHITE;
    private static final Color OUT_COLOR = Color.YELLOW;
    private static final Color WARN_COLOR = Color.YELLOW;
    private static final Color ERR_COLOR = Color.RED;
    private static final Color LOG_COLOR = Color.BLUE;
    private static Path logFilePath;
    private static boolean logMode;

    static {
        try{
            initLogFilePath("Log_%s.txt".formatted(LocalDateTime.now().format(LOG_DATE_TIME_FORMATTER)));
        }catch (Exception e){
            throw new RuntimeException("Can't init Logger system: %s".formatted(e));
        }
        logMode = true;
    }

    public static void initLogFilePath(String fileName) throws URISyntaxException, IOException {
        URL jarUrl = Logger.class.getProtectionDomain().getCodeSource().getLocation();
        if (jarUrl == null) {
            throw new IllegalStateException("Не удалось определить местоположение JAR");
        }

        Path jarPath = Paths.get(jarUrl.toURI());
        Path jarDir = jarPath.getParent();

        if (jarDir == null) {
            jarDir = Paths.get(System.getProperty("user.dir"));
        }

        Path logsDir = jarDir.resolve("logs");
        Files.createDirectories(logsDir);

        logFilePath = logsDir.resolve(fileName);

        if (Files.notExists(logFilePath)) {
            Files.createFile(logFilePath);
        }
    }

    public static void out(String text){
        String formattedString = ("%s[%s] %s%s%n" + Color.RESET).formatted(OUT_COLOR, LocalDateTime.now().format(DATE_FORMATTER),
                DEFAULT_COLOR, text);

        output(formattedString);
    }

    public static void err(String text){
        String formattedString = ("%s[%s ERROR]%s %s%n" + Color.RESET).formatted(ERR_COLOR , LocalDateTime.now().format(DATE_FORMATTER),
                DEFAULT_COLOR, text);

        output(formattedString);
    }

    public static void wrn(String text){
        String formattedString = ("%s[%s WARN] %s%s%n" + Color.RESET).formatted(WARN_COLOR, LocalDateTime.now().format(DATE_FORMATTER),
                WARN_COLOR, text);

        output(formattedString);
    }

    public static void outL(String text){
        String formattedString = ("%s[%s] %s%s" + Color.RESET).formatted(LOG_COLOR, LocalDateTime.now().format(DATE_FORMATTER),
                DEFAULT_COLOR, text);

        output(formattedString);
    }

    public static void errL(String text){
        String formattedString = ("%s[%s ERROR]%s %s" + Color.RESET).formatted(ERR_COLOR, LocalDateTime.now().format(DATE_FORMATTER),
                DEFAULT_COLOR, text);

        output(formattedString);
    }

    public static void wrnL(String text){
        String formattedString = ("%s[%s WARN] %s%s" + Color.RESET).formatted(WARN_COLOR, LocalDateTime.now().format(DATE_FORMATTER),
                WARN_COLOR, text);

        output(formattedString);
    }

    public static void log(String className, String methodName, String text){
        if(!logMode) return;

        String formattedString = ("%s[%s LOG-%s#%s] %s%s%n" + Color.RESET).formatted(LOG_COLOR, LocalDateTime.now().format(DATE_FORMATTER),
                className, methodName,
                Color.CYAN, text);

        output(formattedString);
    }

    private static void output(String str){
        System.out.print(str);
        fileOutput(str);
    }

    private static void fileOutput(String str){
        try{
            if (Files.notExists(logFilePath)) {
                Files.createFile(logFilePath);
            }

            Files.writeString(logFilePath, str,
                    java.nio.file.StandardOpenOption.APPEND,
                    java.nio.file.StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("Can't write logs: %s".formatted(e));
        }
    }

    public enum Color{
        BOLD("\u001B[1m"),
        FAINT("\u001B[2m"),
        ITALIC("\u001B[3m"),
        UNDERLINE("\u001B[4m"),
        BLINK("\u001B[5m"),
        REVERSE("\u001B[7m"),
        HIDDEN("\u001B[8m"),
        STRIKETHROUGH("\u001B[9m"),

        BRIGHT_BLACK("\u001B[90m"),
        BRIGHT_RED("\u001B[91m"),
        BRIGHT_GREEN("\u001B[92m"),
        BRIGHT_YELLOW("\u001B[93m"),
        BRIGHT_BLUE("\u001B[94m"),
        BRIGHT_PURPLE("\u001B[95m"),
        BRIGHT_CYAN("\u001B[96m"),
        BRIGHT_WHITE("\u001B[97m"),

        RESET("\u001B[0m"),
        BLACK("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),
        CYAN("\u001B[36m"),
        WHITE("\u001B[37m"),

        BLACK_BACKGROUND("\u001B[40m"),
        RED_BACKGROUND("\u001B[41m"),
        GREEN_BACKGROUND("\u001B[42m"),
        YELLOW_BACKGROUND("\u001B[43m"),
        BLUE_BACKGROUND("\u001B[44m"),
        PURPLE_BACKGROUND("\u001B[45m"),
        CYAN_BACKGROUND("\u001B[46m"),
        WHITE_BACKGROUND("\u001B[47m");

        final String code;

        Color(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

}
