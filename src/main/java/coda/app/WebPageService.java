package coda.app;

public interface WebPageService<I, O> extends WebApp, CodaService {

    /**
     * Get html page.
     *
     * @param inner inner html
     * @return
     */
    default String getPage(String inner) {
        return htmlWrapper(head(title()) + body(description() + "<br><br>" + inner, "container")
                + footer(author(), license(), version(), "container"));
    }

    String render();
}