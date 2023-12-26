package pg.paymentgateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pg.paymentgateway.interceptor.Interceptor;
import pg.paymentgateway.service.TestService;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired Interceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                //.excludePathPatterns("/css/**", "/image/**", "/js/**", "/swagger-ui.html")
                .addPathPatterns("/api/**");
    }

    @Bean
    public TestService testService() {
        return new TestService();
    }

    @Bean
    public Interceptor interceptor() {
        return new Interceptor();
    }
}
