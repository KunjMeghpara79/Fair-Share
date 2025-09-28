package com.expense_splitter.Fair_Share.Service;

import com.expense_splitter.Fair_Share.DTO.Authresponse;
import com.expense_splitter.Fair_Share.Entity.users;
import com.expense_splitter.Fair_Share.Repository.UserRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;



    public PasswordEncoder encoder = new BCryptPasswordEncoder();


    public users findOrCreateUser(String email, String name) {
        users user = repo.findByemail(email);
        if (user == null) {
            user = new users();
            user.setEmail(email);
            user.setName(name);
            // Optional: set default password or a random string if needed
            repo.save(user);
        }
        return user;
    }

    public ResponseEntity<?> findusername(ObjectId id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated()){
            return new ResponseEntity<>("You are not Authenticated." ,HttpStatus.UNAUTHORIZED);
        }
        users u = repo.findByid(id);
        if (u ==  null){
            return new ResponseEntity<>("User not found ",HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(u.getName(),HttpStatus.OK);
    }

    public ResponseEntity<?> saveuser(users user) {
        if (repo.findByemail(user.getEmail()) != null) {
            return new ResponseEntity<>("User already exists", HttpStatus.BAD_REQUEST);
        }

        user.setCreatedAt(new Date(System.currentTimeMillis()));
        user.setPassword(encoder.encode(user.getPassword()));

        // Decode base64 image string to bytes and set to pfp


        repo.save(user);
        return new ResponseEntity<>("Created", HttpStatus.OK);
    }



    public ResponseEntity<?> validateUser(users user){
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );
            if (authenticate.isAuthenticated()){
                String token = jwtService.generateToken(user.getEmail());
                users u = repo.findByemail(user.getEmail());
                Authresponse authresponse = new Authresponse(token,u.getEmail(),u.getName());
                return new ResponseEntity<>(authresponse,HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);// authentication failed
        }
return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}
