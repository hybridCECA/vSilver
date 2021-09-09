package utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class VLogger {
    private final Logger logger;
    private boolean enabled;

    public VLogger(Logger logger) {
        this.logger = logger;
        enabled = true;
    }

    public void info(String string) {
        if (enabled) {
            logger.info(string);
        }
    }

    public void error(String string) {
        if (enabled) {
            logger.severe(string);
        }
    }

    public void error(Exception e) {
        error(exceptionToString(e));
    }

    private static String exceptionToString(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public void disable() {
        enabled = false;
    }
}
