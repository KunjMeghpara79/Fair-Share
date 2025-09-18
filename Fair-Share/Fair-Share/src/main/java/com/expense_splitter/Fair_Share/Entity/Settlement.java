package com.expense_splitter.Fair_Share.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settlement {

    @Id
    private ObjectId id;

    private String groupcode;
    private Date createdat;
    private List<Balance> balances = new ArrayList<>();


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Balance {
        private String userName;  // or ObjectId userId
        private Double netAmount; // +ve means they should receive, -ve means they owe
    }
}
