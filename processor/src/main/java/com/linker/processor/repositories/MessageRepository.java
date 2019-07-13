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
import java.util.Set;

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

    public Page<Message> findMessages(String toUser, Set<MessageType> types, Set<MessageState> states, Integer pageSize) {
        PageRequest pageable = PageRequest.of(0, pageSize, new Sort(Sort.Direction.ASC, "createdAt"));

        Criteria criteria = Criteria.where("to").is(toUser)
                .and("state").in(states);

        if (types != null) {
            criteria = criteria.and("content.type").in(types);
        }

        return buildPage(criteria, pageable, Message.class);
    }

    public Page<Message> findMessagesByTypes(Set<MessageType> types, Integer pageSize) {
        PageRequest pageable = PageRequest.of(0, pageSize, new Sort(Sort.Direction.ASC, "createdAt"));

        Criteria criteria = Criteria.where("content.type").in(types);
        return buildPage(criteria, pageable, Message.class);
    }

    public long count() {
        return mongoTemplate.count(new Query(), Message.class);
    }

    public void removeAll() {
        mongoTemplate.remove(new Query(), Message.class);
    }

    public <T> Page<T> buildPage(Criteria criteria, PageRequest pageable, Class<T> clazz) {
        Query query = Query.query(criteria).with(pageable);

        long total = mongoTemplate.count(query, clazz);
        List<T> objects;
        if (total > 0) {
            objects = mongoTemplate.find(query, clazz);
        } else {
            objects = ImmutableList.of();
        }

        return new PageImpl<T>(objects, pageable, total);
    }
}
