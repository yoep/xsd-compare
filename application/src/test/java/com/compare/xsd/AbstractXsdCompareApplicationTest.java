package com.compare.xsd;

import javafx.application.Application;
import org.junit.BeforeClass;

public abstract class AbstractXsdCompareApplicationTest {
    @BeforeClass
    public static void initialize() {
        Thread t = new Thread("JavaFX Init Thread") {
            public void run() {
                Application.launch(XsdCompareApplication.class);
            }
        };
        t.setDaemon(true);
        t.start();
    }
}