package com.orgzly.android.repos;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import com.jcraft.jsch.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class SSHClient {
    private SSHConfig authConfig;
    private ChannelSftp channel;
    private Session session;
    private String directory;
    private String key;
    private Boolean isTestConnectionTriggered = false;
    private Exception testException;
    public static Set<String> fileNames = new TreeSet<>();

    public SSHClient(String username, String hostname, String password, String dir) {
        authConfig = new SSHConfig(username, hostname, password);
        this.directory = dir;
    }

    public SSHClient(String username, String hostname, String key, String dir, int stream) {
        this.authConfig = new SSHConfig(username, hostname);
        this.key = key;
        this.directory = dir;
    }

    public void connectSFTP() {
        try {
            JSch ssh = new JSch();
            session = ssh.getSession(authConfig.getUsername(), authConfig.getHostname(), authConfig.getPort());
            Properties config = new Properties();

            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            if (key == null) {
                session.setPassword(authConfig.getPassword());
            } else {
                ssh.addIdentity(authConfig.getUsername(),key.getBytes(),null,"".getBytes());
                session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            }

            session.connect();
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            if (isTestConnectionTriggered) {
                testException = e;
            }
        }
    }

    public void testConnection() throws Exception {
        isTestConnectionTriggered = true;
        connectSFTP();
        disconnectSFTP();
        isTestConnectionTriggered = false;
        if (testException != null) {
            throw new Exception(testException);
        }
    }

    public void disconnectSFTP() {
        new Thread(() -> {
            try {
                channel.disconnect();
                session.disconnect();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace();
                if(isTestConnectionTriggered) {
                    testException = ex;
                }
            }
        }).start();
    }

    public void uploadFile(InputStream localFilePath, String fileName, String repoPath) {
        new Thread(() -> {
            try {
                connectSFTP();
                isDirExist(repoPath);
                channel.put(localFilePath, repoPath + fileName);
                disconnectSFTP();
            } catch (SftpException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void downloadFile(String fileName, File destination) {
        new Thread(() -> {
            try {
                connectSFTP();
                channel.get(directory + fileName, new FileOutputStream(destination)); // saving to local directory
                disconnectSFTP();
            } catch (SftpException | FileNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void removeFile(String remotePath) {
       new Thread(() -> {
           connectSFTP();
           try {
               channel.rm(remotePath);
           } catch (SftpException e) {
               e.printStackTrace();
           }
           disconnectSFTP();
       }).start();
    }

    public void renameFile(String remoteRepoPath, String oldFileName, String newFileName) {
        new Thread(() -> {
            connectSFTP();
            try {
                channel.rename(remoteRepoPath + oldFileName, remoteRepoPath + newFileName);
            } catch (SftpException e) {
                e.printStackTrace();
            }
            disconnectSFTP();
        }).start();
    }

    public void isDirExist(String dir) {
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
    }

    public void getFiles(String dir) {
       new Thread(() -> {
           fileNames.clear();
           connectSFTP();
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
           disconnectSFTP();
       }).start();
    }
}
