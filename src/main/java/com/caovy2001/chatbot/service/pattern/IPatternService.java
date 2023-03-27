package com.caovy2001.chatbot.service.pattern;

import com.caovy2001.chatbot.entity.PatternEntity;
import com.caovy2001.chatbot.model.Paginated;
import com.caovy2001.chatbot.service.IBaseService;
import com.caovy2001.chatbot.service.pattern.command.*;
import com.caovy2001.chatbot.service.pattern.response.ResponsePattern;
import com.caovy2001.chatbot.service.pattern.response.ResponsePatternAdd;

import java.util.List;

public interface IPatternService extends IBaseService {
    ResponsePatternAdd add(CommandPatternAdd command);
    ResponsePattern delete(CommandPatternDelete command);
    ResponsePattern getByIntentId(String intentId,String userId);

    List<PatternEntity> addMany(List<PatternEntity> patternsToAdd);

    List<PatternEntity> addMany(CommandPatternAddMany commandPatternAddMany);

    ResponsePattern getById(String id, String userId);

    ResponsePattern getByUserId(String userId);

    ResponsePattern update(CommandPatternUpdate command);

    Paginated<PatternEntity> getPaginationByUserId(String userId, int page, int size);
    Paginated<PatternEntity> getPaginationByUserId(CommandGetListPattern command);

    Paginated<PatternEntity> getPaginationByIntentId(String intentId, int page, int size);

    void importFromExcel(CommandImportPatternsFromExcel command) throws Exception;

    List<PatternEntity> getList(CommandGetListPattern command);
}
