package com.example.tetoo.service.interfaces;

import com.example.tetoo.model.Match;
import com.example.tetoo.model.UserProfile;
import java.util.List;

public interface IMatchService {
    
    // Создать матч
    boolean createMatch(Integer user1Id, Integer user2Id);
    
    // Получить все активные матчи для пользователя
    List<Match> getMatchesForUser(Integer userId);
    
    // Получить профили пользователей, с которыми есть матчи
    List<UserProfile> getMatchedProfiles(Integer currentUserId);
    
    // Удалить матч
    boolean deleteMatch(Integer user1Id, Integer user2Id);
    
    // Проверить, есть ли матч между двумя пользователями
    boolean matchExists(Integer user1Id, Integer user2Id);
}