package com.orgzly.android.ui.repo.ssh

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.orgzly.R
import com.orgzly.android.App
import com.orgzly.android.repos.RepoFactory
import com.orgzly.android.repos.RepoType
import com.orgzly.android.repos.SSHRepo
import com.orgzly.android.repos.WebdavRepo
import com.orgzly.android.ui.CommonActivity
import com.orgzly.android.ui.repo.RepoViewModel
import com.orgzly.android.ui.repo.directory.DirectoryRepoActivity
import com.orgzly.android.ui.repo.webdav.WebdavRepoActivity
import com.orgzly.android.ui.repo.webdav.WebdavRepoViewModel
import com.orgzly.android.ui.util.ActivityUtils
import com.orgzly.android.util.UriUtils
import com.orgzly.databinding.ActivityRepoSshBinding
import com.orgzly.databinding.DialogCertificatesBinding
import javax.inject.Inject

class SSHRepoActivity : CommonActivity() {
    private lateinit var binding: ActivityRepoSshBinding

    @Inject
    lateinit var repoFactory: RepoFactory

    private lateinit var viewModel: SSHRepoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        App.appComponent.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_repo_ssh)
        setupActionBar(R.string.ssh)
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
//                binding.activityRepoSshPassword.setText(repoWithProps.props[SSHRepo.DIRECTORY_PREF_KEY])
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
        if(isInputValid()){
            val uriString = getUrl()
            val username = getUsername()
            val password = getPassword()
            val hostname = getHostname()
            val directory = getDirectory()
            val sshKey = getSSHKey()

            val props = mutableMapOf("" to "")
//            TODO: to niżej musi być w SSHRepo
//            val props = mutableMapOf(
//                    USERNAME_PREF_KEY to username,
//                    PASSWORD_PREF_KEY to password,
//                    HOSTNAME_PREF_KEY to hostname,
//                    DIRECTORY_PREF_KEY to directory
//            )

            if(sshKey != null) {
    //            props[SSH_KEY_PREF_KEY] = ssh_key
            }

            viewModel.saveRepo(RepoType.SSH, uriString, props)

            // TODO: tu mozna zrobić jeszcze walidację URL, tak jak w WebDav
        }
    }

    fun editSSHKey(view: View) {
        val dialogBinding = DialogCertificatesBinding.inflate(layoutInflater).apply {
            certificates.setText(viewModel.sshKey.value)
            certificates.hint = "-----BEGIN SSH KEY-----"
        }

        alertDialog = AlertDialog.Builder(this)
                .setTitle("SSH KEY")
                .setPositiveButton(R.string.set) { _, _ ->
                    viewModel.sshKey.value = dialogBinding.certificates.text.toString()
                }
                .setNeutralButton(R.string.clear) { _, _ ->
                    viewModel.sshKey.value = null
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    // Cancel
                }
                .setView(dialogBinding.root)
                .show()
                .apply {
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
    }


    private fun getSSHKey(): String? {
        return viewModel.sshKey.value
    }

    private fun getPassword(): String {
        return binding.activityRepoSshPassword.text.toString().trim{it <= ' '}
    }

    private fun getUsername(): String {
        return binding.activityRepoSshUsername.text.toString().trim{it <= ' '}
    }

    private fun getUrl(): String {
        // tu trzeba będzie to jakoś połączyć hostname + directory ?, lub hostname + username ?
        val hostname = getHostname()
        val directory = getDirectory()
        return "ssh:/$hostname$directory"
    }

    private fun isInputValid(): Boolean {
        val username = getUsername()
        val password = getPassword()
        val hostname = getHostname()
        val directory = getDirectory()

        binding.activityRepoSshUsernameLayout.error = when {
            TextUtils.isEmpty(username) -> getString(R.string.can_not_be_empty)
            else -> null
        }

        binding.activityRepoSshPasswordLayout.error = when {
            TextUtils.isEmpty(password) -> getString(R.string.can_not_be_empty)
            else -> null
        }

        binding.activityRepoSshHostnameLayout.error = when {
            TextUtils.isEmpty(hostname) -> getString(R.string.can_not_be_empty)
//            !SSHRepoActivity.SSH_SCHEME_REGEX.matches(hostname) -> getString(R.string.invalid_url)
//            UriUtils.containsUser(hostname) -> getString(R.string.credentials_in_url_not_supported)
            else -> null
        }

        binding.activityRepoSshDirectoryLayout.error = when {
            TextUtils.isEmpty(directory) -> getString(R.string.can_not_be_empty)
            else -> null
        }

        return binding.activityRepoSshUsernameLayout.error == null
                && binding.activityRepoSshPasswordLayout.error == null
                && binding.activityRepoSshHostnameLayout.error == null
                && binding.activityRepoSshDirectoryLayout.error == null
    }

    private fun getHostname(): String {
        return binding.activityRepoSshHostname.text.toString().trim{it <= ' '}
    }

    private fun getDirectory(): String {
        return binding.activityRepoSshDirectory.text.toString().trim{it <= ' '}
    }

    private fun testConnection() {
        ActivityUtils.closeSoftKeyboard(this)
        if (!isInputValid()) {
            return
        }

        val username = getUsername()
        val password = getPassword()
        val hostname = getHostname()
        val directory = getDirectory()
        val sshKey = getSSHKey()

        viewModel.testConnection(username, password, hostname, directory, sshKey)
    }

    companion object {
        private const val ARG_REPO_ID = "repo_id"
        private val SSH_SCHEME_REGEX = Regex("")

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
