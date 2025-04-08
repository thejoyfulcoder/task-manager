package com.omnik.projects.task_manager.entities.history;

import com.omnik.projects.task_manager.storage.DataStore;

public abstract class Operations {

    public final DataStore dataStore;

    protected Operations(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public abstract void apply();

    public abstract void revert();
}
