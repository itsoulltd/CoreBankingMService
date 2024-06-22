package com.infoworks.lab.webapp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.junit.Assert.*;

@Configuration
@ComponentScan(basePackages = {"com.infoworks.lab.controllers"
        ,"com.infoworks.lab.services"})
public class VirtualAccountServiceTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConfig(){
        System.out.println("Test For VirtualAccountServiceTest");
    }
}