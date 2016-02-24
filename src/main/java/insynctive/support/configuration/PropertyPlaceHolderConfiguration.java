package insynctive.support.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class PropertyPlaceHolderConfiguration {

    @Bean
    public PropertySourcesPlaceholderConfigurer propertiesResolver() {
        Resource[] resources = new ClassPathResource[] {
                new ClassPathResource("/application.properties")};
        PropertySourcesPlaceholderConfigurer placeHolder = new PropertySourcesPlaceholderConfigurer();

        placeHolder.setLocations(resources);

        return placeHolder;
    }
}