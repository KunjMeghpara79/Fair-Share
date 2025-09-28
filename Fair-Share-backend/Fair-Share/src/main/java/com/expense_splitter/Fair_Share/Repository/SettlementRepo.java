package com.expense_splitter.Fair_Share.Repository;

import com.expense_splitter.Fair_Share.Entity.Settlement;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SettlementRepo extends MongoRepository<Settlement, ObjectId> {
    Settlement findBygroupcode(String groupcode);
}
