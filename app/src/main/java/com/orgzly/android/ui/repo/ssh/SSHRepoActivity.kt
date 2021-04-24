package com.orgzly.android.ui.repo.ssh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.orgzly.R
import com.orgzly.android.App
import com.orgzly.android.repos.RepoFactory
import com.orgzly.android.repos.SSHRepo
import com.orgzly.android.repos.WebdavRepo
import com.orgzly.android.ui.CommonActivity
import com.orgzly.android.ui.repo.RepoViewModel
import com.orgzly.android.ui.repo.directory.DirectoryRepoActivity
import com.orgzly.android.ui.repo.webdav.WebdavRepoViewModel
import com.orgzly.android.ui.util.ActivityUtils
import com.orgzly.databinding.ActivityRepoSshBinding
import javax.inject.Inject

class SSHRepoActivity : CommonActivity() {
    private lateinit var binding: ActivityRepoSshBinding

    @Inject
    lateinit var repoFactory: RepoFactory

    private lateinit var viewModel: SSHRepoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        App.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setupActionBar(R.string.ssh)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_repo_ssh)
        binding.activityRepoSshTestButton.setOnClickListener { testConnection() }

        val repoId = intent.getLongExtra(ARG_REPO_ID, 0)
        val factory = SSHRepoViewModelFactory.getInstance(dataRepository, repoId)
        viewModel = ViewModelProviders.of(this, factory).get(SSHRepoViewModel::class.java)

        viewModel.finishEvent.observeSingle(this, Observer {
            finish()
        })

        viewModel.alreadyExistsEvent.observeSingle(this, Observer {
            showSnackbar(R.string.repository_url_already_exists)
        })

        viewModel.errorEvent.observeSingle(this, Observer { error ->
            if (error != null) {
                showSnackbar((error.cause ?: error).localizedMessage)
            }
        })

        viewModel.connectionTestStatus.observe(this, Observer {
            binding.activityRepoSshTestResult.text =
                    when (it) {
                        is SSHRepoViewModel.ConnectionResult.InProgress -> {
                            binding.activityRepoSshTestButton.isEnabled = false

                            getString(it.msg)
                        }

                        is SSHRepoViewModel.ConnectionResult.Success -> {
                            binding.activityRepoSshTestButton.isEnabled = true

                            getString(R.string.connection_successful)
                        }

                        is SSHRepoViewModel.ConnectionResult.Error -> {
                            binding.activityRepoSshTestButton.isEnabled = true

                            when (it.msg) {
                                is Int -> getString(it.msg)
                                is String -> it.msg
                                else -> null
                            }
                        }
                    }
        })

        viewModel.sshKey.observe(this, Observer { str ->
            binding.activityRepoSshKey.text = getString(if (str.isNullOrEmpty()) {
                R.string.ssh_key_optional
            } else {
                R.string.edit_ssh_key
            })
        })

        if (viewModel.repoId != 0L) { // Editing existing repository
            viewModel.loadRepoProperties()?.let { repoWithProps ->
//                binding.activityRepoSshHostname.setText(repoWithProps.props[SSHRepo.HOSTNAME_PREF_KEY])
//                binding.activityRepoSshUsername.setText(repoWithProps.props[SSHRepo.USERNAME_PREF_KEY])
//                binding.activityRepoSshPassword.setText(repoWithProps.props[SSHRepo.PASSWORD_PREF_KEY])
//                viewModel.sshKey.value = repoWithProps.props[SSHRepo.SSH_KEY_PREF_KEY]
                // TODO: to implement above properties in SSHRepo
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.done, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.done -> {
                saveAndFinish()
                true
            }

            android.R.id.home -> {
                finish()
                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun saveAndFinish() {
        TODO("Not yet implemented")
    }

    fun editCertificates(view: View) {}

    private fun testConnection() {
        ActivityUtils.closeSoftKeyboard(this)
        if (!isInputValid()) {
            return
        }

        val uriString = getUrl()
        val username = getUsername()
        val password = getPassword()

        viewModel.testConnection(uriString, username, password)
    }

    private fun getPassword(): String {
        TODO("Not yet implemented")
    }

    private fun getUsername(): String {
        TODO("Not yet implemented")
    }

    private fun getUrl(): String {
        TODO("Not yet implemented")
    }

    private fun isInputValid(): Boolean {
        TODO("Not yet implemented")
    }

    companion object {
        private val TAG = SSHRepoActivity::class.java.name
        private const val ARG_REPO_ID = "repo_id"

        @JvmStatic
        @JvmOverloads
        fun start(activity: Activity, repoId: Long = 0) {
            val intent = Intent(Intent.ACTION_VIEW)
                    .setClass(activity, SSHRepoActivity::class.java)
                    .putExtra(ARG_REPO_ID, repoId)

            activity.startActivity(intent)
        }
    }
}