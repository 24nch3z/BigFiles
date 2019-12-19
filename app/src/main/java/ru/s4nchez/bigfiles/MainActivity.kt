package ru.s4nchez.bigfiles

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import kotlinx.android.synthetic.main.activity_main.*
import pl.droidsonroids.gif.GifDrawable
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEFAULT_GIF = "homer_lurking.gif"
        private const val LESS_10MB_GIF = "homers_brain.gif"
        private const val MORE_10MB_GIF = "chiligif.gif"

        private const val MODULE_LESS_10MB = "less10mb"
        private const val MODULE_MORE_10MB = "more10mb"
    }

    private val listener = SplitInstallStateUpdatedListener { state ->
        val log = StringBuilder()
        log.append(state.moduleNames().joinToString(", ")).append("\n")
        log.append("status: ${state.status()}").append("\n")
        log.append("errorCode: ${state.errorCode()}").append("\n")

        log.append("\n")
        text_view_logs.append(log.toString())
    }

    private lateinit var manager: SplitInstallManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manager = SplitInstallManagerFactory.create(this)
        image_view.setOnClickListener { setGif(DEFAULT_GIF) }
        setGif(DEFAULT_GIF)

        btn_less_10mb_set.setOnClickListener { setGif(LESS_10MB_GIF) }
        btn_less_10mb_download.setOnClickListener { downloadModule(MODULE_LESS_10MB) }
        btn_less_10mb_state.setOnClickListener { checkInstalled(MODULE_LESS_10MB) }
        btn_less_10mb_remove.setOnClickListener { removeModule(MODULE_LESS_10MB) }

        btn_more_10mb_set.setOnClickListener { setGif(MORE_10MB_GIF) }
        btn_more_10mb_download.setOnClickListener { downloadModule(MODULE_MORE_10MB) }
        btn_more_10mb_state.setOnClickListener { checkInstalled(MODULE_MORE_10MB) }
        btn_more_10mb_remove.setOnClickListener { removeModule(MODULE_MORE_10MB) }
    }

    override fun onResume() {
        manager.registerListener(listener)
        super.onResume()
    }

    override fun onPause() {
        manager.unregisterListener(listener)
        super.onPause()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.install(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_logs_menu_item -> text_view_logs_wrapper.visibility = View.VISIBLE
            R.id.hide_logs_menu_item -> text_view_logs_wrapper.visibility = View.GONE
            R.id.clear_logs_menu_item -> text_view_logs.text = ""
        }
        return true
    }

    private fun downloadModule(module: String) {
        if (!manager.installedModules.contains(module)) {
            val request = SplitInstallRequest.newBuilder()
                .addModule(module)
                .build()
            toast("Началось скачивание модуля $module")
            manager.startInstall(request)
                .addOnCompleteListener { toast("download, модуль: $module, completeListener") }
                .addOnFailureListener { toast("download, модуль: $module, failureListener") }
                .addOnSuccessListener { toast("download, модуль: $module, successListener") }
        } else {
            toast("Модуль уже установлен")
        }
    }

    private fun removeModule(module: String) {
        manager.deferredUninstall(listOf(module))
            .addOnCompleteListener { toast("remove, модуль: $module, completeListener") }
            .addOnFailureListener { toast("remove, модуль: $module, failureListener") }
            .addOnSuccessListener { toast("remove, модуль: $module, successListener") }
    }

    private fun checkInstalled(module: String) {
        toast("Module installed: ${manager.installedModules.contains(module)}")
    }

    private fun setGif(path: String) {
        try {
            val assets = createPackageContext(packageName, 0).also {
                SplitCompat.install(it)
            }.assets
            image_view.setImageDrawable(GifDrawable(assets, path))
        } catch (e: FileNotFoundException) {
            toast("FileNotFoundException")
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
