package org.mayevskiy.intellij.sonar.inspection;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: Mirasch
 * Date: 09.03.13
 * Time: 15:13
 * To change this template use File | Settings | File Templates.
 */
public class SonarInspectionTest {
    @Test
    public void testConvertResourceKeyToFilePath_commonJavaClass() throws Exception {

        String result = SonarInspection.convertResourceKeyToFilePath("my:project:de.plushnikov.TestKlasse");
        String expected = "de/plushnikov/TestKlasse";

        assertEquals(expected, result);
    }

    @Test
    public void testConvertResourceKeyToFilePath_JavaClassDefaultPackage() throws Exception {

        String result = SonarInspection.convertResourceKeyToFilePath("my:project:javaModul:[default].Blau");
        String expected = "Blau";

        assertEquals(expected, result);
    }

    @Test
    public void testConvertResourceKeyToFilePath_GroovyClassRootPath() throws Exception {

        String result = SonarInspection.convertResourceKeyToFilePath("my:project:groovyModul:[root]/MyClass.groovy");
        String expected = "MyClass.groovy";

        assertEquals(expected, result);
    }

    @Test
    public void testConvertResourceKeyToFilePath_commonGroovyClass() throws Exception {

        String result = SonarInspection.convertResourceKeyToFilePath("my:project:groovyModul:bla/blub/OtherClass.groovy");
        String expected = "bla/blub/OtherClass.groovy";

        assertEquals(expected, result);
    }

}
