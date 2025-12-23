package com.example.tetoo.service;

import com.example.tetoo.model.Chat;
import com.example.tetoo.model.Message;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatService {
    private final SupabaseService supabaseService;
    private final ExecutorService executor;
    
    public ChatService(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
        this.executor = Executors.newFixedThreadPool(2);
    }
    
    public CompletableFuture<List<Message>> getChatHistoryAsync(Integer user1Id, Integer user2Id) {
        return CompletableFuture.supplyAsync(() -> {
            return supabaseService.getChatHistory(user1Id, user2Id);
        }, executor);
    }
    
    public CompletableFuture<List<Chat>> getUserChatsAsync(Integer userId) {
        return CompletableFuture.supplyAsync(() -> {
            return supabaseService.getUserChats(userId);
        }, executor);
    }
    
    public CompletableFuture<Boolean> sendMessageAsync(Integer senderId, Integer receiverId, String text) {
        return CompletableFuture.supplyAsync(() -> {
            return supabaseService.sendMessage(senderId, receiverId, text);
        }, executor);
    }
    
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}