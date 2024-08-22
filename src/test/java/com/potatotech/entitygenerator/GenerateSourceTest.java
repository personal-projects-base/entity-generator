package com.potatotech.entitygenerator;

import com.potatotech.entitygenerator.service.GenerateSource;
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
