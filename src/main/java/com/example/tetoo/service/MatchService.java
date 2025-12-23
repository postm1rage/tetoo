package com.example.tetoo.service;

import com.example.tetoo.model.Match;
import com.example.tetoo.model.UserProfile;
import com.example.tetoo.repository.MatchRepository;
import com.example.tetoo.service.interfaces.IMatchService;
import java.util.ArrayList;
import java.util.List;

public class MatchService implements IMatchService {
    
    private final MatchRepository matchRepository;
    private final SupabaseService supabaseService;
    
    public MatchService() {
        this.matchRepository = new MatchRepository();
        this.supabaseService = new SupabaseService();
    }
    
    @Override
    public boolean createMatch(Integer user1Id, Integer user2Id) {
        return matchRepository.createMatch(user1Id, user2Id);
    }
    
    @Override
    public List<Match> getMatchesForUser(Integer userId) {
        return matchRepository.getMatchesForUser(userId);
    }
    
    @Override
    public List<UserProfile> getMatchedProfiles(Integer currentUserId) {
        List<Match> matches = getMatchesForUser(currentUserId);
        List<UserProfile> profiles = new ArrayList<>();
        
        for (Match match : matches) {
            Integer otherUserId = match.getOtherUserId(currentUserId);
            if (otherUserId != null) {
                UserProfile profile = supabaseService.getUserProfile(otherUserId);
                if (profile != null) {
                    profiles.add(profile);
                }
            }
        }
        
        return profiles;
    }
    
    @Override
    public boolean deleteMatch(Integer user1Id, Integer user2Id) {
        return matchRepository.deleteMatch(user1Id, user2Id);
    }
    
    @Override
    public boolean matchExists(Integer user1Id, Integer user2Id) {
        return matchRepository.matchExists(user1Id, user2Id);
    }
}