package coda.app;

public interface WebPageService<I, O> extends WebApp, CodaService {

    /**
     * Get html page.
     *
     * @param inner inner html
     * @return
     */
    default String getPage(String inner) {
        return htmlWrapper(head(title())
                + body(description() + "<br><br>" + inner, new String[]{"modal-dialog", "modal-content"})
                + footer(author(), license(), version(), new String[]{"container", "fixed-bottom"}));
    }

    String render();
}