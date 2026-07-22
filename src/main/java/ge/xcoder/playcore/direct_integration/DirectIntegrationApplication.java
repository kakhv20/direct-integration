package ge.xcoder.playcore.direct_integration;

import ge.xcoder.playcore.direct_integration.validator.OperatorIdValidator;
import ge.xcoder.playcore.direct_integration.validator.TimestampValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class DirectIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(DirectIntegrationApplication.class, args);
    }

    @Bean
    public TimestampValidator timestampValidator(
            @Value("${app.security.timestamp-plus-minus-boundary:30}") int window) {
        Clock clock = Clock.systemDefaultZone(); 
        return new TimestampValidator(clock, window);
    }

    @Bean
    public OperatorIdValidator operatorIdValidator(
            @Value("${app.security.operator-id:-1}") String operatorId) {
        return new OperatorIdValidator(operatorId);
    }
}
