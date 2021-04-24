package com.orgzly.android.ui.repo.ssh

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.orgzly.R
import com.orgzly.android.App
import com.orgzly.android.data.DataRepository
import com.orgzly.android.repos.SSHRepo
import com.orgzly.android.ui.repo.RepoViewModel

class SSHRepoViewModel(
        dataRepository: DataRepository,
        override var repoId: Long
) : RepoViewModel(dataRepository, repoId){

    sealed class ConnectionResult {
        data class InProgress(val msg: Int): ConnectionResult()
        data class Success(val msg: String): ConnectionResult()
        data class Error(val msg: Any): ConnectionResult()
    }

    private val connectionTestStatus: MutableLiveData<SSHRepoViewModel.ConnectionResult> = MutableLiveData()

    fun testConnection(uriString: String, username: String, password: String) {
        App.EXECUTORS.networkIO().execute {
            try {
                connectionTestStatus.postValue(SSHRepoViewModel.ConnectionResult.InProgress(R.string.connecting))

                // SSHRepo.testConnection(uriString, username, password)

                connectionTestStatus.postValue(SSHRepoViewModel.ConnectionResult.Success("Connected!"))

            } catch (e: Exception) {
                e.printStackTrace()
                connectionTestStatus.postValue(SSHRepoViewModel.ConnectionResult.Error("Error: $e"))
            }
        }
    }
}