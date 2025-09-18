package com.expense_splitter.Fair_Share.Entity;

import com.expense_splitter.Fair_Share.Enums.Currency;
import com.expense_splitter.Fair_Share.Enums.SplitType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

import static com.expense_splitter.Fair_Share.Enums.Currency.INR;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expenses {

    @Id
    private ObjectId id;

    private String description;
    private Double amount;
    private Currency currency = INR;

    private String groupcode;
    private ObjectId group=null;
    private String payername;
    private ObjectId payer=null;

    private List<SplitDetail> splitDetails=null;
    private SplitType splitType = SplitType.EQUAL;

    private ObjectId addedBy=null;


    private Date date;
    private Date createdAt;
    private Date updatedAt=null;

    // Inner class for split details (can be moved to its own file if needed)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitDetail {
        private String name;
        private Double shareAmount;
        private Double percentage;
    }
}

