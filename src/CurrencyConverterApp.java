import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

// Interface for currency converters
interface CurrencyConverter {
    double convert(double amount, String fromCurrency, String toCurrency);
}

// Concrete currency converter class
class BasicCurrencyConverter implements CurrencyConverter {
    private Map<String, Double> exchangeRates;

    public BasicCurrencyConverter() {
        exchangeRates = new HashMap<>();

        exchangeRates.put("USD-EUR", 0.85);
        exchangeRates.put("USD-JPY", 110.0);
        exchangeRates.put("USD-INR", 82.0);
        exchangeRates.put("EUR-INR", 90.0);
        exchangeRates.put("EUR-USD", 1.09);
        exchangeRates.put("EUR-JPY", 159.4);
        exchangeRates.put("JPY-USD", 0.0091);
        exchangeRates.put("JPY-INR", 0.746);
        exchangeRates.put("JPY-EUR", 0.0063);
        exchangeRates.put("INR-USD", 0.0122);
        exchangeRates.put("INR-EUR", 0.0111);
        exchangeRates.put("INR-JPY", 1.34);

    }

    @Override
    public double convert(double amount, String fromCurrency, String toCurrency) {
        String key = fromCurrency + "-" + toCurrency;
        if (exchangeRates.containsKey(key)) {
            return amount * exchangeRates.get(key);
        }
        return amount; // No conversion needed for same currency
    }
}

// Main application class
public class CurrencyConverterApp extends JFrame {
    private JTextField amountTextField;
    private JComboBox<String> fromCurrencyComboBox;
    private JComboBox<String> toCurrencyComboBox;
    private JLabel resultLabel;
    private CurrencyConverter converter;

    public CurrencyConverterApp(CurrencyConverter converter) {
        this.converter = converter;

        setTitle("Currency Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Initialize UI components
        amountTextField = new JTextField();
        fromCurrencyComboBox = new JComboBox<>(new String[]{"USD", "EUR", "JPY", "INR"});
        toCurrencyComboBox = new JComboBox<>(new String[]{"USD", "EUR", "JPY", "INR"});
        JButton convertButton = new JButton("Convert");
        convertButton.setBackground(new Color(0, 123, 255));
        convertButton.setForeground(Color.WHITE);
        resultLabel = new JLabel();
        resultLabel.setForeground(new Color(34, 139, 34));

        // Set font for labels
        Font labelFont = new Font("Arial", Font.BOLD, 14);
        JLabel[] labels = {new JLabel("Amount:"), new JLabel("From Currency:"), new JLabel("To Currency:")};
        for (JLabel label : labels) {
            label.setFont(labelFont);
        }

        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertCurrency();
            }
        });

        // Add components to the main panel
        mainPanel.add(labels[0]);
        mainPanel.add(amountTextField);
        mainPanel.add(labels[1]);
        mainPanel.add(fromCurrencyComboBox);
        mainPanel.add(labels[2]);
        mainPanel.add(toCurrencyComboBox);
        mainPanel.add(new JLabel());
        mainPanel.add(convertButton);
        mainPanel.add(resultLabel);

        add(mainPanel);
        pack();
        setMinimumSize(new Dimension(700, 400));
    }

    private void convertCurrency() {
        try {
            double amount = Double.parseDouble(amountTextField.getText());
            String fromCurrency = (String) fromCurrencyComboBox.getSelectedItem();
            String toCurrency = (String) toCurrencyComboBox.getSelectedItem();

            double convertedAmount = converter.convert(amount, fromCurrency, toCurrency);
            DecimalFormat df = new DecimalFormat("#.##");
            resultLabel.setText("Converted Amount: " + df.format(convertedAmount) + " " + toCurrency);
        } catch (NumberFormatException ex) {
            resultLabel.setText("Invalid input");
        }
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
