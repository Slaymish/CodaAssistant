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

    private BiFunction<String,WebPageService,String> render;

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

    public WebPageServiceBuilder setRender(BiFunction<String,WebPageService,String> render) {
        this.render = render;
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

            /**
             * Run the service.
             *
             * @param input input
             * @param args  arguments
             * @return
             */
            public O runService(Object input, String[] args) {
                return service != null ? service.apply((I) input) : null;
            }

            public String render(){
                return render != null ? render.apply("", this) : "";
            }
        };
    }
}
