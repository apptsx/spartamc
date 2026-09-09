package com.minecraft.core.account.context.objects.toggle;

import com.minecraft.core.account.context.objects.tag.prefix.TagPrefix;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Toggle {

    /* Member Toggle List */
    private boolean allowMessages = true, allowFriendRequests = true,
            allowPartyInvite = true, allowClanInvite = true, allowStatsView = true,
            allowClanTag = true, isConfirmToHub = false, showLevelInChat = true;

    private boolean visiblePlayersHub = true;
    private int visibilityMode = -1; // -1 = não inicializado, usar visiblePlayersHub
    private boolean showColoredBlocks = true;

    public int getVisibilityMode() {
        if (visibilityMode == -1) {
            // Padrão: ON (0)
            visibilityMode = 0;
        }
        return visibilityMode;
    }

    public void setVisibilityMode(int mode) {
        this.visibilityMode = mode;
        // Manter compatibilidade reversa
        this.visiblePlayersHub = (mode == 0);
    }

    /* VIP Toggle List */
    private boolean allowJoinMessage = true, showTitle = true, showOwnTitle = true;
    private TagPrefix tagPrefix = TagPrefix.DEFAULT;

    /* Staff Toggle List */
    private boolean allowLogs = true, allowBuild = false;

    private boolean autoVanish = false, staffChat = false, showPunitionsLeaderboard = true, showReports = true, allowACFlags = true;
}
