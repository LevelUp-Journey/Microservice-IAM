package com.levelupjourney.microserviceiam.profiles.application.acl;

import com.levelupjourney.microserviceiam.profiles.domain.model.commands.CreateProfileCommand;
import com.levelupjourney.microserviceiam.profiles.domain.model.queries.GetProfileByEmailQuery;
import com.levelupjourney.microserviceiam.profiles.domain.model.valueobjects.EmailAddress;
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
            String email,
            String street,
            String number,
            String city,
            String state,
            String postalCode,
            String country) {
        var createProfileCommand = new CreateProfileCommand(
                firstName,
                lastName,
                email,
                street,
                number,
                city,
                state,
                postalCode,
                country);
        var profile = profileCommandService.handle(createProfileCommand);
        return profile.isEmpty() ? null : profile.get().getId();
    }

    public UUID fetchProfileIdByEmail(String email) {
        var getProfileByEmailQuery = new GetProfileByEmailQuery(new EmailAddress(email));
        var profile = profileQueryService.handle(getProfileByEmailQuery);
        return profile.isEmpty() ? null : profile.get().getId();
    }


}
