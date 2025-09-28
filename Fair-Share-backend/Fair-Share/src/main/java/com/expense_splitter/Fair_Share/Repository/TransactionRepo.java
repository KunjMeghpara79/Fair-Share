package com.expense_splitter.Fair_Share.Repository;

import com.expense_splitter.Fair_Share.Entity.Transactions;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepo extends MongoRepository<Transactions, ObjectId> {
    Transactions findBygroupcode(String groupcode);
}
