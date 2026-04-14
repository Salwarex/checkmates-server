package utmn.checkmates.server;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Application {

    public static void main(String[] args) {
        System.out.println("Hello world!");
    }

    private final static Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static Gson getGson() {
        return GSON;
    }
}