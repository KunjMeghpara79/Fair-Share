package com.expense_splitter.Fair_Share.Repository;

import com.expense_splitter.Fair_Share.Entity.Groups;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupRepo extends MongoRepository<Groups, ObjectId> {
    Groups findBygroupcode(String groupcode);
    Groups findByid(ObjectId id);
}
