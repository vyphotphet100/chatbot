package com.caovy2001.chatbot.service.pattern;

import com.caovy2001.chatbot.constant.ExceptionConstant;
import com.caovy2001.chatbot.entity.PatternEntity;
import com.caovy2001.chatbot.repository.PatternRepository;
import com.caovy2001.chatbot.service.BaseService;
import com.caovy2001.chatbot.service.pattern.command.CommandPatternAdd;
import com.caovy2001.chatbot.service.pattern.command.CommandPatternDelete;
import com.caovy2001.chatbot.service.pattern.command.CommandPatternUpdate;
import com.caovy2001.chatbot.service.pattern.response.ResponsePattern;
import com.caovy2001.chatbot.service.pattern.response.ResponsePatternAdd;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class PatternService extends BaseService implements IPatternService {
    @Autowired
    private PatternRepository patternRepository;

    @Override
    public ResponsePatternAdd add(CommandPatternAdd command) {
        if (StringUtils.isAnyBlank(command.getUserId(), command.getContent(), command.getIntentId())) {
            return returnException(ExceptionConstant.missing_param, ResponsePatternAdd.class);
        }

        PatternEntity pattern = PatternEntity.builder()
                .content(command.getContent())
                .intentId(command.getIntentId())
                .userId(command.getUserId())
                .build();
        PatternEntity addedPattern = patternRepository.insert(pattern);
        return ResponsePatternAdd.builder()
                .id(addedPattern.getId())
                .build();
    }

    @Override
    public ResponsePattern delete(CommandPatternDelete command) {
        if (StringUtils.isAnyBlank(command.getId(), command.getUserId())) {
            return returnException(ExceptionConstant.missing_param, ResponsePattern.class);
        }

        PatternEntity pattern = patternRepository.findByIdAndUserId(command.getId(), command.getUserId());
        if (pattern == null) {
            return returnException("pattern_not_exist", ResponsePattern.class);
        }

        patternRepository.deleteById(command.getId());
        return ResponsePattern.builder()
                .pattern(PatternEntity.builder()
                        .id(command.getId())
                        .userId(command.getUserId())
                        .build())
                .build();
    }

    @Override
    public ResponsePattern getByIntentId(String intentId,String userId) {
        if (intentId == null){
            return  returnException(ExceptionConstant.missing_param, ResponsePattern.class);
        }
        return ResponsePattern.builder().patterns(patternRepository.findByIntentIdInAndUserId(intentId,userId)).build();
    }

    @Override
    public List<PatternEntity> addMany(List<PatternEntity> patternsToAdd) {
        return patternRepository.insert(patternsToAdd);
    }

    @Override
    public ResponsePattern getById(String id, String userId) {
        if (StringUtils.isAnyBlank(id, userId)) {
            return returnException(ExceptionConstant.missing_param, ResponsePattern.class);
        }

        PatternEntity pattern = patternRepository.findByIdAndUserId(id, userId);
        if (pattern == null) {
            return returnException("pattern_not_exist", ResponsePattern.class);
        }

        return ResponsePattern.builder()
                .pattern(pattern)
                .build();
    }

    @Override
    public ResponsePattern getByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return returnException(ExceptionConstant.missing_param, ResponsePattern.class);
        }

        List<PatternEntity> patterns = patternRepository.findAllByUserId(userId);
        if (CollectionUtils.isEmpty(patterns)) {
            patterns = new ArrayList<>();
        }

        return ResponsePattern.builder()
                .patterns(patterns)
                .build();
    }

    @Override
    public ResponsePattern update(CommandPatternUpdate command) {
        if (StringUtils.isAnyBlank(command.getId(), command.getUserId())) {
            return returnException(ExceptionConstant.missing_param, ResponsePattern.class);
        }

        PatternEntity pattern = patternRepository.findByIdAndUserId(command.getId(), command.getUserId());
        if (pattern == null) {
            return returnException("pattern_not_exist", ResponsePattern.class);
        }

        pattern.setContent(command.getContent());
        PatternEntity updatedPattern = patternRepository.save(pattern);

        return ResponsePattern.builder()
                .pattern(updatedPattern)
                .build();
    }
}
