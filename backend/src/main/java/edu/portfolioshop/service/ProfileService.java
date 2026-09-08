package edu.portfolioshop.service;

import edu.portfolioshop.dto.ProfileRequest;
import edu.portfolioshop.entities.UserProfile;
import edu.portfolioshop.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserProfileRepository profiles;

    public UserProfile find(String userId) {
        return profiles.findByUserId(userId).orElseGet(() -> {
            UserProfile blank = new UserProfile();
            blank.setUserId(userId);
            return blank;
        });
    }

    public UserProfile update(String userId, ProfileRequest request) {
        UserProfile profile = profiles.findByUserId(userId).orElseGet(() -> {
            UserProfile fresh = new UserProfile();
            fresh.setUserId(userId);
            return fresh;
        });
        profile.setFullName(request.fullName());
        profile.setPhone(request.phone());
        return profiles.save(profile);
    }
}
