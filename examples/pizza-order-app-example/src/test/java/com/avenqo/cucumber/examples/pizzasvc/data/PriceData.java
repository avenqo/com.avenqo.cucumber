package com.avenqo.cucumber.examples.pizzasvc.data;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record PriceData(Float value, String currency) {

    public PriceData {
        value = normalize(value);
    }

    public PriceData(String value, String currency) {
        this(Float.parseFloat(value.replace(",", ".")), currency);
    }

    // Convenience-Constructors:

    public PriceData(Double value, String currency) {
        this(value.floatValue(), currency);
    }

    public PriceData(Integer value, String currency) {
        this(value.floatValue(), currency);
    }

    private static Float normalize(Object rawValue) {
        if (rawValue == null) return null;

        if (rawValue instanceof Float f) return f;
        if (rawValue instanceof Double d) return d.floatValue();
        if (rawValue instanceof Integer i) return i.floatValue();

        if (rawValue instanceof String s) {
            String cleaned = s.trim().replace(",", ".");
            return Float.parseFloat(cleaned);
        }

        throw new IllegalArgumentException(
                "Unsupported value type: " + rawValue.getClass()
        );
    }


    public static PriceData fromText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Price text must not be null or empty");
        }

        // Example: "Artikelpreis: 11,10 €"
        // Groups:
        // 1 = Number
        // 2 = Currency
        Pattern pattern = Pattern.compile(
                ".*?([0-9]+(?:[.,][0-9]+)?)\\s*([\\p{Sc}A-Za-z]{1,5}).*"
        );

        Matcher matcher = pattern.matcher(text);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot parse price from: " + text);
        }

        String valuePart = matcher.group(1);     // "11,10"
        String currencyPart = matcher.group(2);  // "€"

        return new PriceData(valuePart, currencyPart);
    }
}