package com.expense_splitter.Fair_Share.Service;

import com.expense_splitter.Fair_Share.Entity.Groups;
import com.expense_splitter.Fair_Share.Entity.Settlement;
import com.expense_splitter.Fair_Share.Entity.Transactions;
import com.expense_splitter.Fair_Share.Repository.GroupRepo;
import com.expense_splitter.Fair_Share.Repository.SettlementRepo;
import com.expense_splitter.Fair_Share.Repository.TransactionRepo;
import com.expense_splitter.Fair_Share.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepo tRepo;

    @Autowired
    private GroupRepo grepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SettlementRepo srepo;

    /**
     * Create an empty Transactions document for a newly created group.
     */

    public void createTransactionsForNewGroup(String groupCode) {
        Transactions transactions = new Transactions();
        transactions.setGroupcode(groupCode);
        transactions.setTransactions(new ArrayList<>()); // no debts initially
        tRepo.save(transactions);
    }

    /**
     * Generate transactions from a group's settlement.
     * Each transaction represents "who pays whom and how much".
     */
    public void generateTransactionsFromSettlement(Settlement settlement) throws Exception {
        // 1️⃣ Fetch the settlement for the group

        if (settlement == null) throw new Exception("Settlement not found");

        // 2️⃣ Separate creditors (+ve net) and debtors (-ve net)
        List<Settlement.Balance> creditors = settlement.getBalances().stream()
                .filter(b -> b.getNetAmount() > 0)
                .sorted((a, b) -> Double.compare(b.getNetAmount(), a.getNetAmount())) // largest credit first
                .toList();

        List<Settlement.Balance> debtors = settlement.getBalances().stream()
                .filter(b -> b.getNetAmount() < 0)
                .sorted((a, b) -> Double.compare(a.getNetAmount(), b.getNetAmount())) // most negative first
                .toList();

        // 3️⃣ Create a Transactions document for this group
        Transactions transactions = tRepo.findBygroupcode(settlement.getGroupcode());
        if (transactions == null){
            throw new Exception("transaction not found");
        }
        transactions.setGroupcode(settlement.getGroupcode());
        transactions.setTransactions(new ArrayList<>());

        // 4️⃣ Temporary arrays to track remaining credit/debt
        double[] creditorRemaining = creditors.stream().mapToDouble(Settlement.Balance::getNetAmount).toArray();
        double[] debtorRemaining = debtors.stream().mapToDouble(b -> -b.getNetAmount()).toArray(); // make positive

        int c = 0, d = 0;

        // 5️⃣ Generate transactions
        while (c < creditors.size() && d < debtors.size()) {
            double settledAmount = Math.min(creditorRemaining[c], debtorRemaining[d]);

            // Add transaction detail
            transactions.getTransactions().add(new Transactions.TransactionDetail(
                    debtors.get(d).getUserName(),    // from debtor
                    creditors.get(c).getUserName(),  // to creditor
                    settledAmount
            ));

            // Update remaining amounts
            creditorRemaining[c] -= settledAmount;
            debtorRemaining[d] -= settledAmount;

            // Move pointers
            if (creditorRemaining[c] == 0) c++;
            if (debtorRemaining[d] == 0) d++;
        }

        // 6️⃣ Save the Transactions document
        tRepo.save(transactions);

    }
    public ResponseEntity<?> getTransactions(String code){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Groups grp = grepo.findBygroupcode(code);

        if (!grp.getMembers().contains(userRepo.findByemail(auth.getName()))){
            return new ResponseEntity<>("You are not in this group",HttpStatus.FORBIDDEN);
        }
       Transactions transactions = tRepo.findBygroupcode(code);
       if (transactions == null){
           return new ResponseEntity<>("Transaction not found ", HttpStatus.NOT_FOUND);
       }
       return new ResponseEntity<>(transactions,HttpStatus.OK);
    }
}
