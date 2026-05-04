package tvgirl.elmodev.state;

import java.util.List;

public record AMessageState(
        String id,
        int cooldown,
        List<String> messagesList
){}
