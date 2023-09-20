package coda.app;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple web app interface.
 * Contains a head, body and footer.
 */
public interface WebApp {

    /**
     * Get html head
     *
     * @param title title of the page
     * @return
     */
    default String head(String title){
        return "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "<style>body{background-color: #feee;}</style>\n" +
                "<title>"+title+"</title>\n" +
                "<script src='https://unpkg.com/htmx.org@1.9.5' integrity='sha384-xcuj3WpfgjlKF+FXhSQFQ0ZNr39ln+hwjN3npfM9VBnUskLolQAcN80McRIVOPuO' crossorigin='anonymous'></script>" +
                "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css\">\n" +
                "</head>\n";
    }

    /**
     * Get html body.
     *
     * @param body body of the page
     * @param bootstrapClasses bootstrap classes
     * @return
     */
    default String body(String body, String... bootstrapClasses){
        StringBuilder sb = new StringBuilder();
        sb.append("<body>\n");
        for(String bootstrapClass : bootstrapClasses){
            sb.append("<div class=\""+bootstrapClass+"\">\n");
        }
        sb.append(body + "\n");
        for(String ignored : bootstrapClasses){
            sb.append("</div>\n");
        }
        sb.append("</body>\n");
        return sb.toString();
    }

    default String htmlWrapper(String inner){
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                inner +
                "</html>";
    }


    /**
     * Get html footer.
     *
     * @param author author of the page
     * @param license license of the page
     * @param version version of the page
     * @param bootstrapClasses bootstrap classes
     * @return
     */
    default String footer(String author, String license, String version, String... bootstrapClasses){
        StringBuilder sb = new StringBuilder();
        sb.append("<footer class=\"");
        for(String bootstrapClass : bootstrapClasses){
            sb.append(bootstrapClass + " ");
        }
        sb.append("\">\n");
        sb.append("Made by " + author + "\n");
        sb.append("License: " + license + "\n");
        sb.append("Version: " + version + "\n");
        sb.append("</footer>\n");
        return sb.toString();
    }

    Object parseInput(HttpServletRequest request, WebPageService service);
}
