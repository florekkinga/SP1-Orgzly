package com.orgzly.android.repos

import android.net.Uri
import org.joda.time.DateTimeUtils
import org.joda.time.LocalDateTime
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.collections.ArrayList


class SSHRepo(
        private val repoId: Long,
        private val uri: Uri,
        val username: String?,
        val password: String?,
        val hostname: String,
        val directory: String,
        val key: String? = null,
) : SyncRepo {

    private val sshClient = SSHClient(username, hostname, password, directory).apply {
        connectSFTP()
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
        var result: MutableList<VersionedRook> = ArrayList()

        if (SSHClient.fileNames.isNotEmpty()) {
            val currentDateTime = LocalDateTime.now().toString()
            SSHClient.fileNames.forEach { str ->
//                result.add(VersionedRook(
//                        repoId, RepoType.SSH,uri,Uri.parse(uri.toString()+str),str+currentDateTime,DateTimeUtils.currentTimeMillis()
//                ))
                sshClient.downloadFile(str, "/data/user/0/com.orgzly/files/")
                val file = File("/data/user/0/com.orgzly/files/"+str)
                println(str)
                if (file.exists()) {
                    val uriFile = Uri.parse(uri.toString()+str)
                    result.add(VersionedRook(
                        repoId, RepoType.SSH,uriFile,uriFile,
                            "1",
                            111))
                }
            }
        }
        return result
    }


    override fun retrieveBook(fileName: String, destination: File): VersionedRook? {
        val fileUrl = Uri.parse(uri.toString()+fileName)
        println(destination.path)
        sshClient.downloadFile(fileName, destination.path+"/"+fileName)
        return VersionedRook(repoId, RepoType.SSH,
                            fileUrl, fileUrl, "1", 111)
    }

    override fun storeBook(file: File, fileName: String): VersionedRook? {
        val file2 = File("/data/user/0/com.orgzly/files/$fileName")
        copy(file, file2)
        val uriFile = Uri.parse(uri.toString()+fileName)
        sshClient.uploadFile(file2.path, fileName, directory)
        val currentDateTime = LocalDateTime.now().toString()
        return VersionedRook(repoId, RepoType.SSH, uriFile, uriFile, "1", 111)
    }

    @Throws(IOException::class)
    fun copy(src: File?, dst: File?) {
        FileInputStream(src).use { `in` ->
            FileOutputStream(dst).use { out ->
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
        }
    }

    override fun renameBook(from: Uri, name: String): VersionedRook? {
        return null
    }


    override fun delete(uri: Uri) {
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

        fun testConnection(username: String?, password: String?, hostname: String?, directory: String?) {}
    }
}