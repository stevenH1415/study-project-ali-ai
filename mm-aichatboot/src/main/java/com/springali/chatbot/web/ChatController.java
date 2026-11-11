package com.springali.chatbot.web;

import com.springali.chatbot.service.ChatMemoryStore;
import com.springali.chatbot.web.dto.ChatReply;
import com.springali.chatbot.web.dto.ChatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatModel chatModel;
    private final ChatMemoryStore memoryStore;

    @Value("${app.chat.system-prompt:你是一个面向 Java 后端的 AI 学习助手。}")
    private String systemPrompt;

    @Value("${app.chat.max-history:20}")
    private int maxHistory;

    public ChatController(ChatModel chatModel, ChatMemoryStore memoryStore) {
        this.chatModel = chatModel;
        this.memoryStore = memoryStore;
    }

    /**
     * 普通对话（一次性返回完整回答）。
     */
    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "message 不能为空"));
        }
        String conversationId = normalize(request.conversationId());
        try {
            ChatResponse response = chatModel.call(buildPrompt(conversationId, request.message()));
            String reply = response.getResult().getOutput().getText();
            memoryStore.append(conversationId, request.message(), reply, maxHistory);
            return ResponseEntity.ok(new ChatReply(conversationId, reply));
        } catch (Exception e) {
            log.error("调用模型失败", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "调用模型失败：" + rootMessage(e)));
        }
    }

    /**
     * 流式对话（SSE）。
     * 事件顺序：event=conversation（返回会话 id） -> 若干 data 增量 -> data=[DONE]
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@RequestParam("message") String message,
                                                @RequestParam(value = "conversationId", required = false) String conversationId) {
        String cid = normalize(conversationId);
        StringBuilder replyBuffer = new StringBuilder();

        Flux<ServerSentEvent<String>> body = chatModel.stream(buildPrompt(cid, message))
                .map(response -> {
                    if (response == null || response.getResult() == null
                            || response.getResult().getOutput() == null) {
                        return "";
                    }
                    String text = response.getResult().getOutput().getText();
                    return text == null ? "" : text;
                })
                .filter(text -> !text.isEmpty())
                .doOnNext(replyBuffer::append)
                .map(text -> ServerSentEvent.<String>builder(text).build())
                .concatWith(Mono.just(ServerSentEvent.<String>builder("[DONE]").build()));

        return Flux.concat(
                        Mono.just(ServerSentEvent.<String>builder(cid).event("conversation").build()),
                        body)
                .doOnComplete(() -> memoryStore.append(cid, message, replyBuffer.toString(), maxHistory))
                .onErrorResume(error -> {
                    log.error("流式调用模型失败", error);
                    return Flux.just(ServerSentEvent.<String>builder(
                            "[ERROR] " + rootMessage(error)).build());
                });
    }

    /**
     * 清空某个会话的内存记忆。
     */
    @DeleteMapping("/{conversationId}")
    public Map<String, Object> clear(@PathVariable String conversationId) {
        memoryStore.clear(conversationId);
        return Map.of("code", 0, "msg", "ok");
    }

    private Prompt buildPrompt(String conversationId, String message) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.addAll(memoryStore.history(conversationId));
        messages.add(new UserMessage(message));
        return new Prompt(messages);
    }

    private String normalize(String conversationId) {
        return (conversationId == null || conversationId.isBlank())
                ? UUID.randomUUID().toString()
                : conversationId;
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }
}
