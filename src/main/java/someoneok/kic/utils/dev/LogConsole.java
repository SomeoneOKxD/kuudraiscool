package someoneok.kic.utils.dev;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogConsole {
    private static LogConsole INSTANCE;

    public static void init() { if (INSTANCE == null) INSTANCE = new LogConsole(); }
    public static LogConsole get() { return INSTANCE; }
    public static void showConsole() { if (INSTANCE != null) INSTANCE.show(); }

    private final JFrame frame;
    private final StyledDocument allDoc, infoDoc, warnDoc, errorDoc;

    public LogConsole() {
        frame = new JFrame("KIC Log Console");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();

        JTextPane allPane = createPane();
        JTextPane infoPane = createPane();
        JTextPane warnPane = createPane();
        JTextPane errorPane = createPane();

        allDoc = allPane.getStyledDocument();
        infoDoc = infoPane.getStyledDocument();
        warnDoc = warnPane.getStyledDocument();
        errorDoc = errorPane.getStyledDocument();

        GuiLogAppender.setOutputDocs(allDoc, infoDoc, warnDoc, errorDoc);

        tabs.addTab("All", new JScrollPane(allPane));
        tabs.addTab("Info", new JScrollPane(infoPane));
        tabs.addTab("Warn", new JScrollPane(warnPane));
        tabs.addTab("Error", new JScrollPane(errorPane));

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearAll());

        JButton saveButton = new JButton("Save to File");
        saveButton.addActionListener(this::saveToFile);

        JButton sendButton = new JButton("Send Logs To Discord");
        sendButton.addActionListener(e -> sendLogs());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controls.setBackground(new Color(30, 30, 30));
        controls.add(saveButton);
        controls.add(clearButton);
        controls.add(sendButton);

        frame.add(tabs, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
    }

    private JTextPane createPane() {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setBackground(new Color(40, 40, 40));
        pane.setForeground(new Color(220, 220, 220));
        pane.setFont(new Font("Consolas", Font.PLAIN, 13));
        DefaultCaret caret = (DefaultCaret) pane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        return pane;
    }

    private void clearAll() {
        try {
            allDoc.remove(0, allDoc.getLength());
            infoDoc.remove(0, infoDoc.getLength());
            warnDoc.remove(0, warnDoc.getLength());
            errorDoc.remove(0, errorDoc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void saveToFile(ActionEvent e) {
        try {
            String text = allDoc.getText(0, allDoc.getLength());
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "logs/kic-log-" + timestamp + ".log";

            FileWriter writer = new FileWriter(filename);
            writer.write(text);
            writer.close();

            JOptionPane.showMessageDialog(frame, "Saved to " + filename);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to save log");
        }
    }

    private void sendLogs() {
        try {
            String fullText = allDoc.getText(0, allDoc.getLength());
            String[] lines = fullText.split("\\r?\\n");
            List<String> logLines = new ArrayList<>();
            for (String line : lines) if (!line.trim().isEmpty()) logLines.add(line);
            TesterStuff.sendLogs(logLines);
            JOptionPane.showMessageDialog(frame, "Logs sent to discord!");
        } catch (BadLocationException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to send logs.");
        }
    }

    private void show() {
        if (!TesterStuff.testerMode) return;
        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
            frame.toFront();
            frame.requestFocus();
        });

        frame.setVisible(true);
        frame.toFront();
    }
}
