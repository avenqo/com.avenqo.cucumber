package com.avenqo.cucumber.examples.pizzasvc.pages;

import com.avenqo.cucumber.beapp.exceptions.EInconsistencyException;
import com.avenqo.cucumber.beapp.pages.IPage;
import com.avenqo.cucumber.examples.pizzasvc.data.PizzaData;

import java.util.List;


public interface LandingPage extends IPage {

    public List<PizzaData> getVisiblePizzas() throws EInconsistencyException;

    public void selectPizza(String name);
}
