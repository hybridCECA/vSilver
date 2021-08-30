package utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logging {
    private static final Map<String, Logger> loggerCache = new HashMap<>();

    public static VLogger getLogger(Class<?> loggingClass) {
        String className = loggingClass.getName();

        if (loggerCache.containsKey(className)) {
            Logger logger = loggerCache.get(className);
            return new VLogger(logger);
        }

        try {
            Logger logger = Logger.getLogger(className);
            FileHandler fileHandler = new FileHandler(className + ".logs.txt", true);
            fileHandler.setFormatter(new SimpleFormatter());

            logger.addHandler(fileHandler);

            loggerCache.put(className, logger);

            return new VLogger(logger);
        } catch (IOException e) {
            e.printStackTrace();

            throw new RuntimeException("Logger instantiation failed");
        }
    }
}
