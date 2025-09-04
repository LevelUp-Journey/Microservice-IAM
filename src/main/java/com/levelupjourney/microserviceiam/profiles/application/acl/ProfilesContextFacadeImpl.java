package com.levelupjourney.microserviceiam.profiles.application.acl;

import com.levelupjourney.microserviceiam.profiles.domain.model.commands.CreateProfileCommand;
import com.levelupjourney.microserviceiam.profiles.domain.model.queries.GetProfileByUsernameQuery;
import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.Username;
import com.levelupjourney.microserviceiam.profiles.domain.services.ProfileCommandService;
import com.levelupjourney.microserviceiam.profiles.domain.services.ProfileQueryService;
import com.levelupjourney.microserviceiam.profiles.interfaces.acl.ProfilesContextFacade;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProfilesContextFacadeImpl implements ProfilesContextFacade {
    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;

    public ProfilesContextFacadeImpl(ProfileCommandService profileCommandService, ProfileQueryService profileQueryService) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;
    }

    public UUID createProfile(
            String firstName,
            String lastName,
            String username,
            String profileUrl) {
        var createProfileCommand = new CreateProfileCommand(
                firstName,
                lastName,
                username,
                profileUrl);
        var profile = profileCommandService.handle(createProfileCommand);
        return profile.isEmpty() ? null : profile.get().getId();
    }

    public UUID fetchProfileIdByUsername(String username) {
        var getProfileByUsernameQuery = new GetProfileByUsernameQuery(new Username(username));
        var profile = profileQueryService.handle(getProfileByUsernameQuery);
        return profile.isEmpty() ? null : profile.get().getId();
    }


}
