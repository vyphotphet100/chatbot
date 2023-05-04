package com.caovy2001.chatbot.service.pattern;

import com.caovy2001.chatbot.service.IBaseService;
import com.caovy2001.chatbot.service.pattern.command.CommandGetListPattern;
import com.caovy2001.chatbot.service.pattern.command.CommandImportPatternsFromFile;

public interface IPatternService extends IBaseService {
    void importFromFile(CommandImportPatternsFromFile command) throws Exception;

    void exportExcel(CommandGetListPattern command, String sessionId) throws Exception;

    void removeExportedTrainingDataFile(String userId) throws Exception;
}
