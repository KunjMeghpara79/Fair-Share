package com.expense_splitter.Fair_Share.Controllers;

import com.expense_splitter.Fair_Share.Entity.Expenses;
import com.expense_splitter.Fair_Share.Service.ExpenseService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/Expense")
public class ExpensesController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping("/Create")
    public ResponseEntity<?> addexpense(@RequestBody Expenses expense) throws Exception {
        return expenseService.addExpense(expense);
    }

    @DeleteMapping("/Delete")
    public ResponseEntity<?> deleteexpense(@RequestBody Map<String, String> body) throws Exception {
        String eidStr = body.get("id");
        ObjectId eid = new ObjectId(eidStr);
        return expenseService.deleteexpense(eid);
    }



}
