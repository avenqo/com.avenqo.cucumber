package com.avenqo.cucumber.beapp.exceptions;

import org.openqa.selenium.By;

@SuppressWarnings("serial")
public class EPageElementNotFound extends AException {

    public EPageElementNotFound(String msg) {
        super(msg);// TODO Auto-generated constructor stub
    }

    public EPageElementNotFound(By by) {
        super(by.toString());// TODO Auto-generated constructor stub
    }

    public EPageElementNotFound(By by, String txt) {
        super("By '" + by.toString() + "', Text='" + txt + "'");// TODO Auto-generated constructor stub
    }
}
