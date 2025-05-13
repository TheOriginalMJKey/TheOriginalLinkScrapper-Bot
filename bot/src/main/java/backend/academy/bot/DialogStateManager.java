package backend.academy.bot;

import java.util.HashMap;
import java.util.Map;

public class DialogStateManager {

    private final Map<Long, DialogState> states = new HashMap<>();

    public void setState(long chatId, DialogState state) {
        states.put(chatId, state);
    }

    public DialogState getState(long chatId) {
        return states.getOrDefault(chatId, DialogState.NONE);
    }

    public enum DialogState {
        NONE, WAITING_LINK, WAITING_TAGS, WAITING_FILTERS, WAITING_UNTRACK_LINK
    }
}
