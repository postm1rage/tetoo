package com.example.tetoo.service;

import com.example.tetoo.model.Like;
import com.example.tetoo.model.User;
import com.example.tetoo.model.UserProfile;
import com.example.tetoo.repository.LikeRepository;
import com.example.tetoo.service.interfaces.ILikeService;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LikeService implements ILikeService {
    
    private final LikeRepository likeRepository;
    private final SupabaseService supabaseService;
    private final Random random;
    
    public LikeService() {
        this.likeRepository = new LikeRepository();
        this.supabaseService = new SupabaseService();
        this.random = new Random();
    }
    
    @Override
    public boolean sendLike(Integer fromUserId, Integer toUserId, String type) {
        // Проверяем, чтобы пользователь не лайкал сам себя
        if (fromUserId.equals(toUserId)) {
            System.out.println("User cannot like themselves");
            return false;
        }
        
        // Создаем объект лайка
        Like like = new Like(fromUserId, toUserId, type);
        
        // Сохраняем в базу
        boolean success = likeRepository.sendLike(like);
        
        if (success && "like".equals(type)) {
            // Проверяем на взаимный лайк (матч)
            boolean isMutualLike = checkMutualLike(fromUserId, toUserId);
            
            if (isMutualLike) {
                System.out.println("Mutual like detected! Creating match between " + fromUserId + " and " + toUserId);
                
                // Отправляем приветственное сообщение от обоих пользователей
                supabaseService.sendMessage(fromUserId, toUserId, "This user liked you!");
                supabaseService.sendMessage(toUserId, fromUserId, "This user liked you!");
                
                // Можно также создать запись в таблице matches, если нужно
                // createMatch(fromUserId, toUserId);
            }
        }
        
        return success;
    }
    
    @Override
    public boolean checkMutualLike(Integer user1Id, Integer user2Id) {
        // Проверяем, лайкал ли user2 user1 ранее
        return likeRepository.hasLikeFromTo(user2Id, user1Id);
    }
    
    @Override
    public List<Like> getLikesReceived(Integer userId) {
        return likeRepository.getLikesReceived(userId);
    }
    
    @Override
    public UserProfile getRandomUserForLikes(Integer currentUserId) {
        // Получаем всех пользователей из базы
        List<User> allUsers = supabaseService.getAllUsers();
        
        if (allUsers.isEmpty()) {
            System.out.println("No users found in database");
            return null;
        }
        
        // Фильтруем: убираем текущего пользователя и тех, кого уже лайкали
        List<User> availableUsers = new ArrayList<>();
        for (User user : allUsers) {
            if (!user.getId().equals(currentUserId)) {
                // Проверяем, не лайкал ли уже текущий пользователь этого пользователя
                boolean alreadyLiked = likeRepository.hasLikeFromTo(currentUserId, user.getId());
                if (!alreadyLiked) {
                    availableUsers.add(user);
                }
            }
        }
        
        if (availableUsers.isEmpty()) {
            System.out.println("No available users to show");
            return null;
        }
        
        // Выбираем случайного пользователя
        User randomUser = availableUsers.get(random.nextInt(availableUsers.size()));
        
        // Получаем профиль этого пользователя
        return supabaseService.getUserProfile(randomUser.getId());
    }
    
    @Override
    public List<UserProfile> getUsersWhoLikedMe(Integer currentUserId) {
        List<Like> likesReceived = getLikesReceived(currentUserId);
        List<UserProfile> profiles = new ArrayList<>();
        
        for (Like like : likesReceived) {
            if ("like".equals(like.getType())) {
                UserProfile profile = supabaseService.getUserProfile(like.getFromUserId());
                if (profile != null) {
                    profiles.add(profile);
                }
            }
        }
        
        return profiles;
    }
    
    @Override
    public boolean removeLike(Integer fromUserId, Integer toUserId) {
        return likeRepository.removeLike(fromUserId, toUserId);
    }
}