package com.orgzly.android.repos

import android.net.Uri
import android.os.SystemClock
import com.orgzly.android.util.UriUtils
import java.io.*


class SSHRepo(
        private val repoId: Long,
        private val uri: Uri,
        val username: String?,
        val password: String?,
        val hostname: String,
        val directory: String,
        val key: String? = null,
) : SyncRepo {

    private val sshClient = client(key)

    private fun client(key: String?): SSHClient {
        return if (key.isNullOrEmpty()) {
            SSHClient(username, hostname, password, directory)
        } else {
            SSHClient(username, hostname, key, directory, 22)
        }
    }

    override fun isConnectionRequired(): Boolean {
        return true
    }

    override fun isAutoSyncSupported(): Boolean {
        return false
    }

    override fun getUri(): Uri {
        return uri
    }

    override fun getBooks(): MutableList<VersionedRook>? {
        val result: MutableList<VersionedRook> = ArrayList()
        sshClient.getFiles(directory)
        SystemClock.sleep(3000)
        for (fileName in SSHClient.fileNames) {
            val rook = VersionedRook(repoId,
                                    RepoType.SSH,
                                    uri,
                                    Uri.parse(uri.toString()+fileName),
                                    fileName+System.currentTimeMillis(),
                                    System.currentTimeMillis())
            result.add(rook)
        }
        return result
    }

    override fun retrieveBook(fileName: String, destination: File): VersionedRook? {
        sshClient.downloadFile(fileName, destination).run {
            SystemClock.sleep(5000)
        }
        return VersionedRook(repoId, RepoType.SSH, Uri.fromFile(destination),
                Uri.parse(uri.toString()+fileName),fileName + System.currentTimeMillis(),System.currentTimeMillis())
    }

    override fun storeBook(file: File, fileName: String): VersionedRook? {
        val src: InputStream = FileInputStream(file)
        sshClient.uploadFile(src, fileName, directory)
        return VersionedRook(repoId, RepoType.SSH, uri, Uri.parse(uri.toString()+fileName) ,
                fileName + System.currentTimeMillis(),System.currentTimeMillis())
    }

    override fun renameBook(from: Uri, name: String): VersionedRook? {
        val last = this.uri.toString().length
        val oldFileName = from.toString().substring(last)
        val newFileName = UriUtils.getUriForNewName(from, name).toString().substring(last)
        sshClient.renameFile(directory,oldFileName, newFileName)
        return VersionedRook(repoId, RepoType.SSH,
                uri,UriUtils.getUriForNewName(from, name),name+System.currentTimeMillis(),System.currentTimeMillis())
    }


    override fun delete(uri: Uri) {
        val last = this.uri.toString().length
        val fileName = uri.toString().substring(last)
        sshClient.removeFile(directory+fileName)
    }

    companion object {
        const val USERNAME_PREF_KEY = "username"
        const val PASSWORD_PREF_KEY = "password"
        const val SSH_KEY_PREF_KEY = "key"
        const val HOSTNAME_PREF_KEY = "hostname"
        const val DIRECTORY_PREF_KEY = "directory"

        fun getInstance(repoWithProps: RepoWithProps): SSHRepo {
            val id = repoWithProps.repo.id

            val uri = Uri.parse(repoWithProps.repo.url)

            var key = repoWithProps.props[SSH_KEY_PREF_KEY]

            var username = repoWithProps.props[USERNAME_PREF_KEY]

            var password = repoWithProps.props[PASSWORD_PREF_KEY]

            val hostname = checkNotNull(repoWithProps.props[HOSTNAME_PREF_KEY]) {
                "Hostname not found"
            }.toString()

            val directory = checkNotNull(repoWithProps.props[DIRECTORY_PREF_KEY]) {
                "Directory not found"
            }.toString()

            if (password == null && username == null) {
                key = checkNotNull(repoWithProps.props[SSH_KEY_PREF_KEY]) {
                    "Key not found"
                }.toString()
            } else if (key == null){
                password = checkNotNull(repoWithProps.props[PASSWORD_PREF_KEY]) {
                    "Password not found"
                }.toString()

                username = checkNotNull(repoWithProps.props[USERNAME_PREF_KEY]) {
                    "Username not found"
                }.toString()
            }

            return SSHRepo(id, uri, username, password, hostname, directory, key)
        }

        fun testConnection(username: String?, password: String?, hostname: String?, directory: String?, sshKey: String?) {
            val testClient: SSHClient = if (sshKey.isNullOrEmpty()) {
                SSHClient(username, hostname, password, directory)
            } else {
                SSHClient(username, hostname, sshKey, directory, 22)
            }
            testClient.testConnection()
        }
    }
}