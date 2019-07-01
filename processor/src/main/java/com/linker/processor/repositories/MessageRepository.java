package com.linker.processor.repositories;

import com.google.common.collect.ImmutableList;
import com.linker.common.Message;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    public Page<Message> findMessages(String toUser, MessageType type, MessageState state, Integer pageSize) {
        PageRequest pageable = PageRequest.of(0, pageSize, new Sort(Sort.Direction.ASC, "createdAt"));
        Query query = Query.query(
                Criteria.where("to").is(toUser)
                        .and("content.type").is(type)
                        .and("state").is(state)
        ).with(pageable);

        long total = mongoTemplate.count(query, Message.class);
        List<Message> messages;
        if (total > 0) {
            messages = mongoTemplate.find(query, Message.class);
        } else {
            messages = ImmutableList.of();
        }

        return new PageImpl<>(messages, pageable, total);
    }
}
