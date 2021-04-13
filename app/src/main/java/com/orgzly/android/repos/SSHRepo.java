package com.orgzly.android.repos;

import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SSHRepo implements SyncRepo{

    // implementacja podobna do WebdavRepo
    @Override
    public boolean isConnectionRequired() {
        return false;
    }

    @Override
    public boolean isAutoSyncSupported() {
        return false;
    }

    @Override
    public Uri getUri() {
        return null;
    }

    @Override
    public List<VersionedRook> getBooks() throws IOException {
        return null;
    }

    @Override
    public VersionedRook retrieveBook(String fileName, File destination) throws IOException {
        return null;
    }

    @Override
    public VersionedRook storeBook(File file, String fileName) throws IOException {
        return null;
    }

    @Override
    public VersionedRook renameBook(Uri from, String name) throws IOException {
        return null;
    }

    @Override
    public void delete(Uri uri) throws IOException {

    }
}
