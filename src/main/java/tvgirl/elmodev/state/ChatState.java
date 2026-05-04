package tvgirl.elmodev.state;

public record ChatState (
        String permission,
        int cooldown,
        String command,
        String prefix,
        int radius,
        String color,
        String tag
) {}
