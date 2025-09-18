package com.expense_splitter.Fair_Share.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Authresponse {
    private String token;
    private String email;
    private String name; // or whatever fields you want

}
