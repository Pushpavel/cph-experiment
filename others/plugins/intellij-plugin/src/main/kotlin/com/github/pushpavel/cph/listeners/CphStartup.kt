package com.github.pushpavel.cph.listeners

import com.github.pushpavel.cph.services.CphBackend
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFile
import io.javalin.Javalin
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

val LOG = logger<CphStartup>()

class CphStartup : ProjectActivity {
    private var done = false;
    override suspend fun execute(project: Project) {
        if (done) {
            LOG.info("Cph started with multiple projects, seems like cph is already initialized")
            return
        }
        done = true
        LOG.debug("Cph starting the service")
        val backend = service<CphBackend>()
        Javalin.create(/*config*/)
            .sse("/events/onDidChangeActiveTextEditor") { client ->
                client.keepAlive()
                project.messageBus.connect().subscribe(
                    FileEditorManagerListener.FILE_EDITOR_MANAGER,
                    object : FileEditorManagerListener {
                        override fun selectionChanged(event: FileEditorManagerEvent) {
                            println("activeFile: " + event.newFile?.path)
                            client.sendEvent(
                                Json.encodeToString(
                                    event.newFile?.path
                                )
                            )
                        }
                    }
                )
            }
            .sse("/events/onDidCloseTextDocument") { client ->
                client.keepAlive()
                project.messageBus.connect().subscribe(
                    FileEditorManagerListener.FILE_EDITOR_MANAGER,
                    object : FileEditorManagerListener {
                        override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                            println("closedFile: " + file.path)
                            client.sendEvent(
                                Json.encodeToString(
                                    file.path
                                )
                            )
                        }
                    }
                )
            }
            .start(5678)

        val backendURL = backend.getURL()
        println(backendURL)
    }
}
