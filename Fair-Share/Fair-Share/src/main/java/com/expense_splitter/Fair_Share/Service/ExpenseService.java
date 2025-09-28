package com.expense_splitter.Fair_Share.Service;
import com.expense_splitter.Fair_Share.Entity.Expenses;
import com.expense_splitter.Fair_Share.Entity.Groups;
import com.expense_splitter.Fair_Share.Entity.users;
import com.expense_splitter.Fair_Share.Enums.SplitType;
import com.expense_splitter.Fair_Share.Repository.ExpenseRepo;
import com.expense_splitter.Fair_Share.Repository.GroupRepo;
import com.expense_splitter.Fair_Share.Repository.UserRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepo expenseRepo;

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SettlementService settlementService;
    @Transactional
    public ResponseEntity<?> addExpense(Expenses expenseRequest) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {
            String email = auth.getName();
            users adder = userRepo.findByemail(email);
            if(expenseRequest.getAmount() < 0 ){
                return new ResponseEntity<>("Amount can not be less than zero",HttpStatus.BAD_REQUEST);
            }

            // 1. ✅ Find group by groupCode
            Groups group = groupRepo.findBygroupcode(expenseRequest.getGroupcode());
            if (group == null) {
               return new ResponseEntity<>("Group not found",HttpStatus.FORBIDDEN);
            }

            if (!group.getMembers().contains(adder.getId())){
                return new ResponseEntity<>("You are not the part of the group",HttpStatus.FORBIDDEN);
            }

            // 2. ✅ Validate payer

            users u = userRepo.findByname(expenseRequest.getPayername());
            if (u == null) {
                return new ResponseEntity<>("Please Select the name." ,HttpStatus.FORBIDDEN);
            }
            ObjectId payerId = u.getId();

            boolean payerExists = group.getMembers().stream()
                    .anyMatch(m -> m.equals(payerId));
            if (!payerExists) {
                throw new IllegalArgumentException("Payer is not a member of the group");
            }

            double amount = expenseRequest.getAmount();  // total expense
            List<Expenses.SplitDetail> splitDetails = new ArrayList<>();

            // 3. ✅ Generate SplitDetails based on SplitType
            if (expenseRequest.getSplitType() == SplitType.EQUAL) {
                // --- Equal Split ---
                double share = amount / group.getMembers().size();
                for (ObjectId memberId : group.getMembers()) {
                    if (!memberId.equals(payerId)) {
                        splitDetails.add(new Expenses.SplitDetail(userRepo.findByid(memberId).getName(), share, null));
                    }
                }
                settlementService.updateSettlementforequal(group.getGroupcode(),payerId,amount);

            } else if (expenseRequest.getSplitType() == SplitType.PERCENTAGE) {
                // --- Percentage Split ---
                double totalPercentage = expenseRequest.getSplitDetails().stream()
                        .mapToDouble(Expenses.SplitDetail::getPercentage).sum();

                if (Math.abs(totalPercentage - 100.0) > 0.01) {
                    return new ResponseEntity<>("Total must be 100",HttpStatus.FORBIDDEN);
                }

                for (Expenses.SplitDetail detail : expenseRequest.getSplitDetails()) {
                    // ✅ Resolve username to ObjectId
                    users user = userRepo.findByname(detail.getName());
                    if (user == null) {
                        throw new IllegalArgumentException("User not found: " + detail.getName());
                    }
                    double share = (amount * detail.getPercentage()) / 100.0;
                    splitDetails.add(new Expenses.SplitDetail(user.getName(), share, detail.getPercentage()));
                }
                settlementService.updateSettlementPercentage(group.getGroupcode(),payerId,amount,splitDetails);

            } else if (expenseRequest.getSplitType() == SplitType.CUSTOM) {
                // --- Custom Split ---
                double total = expenseRequest.getSplitDetails().stream()
                        .mapToDouble(Expenses.SplitDetail::getShareAmount).sum();

                if (Math.abs(total - amount) > 0.01) { // tolerance for floating-point errors
                    return new ResponseEntity<>("not matching the total amount",HttpStatus.FORBIDDEN);
                }

                for (Expenses.SplitDetail detail : expenseRequest.getSplitDetails()) {
                    // ✅ Resolve username to ObjectId
                    users user = userRepo.findByname(detail.getName());
                    if (user == null) {
                        throw new IllegalArgumentException("User not found: " + detail.getName());
                    }
                    splitDetails.add(new Expenses.SplitDetail(user.getName(), detail.getShareAmount(), null));
                }
                settlementService.updateSettlementCustom(group.getGroupcode(),payerId,amount,splitDetails);
            } else {
                throw new IllegalArgumentException("Invalid split type");
            }


            // 4. ✅ Build Expense object
            Expenses expense = new Expenses();
            expense.setDescription(expenseRequest.getDescription());
            expense.setAmount(amount);
            expense.setPayername(expenseRequest.getPayername());
            expense.setGroup(group.getId());
            expense.setAdder(adder.getName());
            expense.setCurrency(expenseRequest.getCurrency());
            expense.setGroupcode(expenseRequest.getGroupcode());
            expense.setPayer(payerId);
            expense.setSplitDetails(splitDetails);
            expense.setSplitType(expenseRequest.getSplitType());
            expense.setAddedBy(adder.getId());
            expense.setDate(expenseRequest.getDate());
            expense.setCreatedAt(new Date());
            expense.setUpdatedAt(new Date());
            expenseRepo.save(expense);
            group.getExpenses().add(expense.getId());
            groupRepo.save(group);

            // 5. ✅ Save in DB
            return new ResponseEntity<>(expense, HttpStatus.OK);
        }
        return new ResponseEntity<>("You are not authenticated",HttpStatus.UNAUTHORIZED);
    }

    @Transactional
    public ResponseEntity<?> deleteexpense(ObjectId eid) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated()) {
            return new ResponseEntity<>("You are not authenticated", HttpStatus.FORBIDDEN);
        }

        users u = userRepo.findByemail(auth.getName());
        Expenses e = expenseRepo.findByid(eid);
        if (e == null) {
            return new ResponseEntity<>("Expense not found", HttpStatus.FORBIDDEN);
        }
        System.out.println("User ID: " + u.getId());
        System.out.println("Expense addedBy: " + e.getAddedBy());

        // ✅ Fix: compare IDs as Strings
        if (!u.getId().toString().equals(e.getAddedBy().toString())) {
            return new ResponseEntity<>("You can not delete this expense", HttpStatus.FORBIDDEN);
        }

        if (u.getGroups().contains(e.getGroup())) {
            Groups g = groupRepo.findByid(e.getGroup());
            g.getExpenses().remove(eid);
            groupRepo.save(g);
            settlementService.updateSettlementOnExpenseDelete(e);
            expenseRepo.delete(e);
            return new ResponseEntity<>("Expense Deleted", HttpStatus.OK);
        }

        return new ResponseEntity<>("You can not delete the expense as you are not the part of the group", HttpStatus.FORBIDDEN);
    }

}
