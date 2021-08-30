package utils;

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

    public void disable() {
        enabled = false;
    }
}
