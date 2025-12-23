package com.example.tetoo.service;

import java.util.concurrent.*;

public class ChatService {
    private final SupabaseService supabaseService;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    
    public CompletableFuture<List<Chat>> getUserChatsAsync(Integer userId) {
        return CompletableFuture.supplyAsync(() -> {
            return supabaseService.getUserChats(userId);
        }, executor);
    }
    
    public CompletableFuture<List<Message>> getChatHistoryAsync(Integer user1Id, Integer user2Id) {
        return CompletableFuture.supplyAsync(() -> {
            return supabaseService.getChatHistory(user1Id, user2Id);
        }, executor);
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}