package com.expense_splitter.Fair_Share.Service;

import com.expense_splitter.Fair_Share.Entity.Expenses;
import com.expense_splitter.Fair_Share.Entity.Groups;
import com.expense_splitter.Fair_Share.Entity.Settlement;
import com.expense_splitter.Fair_Share.Entity.users;
import com.expense_splitter.Fair_Share.Repository.GroupRepo;
import com.expense_splitter.Fair_Share.Repository.SettlementRepo;
import com.expense_splitter.Fair_Share.Repository.UserRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class SettlementService {


    @Autowired
    private GroupRepo grepo;

    @Autowired
    private UserRepo repo;

    @Autowired
    private SettlementRepo srepo;

    @Autowired
    private TransactionService transactionService;

    public void setSettlement(String code,String name) throws Exception {
        Groups grp = grepo.findBygroupcode(code);
        if (grp == null){
            throw new Exception("Group not found");
        }

        Settlement settlement = new Settlement();
        settlement.setGroupcode(code);
        settlement.setCreatedat(new Date(System.currentTimeMillis()));
            settlement.getBalances().add(new Settlement.Balance(name,0.0));

        srepo.save(settlement);
    }

    public void addMember(String code,String name) throws Exception {
        Groups grp = grepo.findBygroupcode(code);
        if (grp == null){
            throw new Exception("Group not found");
        }

        Settlement settlement = srepo.findBygroupcode(code);
        if (settlement == null){
            throw new Exception("Settlement not found");
        }
        boolean exists = settlement.getBalances().stream()
                .anyMatch(b -> b.getUserName().equals(name));

        if (!exists) {
            settlement.getBalances().add(new Settlement.Balance(name, 0.0));
            srepo.save(settlement);
        }

    }
    public void updateSettlementforequal(String code, ObjectId payerId, double amount) throws Exception {
        Groups group = grepo.findBygroupcode(code);
        if (group == null) {
            throw new Exception("Group not found");
        }

        Settlement settlement = srepo.findBygroupcode(code);
        if (settlement == null) {
            throw new Exception("Settlement not found");
        }

        users payer = repo.findByid(payerId);
        if (payer == null) {
            throw new Exception("Payer not found");
        }

        int totalMembers = group.getMembers().size();
        double share = amount / totalMembers;

        for (ObjectId memberId : group.getMembers()) {
            users member = repo.findByid(memberId);

            if (memberId.equals(payerId)) {
                // ✅ payer gets credit (paid full - their own share)
                updateBalance(settlement, payer.getName(), amount - share);
            } else {
                // ✅ each other member owes their share
                updateBalance(settlement, member.getName(), -share);
            }
        }
        srepo.save(settlement);
        transactionService.generateTransactionsFromSettlement(settlement);

    }

    public void updateSettlementPercentage(String code, ObjectId payerId, double amount, List<Expenses.SplitDetail> splitDetails) throws Exception {
        Groups group = grepo.findBygroupcode(code);
        if (group == null) throw new Exception("Group not found");

        Settlement settlement = srepo.findBygroupcode(code);
        if (settlement == null) throw new Exception("Settlement not found");

        users payer = repo.findByid(payerId);
        if (payer == null) throw new Exception("Payer not found");

        // Loop over each split detail
        for (Expenses.SplitDetail detail : splitDetails) {
            String memberName = detail.getName();
            double share = (amount * detail.getPercentage()) / 100.0;

            if (memberName.equals(payer.getName())) {
                // ✅ Payer gets credit (paid full - their own share)
                updateBalance(settlement, payer.getName(), amount - share);
            } else {
                // ✅ Others owe their share
                updateBalance(settlement, memberName, -share);
            }
        }
        srepo.save(settlement);
        transactionService.generateTransactionsFromSettlement(settlement);
    }
    public void updateSettlementCustom(String code, ObjectId payerId, double amount, List<Expenses.SplitDetail> splitDetails) throws Exception {
        Groups group = grepo.findBygroupcode(code);
        if (group == null) throw new Exception("Group not found");

        Settlement settlement = srepo.findBygroupcode(code);
        if (settlement == null) throw new Exception("Settlement not found");

        users payer = repo.findByid(payerId);
        if (payer == null) throw new Exception("Payer not found");

        for (Expenses.SplitDetail detail : splitDetails) {
            String memberName = detail.getName();
            double share = detail.getShareAmount(); // custom amounts directly

            if (memberName.equals(payer.getName())) {
                // ✅ Payer gets credited (paid full - their own share)
                updateBalance(settlement, payer.getName(), amount - share);
            } else {
                // ✅ Others owe exactly their share
                updateBalance(settlement, memberName, -share);
            }
        }
        srepo.save(settlement);
        transactionService.generateTransactionsFromSettlement(settlement);
    }

    public void updateSettlementOnExpenseDelete(Expenses expense) throws Exception {
        Settlement settlement = srepo.findBygroupcode(expense.getGroupcode());
        if (settlement == null) {
            throw new Exception("Settlement not found");
        }
        String payerName = expense.getPayername();
        double amount = expense.getAmount();

        // Reverse payer's credit
        for (Settlement.Balance bal : settlement.getBalances()) {
            if (Objects.equals(bal.getUserName(), payerName)) {
                bal.setNetAmount(bal.getNetAmount() - amount);
            }
        }

        // Reverse members' debts (❌ exclude payer if he is in splitDetails)
        for (Expenses.SplitDetail detail : expense.getSplitDetails()) {
                for (Settlement.Balance bal : settlement.getBalances()) {
                    if (bal.getUserName().equals(detail.getName())) {
                        bal.setNetAmount(bal.getNetAmount() + detail.getShareAmount());
                    }
                }

        }
        srepo.save(settlement);
        transactionService.generateTransactionsFromSettlement(settlement);
    }



    /**
     * Utility method to update balance of a user
     */
    private void updateBalance(Settlement settlement, String userName, double delta) {
        for (Settlement.Balance balance : settlement.getBalances()) {
            if (balance.getUserName().equals(userName)) {
                balance.setNetAmount(balance.getNetAmount() + delta);
                return;
            }
        }
        // If user not found in balances (edge case, new joiner not initialized)
        settlement.getBalances().add(new Settlement.Balance(userName, delta));
    }

}
