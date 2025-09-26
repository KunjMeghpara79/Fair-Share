package com.expense_splitter.Fair_Share.Service;

import com.expense_splitter.Fair_Share.Entity.Expenses;
import com.expense_splitter.Fair_Share.Entity.Groups;
import com.expense_splitter.Fair_Share.Entity.users;
import com.expense_splitter.Fair_Share.Repository.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroupsService {

    @Autowired
    private GroupRepo repo;

    @Autowired
    private SettlementService settlementService;


    @Autowired
    private ExpenseRepo erepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SettlementRepo srepo;


    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionRepo transactionRepo;

    private String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private String GenerateGroupcode(){

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        String randomCode = sb.toString();
        return randomCode;
    }

    public ResponseEntity<?> savegroup(Groups group) throws Exception {
        Authentication auth  = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()){
            users u = userRepo.findByemail(auth.getName());
            group.setCreatedby(u.getId());
            group.getMembers().add(u.getId());
            group.setCreatedAt(new Date(System.currentTimeMillis()));
            String grpcode;
            do {
                grpcode = GenerateGroupcode();
            } while (repo.findBygroupcode(grpcode) != null);
            group.setGroupcode(grpcode);
            repo.save(group);
            u.getGroups().add(group.getId());
            userRepo.save(u);
            settlementService.setSettlement(group.getGroupcode(),u.getName());
            transactionService.createTransactionsForNewGroup(grpcode);
            return new ResponseEntity<>("Group Created", HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>("You are not Authenticated", HttpStatus.UNAUTHORIZED);

        }
    }

    public ResponseEntity<?> joingroup(String code) throws Exception {
        Authentication auth  = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()){
            users u = userRepo.findByemail(auth.getName());
            Groups g = repo.findBygroupcode(code);
            if (g != null){
                if (g.getMembers().contains(u.getId())) {
                    return new ResponseEntity<>(" You are already in the group", HttpStatus.BAD_REQUEST);
                }
                g.getMembers().add(u.getId());
                repo.save(g);
                u.getGroups().add(g.getId());
                userRepo.save(u);
                settlementService.addMember(code,u.getName());
                return new ResponseEntity<>(u.getName()+ " Succesfully joined the group ",HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("Group Not Found",HttpStatus.NOT_FOUND);
            }
        }
        else
            return new ResponseEntity<>("You are not authenticated", HttpStatus.UNAUTHORIZED);

    }


    public ResponseEntity<?> getgroups() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String email = auth.getName();
        users u = userRepo.findByemail(email);

        if (u == null) {
            return new ResponseEntity<>("User not found: " + email, HttpStatus.NOT_FOUND);
        }

        List<Groups> groups = u.getGroups() == null || u.getGroups().isEmpty()
                ? List.of()
                : repo.findAllById(u.getGroups());

        return ResponseEntity.ok(groups);
    }

    @Transactional
    public ResponseEntity<?> deletegroup(String code){
        Authentication auth  = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {
            users u = userRepo.findByemail(auth.getName());
            Groups grp = repo.findBygroupcode(code);
            if (grp != null){
                if (u.getId().equals(grp.getCreatedby())){

                    for(ObjectId uid:grp.getMembers()){
                        users user = userRepo.findByid(uid);
                        user.getGroups().remove(grp.getId());
                        userRepo.save(user);
                    }

                    for (ObjectId eid:grp.getExpenses()){
                        erepo.delete(erepo.findByid(eid));
                    }
                    srepo.delete(srepo.findBygroupcode(grp.getGroupcode()));
                    transactionRepo.delete(transactionRepo.findBygroupcode(code));
                    repo.delete(grp);
                    return new ResponseEntity<>("Group Deleted",HttpStatus.OK);
                }
                else return new ResponseEntity<>("You can not delete the group",HttpStatus.FORBIDDEN);
            }
        }

        return new ResponseEntity<>("You are not authenticated", HttpStatus.UNAUTHORIZED);

    }

    @Transactional
    public ResponseEntity<?> exitgroup(String code){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()){
            users u = userRepo.findByemail(auth.getName());
            Groups g = repo.findBygroupcode(code);
            if (g == null){
                return new ResponseEntity<>("Group does not exist",HttpStatus.NOT_FOUND);
            }
            if (u.getId().equals(g.getCreatedby())){
                return new ResponseEntity<>("You can not exit the group",HttpStatus.FORBIDDEN);
            }
            else {
                if (g.getMembers().contains(u.getId())){
                    g.getMembers().remove(u.getId());
                    repo.save(g);
                    u.getGroups().remove(g.getId());
                    userRepo.save(u);
                    return new ResponseEntity<>(u.getName()+" Exited the group", HttpStatus.OK);
                }
                else {
                    return new ResponseEntity<>("You are not in the group",HttpStatus.BAD_REQUEST);
                }
            }

        }
        else
            return new ResponseEntity<>("You are not authenticated", HttpStatus.UNAUTHORIZED);


    }


    public ResponseEntity<?> getexpenses(String code){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated()){
            return new ResponseEntity<>("you are not authenticated",HttpStatus.FORBIDDEN);
        }
        ObjectId uid = userRepo.findByemail(auth.getName()).getId();
        Groups grp = repo.findBygroupcode(code);
        if (grp == null){
            return new ResponseEntity<>("Group not found",HttpStatus.NOT_FOUND);
        }
        if (!grp.getMembers().contains(uid)){
            return new ResponseEntity<>("You can not get expenses ",HttpStatus.FORBIDDEN);
        }

        List<Expenses> list = erepo.findAllById(grp.getExpenses());


        return new ResponseEntity<>(list,HttpStatus.OK);
    }


    public ResponseEntity<?> gettransaction(String groupcode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated()){
            return new ResponseEntity<>("you are not authenticated",HttpStatus.FORBIDDEN);
        }
       if (transactionRepo.findBygroupcode(groupcode) == null){
           return new ResponseEntity<>("Settlement not found",HttpStatus.NOT_FOUND);
       }
        return new ResponseEntity<>(transactionRepo.findBygroupcode(groupcode),HttpStatus.OK);
    }

    public ResponseEntity<?> getbycode(String groupcode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return new ResponseEntity<>("you are not authenticated", HttpStatus.FORBIDDEN);
        }

        var group = repo.findBygroupcode(groupcode);
        if (group == null) {
            return new ResponseEntity<>("Settlement not found", HttpStatus.NOT_FOUND);
        }

        // Convert ObjectIds to string
        List<String> memberIds = group.getMembers().stream()
                .map(ObjectId::toString)
                .toList();

        // Build response map
        Map<String, Object> response = new HashMap<>();
        response.put("id", group.getId().toString());
        response.put("name", group.getName());
        response.put("groupcode", group.getGroupcode());
        response.put("description", group.getDescription());
        response.put("members", memberIds);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
