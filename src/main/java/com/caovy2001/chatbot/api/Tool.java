package com.caovy2001.chatbot.api;

import com.caovy2001.chatbot.entity.IntentEntity;
import com.caovy2001.chatbot.entity.PatternEntity;
import com.caovy2001.chatbot.service.intent.IIntentService;
import com.caovy2001.chatbot.service.intent.command.CommandIntentAddMany;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@RestController
@RequestMapping("/tool")
@Slf4j
public class Tool {
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/tool1")
    public ResponseEntity<Boolean> tool1() {
        try {
            ResponseListPatterns responseListPatterns = null;
            int page = 1;

            String intentId = "634d6c0242d14f7ec2df007e";
            while (true) {
                Map<String, Object> command = new HashMap<>();
                command.put("keyword", "");
                command.put("page", page);
                command.put("size", 50);
                command.put("intent_ids", Collections.singletonList(intentId));

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ0ZW5hbnRfaWQiOiI2MzU1NjYwM2U1MzgxYjJjMmUwNjBhZjQiLCJhZ2VudF9pZCI6IjYzNTU2NjAzZTUzODFiMmMyZTA2MGFmNyIsImxpc3Rfcm9sZSI6bnVsbCwiZnVsbF9uYW1lIjoiQ2FsbEJvdCBBZG1pbiIsImJ1c2luZXNzX3R5cGUiOiJub3JtYWwiLCJpc19vd25lcl90ZW5hbnQiOnRydWUsImNvbnRhY3RfaWQiOiI2MzU1NjYzYjU0MzRhOTdhOTJkZGNlYmQiLCJsbmciOiJ2aSIsInNvZnRfa2V5IjoiSlMzUVhYRFY0SCIsInN1YiI6Im9taWNybS5kZXYiLCJleHAiOjE2NzA1NzEzNjMsImlhdCI6MTY2ODc1Njk2M30.Aw7kEQexG52ctk6DobpJ95Wmc9poFpCwzdU0Bq1ukvRslTQUErHL4q5D3Zv6NfidhuePwM7F0jr21So1QP45LYoYECJztlwAO-irDyKAbBx8VF40Y91jLaFb2RuUeLQXamyfguoRAd0jSjNR31o5Vhx6EUPV-Gc6VolzIHxmtDrOlznwslwnlE7cMW5TsDRF_PkMF0WAY0rfAoQhpZCSFzmjv1T56WaLvupEd4HjQyCAk3V74-Yn0MA_bpnr4i3UmJfCariQ6OF7tiU89adsq1ii7dL5Li5cIVigzk3fEhI3AoIWsTskIsKrAr1BxLaRH7RRu1m4MEKvKOUhZYnWbQ");

                String commandBody = objectMapper.writeValueAsString(command);
                HttpEntity<String> request =
                        new HttpEntity<>(commandBody, headers);
                responseListPatterns =
                        restTemplate.postForObject("https://chatbot-gateway-v1-stg.omicrm.com/callbot/training/search_v2", request, ResponseListPatterns.class);

                if (responseListPatterns == null ||
                        responseListPatterns.getPayload() == null ||
                        CollectionUtils.isEmpty(responseListPatterns.getPayload().getItems())) {
                    break;
                }

                File myObj = new File("patterns_vi.txt");
                if (myObj.createNewFile()) {
                    log.info(page + "|File created: " + myObj.getName());
                } else {
                    log.info(page + "|File already exists.");
                }

                for (Item item : responseListPatterns.getPayload().getItems()) {
                    String content = item.getContent() + ".\r\n";

                    Files.write(
                            Paths.get(myObj.getName()),
                            content.getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.APPEND);
                }

                page++;
            }

            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @Autowired
    private IIntentService intentService;

    @GetMapping("/tool2")
    public ResponseEntity<Boolean> tool2() {
        try {
            IntentEntity intent = IntentEntity.builder()
                    .patterns(new ArrayList<>())
                    .build();
            File myObj = new File("patterns/critical_evaluation.txt");
            Scanner myReader = new Scanner(myObj);
            int line = 1;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                // lay intent
                if (line == 1) {
                    String intentName = data.trim();
                    String intentCode = data.trim().toLowerCase();
                    while (intentCode.contains(" ")) {
                        intentCode = intentCode.replace(" ", "_");
                    }
                    intent.setName(intentName);
                    intent.setCode(intentCode);
                    line++;
                    continue;
                }

                PatternEntity pattern = PatternEntity.builder()
                        .content(data.trim())
                        .build();
                intent.getPatterns().add(pattern);

                line++;
            }
            myReader.close();

            CommandIntentAddMany commandIntentAddMany = CommandIntentAddMany.builder()
                    .userId("637755b55c31f1122c6f6441")
                    .intents(Collections.singletonList(intent))
                    .build();
            intentService.addMany(commandIntentAddMany);

            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ResponseListPatterns {
        private Payload payload;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Payload {
        private List<Item> items;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Item {
        private String content;
    }
}
