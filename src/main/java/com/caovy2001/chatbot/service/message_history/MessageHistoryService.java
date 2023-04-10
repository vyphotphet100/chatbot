package com.caovy2001.chatbot.service.message_history;

import com.caovy2001.chatbot.constant.ExceptionConstant;
import com.caovy2001.chatbot.entity.EntityEntity;
import com.caovy2001.chatbot.entity.MessageHistoryEntity;
import com.caovy2001.chatbot.model.Paginated;
import com.caovy2001.chatbot.repository.MessageHistoryRepository;
import com.caovy2001.chatbot.service.BaseService;
import com.caovy2001.chatbot.service.message_history.command.CommandAddMessageHistory;
import com.caovy2001.chatbot.service.message_history.command.CommandGetListMessageHistory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MessageHistoryService extends BaseService implements IMessageHistoryService {
    @Autowired
    private MessageHistoryRepository messageHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public MessageHistoryEntity add(CommandAddMessageHistory command) {
        if (StringUtils.isAnyBlank(command.getUserId(), command.getMessage(), command.getScriptId(), command.getNodeId(), command.getSessionId()) ||
                command.getFrom() == null ||
                command.getCreatedDate() == null) {
            log.error("[{}] {}", new Exception().getStackTrace()[0], ExceptionConstant.missing_param);
            return null;
        }

        // Convert entity thành document (do trường entity type là transient)
        List<Document> entities = null;
        if (CollectionUtils.isNotEmpty(command.getEntities())) {
            entities = new ArrayList<>();
            for (EntityEntity entity: command.getEntities()) {
                entity.getEntityType().setUuid(null);
                entity.getEntityType().setUserId(null);
                entity.getEntityType().setLowerCaseName(null);
                entity.getEntityType().setSearchableName(null);
                entities.add(objectMapper.convertValue(entity, Document.class));
            }
        }

        return messageHistoryRepository.insert(MessageHistoryEntity.builder()
                .userId(command.getUserId())
                .nodeId(command.getNodeId())
                .scriptId(command.getScriptId())
                .sessionId(command.getSessionId())
                .message(command.getMessage())
                .from(command.getFrom())
                .entities(entities)
                .createdDate(command.getCreatedDate())
                .build());
    }

    @Override
    public List<MessageHistoryEntity> getList(CommandGetListMessageHistory command) {
        if (StringUtils.isAnyBlank(command.getUserId())) {
            log.error("[{}]: {}", new Exception().getStackTrace()[0], ExceptionConstant.missing_param);
            return null;
        }

        Query query = this.buildQueryGetList(command);
        if (query == null) {
            return null;
        }

        List<MessageHistoryEntity> messageHistories = mongoTemplate.find(query, MessageHistoryEntity.class);
        if (CollectionUtils.isEmpty(messageHistories)) {
            return null;
        }

        return messageHistories;
    }

    @Override
    public Paginated<MessageHistoryEntity> getPaginatedListBySessionIdAndScriptId(CommandGetListMessageHistory command) throws Exception {
        if (StringUtils.isAnyBlank(command.getUserId(), command.getSessionId(), command.getScriptId())) {
            throw new Exception(ExceptionConstant.missing_param);
        }

        if (command.getPage() <= 0) {
            throw new Exception("invalid_page_or_size");
        }

        if (command.getSize() <= 0) {
            return new Paginated<>(new ArrayList<>(), command.getPage(), command.getSize(), 0);
        }

        Query query = this.buildQueryGetList(command);
        if (query == null) {
            return new Paginated<>(new ArrayList<>(), command.getPage(), command.getSize(), 0);
        }

        long total = mongoTemplate.count(query, MessageHistoryEntity.class);
        if (total == 0L) {
            return new Paginated<>(new ArrayList<>(), command.getPage(), command.getSize(), 0);
        }

        PageRequest pageRequest = PageRequest.of(command.getPage() - 1, command.getSize());
        query.with(pageRequest);
        List<MessageHistoryEntity> messageHistories = mongoTemplate.find(query, MessageHistoryEntity.class);
        return new Paginated<>(messageHistories, command.getPage(), command.getSize(), total);
    }

    @Override
    public Paginated<MessageHistoryEntity> getPaginatedList(CommandGetListMessageHistory command) throws Exception {
        if (StringUtils.isBlank(command.getUserId())) {
            throw new Exception(ExceptionConstant.missing_param);
        }

        if (command.getPage() <= 0) {
            throw new Exception("invalid_page_or_size");
        }

        if (command.getSize() <= 0) {
            return new Paginated<>(new ArrayList<>(), command.getPage(), command.getSize(), 0);
        }

        Query query = this.buildQueryGetList(command);
        if (query == null) {
            return new Paginated<>(new ArrayList<>(), command.getPage(), command.getSize(), 0);
        }

        long total = mongoTemplate.count(query, MessageHistoryEntity.class);
        if (total == 0L) {
            return new Paginated<>(new ArrayList<>(), command.getPage(), command.getSize(), 0);
        }

        PageRequest pageRequest = PageRequest.of(command.getPage() - 1, command.getSize());
        query.with(pageRequest);
        List<MessageHistoryEntity> messageHistories = mongoTemplate.find(query, MessageHistoryEntity.class);
        return new Paginated<>(messageHistories, command.getPage(), command.getSize(), total);
    }

    private Query buildQueryGetList(CommandGetListMessageHistory command) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        List<Criteria> orCriteriaList = new ArrayList<>();
        List<Criteria> andCriteriaList = new ArrayList<>();

        andCriteriaList.add(Criteria.where("user_id").is(command.getUserId()));

        if (StringUtils.isNotBlank(command.getScriptId())) {
            andCriteriaList.add(Criteria.where("script_id").is(command.getScriptId()));
        }

        if (StringUtils.isNotBlank(command.getSessionId())) {
            andCriteriaList.add(Criteria.where("session_id").is(command.getSessionId()));
        }

        if (CollectionUtils.isNotEmpty(orCriteriaList)) {
            criteria.orOperator(orCriteriaList);
        }
        if (CollectionUtils.isNotEmpty(andCriteriaList)) {
            criteria.andOperator(andCriteriaList);
        }

        query.addCriteria(criteria);
        return query;
    }
}
