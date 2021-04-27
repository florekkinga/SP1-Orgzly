package com.orgzly.android.util;

import com.jcraft.jsch.*;

public class SSHClient {
    private SSHConfig authConfig;
    private ChannelSftp channel;
    private Session session;

    public SSHClient(String username, String hostname, String password) {
        authConfig = new SSHConfig(username, hostname, password);
    }

    public void connectSFTP() throws JSchException, SftpException {
        JSch ssh = new JSch();
        session = ssh.getSession(authConfig.getUsername(), authConfig.getHostname(), authConfig.getPort());
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword(authConfig.getPassword());

        session.connect();
        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
    }

    public void disconnectSFTP() throws JSchException, SftpException {
        channel.disconnect();
        session.disconnect();
    }

    public void uploadFile(String localFilePath, String fileName, String repoPath) throws JSchException, SftpException {
        connectSFTP();
        channel.put(localFilePath, repoPath + fileName);
        disconnectSFTP();
    }

    public void downloadFile(String fileName, String localRepoPath) throws JSchException, SftpException {
        connectSFTP();
        channel.get(fileName, localRepoPath + fileName);
        disconnectSFTP();
    }
}
