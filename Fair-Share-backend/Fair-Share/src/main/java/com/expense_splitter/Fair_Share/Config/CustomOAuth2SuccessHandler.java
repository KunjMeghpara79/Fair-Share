package com.expense_splitter.Fair_Share.Config;

import com.expense_splitter.Fair_Share.Entity.users;
import com.expense_splitter.Fair_Share.Repository.UserRepo;
import com.expense_splitter.Fair_Share.Service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    @Value("${frontend.url}")
    private String frontendurl;


    @Autowired
    private UserRepo urepo;


    public CustomOAuth2SuccessHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        String email = oauthToken.getPrincipal().getAttribute("email");


        if (email == null) {
            response.sendRedirect(frontendurl + "/login?error=email_not_found");
            return;
        }


        String jwt = jwtService.generateToken(email);
        String username = jwtService.ExtractUsername(jwt);
        users u = urepo.findByemail(username);

        String name = u.getName();
        String Email =u.getEmail();


        String redirectUrl = frontendurl+"/dashboard?token=" + jwt
                + "&name=" + name
                + "&email=" + Email;


        response.sendRedirect(redirectUrl);

    }}
