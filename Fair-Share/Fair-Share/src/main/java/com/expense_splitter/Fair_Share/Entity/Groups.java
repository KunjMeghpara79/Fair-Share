package com.expense_splitter.Fair_Share.Entity;

import com.expense_splitter.Fair_Share.Enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.expense_splitter.Fair_Share.Enums.Currency.INR;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Groups {


    @Id
    private ObjectId id;

    private String name;

    private String description;

    private Set<ObjectId> Expenses= new HashSet<>();

    private String groupcode;


    private ObjectId createdby;


    private Set<ObjectId> members = new HashSet<>();

    private Currency currency = INR;

    private Date createdAt;

    private Date updatedAt =null;
}
