package com.kanpo.trial.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;


@TestPropertySource("/test.properties")
@SpringBootTest
public class MyPropertiesTest {
    @Autowired
    MyProperties myProperties;

    @Test
    @DisplayName("set non-default positive value")
    public void setNonDefaultPositiveValue() {
        myProperties.setCronDeleteMinute("2");
        assertEquals(2, ReflectionTestUtils.getField(myProperties, "cronDeleteMinute"));
    }

    @Test
    @DisplayName("set default value")
    public void setDefaultValue() {
        myProperties.setCronDeleteMinute("1");
        assertEquals(1, ReflectionTestUtils.getField(myProperties, "cronDeleteMinute"));
    }

    @Test
    @DisplayName("set non-default negative value")
    public void setNonDefaultNegativeValue() {
        myProperties.setCronDeleteMinute("-1");
        assertEquals(1, ReflectionTestUtils.getField(myProperties, "cronDeleteMinute"));
    }

    @Test
    @DisplayName("set non-numerical value")
    public void setNonNumericalValue() {
        myProperties.setCronDeleteMinute("a");
        assertEquals(1, ReflectionTestUtils.getField(myProperties, "cronDeleteMinute"));
    }
}
