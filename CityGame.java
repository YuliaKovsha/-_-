package com.kovsha;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class CityGame {
    // Дані гри
    private static HashSet<String> usedCities = new HashSet<>();
    private static String lastCity = "";
    private static List<String> citiesList = new ArrayList<>();
    private static int playerScore = 0;
    private static int botScore = 0;

    // Додала показ вікна з правилами гри
    private static void showHowToPlay() {
        JDialog dialog = new JDialog();
        dialog.setTitle("📖 Як грати в міста");
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);
        dialog.setBackground(new Color(20, 25, 45));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(20, 25, 45));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Додала текст правил
        JTextArea rulesText = new JTextArea();
        rulesText.setEditable(false);
        rulesText.setBackground(new Color(30, 35, 55));
        rulesText.setForeground(Color.WHITE);
        rulesText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rulesText.setBorder(BorderFactory.createLineBorder(new Color(70, 75, 100), 1));

        String rules =
                "🎮 ПРАВИЛА ГРИ В МІСТА\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 МЕТА ГРИ:\n" +
                        "   Назвати більше міст, ніж бот.\n\n" +
                        "📌 ЯК ГРАТИ:\n" +
                        "   1. Введи назву міста в поле вводу\n" +
                        "   2. Натисни кнопку «Відповісти» або клавішу Enter\n" +
                        "   3. Бот повинен назвати місто на останню букву твого міста\n" +
                        "   4. Якщо бот не може відповісти — ти перемагаєш!\n\n" +
                        "📌 ОСОБЛИВОСТІ:\n" +
                        "   • Міста не можна повторювати\n" +
                        "   • Букви «ь», «й», «и» ігноруються (береться попередня буква)\n" +
                        "   • Наприклад: Тернопіль → остання буква «л» (не «ь»)\n\n" +
                        "🔘 КНОПКИ:\n" +
                        "   • «Відповісти» — надіслати місто\n" +
                        "   • «Підказка» — показати можливе місто\n" +
                        "   • «Нова гра» — почати гру заново\n\n" +
                        "🏆 РАХУНОК:\n" +
                        "   Кожне назване місто додає 1 бал гравцю або боту.\n\n" +
                        "💡 ПОРАДА:\n" +
                        "   Намагайся використовувати рідкісні букви (є, ї, ґ), \n" +
                        "   щоб боту було важче знайти відповідь!\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "🎉 Бажаємо гарної гри! 🎉";

        rulesText.setText(rules);
        rulesText.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(rulesText);
        scrollPane.setBorder(null);

        // Додала кнопку закриття
        JButton closeButton = createStyledButton("Зрозуміло! Грати →", new Color(0, 130, 200));
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(20, 25, 45));
        buttonPanel.add(closeButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    // Компоненти інтерфейсу
    private static JTextArea historyArea;
    private static JTextField inputField;
    private static JButton submitButton;
    private static JButton hintButton;
    private static JButton newGameButton;
    private static JLabel playerScoreLabel;
    private static JLabel botScoreLabel;

    public static void main(String[] args) {
        // Завантажила міста з файлу
        loadCitiesFromFile();

        JFrame frame = new JFrame("🏙️ ГРА В МІСТА");

        try {
            ImageIcon icon = new ImageIcon(CityGame.class.getResource("/icon.png"));
            frame.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.out.println("Іконку не знайдено, використовуємо стандартну");
        }
        frame.setSize(750, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Додала головну панель
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(20, 25, 45));

        // Додала верхню панель (там дебали)
        JPanel topPanel = createScorePanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Додала центральну панель (історія)
        JPanel centerPanel = createHistoryPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Додала нижню панель (там де ввід та кнопки)
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);

        appendToHistory("🎮 Ласкаво просимо до ГРИ В МІСТА!\n", new Color(255, 215, 0));
        appendToHistory("📋 Завантажено міст: " + citiesList.size() + "\n", new Color(100, 200, 255));
        appendToHistory("💡 Введи перше місто та натисни Enter!\n\n", new Color(200, 200, 200));
    }

    private static JPanel createScorePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.setBackground(new Color(15, 20, 35));
        panel.setBorder(new EmptyBorder(15, 25, 15, 25));

        playerScoreLabel = new JLabel("👤 ГРАВЕЦЬ: 0");
        playerScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        playerScoreLabel.setForeground(new Color(100, 255, 150));
        playerScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);

        botScoreLabel = new JLabel("🤖 БОТ: 0");
        botScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        botScoreLabel.setForeground(new Color(255, 120, 120));
        botScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(playerScoreLabel);
        panel.add(botScoreLabel);

        return panel;
    }

    private static JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 25, 45));
        panel.setBorder(new EmptyBorder(10, 25, 10, 25));

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        historyArea.setBackground(new Color(30, 35, 55));
        historyArea.setForeground(Color.WHITE);
        historyArea.setCaretColor(Color.WHITE);
        historyArea.setBorder(BorderFactory.createLineBorder(new Color(70, 75, 100), 1));

        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 35, 55));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 20, 35));
        panel.setBorder(new EmptyBorder(15, 25, 25, 25));

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        inputField.setBackground(new Color(40, 45, 65));
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 95, 120), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 12, 0));  // ← 4 кнопки!
        buttonPanel.setBackground(new Color(15, 20, 35));
        buttonPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton rulesButton = createStyledButton("📖 Правила", new Color(100, 100, 140));
        JButton submitButton = createStyledButton("✏️ Відповісти", new Color(0, 130, 200));
        JButton hintButton = createStyledButton("💡 Підказка", new Color(220, 140, 0));
        JButton newGameButton = createStyledButton("🔄 Нова гра", new Color(90, 90, 110));

        buttonPanel.add(rulesButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(hintButton);
        buttonPanel.add(newGameButton);

        panel.add(inputField, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        submitButton = submitButton;
        hintButton = hintButton;
        newGameButton = newGameButton;

        // Додаємо обробники
        rulesButton.addActionListener(e -> showHowToPlay());
        submitButton.addActionListener(e -> processPlayerTurn());
        hintButton.addActionListener(e -> showHint());
        newGameButton.addActionListener(e -> resetGame());
        inputField.addActionListener(e -> processPlayerTurn());

        return panel;
    }

    private static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private static void setupEventHandlers() {
        submitButton.addActionListener(e -> processPlayerTurn());
        hintButton.addActionListener(e -> showHint());
        newGameButton.addActionListener(e -> resetGame());
        inputField.addActionListener(e -> processPlayerTurn());
    }

    private static void processPlayerTurn() {
        String playerCity = inputField.getText().trim();
        inputField.setText("");

        if (playerCity.isEmpty()) {
            appendToHistory("❌ Введи назву міста!\n", new Color(255, 100, 100));
            return;
        }

        if (!isCityExists(playerCity)) {
            appendToHistory("❌ '" + playerCity + "' — невідоме місто!\n", new Color(255, 100, 100));
            return;
        }

        if (!lastCity.isEmpty()) {
            char requiredLetter = getLastLetter(lastCity);
            char firstLetter = playerCity.charAt(0);
            if (Character.toLowerCase(requiredLetter) != Character.toLowerCase(firstLetter)) {
                appendToHistory("❌ Потрібне місто на букву '" + requiredLetter + "'!\n", new Color(255, 100, 100));
                return;
            }
        }

        if (usedCities.contains(playerCity.toLowerCase())) {
            appendToHistory("❌ Місто '" + playerCity + "' вже називали!\n", new Color(255, 100, 100));
            return;
        }

        appendToHistory("👤 Ви: " + playerCity + "\n", new Color(100, 255, 150));
        usedCities.add(playerCity.toLowerCase());
        lastCity = playerCity;
        playerScore++;
        updateScores();

        String botCity = findBotCity();
        if (botCity == null) {
            appendToHistory("\n🎉🤖 Бот не знайшов місто! ВИ ПЕРЕМОГЛИ! 🎉\n", new Color(255, 215, 0));
            submitButton.setEnabled(false);
            hintButton.setEnabled(false);
        } else {
            appendToHistory("🤖 Бот: " + botCity + "\n", new Color(255, 150, 150));
            usedCities.add(botCity.toLowerCase());
            lastCity = botCity;
            botScore++;
            updateScores();
        }
    }

    private static void showHint() {
        if (lastCity.isEmpty()) {
            appendToHistory("💡 Підказка: введи будь-яке місто зі списку!\n", new Color(255, 200, 100));
            return;
        }

        char requiredLetter = getLastLetter(lastCity);
        String hintCity = findBotCity();

        if (hintCity == null) {
            appendToHistory("💡 Підказка: бот не може знайти місто. Нічия!\n", new Color(255, 200, 100));
        } else {
            appendToHistory("💡 Підказка: спробуй місто на букву '" + requiredLetter + "'.\n", new Color(255, 200, 100));
            appendToHistory("   Наприклад: " + hintCity + "\n", new Color(255, 200, 100));
        }
    }

    private static String findBotCity() {
        if (lastCity.isEmpty()) return null;

        char neededLetter = getLastLetter(lastCity);

        for (String city : citiesList) {
            char firstLetter = city.charAt(0);
            if (Character.toLowerCase(firstLetter) == Character.toLowerCase(neededLetter)
                    && !usedCities.contains(city.toLowerCase())) {
                return city;
            }
        }
        return null;
    }

    private static char getLastLetter(String city) {
        if (city == null || city.isEmpty()) return '?';

        String lowerCity = city.toLowerCase();
        char last = lowerCity.charAt(lowerCity.length() - 1);

        if (last == 'ь' || last == 'й' || last == 'и') {
            if (lowerCity.length() > 1) {
                return lowerCity.charAt(lowerCity.length() - 2);
            }
        }
        return last;
    }

    private static boolean isCityExists(String city) {
        for (String c : citiesList) {
            if (c.equalsIgnoreCase(city)) {
                return true;
            }
        }
        return false;
    }

    private static void updateScores() {
        playerScoreLabel.setText("👤 ГРАВЕЦЬ: " + playerScore);
        botScoreLabel.setText("🤖 БОТ: " + botScore);
    }

    private static void resetGame() {
        usedCities.clear();
        lastCity = "";
        playerScore = 0;
        botScore = 0;
        updateScores();
        submitButton.setEnabled(true);
        hintButton.setEnabled(true);
        historyArea.setText("");

        appendToHistory("🔄 НОВА ГРА!\n", new Color(255, 215, 0));
        appendToHistory("📋 Завантажено міст: " + citiesList.size() + "\n", new Color(100, 200, 255));
        appendToHistory("💡 Введи перше місто!\n\n", new Color(200, 200, 200));
    }

    private static void appendToHistory(String text, Color color) {
        historyArea.append(text);
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
    }

    private static void loadCitiesFromFile() {
        try {
            InputStream is = CityGame.class.getResourceAsStream("/cities.txt");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        citiesList.add(trimmed);
                    }
                }
                reader.close();
            }

            if (citiesList.isEmpty()) {
                String[] defaultCities = {"Київ", "Харків", "Одеса", "Дніпро", "Львів", "Вінниця"};
                citiesList = new ArrayList<>(Arrays.asList(defaultCities));
            }
        } catch (IOException e) {
            String[] defaultCities = {"Київ", "Харків", "Одеса", "Дніпро", "Львів"};
            citiesList = new ArrayList<>(Arrays.asList(defaultCities));
        }
    }
}
