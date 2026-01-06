package com.avenqo.cucumber.examples.pizzasvc.utils;

import com.avenqo.cucumber.examples.pizzasvc.data.PriceData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriceUtil {

    /**
     * Converts input strings like "1,22€", "10.00 $" etc. into PriceData
     *
     * @param priceString
     * @return
     */
    public static PriceData parse(String priceString) {

        if (priceString == null || priceString.isBlank()) {
            throw new IllegalArgumentException("Price string is empty");
        }

        // Zahl + optional Dezimalteil + Rest = Währung
        Pattern pattern = Pattern.compile("\\s*([0-9]+(?:[.,][0-9]+)?)\\s*(.*)");
        Matcher matcher = pattern.matcher(priceString);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot parse price: '" + priceString + "'");
        }

        String valueStr = matcher.group(1).trim();
        String currency = matcher.group(2).trim();

        // Normalize number formats:
        // - Replace comma with dot
        // - Remove leading zeros EXCEPT when the number is just "0" or "0.xx"
        valueStr = valueStr.replace(",", ".");

        // Parse to float
        Float value = Float.parseFloat(valueStr);

        return new PriceData(value, currency);
    }
}
