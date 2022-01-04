package me.spikey.spikeyenderpearlcooldown;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CF {

    public static String getCoolDownTimeInDays(UUID uuid, int id) {
        long secs = Main.getRemainingSeconds(uuid);
        if (secs < 60) return secs + " Second(s)";

        long days = (long) Math.floor(TimeUnit.SECONDS.toDays(secs));
        long hours = (long) Math.floor(TimeUnit.SECONDS.toHours(secs) - (days*24));
        long minutes = (long) Math.floor(TimeUnit.SECONDS.toMinutes(secs) - (days*24*60) - (hours*60));

        String formatted = "%s%s%s";

        String day = "";
        if (days != 0) day = days + " Day(s) ";
        String hour = "";
        if (hours != 0) hour = hours + " Hour(s) ";
        String minute = "";
        if (minutes != 0) minute = minutes + " Minute(s)";
        return formatted.formatted(day, hour, minute);

    }
}
