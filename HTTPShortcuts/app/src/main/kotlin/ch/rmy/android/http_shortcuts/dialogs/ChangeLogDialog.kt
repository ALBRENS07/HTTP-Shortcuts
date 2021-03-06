package ch.rmy.android.http_shortcuts.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.view.LayoutInflater
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.CheckBox
import androidx.core.content.pm.PackageInfoCompat
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.isDarkThemeEnabled
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.utils.Settings
import io.reactivex.Completable
import io.reactivex.subjects.CompletableSubject


class ChangeLogDialog(private val context: Context, private val whatsNew: Boolean) : Dialog {

    private val settings: Settings = Settings(context)

    override fun shouldShow(): Boolean {
        if (isPermanentlyHidden) {
            return false
        }
        val lastSeenVersion = settings.changeLogLastVersion
        return version != lastSeenVersion && lastSeenVersion != 0L
    }

    private val isPermanentlyHidden: Boolean
        get() = settings.isChangeLogPermanentlyHidden

    @SuppressLint("InflateParams")
    override fun show(): Completable {
        try {
            settings.changeLogLastVersion = version

            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.changelog_dialog, null)
            val webView = view.findViewById<WebView>(R.id.changelog_webview)
            val showAtStartupCheckbox = view.findViewById<CheckBox>(R.id.checkbox_show_at_startup)

            webView.settings.javaScriptEnabled = true
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    webView.visible = true
                }
            }

            val completable = CompletableSubject.create()

            val dialog = DialogBuilder(context)
                .view(view)
                .title(if (whatsNew) R.string.changelog_title_whats_new else R.string.changelog_title)
                .positive(android.R.string.ok)
                .dismissListener {
                    completable.onComplete()
                }
                .showIfPossible()

            return if (dialog != null) {
                webView.loadUrl(if (context.isDarkThemeEnabled()) {
                    CHANGELOG_ASSET_URL_DARK_MODE
                } else {
                    CHANGELOG_ASSET_URL
                })

                showAtStartupCheckbox.isChecked = !isPermanentlyHidden
                showAtStartupCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    settings.isChangeLogPermanentlyHidden = !isChecked
                }

                completable.doOnDispose {
                    dialog.dismiss()
                }
            } else {
                Completable.complete()
            }
        } catch (e: Exception) {
            logException(e)
            return Completable.error(e)
        }
    }

    private val version: Long
        get() = try {
            (PackageInfoCompat.getLongVersionCode(
                context.packageManager
                    .getPackageInfo(context.packageName, 0)
            ) / 10000) - 110000
        } catch (e: NameNotFoundException) {
            0L
        }

    companion object {

        private const val CHANGELOG_ASSET_URL = "file:///android_asset/changelog.html"
        private const val CHANGELOG_ASSET_URL_DARK_MODE = "$CHANGELOG_ASSET_URL?dark"

    }

}
