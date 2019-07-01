package com.linker.processor.repositories;

import com.linker.common.Message;
import com.linker.common.MessageState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    public Message findById(String messageId) {
        return mongoTemplate.findById(messageId, Message.class);
    }

    public void save(Message message) {
        mongoTemplate.save(message);
    }

    public void updateState(String messageId, MessageState state) {
        Query query = Query.query(Criteria.where("_id").is(messageId));
        Update update = Update.update("state", state);
        mongoTemplate.updateFirst(query, update, Message.class);
    }
}
