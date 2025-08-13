package mc.sayda;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class Main extends JFrame {
    private final JTextField tfFilamentGrams = new JTextField("0");
    private final JTextField tfFilamentPricePerKg = new JTextField("200"); // SEK/kg
    private final JTextField tfWatts = new JTextField("140");              // Ender 3 V3 Plus avg (editable)
    private final JTextField tfPricePerKWh = new JTextField("100");        // öre/kWh (example)
    private final JTextField tfPrintHours = new JTextField("0");
    private final JTextField tfFixedCosts = new JTextField("0");

    private final JLabel lblFilamentCost = new JLabel("—");
    private final JLabel lblElectricityCost = new JLabel("—");
    private final JLabel lblElectricityPerHour = new JLabel("—");
    private final JLabel lblTotalCost = new JLabel("—");

    private final NumberFormat sek = NumberFormat.getCurrencyInstance(new Locale("sv", "SE"));

    public Main() {
        super("3D Printing Cost Calculator (SEK)");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        JPanel inputs = new JPanel(new GridBagLayout());
        inputs.setBorder(new TitledBorder("Inputs"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        int row = 0;
        addRow(inputs, c, row++, "Filament used (grams):", tfFilamentGrams, "e.g. 35");
        addRow(inputs, c, row++, "Filament price per kg (SEK):", tfFilamentPricePerKg, "e.g. 200");
        addRow(inputs, c, row++, "Printer power (W):", tfWatts, "e.g. 140");
        addRow(inputs, c, row++, "Electricity price (öre/kWh):", tfPricePerKWh, "e.g. 93,69");
        addRow(inputs, c, row++, "Print time (hours):", tfPrintHours, "e.g. 6.5");
        addRow(inputs, c, row++, "Additional fixed costs (SEK):", tfFixedCosts, "e.g. 0");

        JPanel outputs = new JPanel(new GridBagLayout());
        outputs.setBorder(new TitledBorder("Results (SEK)"));
        GridBagConstraints d = new GridBagConstraints();
        d.insets = new Insets(6, 8, 6, 8);
        d.fill = GridBagConstraints.HORIZONTAL;
        d.weightx = 1.0;

        int r2 = 0;
        addRow(outputs, d, r2++, "Filament cost:", lblFilamentCost);
        addRow(outputs, d, r2++, "Electricity cost (total):", lblElectricityCost);
        addRow(outputs, d, r2++, "Electricity cost (per hour):", lblElectricityPerHour);
        addRow(outputs, d, r2++, "Total cost:", lblTotalCost);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnCalc = new JButton("Calculate");
        JButton btnReset = new JButton("Reset");
        buttons.add(btnReset);
        buttons.add(btnCalc);

        btnCalc.addActionListener(e -> calculate());
        btnReset.addActionListener(e -> resetFields());

        add(inputs, BorderLayout.NORTH);
        add(outputs, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        // Helpful tooltip to avoid confusion
        tfPricePerKWh.setToolTipText("Enter market price in öre/kWh (e.g. 93,69). The app converts to SEK automatically.");
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field, String hint) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(field, c);

        if (hint != null && !hint.isEmpty()) {
            c.gridx = 2;
            c.weightx = 0;
            JLabel hintLabel = new JLabel(hint);
            hintLabel.setForeground(new Color(90, 90, 90));
            hintLabel.setFont(hintLabel.getFont().deriveFont(Font.ITALIC, 11f));
            panel.add(hintLabel, c);
        }
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent valueLabel) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(valueLabel, c);
    }

    private void calculate() {
        try {
            double filamentGrams     = parsePositive(tfFilamentGrams.getText(), "Filament used (grams)", true);
            double filamentPriceKgSEK= parsePositive(tfFilamentPricePerKg.getText(), "Filament price per kg (SEK)", true);
            double watts             = parsePositive(tfWatts.getText(), "Printer power (W)", true);
            double priceOrePerKWh    = parsePositive(tfPricePerKWh.getText(), "Electricity price (öre/kWh)", true);
            double printHours        = parsePositive(tfPrintHours.getText(), "Print time (hours)", true);
            double fixedCostsSEK     = parsePositive(tfFixedCosts.getText(), "Additional fixed costs (SEK)", false);

            // Convert öre -> SEK for calculations
            double priceSEKPerKWh = priceOrePerKWh / 100.0;

            // Cost breakdown
            double filamentCostSEK = (filamentGrams / 1000.0) * filamentPriceKgSEK;
            double electricityPerHourSEK = (watts / 1000.0) * priceSEKPerKWh;
            double electricityPerHourOre = electricityPerHourSEK * 100.0;
            double electricityCostSEK = electricityPerHourSEK * printHours;
            double totalSEK = filamentCostSEK + electricityCostSEK + fixedCostsSEK;

            // Update UI
            lblFilamentCost.setText(sek.format(filamentCostSEK));
            lblElectricityPerHour.setText(sek.format(electricityPerHourSEK) + String.format(" (%.2f öre/h)", electricityPerHourOre));
            lblElectricityCost.setText(sek.format(electricityCostSEK));
            lblTotalCost.setText(sek.format(totalSEK));
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFields() {
        tfFilamentGrams.setText("0");
        tfFilamentPricePerKg.setText("200");
        tfWatts.setText("140");       // Ender 3 V3 Plus default
        tfPricePerKWh.setText("100"); // öre/kWh
        tfPrintHours.setText("0");
        tfFixedCosts.setText("0");
        lblFilamentCost.setText("—");
        lblElectricityCost.setText("—");
        lblElectricityPerHour.setText("—");
        lblTotalCost.setText("—");
    }

    private double parsePositive(String s, String fieldName, boolean strictlyPositive) {
        double v;
        try {
            v = Double.parseDouble(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + ": please enter a valid number.");
        }
        if (strictlyPositive && v <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0.");
        }
        if (!strictlyPositive && v < 0) {
            throw new IllegalArgumentException(fieldName + " must be ≥ 0.");
        }
        return v;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new Main().setVisible(true);
        });
    }
}
