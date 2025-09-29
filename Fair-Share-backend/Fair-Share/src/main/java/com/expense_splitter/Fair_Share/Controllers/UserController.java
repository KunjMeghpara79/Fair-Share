package com.expense_splitter.Fair_Share.Controllers;

import com.expense_splitter.Fair_Share.DTO.Emailpassword;
import com.expense_splitter.Fair_Share.DTO.Objid;
import com.expense_splitter.Fair_Share.DTO.emailandotp;
import com.expense_splitter.Fair_Share.Entity.users;
import com.expense_splitter.Fair_Share.Service.ForgotPasswordService;
import com.expense_splitter.Fair_Share.Service.JwtService;
import com.expense_splitter.Fair_Share.Service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@CrossOrigin(origins = "${frontend.url}")
public class UserController {
    @Autowired
    private UserService service;

    @Autowired
    private JwtService jwtService; // Your JWT generator

    @Autowired
    private UserService userService;

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @PostMapping("/Register")
    public ResponseEntity<?> Register(@RequestBody users user){
        try {

           return service.saveuser(user);

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>("Enter Valid email",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/Login")
    public ResponseEntity<?> Login(@RequestBody users user) throws IOException {
       return service.validateUser(user);
    }

    @PostMapping("/api/auth/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        String googleToken = body.get("token");

        try {
            // Create verifier
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList("649806980196-nhcs98ihen9g0dukvmsvu639u66tmd38.apps.googleusercontent.com"))
                    .build();

            // Verify token
            GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                // Safer email retrieval
                String email = (String) payload.get("email");
                String name = (String) payload.get("name");

                // Create user if not exists
                users user = userService.findOrCreateUser(email, name);

                // Generate backend JWT
                String jwt = jwtService.generateToken(email);

                return ResponseEntity.ok(Map.of(
                        "jwt", jwt,
                        "email", email,
                        "name", user.getName()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid Google token"));
            }
        } catch (Exception e) {
            e.printStackTrace(); // optional: log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Google login failed"));
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> Sendpassword(@RequestBody String mail) throws Exception {
        return forgotPasswordService.sendandsaveotp(mail);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyotp(@RequestBody  emailandotp emailandotp){
        return forgotPasswordService.verifyotp(emailandotp);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changepassword(@RequestBody Emailpassword emailpassword){
         return  forgotPasswordService.changepssword(emailpassword);
    }

    @PostMapping("/get-namebyid")
    public ResponseEntity<?> getnamebyid(@RequestBody Objid obj){
        ObjectId objectId = new ObjectId(obj.getId());
        return service.findusername(objectId);
    }

    
}
