package io.github.sakurawald.module.common.manager.interfaces;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public abstract class AbstractBackupManager {

    protected Path BACKUP_PATH;

    public AbstractBackupManager(Path BACKUP_PATH) {
        this.BACKUP_PATH = BACKUP_PATH;
    }

    protected abstract @NotNull List<File> getInputFiles();

    protected abstract @NotNull File getOutputFile();

    protected abstract void makeBackup();

    public abstract void backup();
}
