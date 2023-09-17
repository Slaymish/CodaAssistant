package Coda.Tests;

import Coda.App.CodaApplication;
import Coda.App.CodaService;
import Coda.App.WebPageService;
import Coda.App.WebPageServiceBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CodaTests {

    @BeforeAll
    public static void checkAsserts(){
        try{
            assert false;
        }catch (AssertionError e){
            System.out.println("Asserts are enabled.");
            return;
        }
        fail("Asserts are disabled.");
    }

    @Test
    public void testBlenderFarm() {
        CodaApplication app = new CodaApplication();

        WebPageService<File,File> blenderFarm = app.buildBlenderFarm();
        blenderFarm.runService(new File("test.blend"));

        Object o = blenderFarm.runService(new File("test.blend"));
        assertEquals(null, o);
    }

    @Test
    public void testWebPageServiceCreation(){
        WebPageServiceBuilder<Integer,Integer> builder = new WebPageServiceBuilder<>();
        builder.setService((Integer i) -> i + 1);
        WebPageService<Integer,Integer> adder = builder.build();
        assertEquals(adder.runService(1), 2);
    }

    @Test
    public void testCodaServiceCreation(){
        WebPageServiceBuilder<Integer,Integer> builder = new WebPageServiceBuilder<>();
        builder.setService((Integer i) -> i + 1);
        CodaService<Integer,Integer> adder = builder.build();
        assertEquals(adder.runService(1), 2);
    }

    @Test
    public void testHTMLPageOutput(){
        WebPageServiceBuilder<Integer,Integer> builder = new WebPageServiceBuilder<>();
        builder.setAuthor("test author");
        builder.setTitle("test title");
        builder.setVersion("test version");
        builder.setLicense("test license");
        builder.setDescription("test description");


        WebPageService ws = builder.build();
        String fullHTML = ws.getPage("test");

        String expectedFullHTML = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>body{background-color: #f0f0f0;}</style>
                <title>test title</title>
                <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
                </head>
                <body>
                <div class="container">
                test description<br><br>test
                </div>
                </body>
                <footer class="container ">
                Made by test author
                License: test license
                Version: test version
                </footer>
                </html>""";

        assertEquals(expectedFullHTML, fullHTML);

    }

}
