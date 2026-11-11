package com.springali.chatbot.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的内存会话记忆：conversationId -> 最近 N 条消息。
 * 学习阶段够用；后续可替换成 Redis / PostgreSQL 持久化。
 */
@Component
public class ChatMemoryStore {

    private final Map<String, Deque<Message>> store = new ConcurrentHashMap<>();

    public List<Message> history(String conversationId) {
        Deque<Message> deque = store.get(conversationId);
        if (deque == null) {
            return List.of();
        }
        synchronized (deque) {
            return new ArrayList<>(deque);
        }
    }

    public void append(String conversationId, String userText, String assistantText, int maxMessages) {
        Deque<Message> deque = store.computeIfAbsent(conversationId, key -> new ArrayDeque<>());
        int limit = Math.max(2, maxMessages);
        synchronized (deque) {
            deque.addLast(new UserMessage(userText));
            deque.addLast(new AssistantMessage(assistantText));
            while (deque.size() > limit) {
                deque.removeFirst();
            }
        }
    }

    public void clear(String conversationId) {
        store.remove(conversationId);
    }
}
