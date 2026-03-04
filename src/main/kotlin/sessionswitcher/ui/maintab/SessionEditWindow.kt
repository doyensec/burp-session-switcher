package sessionswitcher.ui.maintab

import burp.api.montoya.ui.Theme
import sessionswitcher.SessionSwitcher
import sessionswitcher.sessions.Session
import sessionswitcher.ui.ButtonPrimary
import sessionswitcher.ui.TextFieldWithPlaceholder
import sessionswitcher.ui.tables.PairListTableModel
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import kotlin.math.min

class SessionEditWindow(private val sessionSwitcher: SessionSwitcher, private val initialSession: Optional<Session>) :
    JDialog(
        sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame(),
        if (initialSession.isEmpty) "New Session" else "Edit Session",
        true
    ) {

    // Stored data
    private val headers = mutableListOf<Pair<String, String>>()
    private val cookies = mutableListOf<Pair<String, String>>()

    // Flags
    var shouldSave = false

    // UI elements
    val saveButton = ButtonPrimary("Save")
    val cancelButton = JButton("Cancel")
    val validationLabel = JLabel("")

    val nameField = TextFieldWithPlaceholder("", "New Session")
    val hostField = TextFieldWithPlaceholder("", "example.com")
    val headerTableModel = PairListTableModel(headers)
    val cookieTableModel = PairListTableModel(cookies)
    val headerTableSection = TableSection(
        "Headers",
        null,
        headerTableModel,
        showRefreshButton = false,
        showDeleteButton = false,
        showDuplicateButton = false,
        showEditButton = false,
        showNewButton = false
    )
    val cookieTableSection = TableSection(
        "Cookies",
        null,
        cookieTableModel,
        showRefreshButton = false,
        showDeleteButton = false,
        showDuplicateButton = false,
        showEditButton = false,
        showNewButton = false
    )

    private fun loadInitialSession() {
        if (initialSession.isPresent) {
            val session = initialSession.get()
            this.nameField.text = session.name
            this.hostField.text = session.getHost()
            this.nameField.isEnabled = false
            this.hostField.isEnabled = false
            this.headers.addAll(session.headers.toList())
            this.cookies.addAll(session.cookies.getPairs())
            checkEnableSaveButton()
        }
    }

    fun autoSize() {
        // Gets the size of the screen the Burp window is on (for multi-monitor setups)
        val screenSize = sessionSwitcher.montoyaApi.userInterface().swingUtils()
            .suiteFrame().graphicsConfiguration.device.displayMode

        val reasonableHeight = min(this.preferredSize.height, screenSize.height - 50)
        this.maximumSize = Dimension(screenSize.width, reasonableHeight)
        this.minimumSize = Dimension(680, 480)
        this.preferredSize = Dimension(1100, 550)

        // Pack the window to fit its content
        this.pack()
        this.setLocationRelativeTo(sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame())
    }

    fun refreshTables() {
        this.headerTableSection.refreshTable()
        this.cookieTableSection.refreshTable()
    }

    fun saveSession(): Session {
        val session: Session
        if (initialSession.isPresent) {
            session = initialSession.get()
            session.headers.clear()
            session.headers.putAll(headers)
            session.cookies.clear()
            session.cookies.setPairs(cookies)
        } else {
            session = sessionSwitcher.sessions.createSession(nameField.text.trim())
            session.setHost(hostField.text.trim())
            session.headers.putAll(headers)
            session.cookies.setPairs(cookies)
        }
        sessionSwitcher.sessions.updateChildObjectAsync(session)
        return session
    }

    fun showDialog(): Optional<Session> {
        // This will block until the window is closed
        this.isVisible = true

        // BEFORE CLOSING vvv

        // Make sure to save any cell edits before closing the window
        if (headerTableSection.table.isEditing) {
            headerTableSection.table.getCellEditor().stopCellEditing()
        }
        if (cookieTableSection.table.isEditing) {
            cookieTableSection.table.getCellEditor().stopCellEditing()
        }

        return if (this.shouldSave) {
            Optional.of(saveSession())
        } else {
            Optional.empty()
        }
    }

    init {
        // Set window properties
        this.isResizable = true
        this.isAutoRequestFocus = true

        saveButton.isEnabled = false

        // Set the renderer for the new row placeholder
        headerTableSection.table.columnModel.getColumn(0).cellRenderer = NewRowCellRenderer()
        cookieTableSection.table.columnModel.getColumn(0).cellRenderer = NewRowCellRenderer()
        val theme = sessionSwitcher.montoyaApi.userInterface().currentTheme()
        val headerTableDeleteButtonCell =
            DeleteButtonCell(headerTableSection.table, theme) { row -> headers.removeAt(row) }
        val cookieTableDeleteButtonCell =
            DeleteButtonCell(cookieTableSection.table, theme) { row -> cookies.removeAt(row) }
        headerTableSection.table.columnModel.getColumn(2).cellRenderer = headerTableDeleteButtonCell
        cookieTableSection.table.columnModel.getColumn(2).cellRenderer = cookieTableDeleteButtonCell
        headerTableSection.table.columnModel.getColumn(2).cellEditor = headerTableDeleteButtonCell
        cookieTableSection.table.columnModel.getColumn(2).cellEditor = cookieTableDeleteButtonCell
        headerTableSection.table.columnModel.getColumn(2).maxWidth = 50
        cookieTableSection.table.columnModel.getColumn(2).maxWidth = 50

        val controls = arrayOf(
            Pair("Session Name:", nameField),
            Pair("Session Hostname:", hostField),
        )

        val textFieldPanel = JPanel(GridBagLayout())
        val c = GridBagConstraints()
        c.insets = Insets(5, 5, 5, 5)
        var index = 0
        for ((text, control) in controls) {
            c.gridy = index
            // Add label first
            c.anchor = GridBagConstraints.LINE_START
            c.fill = GridBagConstraints.NONE
            c.ipadx = 0
            c.ipady = 0
            c.gridx = 0
            c.weightx = 0.0
            c.gridwidth = 1
            val label = JLabel(text)
            textFieldPanel.add(label, c)

            // Add combo box
            c.anchor = GridBagConstraints.LINE_END
            c.fill = GridBagConstraints.HORIZONTAL
            c.gridx = 1
            c.ipadx = 100
            c.weightx = 1.0
            c.gridwidth = 2
            textFieldPanel.add(control, c)

            // Next row
            index++
        }

        // Build the main window
        val panel = JPanel().also {
            it.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.add(textFieldPanel)
            it.add(Box.createVerticalStrut(5))
            it.add(headerTableSection.getComponent())
            it.add(cookieTableSection.getComponent())
            it.add(JPanel().also { p ->
                p.add(Box.createHorizontalStrut(5))
                p.layout = BoxLayout(p, BoxLayout.X_AXIS)
                p.add(validationLabel)
                p.add(Box.createHorizontalGlue())
                p.add(saveButton)
                p.add(Box.createHorizontalStrut(5))
                p.add(cancelButton)
                p.add(Box.createHorizontalStrut(5))
            })
            it.add(Box.createVerticalStrut(5))
        }

        // Set button listeners
        saveButton.addActionListener {
            this.shouldSave = true
            this.dispose()
        }
        cancelButton.addActionListener {
            this.shouldSave = false
            this.dispose()
        }

        // Add listeners for validation
        val listener = object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = checkEnableSaveButton()
            override fun removeUpdate(e: DocumentEvent) = checkEnableSaveButton()
            override fun changedUpdate(e: DocumentEvent) = checkEnableSaveButton()
        }
        nameField.document.addDocumentListener(listener)
        hostField.document.addDocumentListener(listener)
        headerTableModel.addEditListener(this::tableEditListener)
        cookieTableModel.addEditListener(this::tableEditListener)

        this.add(panel)
        this.autoSize()

        this.loadInitialSession()
    }

    private fun validateFields(): Pair<Boolean, String> {
        val sessionName = nameField.text.trim()
        if (!Session.Companion.isValidName(sessionName)) {
            return Pair(false, "Session name is invalid. Allowed characters: [A-Za-z0-9._-]")
        }
        if (initialSession.isEmpty && sessionSwitcher.sessions.hasSession(sessionName)) {
            return Pair(false, "A session with this name already exists")
        }

        // Try validate hostname
        val host = hostField.text.trim()
        val urlText = "https://${host}"
        try {
            val uri = URI(urlText)
            if (uri.host != host) {
                return Pair(false, "Invalid hostname")
            }
        } catch (_: URISyntaxException) {
            return Pair(false, "Invalid hostname")
        }

        if (headers.isEmpty() && cookies.isEmpty()) {
            // Empty session
            return Pair(false, "Session must have at least one header or cookie")
        }

        return Pair(true, "")
    }

    fun checkEnableSaveButton() {
        val (enable, validationText) = validateFields()
        this.validationLabel.text = validationText
        saveButton.isEnabled = enable
    }

    @Suppress("UNUSED_PARAMETER")
    fun tableEditListener(row: Int, column: Int) {
        checkEnableSaveButton()
    }

    class NewRowCellRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component? {
            val lastRow = (table?.rowCount ?: 0) - 1
            return if (row == lastRow && column == 0 && ((value == null) || (value == ""))) {
                super.getTableCellRendererComponent(table, "(New)", isSelected, hasFocus, row, column)
            } else {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            }
        }
    }

    class DeleteButtonCell(val table: JTable, theme: Theme, val action: (Int) -> (Unit)) : AbstractCellEditor(),
        TableCellEditor, TableCellRenderer, ActionListener, MouseListener {
        val fullSizeIcon =
            ImageIcon(DeleteButtonCell::class.java.getResource("/icons/${theme.name.lowercase()}/delete.png"))
        val icon = ImageIcon(fullSizeIcon.image.getScaledInstance(16, 16, Image.SCALE_SMOOTH))
        val renderButton = JButton(icon)
        val editButton = JButton(icon)
        var isButtonColumnEditor = false

        init {
            editButton.addActionListener(this)
            table.addMouseListener(this)
        }

        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component? {
            if (table == null) return renderButton
            val lastRow = (table.rowCount - 1)
            if (row == lastRow) {
                return null
            }
            return renderButton
        }

        override fun getCellEditorValue(): Any {
            return "delete"
        }

        override fun getTableCellEditorComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            row: Int,
            column: Int
        ): Component? {
            if (table == null) return renderButton
            val lastRow = (table.rowCount - 1)
            if (row == lastRow) {
                return null
            }
            return editButton
        }

        override fun actionPerformed(e: ActionEvent?) {
            val row = table.editingRow
            fireEditingStopped()
            action(row)
        }

        override fun mousePressed(e: MouseEvent?) {
            if (table.isEditing && table.cellEditor == this) {
                isButtonColumnEditor = true
            }
        }

        override fun mouseReleased(e: MouseEvent?) {
            if (isButtonColumnEditor && table.isEditing) {
                table.cellEditor.stopCellEditing()
            }
            isButtonColumnEditor = false
        }

        override fun mouseClicked(e: MouseEvent?) {}
        override fun mouseEntered(e: MouseEvent?) {}
        override fun mouseExited(e: MouseEvent?) {}
    }
}