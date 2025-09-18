package com.expense_splitter.Fair_Share.Controllers;

import com.expense_splitter.Fair_Share.Entity.Groups;
import com.expense_splitter.Fair_Share.Service.GroupsService;
import com.expense_splitter.Fair_Share.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Groups")
public class GroupsController {


    @Autowired
    private GroupsService gservice;

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/Create")
    public ResponseEntity<?> creategroup(@RequestBody Groups group) throws Exception {
        return gservice.savegroup(group);
    }

    @PostMapping("/Join")
    public ResponseEntity<?> joingroup(@RequestBody String code) throws Exception {
        return gservice.joingroup(code);
    }

    @GetMapping("/Get-Groups")
    public ResponseEntity<?> getgroups(){
        return gservice.getgroups();
    }

    @DeleteMapping("/Delete-Group")
    public ResponseEntity<?> deletegroup(@RequestBody  String code){
        return gservice.deletegroup(code);
    }

    @DeleteMapping("/Exit-Group")
    public ResponseEntity<?> exitgroup(@RequestBody String code){
        return gservice.exitgroup(code);
    }

    @GetMapping("/Get-Expenses/{code}")
    public ResponseEntity<?> getexpenses(@PathVariable String code){
        return gservice.getexpenses(code);
    }

    @GetMapping("/Get-Transactions")
    public ResponseEntity<?> getTransactions(@RequestBody String code){
        return transactionService.getTransactions(code);
    }


}
