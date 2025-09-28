package com.expense_splitter.Fair_Share.Service;

import com.expense_splitter.Fair_Share.DTO.Emailpassword;
import com.expense_splitter.Fair_Share.DTO.emailandotp;
import com.expense_splitter.Fair_Share.Entity.users;
import com.expense_splitter.Fair_Share.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ForgotPasswordService {

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepo repo;

    @Autowired
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Forgot password - Generate and send OTP
    public ResponseEntity<?> sendandsaveotp(String mail) throws Exception {
        users u = repo.findByemail(mail);
        if (u == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        String otp = mailService.sendOtpEmail(mail);
        if (otp == null || otp.isEmpty()) {
            return new ResponseEntity<>("OTP not generated", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        u.setOtp(otp);
        repo.save(u);
        return new ResponseEntity<>("OTP sent successfully", HttpStatus.OK);
    }

    // Verify OTP
    public ResponseEntity<?> verifyotp(emailandotp emailandotp) {
        users u = repo.findByemail(emailandotp.getEmail());
        if (u == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        if (u.getOtp() != null && u.getOtp().equals(emailandotp.getOtp())) {
            u.setOtp(null); // ✅ clear OTP after successful verification
            repo.save(u);
            return new ResponseEntity<>("OTP verified", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid OTP", HttpStatus.BAD_REQUEST);
        }
    }

    // Change password
    public ResponseEntity<?> changepssword(Emailpassword emailpassword) {
        users u = repo.findByemail(emailpassword.getEmail());
        if (u == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        
        u.setPassword(passwordEncoder.encode(emailpassword.getPassword()));
        repo.save(u);
        return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
    }

}

