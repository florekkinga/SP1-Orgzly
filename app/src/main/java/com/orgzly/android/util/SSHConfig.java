package com.orgzly.android.util;

protected class SSHConfig {
    protected String user = "";
    protected String host = "";
    protected String pwd = "";
    protected Int port = 22;

    public SSHConfig(String username, String hostname, String password) {
        user = username;
        host = hostname;
        pwd = password;
    }

    public getUsername() {
        return user;
    }
    public setUsername(String newUsername) {
        user = newUsername;
    }
    public getHostname() {
        return host;
    }
    public setHostname(String newHostname) {
        host = newHostname;
    }
    public getPassword() {
        return pwd;
    }
    public setPassword(String newPassword) {
        pwd = newPassword;
    }
    public getPort() {
        return port;
    }
    public setPort(Int newPort) {
        port = newPort;
    }
}
