package com.minecraft.core.account.context.objects.joinmessage;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.joinmessage.JoinMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JoinMessageMetadata {

    private String selectedMessage = null;
    private long updatedAt = System.currentTimeMillis();

    public JoinMessageMetadata() {
    }

    public void setSelectedMessage(JoinMessage message) {
        if (message == null) {
            this.selectedMessage = null;
        } else {
            this.selectedMessage = message.getId();
        }
        updatedAt = System.currentTimeMillis();
    }

    public JoinMessage getSelectedJoinMessage() {
        if (selectedMessage == null) return null;
        return Core.getJoinMessageController().of(selectedMessage);
    }

    public String getMessage(RankType rank) {
        JoinMessage selected = getSelectedJoinMessage();
        
        if (selected != null && selected.hasAccess(rank)) {
            return selected.getMessage();
        }
        
        return "entrou no lobby!";
    }

    public List<JoinMessage> getAvailableMessages(RankType rank) {
        return Core.getJoinMessageController().list(rank).stream()
                .filter(msg -> {
                    if (msg.isExactRank() && msg.getRequiredRank() != rank) {
                        return false;
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
    }
    
    public List<JoinMessage> getAllMessages() {
        return Core.getJoinMessageController().filter(m -> true);
    }
}
