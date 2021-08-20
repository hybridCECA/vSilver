package utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logging {
    public static Logger getLogger(Class<?> loggingClass) {
        String className = loggingClass.getName();

        try {
            Logger logger = Logger.getLogger(className);
            FileHandler fileHandler = new FileHandler(className + ".logs.txt", true);
            fileHandler.setFormatter(new SimpleFormatter());

            logger.addHandler(fileHandler);

            return logger;
        } catch (IOException e) {
            e.printStackTrace();

            throw new RuntimeException("Logger instantiation failed");
        }
    }
}
