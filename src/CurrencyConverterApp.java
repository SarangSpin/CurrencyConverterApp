
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONObject;

// Interface for currency converters
interface CurrencyConverter {
    double convert(double amount, String fromCurrency, String toCurrency);
    void updateExchangeRatesFromAPIResponse(String apiResponse);
}

// Concrete currency converter class
class BasicCurrencyConverter implements CurrencyConverter {
    private Map<String, Double> exchangeRates;

    public BasicCurrencyConverter() {
        exchangeRates = new HashMap<>();
    }

    @Override
    public double convert(double amount, String fromCurrency, String toCurrency) {
        if (exchangeRates.containsKey(fromCurrency) && exchangeRates.containsKey(toCurrency)) {
            return amount * exchangeRates.get(toCurrency) / exchangeRates.get(fromCurrency);
        }
        return amount; // No conversion needed for the same currency
    }

    @Override
    public void updateExchangeRatesFromAPIResponse(String apiResponse) {
        // Parse the API response and update exchange rates in the map
        // Example code to parse JSON response and update rates:
        JSONObject jsonObject = new JSONObject(apiResponse);
        JSONObject rates = jsonObject.getJSONObject("rates");
        System.out.println(rates);
        exchangeRates.clear();
        for (String curr : rates.keySet()) {
            exchangeRates.put(curr, rates.getDouble(curr));
        }
    }
}

class CurrencyRateUpdater {
    private CurrencyConverter currencyConverter;
    private String apiUrl;
    private boolean errorOccurred = false;
    private boolean internetAccess = false; // Internet access status

    public CurrencyRateUpdater(CurrencyConverter currencyConverter, String apiUrl) {
        this.currencyConverter = currencyConverter;
        this.apiUrl = apiUrl;
    }

    public boolean isErrorOccurred() {
        return errorOccurred;
    }

    public boolean hasInternetAccess() {
        return internetAccess;
    }

    public void updateRatesFromAPI() {
        try {
            // Make an API request to fetch updated exchange rates
            System.out.println("Running...");
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Internet access is available if the first API request is successful
                internetAccess = true;
                // Parse the response
                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder response = new StringBuilder();

                do {
                    response.append(scanner.nextLine());
                } while (scanner.hasNextLine());
                scanner.close();

                // Update exchange rates in the currency converter
                currencyConverter.updateExchangeRatesFromAPIResponse(response.toString());
            } else {
                System.out.println("Failed to fetch exchange rates. HTTP Error: " + responseCode);
                errorOccurred = true;
                internetAccess = false;// Set the error flag
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorOccurred = true;
            internetAccess = false;// Set the error flag
        }
    }
}

public class CurrencyConverterApp extends JFrame {
    private JTextField amountTextField;
    private JComboBox<String> fromCurrencyComboBox;
    private JComboBox<String> toCurrencyComboBox;
    private JLabel resultLabel;
    private JLabel internetStatusLabel; // Internet access status label
    private CurrencyConverter converter;
    private CurrencyRateUpdater rateUpdater;
    private JToggleButton darkModeToggleButton; // Toggle button for dark mode

    public CurrencyConverterApp(CurrencyConverter converter) {
        this.converter = converter;
        rateUpdater = new CurrencyRateUpdater(converter, "http://api.exchangeratesapi.io/v1/latest?access_key=128f796389da3a3162aed70802116a19&format=1");

        // Start the rate update process in a separate thread
        Thread updaterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                rateUpdater.updateRatesFromAPI();
            }
        });
        updaterThread.start();

        setTitle("Currency Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(6, 3, 30, 15)); // Increased rows to accommodate internet access status
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Initialize UI components
        String[] currencies = new String[]{"FJD","MXN","STD","LVL","SCR","CDF","BBD","GTQ","CLP","HNL","UGX","ZAR","TND","SLE","CUC","BSD","SLL","SDG","IQD","CUP","GMD","TWD","RSD","DOP","KMF","MYR","FKP","XOF","GEL","BTC","UYU","MAD","CVE","TOP","AZN","OMR","PGK","KES","SEK","BTN","UAH","GNF","ERN","MZN","ARS","QAR","IRR","MRO","CNY","THB","UZS","XPF","BDT","LYD","BMD","KWD","PHP","RUB","PYG","ISK","JMD","COP","MKD","USD","DZD","PAB","GGP","SGD","ETB","JEP","KGS","SOS","VEF","VUV","LAK","BND","ZMK","XAF","LRD","XAG","CHF","HRK","ALL","DJF","VES","ZMW","TZS","VND","XAU","AUD","ILS","GHS","GYD","KPW","BOB","KHR","MDL","IDR","KYD","AMD","BWP","SHP","TRY","LBP","TJS","JOD","AED","HKD","RWF","EUR","LSL","DKK","CAD","BGN","MMK","MUR","NOK","SYP","IMP","ZWL","GIP","RON","LKR","NGN","CRC","CZK","PKR","XCD","ANG","HTG","BHD","KZT","SRD","SZL","LTL","SAR","TTD","YER","MVR","AFN","INR","AWG","KRW","NPR","JPY","MNT","AOA","PLN","GBP","SBD","BYN","HUF","BYR","BIF","MWK","MGA","XDR","BZD","BAM","EGP","MOP","NAD","SSP","NIO","PEN","NZD","WST","TMT","CLF","BRL"};
        Arrays.sort(currencies);
        amountTextField = new JTextField();
        fromCurrencyComboBox = new JComboBox<>(currencies);
        toCurrencyComboBox = new JComboBox<>(currencies);
        JButton convertButton = new JButton("Convert");
        convertButton.setBackground(new Color(0, 123, 255));
        convertButton.setForeground(Color.WHITE);
        resultLabel = new JLabel();
        resultLabel.setForeground(new Color(34, 139, 34));

        // Set font for labels
        Font labelFont = new Font("Arial", Font.BOLD, 14);
        JLabel[] labels = {new JLabel("Amount:"), new JLabel("From Currency:"), new JLabel("To Currency:"), new JLabel("Internet:")};
        for (JLabel label : labels) {
            label.setFont(labelFont);
            label.setForeground(Color.BLUE);
        }

        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertCurrency();
            }
        });
        JButton updateRatesButton = new JButton("Update Rates");
        updateRatesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateExchangeRates(mainPanel);
            }
        });

        internetStatusLabel = new JLabel("Checking...");
        internetStatusLabel.setFont(labelFont);
        internetStatusLabel.setForeground(Color.BLUE);

        // Dark mode toggle button
        darkModeToggleButton = new JToggleButton("Dark Mode");
        darkModeToggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleDarkMode(mainPanel);
            }
        });

        // Add components to the main panel
        mainPanel.add(labels[0]);
        mainPanel.add(labels[1]);
        mainPanel.add(labels[2]);
        mainPanel.add(amountTextField);
        mainPanel.add(fromCurrencyComboBox);
        mainPanel.add(toCurrencyComboBox);
        mainPanel.add(labels[3]);
        mainPanel.add(internetStatusLabel); // Internet access status label
        mainPanel.add(darkModeToggleButton); // Dark mode toggle button
        mainPanel.add(convertButton);
        mainPanel.add(new JLabel());
        mainPanel.add(new JLabel());
        mainPanel.add(resultLabel);
        mainPanel.add(new JLabel());
        mainPanel.add(new JLabel());
        mainPanel.add(updateRatesButton);
        mainPanel.add(new JLabel());

        add(mainPanel);
        pack();
        setMinimumSize(new Dimension(1000, 400));
    }

    private void convertCurrency() {
        try {
            double amount = Double.parseDouble(amountTextField.getText());
            String fromCurrency = (String) fromCurrencyComboBox.getSelectedItem();
            String toCurrency = (String) toCurrencyComboBox.getSelectedItem();

            double convertedAmount = converter.convert(amount, fromCurrency, toCurrency);
            DecimalFormat df = new DecimalFormat("#.#####");
            resultLabel.setText("Converted Amount: " + df.format(convertedAmount) + " " + toCurrency);
        } catch (NumberFormatException ex) {
            resultLabel.setText("Invalid input");
        }
    }

    private void updateExchangeRates(JPanel mainPanel) {
        // Show "Running" pop-up
        JOptionPane runningDialog = new JOptionPane("Running...", JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = runningDialog.createDialog("Updating Rates");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setModal(true);

        // Set the parent frame as the owner of the dialog
        dialog.setAlwaysOnTop(true);
        dialog.setLocationRelativeTo(this);

        // Create an ExecutorService for multithreading
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        // Use Future to track the background task
        Future<?> updateTask = executorService.submit(new Runnable() {
            @Override
            public void run() {
                // Simulate API request and update
                rateUpdater.updateRatesFromAPI();
                if (rateUpdater.isErrorOccurred()) {
                    // Close the "Running" pop-up and show a network error message
                    checkInternetAccess();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(mainPanel, "Network Error: Unable to fetch exchange rates", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Update internet access status and show success message
                    checkInternetAccess();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(mainPanel, "Success", "Update Complete", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Show the "Running" pop-up
        dialog.setVisible(true);

        // Shutdown the ExecutorService when the task is done
        executorService.shutdown();
    }

    private void checkInternetAccess() {
        // Check internet access based on the first API request
        if (rateUpdater.hasInternetAccess()) {
            internetStatusLabel.setText("Internet: Accessible");
            internetStatusLabel.setForeground(Color.GREEN);
        } else {
            internetStatusLabel.setText("Internet: Not Accessible");
            internetStatusLabel.setForeground(Color.RED);
        }
    }

//    private void toggleDarkMode() {
//        if (darkModeToggleButton.isSelected()) {
//            // Dark mode enabled
//            UIManager.put("Panel.background", Color.BLACK);
//            UIManager.put("Label.foreground", Color.WHITE);
//            UIManager.put("TextField.background", Color.BLACK);
//            UIManager.put("TextField.foreground", Color.WHITE);
//            UIManager.put("ComboBox.background", Color.BLACK);
//            UIManager.put("ComboBox.foreground", Color.WHITE);
//            darkModeToggleButton.setText("Light Mode");
//        } else {
//            // Light mode enabled
//            UIManager.put("Panel.background", new Color(240, 240, 240));
//            UIManager.put("Label.foreground", Color.BLACK);
//            UIManager.put("TextField.background", Color.WHITE);
//            UIManager.put("TextField.foreground", Color.BLACK);
//            UIManager.put("ComboBox.background", Color.WHITE);
//            UIManager.put("ComboBox.foreground", Color.BLACK);
//            darkModeToggleButton.setText("Dark Mode");
//        }
//        SwingUtilities.updateComponentTreeUI(this);
//    }
    private void toggleDarkMode(JPanel mainPanel) {
        Color darkBackgroundColor = new Color(30, 30, 30);
        Color darkTextColor = Color.WHITE;

        if (getContentPane().getBackground().equals(darkBackgroundColor)) {
            // Switch to light mode
            getContentPane().setBackground(UIManager.getColor("Panel.background"));
            mainPanel.setBackground(Color.WHITE);

            amountTextField.setBackground(Color.WHITE);
            amountTextField.setForeground(Color.BLACK);
            fromCurrencyComboBox.setBackground(Color.WHITE);
            fromCurrencyComboBox.setForeground(Color.BLACK);
            toCurrencyComboBox.setBackground(Color.WHITE);
            toCurrencyComboBox.setForeground(Color.BLACK);
            resultLabel.setForeground(new Color(34, 139, 34));
        } else {
            // Switch to dark mode
            getContentPane().setBackground(darkBackgroundColor);
            mainPanel.setBackground(darkBackgroundColor);

            amountTextField.setBackground(darkBackgroundColor);
            amountTextField.setForeground(darkTextColor);
            fromCurrencyComboBox.setBackground(darkBackgroundColor);
            fromCurrencyComboBox.setForeground(darkTextColor);
            toCurrencyComboBox.setBackground(darkBackgroundColor);
            toCurrencyComboBox.setForeground(darkTextColor);
            resultLabel.setForeground(Color.GREEN);
        }

        // Refresh the UI
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CurrencyConverter converter = new BasicCurrencyConverter();
                new CurrencyConverterApp(converter).setVisible(true);
            }
        });
    }
}

