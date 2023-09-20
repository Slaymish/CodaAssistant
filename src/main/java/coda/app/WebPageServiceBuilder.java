package coda.app;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Builder for a WebPage Coda Service.
 *
 * @param <I> input type
 * @param <O> output type
 */
public class WebPageServiceBuilder<I,O> {
    private String title;
    private String description;
    private String version;
    private String author;
    private String license;

    private Function<I, O> service;

    private BiFunction<String, WebPageService, O> inputParser;
    private Function<WebPageService,String> render;

    public WebPageServiceBuilder() {}

    public WebPageServiceBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public WebPageServiceBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public WebPageServiceBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    public WebPageServiceBuilder setAuthor(String author) {
        this.author = author;
        return this;
    }

    public WebPageServiceBuilder setLicense(String license) {
        this.license = license;
        return this;
    }

    public WebPageServiceBuilder setService(Function<I, O> service) {
        this.service = service;
        return this;
    }

    public WebPageServiceBuilder setRender(Function<WebPageService,String> render) {
        this.render = render;
        return this;
    }

    public WebPageServiceBuilder setInputParser(BiFunction<String, WebPageService, O> inputParser) {
        this.inputParser = inputParser;
        return this;
    }

    public WebPageService<I, O> build() {
        return new WebPageService<I, O>() {
            public String title() {
                return title;
            }

            public String description() {
                return description;
            }

            public String version() {
                return version;
            }

            public String author() {
                return author;
            }

            public String license() {
                return license;
            }

            public O runService(Object input){
                return service != null ? service.apply((I) input) : null;
            }

            public O parseInput(String request, WebPageService service){
                return inputParser != null ? inputParser.apply(request, service) : null;
            }

            public String render(){
                return render != null ? render.apply(this) : "";
            }
        };
    }
}
