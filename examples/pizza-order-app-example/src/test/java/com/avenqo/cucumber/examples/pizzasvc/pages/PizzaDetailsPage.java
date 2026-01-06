package com.avenqo.cucumber.examples.pizzasvc.pages;

import com.avenqo.cucumber.beapp.exceptions.EInconsistencyException;
import com.avenqo.cucumber.beapp.pages.IPage;
import com.avenqo.cucumber.examples.pizzasvc.data.PriceData;
import com.avenqo.cucumber.examples.pizzasvc.data.SelectableToppingData;

import java.util.Map;


public interface PizzaDetailsPage extends IPage {

    public Map<String, SelectableToppingData> getVisibleToppings() throws EInconsistencyException;

    public boolean isToppingVisible(SelectableToppingData expectedTopping) throws EInconsistencyException;

    public int getIndexOfToppingByName(String topping) throws EInconsistencyException;

    public void selectToppingByIndex(int index) throws Throwable;

    public PriceData getPrice();

    public void add2Basket();

    public void goToBasket();
}
