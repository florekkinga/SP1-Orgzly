package com.orgzly.android.util;

import com.jcraft.jsch.*;

public class SSHClient {
    private SSHConfig authConfig;

    public SSHClient(String username, String hostname, String password) {
        authConfig = new SSHConfig(username, hostname, password)
    }

    public Channel connectSFTP() throws JSchException, SftpException {
        JSch ssh = new JSch();
        Session session = ssh.getSession(authConfig.user, authConfig.host, authConfig.port);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword(authConfig.pwd);

        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();
        return channel;
    }

    public void disconnectSFTP(Channel channel, Session session) throws JSchException, SftpException {
             channel.disconnect();
             session.disconnect();
    }
}
