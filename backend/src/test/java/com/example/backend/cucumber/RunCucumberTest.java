package com.example.backend.cucumber;


import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("cucumber") // Punta alla cartella nei resources
public class RunCucumberTest {
    // Questa classe rimane vuota, serve solo come trigger per Maven
}