package utils;

import coinsources.CoinSources;
import coinsources.CoinSourcesImpl;
import nicehash.NHApi;
import nicehash.NHApiImpl;
import nicehash.PriceTools;
import nicehash.PriceToolsImpl;
import services.MaxProfit;
import services.MaxProfitImpl;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class SingletonFactory {
    private static final Map<Class<?>, Class<?>> implementationMap = Map.of(
            CoinSources.class, CoinSourcesImpl.class,
            NHApi.class, NHApiImpl.class,
            PriceTools.class, PriceToolsImpl.class,
            MaxProfit.class, MaxProfitImpl.class
    );

    private static final Map<Class<?>, Object> instanceMap = new HashMap<>();

    public static <T> void setInstance(Class<T> requestedInterface, T instance) {
        instanceMap.put(requestedInterface, instance);
    }

    // Singleton
    public static <T> T getInstance(Class<T> requestedInterface) {
        try {
            if (instanceMap.containsKey(requestedInterface)) {
                return (T) instanceMap.get(requestedInterface);
            } else {
                Class<T> implementation = (Class<T>) implementationMap.get(requestedInterface);
                Constructor<T> constructor = implementation.getConstructor();
                T instance = constructor.newInstance();

                instanceMap.put(requestedInterface, instance);

                return instance;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Get instance failed");
        }
    }
}
