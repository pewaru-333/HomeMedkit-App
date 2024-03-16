package ru.application.homemedkit.databaseController;

import androidx.room.Ignore;

public class Technical {
    public boolean scanned = false;
    public boolean verified = false;

    public Technical() {
    }

    @Ignore
    public Technical(boolean scanned, boolean verified) {
        this.scanned = scanned;
        this.verified = verified;
    }
}
