package com.avenqo.cucumber.examples.pizzasvc.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ToppingData(String name, PriceData price) {

    public static ToppingData parse(String input) {

        Pattern pattern = Pattern.compile(
                "^\\s*([0-9]+(?:[.,][0-9]{1,2})?)\\s*([^\\d\\s]+)\\s*(.*)$"
        );

        Matcher matcher = pattern.matcher(input);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot parse: " + input);
        }

        String rawAmount = matcher.group(1)
                .replace(',', '.');

        Float amount = Float.parseFloat(rawAmount);
        String currency = matcher.group(2);
        String topping = matcher.group(3).trim();

        return new ToppingData(topping, new PriceData(amount, currency));
    }
}
