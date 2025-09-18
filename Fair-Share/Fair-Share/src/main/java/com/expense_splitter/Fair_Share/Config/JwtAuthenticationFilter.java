package com.expense_splitter.Fair_Share.Config;

import com.expense_splitter.Fair_Share.Service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService service;


    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService service, UserDetailsService userDetailsService) {
        this.service = service;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");  // Here we are checking  that in the request if the header is passed or not .
        if (authHeader == null || !authHeader.startsWith("Bearer")){
            filterChain.doFilter(request,response);  // if the header is not there or not startting with Bearer then it will be redirected to authentication
            return;
        }

        final String jwt = authHeader.substring(7);  // now if header is there then we are extracting the actual token from header
        final String username = service.ExtractUsername(jwt);  // from  that token we are extract the username of the user

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // now we are getting the authentication of that user

        //if we get the username but he is not authenticated then we have to first authenticate that user

        if (username != null && auth == null){
            UserDetails userDetails
                    = userDetailsService.loadUserByUsername(username);
            if (service.isTokenValid(jwt,userDetails)){   // now we are checking that the user which was in jwt is actually in the db or not by validating the token if it is then we assign the auth token to the next filter so that it will not authenticate again
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);   //  Here we are updating the securitycontext that user is now authenticated

            }
        }
        filterChain.doFilter(request,response);


    }
}
