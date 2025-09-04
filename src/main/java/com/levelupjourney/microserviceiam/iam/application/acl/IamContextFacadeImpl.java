package com.levelupjourney.microserviceiam.iam.application.acl;

import com.levelupjourney.microserviceiam.iam.domain.services.UserCommandService;
import com.levelupjourney.microserviceiam.iam.domain.services.UserQueryService;
import com.levelupjourney.microserviceiam.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;

@Service
public class IamContextFacadeImpl extends IamContextFacade {
    
    public IamContextFacadeImpl(UserCommandService userCommandService, UserQueryService userQueryService) {
        super(userCommandService, userQueryService);
    }
}