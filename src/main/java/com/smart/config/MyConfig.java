package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig {

	@Bean
	public UserDetailsService userUserDetailsService() 
	{
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() 
	{
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() 
	{
		DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.userUserDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		
		return daoAuthenticationProvider;
	}
	
	// configure method...
	
	protected void configure(AuthenticationManagerBuilder auth) throws Exception{
		auth.authenticationProvider(authenticationProvider());
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception 
	{
		http.csrf(csrf->csrf.disable())
		.authorizeHttpRequests()
		.requestMatchers("/admin/**")
		.hasRole("ADMIN")
		.requestMatchers("/user/**")
		.hasRole("USER")
		.requestMatchers("/**")
		.permitAll()
		.and()
		.formLogin()
		.loginPage("/signin")
		.loginProcessingUrl("/dologin")
		.defaultSuccessUrl("/user/index")
		.failureUrl("/login-fail");
		
		return http.build();
				
	}
	
//	protected void configure(HttpSecurity http) throws Exception{
//	http.authorizeRequests().requestMatchers("/admin/**").hasRole("ADMIN")
//	.requestMatchers("/user/**").hasRole("USER")
//	.requestMatchers("/**").permitAll().and().formLogin().and().csrf().disable();
//}
	
//	 @Bean
//	    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
//	        return httpSecurity
//	                .csrf(csrf->csrf.disable())
//	                .authorizeRequests(auth->auth.antMatchers("/token/**").permitAll())
//	                .authorizeRequests(auth->auth.anyRequest().authenticated())
//	                .sessionManagement(sess->sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//	                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
//	                .httpBasic(Customizer.withDefaults())
//	                .build();
//	    }
	
//	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            .authorizeHttpRequests((authz) -> authz
//                .anyRequest().authenticated()
//            )
//            .httpBasic(withDefaults());
//        return http.build();
//    }

	
	
}
