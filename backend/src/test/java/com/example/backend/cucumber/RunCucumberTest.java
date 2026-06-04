package com.example.backend.cucumber;


import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.ConfigurationParameter;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("cucumber") // Punta alla cartella nei resources
// Keep Cucumber from scanning dependency classes as glue on Windows.
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.example.backend.cucumber")
public class RunCucumberTest {
    // Questa classe rimane vuota, serve solo come trigger per Maven
}
