package com.gonthera.cli;

import com.gonthera.cli.service.GenerateSource;
import org.junit.Test;

public class GenerateSourceTest {


    @Test
    public void testSource(){
        GenerateSource gen = new GenerateSource();
        try {
            gen.generateSource();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
