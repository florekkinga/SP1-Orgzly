package com.orgzly.android.repos;

public class SSHConfig {
    private String user = "";
    private String host = "";
    private String pwd = "";
    private int port = 22;

    public SSHConfig(String username, String hostname, String password) {
        user = username;
        host = hostname;
        pwd = password;
    }

    public SSHConfig(String username, String hostname) {
        this.user = username;
        this.host = hostname;
    }

    public String getUsername() {
        return this.user;
    }
    public void setUsername(String newUsername) {
        this.user = newUsername;
    }
    public String getHostname() {
        return this.host;
    }
    public void setHostname(String newHostname) {
        this.host = newHostname;
    }
    public String getPassword() {
        return this.pwd;
    }
    public void setPassword(String newPassword) {
        this.pwd = newPassword;
    }
    public int getPort() {
        return this.port;
    }
    public void setPort(int newPort) {
        this.port = newPort;
    }
}
