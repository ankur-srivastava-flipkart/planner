package planner.bootstrap;

import com.hubspot.dropwizard.guice.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.config.ScannerFactory;
import io.swagger.jaxrs.config.DefaultJaxrsScanner;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.jersey.listing.ApiListingResourceJSON;
import planner.core.model.Person;
import planner.core.model.Planner;
import planner.core.model.Team;
import planner.core.resource.PlannerResource;

public class PlannerApplication extends Application<PlannerConfiguration> {
    public static void main(String[] args) throws Exception {
        new PlannerApplication().run(args);
    }

    @Override
    public String getName() {
        return "Planner";
    }

    private final HibernateBundle<PlannerConfiguration> hibernateBundle
            = new HibernateBundle<PlannerConfiguration>(Person.class, Team.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(
                PlannerConfiguration configuration
        ) {
            return configuration.getDataSourceFactory();
        }
    };

    @Override
    public void initialize(Bootstrap<PlannerConfiguration> bootstrap) {
        GuiceBundle<PlannerConfiguration> guiceBundle = GuiceBundle.<PlannerConfiguration>newBuilder()
                .setConfigClass(PlannerConfiguration.class)
                .addModule(new PlannerModule(hibernateBundle))
                .enableAutoConfig("planner")
                .build();

        bootstrap.addBundle(guiceBundle);

        bootstrap.addBundle(new MigrationsBundle<PlannerConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(PlannerConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });

        bootstrap.addBundle(new AssetsBundle("/swagger-ui", "/planner", "index.html"));

        bootstrap.addBundle(hibernateBundle);
    }

    @Override
    public void run(PlannerConfiguration configuration, Environment environment) {
        environment.jersey().register(new PlannerResource(new Planner("MJJA")));
        environment.jersey().register(ApiListingResourceJSON.class);
        environment.jersey().register(SwaggerSerializers.class);
        ScannerFactory.setScanner(new DefaultJaxrsScanner());
    }
}
