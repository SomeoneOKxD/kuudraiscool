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
    private static final List<BufferedLog> guiQueue = new ArrayList<>();
    private static final int BUFFER_LIMIT = 300;
    private static final int MAX_LINES = 1000;

    private static final Timer guiFlushTimer = new Timer(100, e -> {
        try {
            flushGuiQueue();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    });

    static {
        guiFlushTimer.setRepeats(true);
        guiFlushTimer.start();
    }

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
            List<BufferedLog> toFlush;
            synchronized (logBuffer) {
                int start = Math.max(0, logBuffer.size() - MAX_LINES);
                toFlush = new ArrayList<>(logBuffer.subList(start, logBuffer.size()));
                logBuffer.clear();
            }
            flushLogsToDocs(toFlush);
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

        synchronized (logBuffer) {
            if (logBuffer.size() >= BUFFER_LIMIT) {
                logBuffer.remove(0);
            }
            logBuffer.add(new BufferedLog(message, level));
        }

        if (allDoc != null) {
            synchronized (guiQueue) {
                guiQueue.add(new BufferedLog(message, level));
            }
        }
    }

    private static void flushGuiQueue() {
        if (allDoc == null) return;

        List<BufferedLog> logsToFlush;
        synchronized (guiQueue) {
            if (guiQueue.isEmpty()) return;
            logsToFlush = new ArrayList<>(guiQueue);
            guiQueue.clear();
        }

        flushLogsToDocs(logsToFlush);
    }

    private static void flushLogsToDocs(List<BufferedLog> logs) {
        if (logs.isEmpty()) return;

        StringBuilder allBuilder = new StringBuilder();
        StringBuilder infoBuilder = new StringBuilder();
        StringBuilder warnBuilder = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();

        for (BufferedLog log : logs) {
            allBuilder.append(log.message);
            switch (log.level) {
                case "INFO": infoBuilder.append(log.message); break;
                case "WARN": warnBuilder.append(log.message); break;
                case "ERROR": errorBuilder.append(log.message); break;
            }
        }

        SwingUtilities.invokeLater(() -> {
            try {
                if (allBuilder.length() > 0) insertIntoDoc(allDoc, allBuilder.toString(), "ALL");
                if (infoBuilder.length() > 0) insertIntoDoc(infoDoc, infoBuilder.toString(), "INFO");
                if (warnBuilder.length() > 0) insertIntoDoc(warnDoc, warnBuilder.toString(), "WARN");
                if (errorBuilder.length() > 0) insertIntoDoc(errorDoc, errorBuilder.toString(), "ERROR");
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private static void insertIntoDoc(StyledDocument doc, String message, String level) throws BadLocationException {
        Color color;
        switch (level) {
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

        doc.insertString(doc.getLength(), message, style);
        trimLines(doc);
    }

    private static void trimLines(StyledDocument doc) throws BadLocationException {
        Element root = doc.getDefaultRootElement();
        int lineCount = root.getElementCount();
        if (lineCount > MAX_LINES) {
            int linesToRemove = lineCount - MAX_LINES;
            int endOffset = root.getElement(linesToRemove - 1).getEndOffset();
            doc.remove(0, endOffset);
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
