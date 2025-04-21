package com.github.pushpavel.cph.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import org.apache.commons.lang3.SystemUtils
import java.io.File
import java.net.URL

val LOG = logger<DownloadFileAction>()

class DownloadFileAction : AnAction("Setup CPH") {
    override fun actionPerformed(e: AnActionEvent) {
        val homeDir = SystemUtils.getUserDir()

        LOG.info("Downloading binary to home directory '$homeDir'")
        val url =
            "https://github.com/goreleaser/goreleaser/releases/download/v1.26.1/goreleaser-1.26.1-1-aarch64.pkg.tar.zst"
        val fileName = "goreleaser"

        val downloadTask = object : Task.Backgroundable(null, "Downloading File...", false) {
            override fun run(indicator: ProgressIndicator) {

                indicator.text = "Downloading..."
                val file = File(homeDir, fileName)
                try {
                    URL(url).openStream().use { inputStream ->
                        file.outputStream().use { outputStream ->
                            val buffer = ByteArray(1024)
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                indicator.fraction = indicator.fraction + bytesRead.toDouble() / file.length()
                            }
                        }
                    }

                } catch (e: Exception) {
                    Messages.showMessageDialog(
                        project,
                        "Download failed: ${e.message}",
                        "Error",
                        Messages.getErrorIcon()
                    )
                }
            }
        }
        ProgressManager.getInstance().run(downloadTask)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }
}
