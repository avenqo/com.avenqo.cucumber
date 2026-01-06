package com.avenqo.cucumber.beapp.pages;

import io.cucumber.spring.ScenarioScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ScenarioScope
@Slf4j
public class PageRegistry {

    private final Map<String, IPage> pagesByName = new HashMap<>();

    public PageRegistry(List<IPage> pages) {
        log.info("{}", pages);
        for (IPage page : pages) {
            String name = page.getName();
            pagesByName.put(name.toLowerCase(), page); // case-insensitive
        }
    }

    public IPage get(String name) {
        log.info("name [{}]", name);
        IPage page = pagesByName.get(name.toLowerCase());
        if (page == null) {
            throw new IllegalArgumentException(
                    "Unknown page name: '" + name + "'. Known pages: " + pagesByName.keySet());
        }
        return page;
    }
}
