package com.john.inkwell

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.john.inkwell.data.InkwellDatabase
import com.john.inkwell.data.InkwellRepository
import com.john.inkwell.data.UserPreferences
import com.john.inkwell.data.drive.DriveSyncManager
import com.john.inkwell.lock.BiometricGate
import com.john.inkwell.ui.AppRoot
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private lateinit var preferences: UserPreferences
    private lateinit var driveSyncManager: DriveSyncManager

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val account = task.result
        if (account != null) {
            lifecycleScope.launch { preferences.setDriveEmail(account.email) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = InkwellDatabase.get(applicationContext).blockDao()
        val repository = InkwellRepository(dao)
        preferences = UserPreferences(applicationContext)
        driveSyncManager = DriveSyncManager(applicationContext, repository)

        setContent {
            AppRoot(
                repository = repository,
                preferences = preferences,
                driveSyncManager = driveSyncManager,
                onSignInClick = { signInLauncher.launch(driveSyncManager.signInIntent()) },
                onRequestUnlock = { onSuccess ->
                    BiometricGate.authenticate(
                        activity = this,
                        onSuccess = onSuccess,
                        onFailure = { /* user can tap Unlock again */ }
                    )
                }
            )
        }
    }
}
