package someoneok.kic.utils.dev;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Plugin(name = "GuiLogAppender", category = "Core", elementType = "appender", printObject = true)
public class GuiLogAppender extends AbstractAppender {
    private static StyledDocument allDoc;
    private static StyledDocument infoDoc;
    private static StyledDocument warnDoc;
    private static StyledDocument errorDoc;

    private static final List<BufferedLog> logBuffer = new ArrayList<>();

    private static final int BUFFER_LIMIT = 300;
    private static final int MAX_LINES = 1000;

    protected GuiLogAppender(String name, Layout<? extends Serializable> layout) {
        super(name, null, layout, false);
    }

    public static void init() {
        GuiLogAppender appender = createAppender();
        appender.start();

        attachToLogger("KIC.INFO", appender);
        attachToLogger("KIC.WARN", appender);
        attachToLogger("KIC.ERROR", appender);
    }

    private static void attachToLogger(String name, GuiLogAppender appender) {
        Logger logger = (Logger) LogManager.getLogger(name);
        if (logger.getAppenders().get("GuiLogAppender") == null) {
            logger.addAppender(appender);
        }
    }

    public static void setOutputDocs(StyledDocument all, StyledDocument info, StyledDocument warn, StyledDocument error) {
        allDoc = all;
        infoDoc = info;
        warnDoc = warn;
        errorDoc = error;

        SwingUtilities.invokeLater(() -> {
            synchronized (logBuffer) {
                for (BufferedLog buffered : logBuffer) {
                    dispatchToDocs(buffered.message, buffered.level);
                }
                logBuffer.clear();
            }
        });
    }

    @PluginFactory
    public static GuiLogAppender createAppender() {
        return new GuiLogAppender("GuiLogAppender", null);
    }

    @Override
    public void append(LogEvent event) {
        String message = "[" + event.getLevel() + "] " + event.getMessage().getFormattedMessage() + "\n";
        String level = event.getLevel().name();

        if (allDoc == null || infoDoc == null || warnDoc == null || errorDoc == null) {
            synchronized (logBuffer) {
                if (logBuffer.size() >= BUFFER_LIMIT) {
                    logBuffer.remove(0);
                }
                logBuffer.add(new BufferedLog(message, level));
            }
            return;
        }

        SwingUtilities.invokeLater(() -> dispatchToDocs(message, level));
    }

    private static void dispatchToDocs(String message, String level) {
        insertIntoDoc(allDoc, message, level);

        switch (level) {
            case "INFO": insertIntoDoc(infoDoc, message, level); break;
            case "WARN": insertIntoDoc(warnDoc, message, level); break;
            case "ERROR": insertIntoDoc(errorDoc, message, level); break;
        }
    }

    private static void insertIntoDoc(StyledDocument doc, String message, String level) {
        Color color;
        switch (level)
        {
            case "INFO": color = new Color(150, 200, 255); break;
            case "WARN": color = new Color(255, 200, 100); break;
            case "ERROR": color = new Color(255, 100, 100); break;
            default: color = new Color(220, 220, 220); break;
        }

        Style style = doc.getStyle(level);
        if (style == null) {
            style = doc.addStyle(level, null);
            StyleConstants.setForeground(style, color);
        }

        try {
            doc.insertString(doc.getLength(), message, style);
            trimLines(doc);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private static void trimLines(StyledDocument doc) throws BadLocationException {
        Element root = doc.getDefaultRootElement();
        int lineCount = root.getElementCount();
        if (lineCount > MAX_LINES) {
            Element first = root.getElement(0);
            doc.remove(0, first.getEndOffset());
        }
    }

    public static List<String> getCurrentLogs() {
        List<String> logs = new ArrayList<>();

        if (allDoc == null) {
            synchronized (logBuffer) {
                for (BufferedLog buffered : logBuffer) {
                    logs.add(buffered.message.trim());
                }
            }
            return logs;
        }

        try {
            String text = allDoc.getText(0, allDoc.getLength());
            String[] lines = text.split("\\r?\\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    logs.add(line.trim());
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return logs;
    }

    private static class BufferedLog {
        final String message;
        final String level;

        BufferedLog(String message, String level) {
            this.message = message;
            this.level = level;
        }
    }
}
