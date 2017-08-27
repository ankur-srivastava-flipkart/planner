package planner.bootstrap;

import com.hubspot.dropwizard.guice.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import planner.core.model.Planner;
import planner.core.resource.PlannerResource;

public class PlannerApplication extends Application<PlannerConfiguration> {
    public static void main(String[] args) throws Exception {
        new PlannerApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<PlannerConfiguration> bootstrap) {
        GuiceBundle<PlannerConfiguration> guiceBundle = GuiceBundle.<PlannerConfiguration>newBuilder()
                .setConfigClass(PlannerConfiguration.class)
                .addModule(new PlannerModule())
                .enableAutoConfig("planner")
                .build();

        bootstrap.addBundle(guiceBundle);

        bootstrap.addBundle(new MigrationsBundle<PlannerConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(PlannerConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }

    @Override
    public void run(PlannerConfiguration configuration, Environment environment) {
        environment.jersey().register(new PlannerResource(new Planner("MJJA")));
    }
}
