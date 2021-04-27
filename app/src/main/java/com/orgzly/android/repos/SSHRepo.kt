package com.orgzly.android.repos

import android.net.Uri
import java.io.File

class SSHRepo(
        private val repoId: Long,
        private val uri: Uri,
        val username: String?,
        val password: String?,
        val hostname: String,
        val directory: String,
        val key: String? = null
) : SyncRepo {

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
        val url = uri.toUrl()
        return null
    }


    override fun retrieveBook(fileName: String, destination: File): VersionedRook? {
        return null
    }

    override fun storeBook(file: File, fileName: String): VersionedRook? {
        return null
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

    // DLA CELOW TESTOWYCH, Do usuniecia
    fun callSSHTest() {
        val client = SSHClient(username, hostname, password)
        client.connectSFTP()
    }

    // REGEX DO POPRAWY
    private fun Uri.toUrl(): String {
        return this.toString().replace("^(?:ssh://)".toRegex(), "http$1")
    }
}