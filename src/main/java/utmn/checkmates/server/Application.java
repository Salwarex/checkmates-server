package utmn.checkmates.server;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import utmn.checkmates.server.network.tcp.NetworkServer;
import utmn.checkmates.server.utility.logger.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application {

    private static final int PORT = 8228;
    private static NetworkServer server;
    private static final ExecutorService POOL = Executors.newVirtualThreadPerTaskExecutor();

    public static void main(String[] args) {
        Logger.log("Application", "main", "Программа запущена!");
        server = new NetworkServer(PORT);
        server.run();
        Logger.log("Application", "main", "Программа завершила выполнение!");
    }

    private final static Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setLenient()
            .create();

    public static Gson getGson() {
        return GSON;
    }

    public static NetworkServer getServer() {
        return server;
    }

    public static ExecutorService getPool() {
        return POOL;
    }
}