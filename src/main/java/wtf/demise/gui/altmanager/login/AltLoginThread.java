package wtf.demise.gui.altmanager.login;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import lombok.NonNull;
import net.minecraft.util.Session;
import org.jetbrains.annotations.Nullable;
import wtf.demise.gui.altmanager.repository.credential.AltCredential;

import java.net.Proxy;

public final class AltLoginThread {

    @NonNull
    public final AltCredential credentials;
    @Nullable
    public AltLoginListener handler;
    @Nullable
    private String caller;

    public AltLoginThread(@NonNull AltCredential credential, @NonNull AltLoginListener handler) {
        this.credentials = credential;
        this.handler = handler;
    }

    public AltLoginThread(@NonNull AltCredential credential, @Nullable String caller) {
        this.credentials = credential;
        this.caller = caller;
    }

    /* methods */
    @Nullable
    public Session run() {
        final String password = this.credentials.getPassword();

        if (password == null) {
            final Session crackedSession = new Session(this.credentials.getLogin(), "", "", "mojang");

            if (this.handler != null) this.handler.onLoginSuccess(AltType.CRACKED, crackedSession);
            return crackedSession;
        }

        final Session session = createSession(this.credentials.getLogin(), password);

        if (session == null) {
            if (this.handler != null) this.handler.onLoginFailed();
            return null;
        }

        if (this.handler != null) this.handler.onLoginSuccess(AltType.PREMIUM, session);
        return session;
    }

    @Nullable
    public static Session createSession(String username, String password) {
        final YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
        final YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) service
                .createUserAuthentication(Agent.MINECRAFT);

        auth.setUsername(username);
        auth.setPassword(password);

        try {
            auth.logIn();
            final GameProfile selectedProfile = auth.getSelectedProfile();
            return new Session(selectedProfile.getName(), selectedProfile.getId().toString(),
                    auth.getAuthenticatedToken(), "mojang");
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return null;
        }
    }

}