package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.db.ebean.EbeanConfig;

@Singleton
public class TagRepository {

    public enum TagType {
        DESTINATION_TAG,
        PHOTO_TAG,
        TRIP_TAG
    }

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public TagRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    

}
