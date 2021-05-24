package com.orgzly.android.repos;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.jcraft.jsch.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class SSHClient {
    private SSHConfig authConfig;
    private ChannelSftp channel;
    private Session session;
    private String directory;
    public static Set<String> fileNames = new TreeSet<>();

    public SSHClient(String username, String hostname, String password, String dir) {
        authConfig = new SSHConfig(username, hostname, password);
        this.directory = dir;
    }

    @SuppressLint("StaticFieldLeak")
    public void connectSFTP() {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... unused) {
                try {
                    JSch ssh = new JSch();
                    session = ssh.getSession(authConfig.getUsername(), authConfig.getHostname(), authConfig.getPort());
                    java.util.Properties config = new java.util.Properties();
                    config.put("StrictHostKeyChecking", "no");
                    session.setConfig(config);
                    session.setPassword(authConfig.getPassword());

                    session.connect();
                    channel = (ChannelSftp) session.openChannel("sftp");
                    channel.connect();
                    System.out.println("CONNECTED");
                    getFiles(directory);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();

                }
                return null;
            }
            protected void onPostExecute(Void unused) {}
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void disconnectSFTP()  {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... unused) {
                channel.disconnect();
                session.disconnect();
                return null;
            }
            protected void onPostExecute(Void unused) {}
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void uploadFile(InputStream localFilePath, String fileName, String repoPath) {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... unused) {
                try {
                    connectSFTP();
                    //System.out.println(localFilePath);
                    //System.out.println(repoPath + fileName);
                    channel.put(localFilePath, repoPath + fileName);
                    disconnectSFTP();
                } catch (SftpException e) {
                    e.printStackTrace();
                }
                return null;
            }
            protected void onPostExecute(Void unused) {}
        }.execute();

    }

    @SuppressLint("StaticFieldLeak")
    public void downloadFile(String fileName, OutputStream localRepoPath) {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... unused) {
                try {
                    connectSFTP();
                    channel.get(directory+fileName, localRepoPath); // saving to local directory
                    disconnectSFTP();
                } catch (SftpException e) {
                    e.printStackTrace();
                }
                return null;
            }
            protected void onPostExecute(Void unused) {}
        }.execute();
    }

    public void removeFile(String remotePath) throws SftpException {
        connectSFTP();
        channel.rm(remotePath);
        disconnectSFTP();
    }

    public void renameFile(String remoteRepoPath, String oldFileName, String newFileName) throws JSchException, SftpException {
        connectSFTP();
        channel.rename(remoteRepoPath + oldFileName, remoteRepoPath + newFileName);
        disconnectSFTP();
    }

    @SuppressLint("StaticFieldLeak")
    public void isDirExist(String dir) {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... unused) {
                try {
                    SftpATTRS attrs=null;
                    try {
                        attrs = channel.stat(dir);
                    } catch (Exception e) {
                        System.out.println(dir+" not found");
                    }

                    if (attrs != null) {
                        System.out.println("Directory exists IsDir="+attrs.isDir());
                    } else {
                        System.out.println("Creating dir "+dir);
                        channel.mkdir(dir);
                    }
                } catch (SftpException e) {
                    e.printStackTrace();
                }
                return null;
            }
            protected void onPostExecute(Void unused) {}
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void getFiles(String dir) {
       new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... unused) {
                try {
                    if(channel.isConnected()) {
                        Vector filelist = channel.ls(dir);
                        for (int i = 0; i < filelist.size(); i++) {
                            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) filelist.get(i);
                            if (entry.getFilename().endsWith(".org")) {
                                fileNames.add(entry.getFilename());
                            }
                        }
                    }
                } catch (SftpException e) {
                    e.printStackTrace();
                }
                return null;
            }
            protected void onPostExecute(List<String> unused) { }
        }.execute();
    }
}
