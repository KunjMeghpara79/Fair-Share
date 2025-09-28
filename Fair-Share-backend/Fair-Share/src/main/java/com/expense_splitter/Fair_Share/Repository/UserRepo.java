package com.expense_splitter.Fair_Share.Repository;

import com.expense_splitter.Fair_Share.Entity.users;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<users, ObjectId> {
    users findByemail(String email);
    users findByid(ObjectId id);
    users findByname(String name);
}
