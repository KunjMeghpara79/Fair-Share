package com.expense_splitter.Fair_Share.Entity;

import com.expense_splitter.Fair_Share.Enums.Currency;
import com.expense_splitter.Fair_Share.Enums.Timezones;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.expense_splitter.Fair_Share.Enums.Currency.INR;
import static com.expense_splitter.Fair_Share.Enums.Timezones.IST;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class users {

    @Id
    private ObjectId id;

    private String name;

    @Indexed(unique = true)
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Invalid email format"
    )
    private String email;

    private String password;



    private Set<ObjectId> Groups = new HashSet<>();

    private String avatar = "";

    private String otp ="";

    private Currency currency = INR;

    private Timezones timezone = IST;

    private Object notification=null;

    private Date createdAt;

    private Date updatedAt=null;

}
