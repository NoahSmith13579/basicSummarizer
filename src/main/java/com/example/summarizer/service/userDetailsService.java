

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Bean
public UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager(
            User.withUsername("testuser")
                    .password("{noop}password")
                    .roles("USER")
                    .build()
    );
}

void main() {
}
