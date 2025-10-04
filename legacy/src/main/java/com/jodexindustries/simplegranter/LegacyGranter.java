package com.jodexindustries.simplegranter;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;

public class LegacyGranter extends SimpleGranter {

    @Override
    public void onEnable() {
        loadLibraries();

        super.onEnable();
    }

    private void loadLibraries() {
        BukkitLibraryManager libManager = new BukkitLibraryManager(this);
        libManager.addMavenCentral();

        Library ormlite = Library.builder()
                .groupId("com{}j256{}ormlite")
                .artifactId("ormlite-jdbc")
                .version("6.1")
                .id("ormlite")
                .build();

        libManager.loadLibrary(ormlite);
    }
}
