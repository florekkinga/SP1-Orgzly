package com.orgzly.android.repos;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.jcraft.jsch.*;

public class SSHClient {
    private SSHConfig authConfig;
    private ChannelSftp channel;
    private Session session;

    public SSHClient(String username, String hostname, String password) {
        authConfig = new SSHConfig(username, hostname, password);
    }

    @SuppressLint("StaticFieldLeak")
    public void connectSFTP() throws JSchException, SftpException {
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
    public void disconnectSFTP() throws JSchException, SftpException {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... unused) {
                channel.disconnect();
                session.disconnect();
                return null;
            }
            protected void onPostExecute(Void unused) {}
        }.execute();
    }

    public void uploadFile(String localFilePath, String fileName, String repoPath) throws JSchException, SftpException {
        connectSFTP();
        channel.put(localFilePath, repoPath + fileName);
        disconnectSFTP();
    }

    public void downloadFile(String fileName, String localRepoPath) throws JSchException, SftpException {
        connectSFTP();
        channel.get(fileName, localRepoPath + fileName); // saving to local directory
        disconnectSFTP();
    }

    public void removeFile(String remotePath) throws JSchException, SftpException {
        connectSFTP();
        channel.rm(remotePath);
        disconnectSFTP();
    }

    public void renameFile(String remoteRepoPath, String oldFileName, String newFileName) throws JSchException, SftpException {
        connectSFTP();
        channel.rename(remoteRepoPath + oldFileName, remoteRepoPath + newFileName);
        disconnectSFTP();
    }
}
