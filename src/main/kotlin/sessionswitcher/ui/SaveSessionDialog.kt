package sessionswitcher.ui

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.SessionSwitcher
import sessionswitcher.sessions.Session
import javax.swing.JOptionPane

class SaveSessionDialog(private val sessionSwitcher: SessionSwitcher) {
    fun newSessionDialog(httpRequest: HttpRequest): Session? {
        var name: String?
        var ok = false
        do {
            name = JOptionPane.showInputDialog(
                sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame(),
                "Choose a name for the new Session",
                "New Session",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                "",
            ) as String?
            if (name == null) return null
            if (!Session.isValidName(name)) {
                JOptionPane.showMessageDialog(
                    sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame(),
                    "The chosen name contains invalid characters. Allowed characters: [A-Za-z0-9._-]",
                    "Invalid characters in name",
                    JOptionPane.WARNING_MESSAGE
                )
                continue
            }
            if (this.sessionSwitcher.sessions.hasSession(name)) {
                JOptionPane.showMessageDialog(
                    sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame(),
                    "A session with this name already exists in this project, please choose a different name",
                    "Name already in use",
                    JOptionPane.WARNING_MESSAGE
                )
                continue
            }
            ok = true
        } while (!ok)
        val session = this.sessionSwitcher.sessions.createSession(name!!)
        session.loadFromRequest(httpRequest)
        return session
    }

    fun duplicateSessionDialog(oldSession: Session): Session? {
        var name: String?
        var ok = false
        do {
            name = JOptionPane.showInputDialog(
                sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame(),
                "Choose a name for the new Session",
                "Duplicate Session",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                "",
            ) as String?
            if (name == null) return null
            if (!Session.isValidName(name)) {
                JOptionPane.showMessageDialog(
                    sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame(),
                    "The chosen name contains invalid characters. Allowed characters: [A-Za-z0-9._-]",
                    "Invalid characters in name",
                    JOptionPane.WARNING_MESSAGE
                )
                continue
            }
            if (this.sessionSwitcher.sessions.hasSession(name)) {
                JOptionPane.showMessageDialog(
                    sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame(),
                    "A session with this name already exists in this project, please choose a different name",
                    "Name already in use",
                    JOptionPane.WARNING_MESSAGE
                )
                continue
            }
            ok = true
        } while (!ok)
        val session = this.sessionSwitcher.sessions.duplicateSession(oldSession.name, name!!)
        return session
    }

    fun updateSessionDialog(httpRequest: HttpRequest): Session? {
        val sessions = this.sessionSwitcher.sessions.getSessions().toTypedArray()

        if (sessions.isEmpty()) {
            JOptionPane.showMessageDialog(
                sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame(),
                "There are no sessions saved in this project file yet. Create a new session first.",
                "No sessions",
                JOptionPane.WARNING_MESSAGE
            )
            return null
        }

        val session = JOptionPane.showInputDialog(
            sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame(),
            "Choose a session to update using this request",
            "Update session",
            JOptionPane.QUESTION_MESSAGE,
            null,
            sessions,
            sessions[0],
        ) as Session? ?: return null


        val settings = this.sessionSwitcher.settings
        session.updateFromRequest(
            httpRequest,
            settings.cookiesUpdateMode.get(),
            settings.headersUpdateMode.get()
        )

        // TODO: trigger global session list update?
        return session
    }
}