package Coda.App;

/**
 * Simple web service interface.
 * Contains a title, description, version, author and license.
 * And a run method, returning the end result.
 * @param <I> input type
 * @param <O> output type
 */
public interface CodaService<I,O> {
    // Create a web service
    String title();
    String description();
    String version();
    String author();
    String license();

    default String endpoint(){
        return title().toLowerCase().replace(" ", "-");
    }


    default O runService( I input ){ return runService( input, null );}
    O runService( I input, String[] args );
}
