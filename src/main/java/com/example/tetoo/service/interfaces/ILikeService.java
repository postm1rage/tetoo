package com.example.tetoo.service.interfaces;

import com.example.tetoo.model.Like;
import com.example.tetoo.model.UserProfile;
import java.util.List;

public interface ILikeService {
    
    // Отправить лайк/дизлайк
    boolean sendLike(Integer fromUserId, Integer toUserId, String type);
    
    // Проверить, есть ли взаимный лайк (матч)
    boolean checkMutualLike(Integer user1Id, Integer user2Id);
    
    // Получить список лайков, полученных пользователем
    List<Like> getLikesReceived(Integer userId);
    
    // Получить случайного пользователя для показа в разделе лайков
    UserProfile getRandomUserForLikes(Integer currentUserId);
    
    // Получить профили пользователей, которые лайкнули текущего пользователя
    List<UserProfile> getUsersWhoLikedMe(Integer currentUserId);
    
    // Удалить лайк (при дизлайке из Matches)
    boolean removeLike(Integer fromUserId, Integer toUserId);
}