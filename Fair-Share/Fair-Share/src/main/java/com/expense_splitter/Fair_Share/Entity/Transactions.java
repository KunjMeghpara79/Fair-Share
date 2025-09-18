package com.expense_splitter.Fair_Share.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transactions {

    @Id
    private ObjectId id;

    private String groupcode;

    private List<TransactionDetail> transactions = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransactionDetail {
        private String fromUser;
        private String toUser;
        private double amount;
    }
}
