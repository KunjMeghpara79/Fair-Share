package com.expense_splitter.Fair_Share.Service;

import com.expense_splitter.Fair_Share.CustomUserDetails;
import com.expense_splitter.Fair_Share.Entity.users;
import com.expense_splitter.Fair_Share.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo repo;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        users user = repo.findByemail(email);
        if (Objects.isNull(user)){
            System.out.println("User not found");
            throw new UsernameNotFoundException("Not Found");
        }
        return new CustomUserDetails(user);

    }
}
