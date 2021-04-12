package com.orgzly.android.ui.repo.ssh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.orgzly.R
import com.orgzly.android.App
import com.orgzly.android.repos.RepoFactory
import com.orgzly.android.ui.CommonActivity
import com.orgzly.android.ui.repo.RepoViewModel
import com.orgzly.android.ui.repo.directory.DirectoryRepoActivity
import com.orgzly.databinding.ActivityRepoSshBinding
import javax.inject.Inject

class SSHRepoActivity : CommonActivity() {
    private lateinit var binding: ActivityRepoSshBinding

    @Inject
    lateinit var repoFactory: RepoFactory // to jest potrzebne żeby wgl się aplikacja zbuildowała, ale nw czemu

    private lateinit var viewModel: RepoViewModel // to nie wiem jeszcze do czego

    override fun onCreate(savedInstanceState: Bundle?) {
        App.appComponent.inject(this)

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_repo_ssh)

        setupActionBar(R.string.ssh)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.done, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.done -> {
//                saveAndFinish()
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
}