package planner.bootstrap;

import com.hubspot.dropwizard.guice.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.swagger.config.ScannerFactory;
import io.swagger.jaxrs.config.DefaultJaxrsScanner;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.jersey.listing.ApiListingResourceJSON;
import planner.core.model.*;

public class PlannerApplication extends Application<PlannerConfiguration> {
    public static void main(String[] args) throws Exception {
        try {
            new PlannerApplication().run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getName() {
        return "Planner";
    }

    private final HibernateBundle<PlannerConfiguration> hibernateBundle
            = new HibernateBundle<PlannerConfiguration>(Person.class, Team.class, Okr.class, Week.class, PersonWeek.class, Plan.class) {
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

        bootstrap.addBundle(new AssetsBundle("/swagger-ui", "/planner-ui", "index.html"));

        bootstrap.addBundle(hibernateBundle);

        bootstrap.addBundle(new ViewBundle());
    }

    @Override
    public void run(PlannerConfiguration configuration, Environment environment) {
        environment.jersey().register(ApiListingResourceJSON.class);
        environment.jersey().register(SwaggerSerializers.class);
        ScannerFactory.setScanner(new DefaultJaxrsScanner());
    }
}
