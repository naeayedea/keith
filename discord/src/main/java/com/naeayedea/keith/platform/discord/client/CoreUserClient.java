package com.naeayedea.keith.platform.discord.client;

import com.naeayedea.keith.common.model.user.BasicKeithUser;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * Resolves Discord users against core over HTTP, replacing the old in-process
 * {@code KeithUserManager} dependency.
 *
 * <p>This always resolves-or-creates rather than offering a strict "must already exist" lookup -
 * core's {@code /api/v1/entity/user} endpoint is the only user endpoint that exists so far, and
 * unlike the old in-process manager, always provisioning on first contact is the correct behavior
 * anyway (a brand new Discord user's first message should work, not throw).
 *
 * @author naeayedea
 */
@Component
public class CoreUserClient {

    private static final String USER_PATH = "/api/v1/entity/user";

    /**
     * The underlying client this resolves users through.
     */
    private final CoreApiClient coreApiClient;

    public CoreUserClient(CoreApiClient coreApiClient) {
        this.coreApiClient = coreApiClient;
    }

    /**
     * @param platformUserId the Discord id of the user to resolve
     * @return the resolved (or newly created) user
     */
    @NonNull
    public BasicKeithUser getOrCreateUser(@NonNull String platformUserId) {
        return coreApiClient.getAsUser(USER_PATH, platformUserId, BasicKeithUser.class);
    }
}
