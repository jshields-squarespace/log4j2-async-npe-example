package example;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;

public class App {

    public static void main(String[] args) {
        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

        Message message = new StringFormattedMessage("%s", new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("toString exception");
            }
        });

        Logger logger = LogManager.getLogger();
        ExtendedLogger extendedLogger = (ExtendedLogger) logger;

        try {
            extendedLogger.logMessage(App.class.getName(), Level.ERROR, null, message, null);
        } catch (Exception e) {
            // Catch RuntimeException from toString
        }
    }
}
