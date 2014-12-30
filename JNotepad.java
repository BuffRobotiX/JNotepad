// 
// David F. Buff
// 
// Description: 
// 		A complete Java Swing implementation of Windows Notepad.
//	Remembers location, size, word wrapping, font, status bar visibility on close and restores them the next time you open.
//	Has print, find, go to, replace, undo, select all, status bar, and view help functionality.
//	The only menu item not implemented is Page Setup, which is unnecessary because the textArea.print() dialog has a tab for Page Setup. 
//	A complete replication of Windows Notepad in Java.
//

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.io.*;
import java.awt.print.PrinterException;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.awt.GraphicsEnvironment;
import java.awt.font.*;
import javax.swing.text.Position.Bias;
import java.util.Vector;
import java.util.Properties;
import java.io.InputStream;
import java.awt.Point;

class JNotepad {
	JFrame frame;
	JTextArea textArea;
	boolean saved;
	boolean neverSaved;
	String fileName;
	File file;
	boolean isTextSelected;
	boolean findMatchCase;
	boolean findDirectionDown;
	String findString;
	String undoText;
	JPanel statusPanel;
	JLabel statusLabel;
	JCheckBoxMenuItem wordWrapMenu;
	JCheckBoxMenuItem statusBarMenu;
	JNotepad() {
		/* =================
		Create and setup the frame
		=====================*/
		frame = new JFrame("Untitled - JNotepad");
		frame.setSize(575, 490);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		ImageIcon icon = new ImageIcon("JNotepad.png");
		frame.setIconImage(icon.getImage());
		/*==========================
		File Menu
		============================*/
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		JMenuItem newMenu = new JMenuItem("New");
		newMenu.setMnemonic('N');
		newMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		JMenuItem openMenu = new JMenuItem("Open...");
		openMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		JMenuItem saveMenu = new JMenuItem("Save");
		saveMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		JMenuItem saveAsMenu = new JMenuItem("Save As...");
		JMenuItem printMenu = new JMenuItem("Print...");
		printMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		JMenuItem exitMenu = new JMenuItem("Exit");
		exitMenu.setMnemonic('x');

		fileMenu.add(newMenu);
		fileMenu.add(openMenu);
		fileMenu.add(saveMenu);
		fileMenu.add(saveAsMenu);
		fileMenu.addSeparator();
		fileMenu.add(printMenu);
		fileMenu.addSeparator();
		fileMenu.add(exitMenu);
		/*=============================
		Edit Menu
		==============================*/
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('E');
		JMenuItem undoMenu = new JMenuItem("Undo");
		undoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		JMenuItem cutMenu = new JMenuItem("Cut");
		cutMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		JMenuItem copyMenu = new JMenuItem("Copy");
		copyMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		JMenuItem pasteMenu = new JMenuItem("Paste");
		pasteMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		JMenuItem deleteMenu = new JMenuItem("Delete");
		deleteMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		JMenuItem findMenu = new JMenuItem("Find...");
		findMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		JMenuItem findNextMenu = new JMenuItem("Find Next");
		JMenuItem replaceMenu = new JMenuItem("Replace...");
		replaceMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK));
		JMenuItem gotoMenu = new JMenuItem("Go To...");
		gotoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
		JMenuItem selectAllMenu = new JMenuItem("Select All");
		selectAllMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		JMenuItem timeDateMenu = new JMenuItem("Time/Date");
		timeDateMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

		undoMenu.setEnabled(false);
		cutMenu.setEnabled(false);
		copyMenu.setEnabled(false);
		deleteMenu.setEnabled(false);
		findMenu.setEnabled(false);
		findNextMenu.setEnabled(false);
		isTextSelected = false;

		editMenu.add(undoMenu);
		editMenu.addSeparator();
		editMenu.add(cutMenu);
		editMenu.add(copyMenu);
		editMenu.add(pasteMenu);
		editMenu.add(deleteMenu);
		editMenu.addSeparator();
		editMenu.add(findMenu);
		editMenu.add(findNextMenu);
		editMenu.add(replaceMenu);
		editMenu.add(gotoMenu);
		editMenu.addSeparator();
		editMenu.add(selectAllMenu);
		editMenu.add(timeDateMenu);
		/*=========================
		Format Menu
		==========================*/
		JMenu formatMenu = new JMenu("Format");
		formatMenu.setMnemonic('o');
		wordWrapMenu = new JCheckBoxMenuItem("Word Wrap"); //wordWrapMenu.getState();
		wordWrapMenu.setMnemonic('W');
		JMenuItem fontMenu = new JMenuItem("Font...");
		fontMenu.setMnemonic('F');

		formatMenu.add(wordWrapMenu);
		formatMenu.add(fontMenu);
		/*=======================
		View Menu
		========================*/
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic('V');
		statusBarMenu = new JCheckBoxMenuItem("Status Bar", false);
		statusBarMenu.setMnemonic('S');

		viewMenu.add(statusBarMenu);
		/*======================
		Help menu
		=======================*/
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		JMenuItem viewHelpMenu = new JMenuItem("View Help");
		viewHelpMenu.setMnemonic('H');
		File helpFile = new File("JNotepadHelp.pdf");
		viewHelpMenu.setEnabled(helpFile.exists());
		JMenuItem aboutMenu = new JMenuItem("About JNotepad");

		helpMenu.add(viewHelpMenu);
		helpMenu.addSeparator();
		helpMenu.add(aboutMenu);
		/*==========================
		Menu Bar
		===========================*/
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(formatMenu);
		menuBar.add(viewMenu);
		menuBar.add(helpMenu);
		/*========================
		Text area and Scroll Pane
		=========================*/
		textArea = new JTextArea();
		textArea.setWrapStyleWord(true);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		/*=======================
		Variables
		=======================*/
		saved = true;
		neverSaved = true;
		fileName = "Untitled";
		findString = "";
		undoText = "";
		statusLabel = new JLabel("Index " + textArea.getCaretPosition());
		/*========================
		Menu Action Listener
		========================*/
		ActionListener menuListener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				switch (ae.getActionCommand()) {
					case "New": newFile();
					break;
					case "Open...": open();
					break;
					case "Save": save();
					break;
					case "Save As...": saveAs();
					break;
					case "Print...": try {
							textArea.print();
						}
						catch(PrinterException pe) {
							JOptionPane.showMessageDialog(null, "Error printing file.", "Error!", JOptionPane.ERROR_MESSAGE);
						}
					break;
					case "Exit": exit();
					break;
					case "Undo": String tempText = undoText;
						undoText = textArea.getText();
						textArea.setText(tempText);
						textArea.selectAll();
					break;
					case "Cut": textArea.cut();
					break;
					case "Copy": textArea.copy();
					break;
					case "Paste": textArea.paste();
					break;
					case "Delete": textArea.replaceSelection("");
					break;
					case "Go To...": goTo();
					break;
					case "Select All": textArea.selectAll();
					break;
					case "Find...": find();
					break;
					case "Find Next": if (findString == "")
							find();
						else
							findNext();
					break;
					case "Replace...": replace();
					break;
					case "Time/Date": /*textArea.append(new Date(ae.getWhen()).toString());*/
						SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a M/d/yyyy");
						textArea.append(dateFormat.format(new Date(ae.getWhen())));
					break;
					case "Word Wrap": textArea.setLineWrap(wordWrapMenu.getState());
						scrollPane.setHorizontalScrollBarPolicy(wordWrapMenu.getState() ? JScrollPane.HORIZONTAL_SCROLLBAR_NEVER : JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
						gotoMenu.setEnabled(!wordWrapMenu.getState()); //only enabled if there's no word wrapping

					break;
					case "Font...": font();
					break;
					case "Status Bar": if (statusBarMenu.getState())
						statusBar();
						else {
							frame.remove(statusPanel);
							frame.setVisible(true);
						}
					break;
					case "View Help": try{
							Desktop.getDesktop().open(helpFile); //crashes on Windows XP and lower.
						}
						catch (IOException ie) {
							JOptionPane.showMessageDialog(null, "Error opening help file.", "Error!", JOptionPane.ERROR_MESSAGE);
						}
					break;
					case "About JNotepad": JOptionPane.showMessageDialog(frame, "(C) David F. Buff 2014", "About JNotepad", JOptionPane.INFORMATION_MESSAGE);
					break;
				}
			}
		};
		newMenu.addActionListener(menuListener);
		openMenu.addActionListener(menuListener);
		saveMenu.addActionListener(menuListener);
		saveAsMenu.addActionListener(menuListener);
		printMenu.addActionListener(menuListener);
		exitMenu.addActionListener(menuListener);
		undoMenu.addActionListener(menuListener);
		cutMenu.addActionListener(menuListener);
		copyMenu.addActionListener(menuListener);
		pasteMenu.addActionListener(menuListener);
		deleteMenu.addActionListener(menuListener);
		gotoMenu.addActionListener(menuListener);
		replaceMenu.addActionListener(menuListener);
		selectAllMenu.addActionListener(menuListener);
		findMenu.addActionListener(menuListener);
		findNextMenu.addActionListener(menuListener);
		timeDateMenu.addActionListener(menuListener);
		wordWrapMenu.addActionListener(menuListener);
		fontMenu.addActionListener(menuListener);
		statusBarMenu.addActionListener(menuListener);
		viewHelpMenu.addActionListener(menuListener);
		aboutMenu.addActionListener(menuListener);
		/*=======================
		Document Listener
		========================*/
		textArea.getDocument().addDocumentListener(new DocumentListener() { //if the document is changed the file needs to be saved again
			public void insertUpdate(DocumentEvent de) {
				saved = false;
				if (textArea.getText() != "") { //only enable the find options if there is text.
					findMenu.setEnabled(true);
					findNextMenu.setEnabled(true);
					undoMenu.setEnabled(true);

				}
				else {
					findMenu.setEnabled(false);
					findNextMenu.setEnabled(false);
					undoMenu.setEnabled(false);
				}
			}
			public void removeUpdate(DocumentEvent de) {
				saved = false;
				if (textArea.getText() != "") { //only enable the find options if there is text.
					findMenu.setEnabled(true);
					findNextMenu.setEnabled(true);
					undoMenu.setEnabled(true);
				}
				else {
					findMenu.setEnabled(false);
					findNextMenu.setEnabled(false);
					undoMenu.setEnabled(false);
				}
			}
			public void changedUpdate(DocumentEvent de) {}
		});
		/*====================
		Caret Listener
		======================*/
		textArea.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent ce) {
				statusLabel.setText("Index " + ce.getDot());
			}
		});
		/*=====================
		Window Listener
		======================*/
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				exit();
			}
		});
		/*=====================
		Mouse Listener
		======================*/
		textArea.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent me) {}
			public void mouseEntered(MouseEvent me) {}
			public void mouseExited(MouseEvent me) {}
			public void mousePressed(MouseEvent me) {}
			public void mouseReleased(MouseEvent me) {
				if(me.isPopupTrigger()) {
					JPopupMenu popupMenu = new JPopupMenu();
					JMenuItem cutPopupMenu = new JMenuItem("Cut");
					JMenuItem copyPopupMenu = new JMenuItem("Copy");
					JMenuItem pastePopupMenu = new JMenuItem("Paste");

					cutPopupMenu.setEnabled(isTextSelected);
					copyPopupMenu.setEnabled(isTextSelected);

					cutPopupMenu.addActionListener(menuListener); //use the menu listener, the event names are the same.
					copyPopupMenu.addActionListener(menuListener);
					pastePopupMenu.addActionListener(menuListener);

					popupMenu.add(cutPopupMenu);
					popupMenu.add(copyPopupMenu);
					popupMenu.add(pastePopupMenu);
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
				else {
					if (textArea.getSelectedText() == null) { //disabled if no text is selected
						cutMenu.setEnabled(false);
						copyMenu.setEnabled(false);
						deleteMenu.setEnabled(false);
						isTextSelected = false;
					}
					else {
						cutMenu.setEnabled(true);
						copyMenu.setEnabled(true);
						deleteMenu.setEnabled(true);
						isTextSelected = true;
					}
				}
			}
		});

		frame.setJMenuBar(menuBar);
		frame.add(scrollPane, BorderLayout.CENTER);
		/*=====================
		Load Config File
		======================*/
		//this is done after the defaults are set in case the config file doesn't exist
		File configFile = new File("configuration.properties");
		if (configFile.exists())
		{
			Properties properties = new Properties();
			InputStream inStream = null;
			try {
	        	inStream = new FileInputStream(configFile);
	        	try {
	        		properties.load(inStream);
	        	}
	        	catch (IOException ioe) {}
	        	frame.setSize(Integer.parseInt(properties.getProperty("Width", "575")), Integer.parseInt(properties.getProperty("Height", "490")));
	        	String xlocation = properties.getProperty("XLocation", "null");
	        	String ylocation = properties.getProperty("YLocation", "null");
	        	if (xlocation.equals("null") || ylocation.equals("null"))
	        		frame.setLocationRelativeTo(null);
	        	else
	        		frame.setLocation(Integer.parseInt(xlocation), Integer.parseInt(ylocation));
	        	textArea.setFont(new Font(properties.getProperty("Font", "Monospaced"), Integer.parseInt(properties.getProperty("FontStyle", "0")), Integer.parseInt(properties.getProperty("FontSize", "12"))));
	        	textArea.setLineWrap(Boolean.parseBoolean(properties.getProperty("WordWrap", "false")));
	        	if (Boolean.parseBoolean(properties.getProperty("StatusBar", "false"))) {
	        		statusBarMenu.setState(true);
	        		statusBar();
	        	}
	        	if (Boolean.parseBoolean(properties.getProperty("WordWrap", "false"))) {
	        		wordWrapMenu.setState(true);
	        		textArea.setLineWrap(true);
					scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
					gotoMenu.setEnabled(false); //only enabled if there's no word wrapping

	        	}
	        }
	        catch (FileNotFoundException fne) {}
		}
		frame.setVisible(true);
	}
	/*====================
	New File
	=====================*/
	private void newFile() {
	if (!saved) { //check if the file is saved or not
			Object[] options = {"Save", "Don't Save", "Cancel"};
			int result = JOptionPane.showOptionDialog(null/*real Notepad has this centered, maybe so it catches the user's eye more*/, 
				"Do you want to save changes to " + fileName, "JNotepad", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			switch (result) {
				case 0: save();
				break;
				case 1: textArea.setText("");
					fileName = "Untitled";
					saved = true;
					neverSaved = true;
					frame.setTitle("Untitled - JNotepad");
				break;
			}
		}
	}
	/*======================
	Open File
	=======================*/
	private void open() {
		newFile();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Open");
		fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "text"));
		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) { //if opened
			File file = fileChooser.getSelectedFile();
			if (file.isFile()) { //if is a file
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) { //try with resources! Closes file automatically!
					String fileText = "";
					String line = reader.readLine();
					while (line != null) {
						fileText += line;
						fileText += "\n";
						line = reader.readLine();
					}
					textArea.setText(fileText);
				}
				catch (IOException ie) {
					JOptionPane.showMessageDialog(null, "Error reading file.", "Error!", JOptionPane.ERROR_MESSAGE);
				}
			}
			else { //if not a file
				JOptionPane.showMessageDialog(frame, "File not found.");
			}
		}
	}
	/*=====================
	Save File
	=====================*/
	private void save() {
		if (neverSaved) 
			saveAs();
		else {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				textArea.write(writer);
			}
			catch (IOException ie) {
				JOptionPane.showMessageDialog(null, "Error writing file.", "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
		saved = true;
	}
	/*====================
	Save File As
	=====================*/
	private void saveAs() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save As");
		fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "text"));
		String ext;
		if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			if (file.getName().contains(".")) { //if there is an extension
				fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
				ext = "";
			}
			else {
				fileName = file.getName();
				ext = ".txt";
			}
			file = new File(file + ext); //add the extension
			if (file.exists()) { //if the file exists, ask if you want to replace it
				int result = JOptionPane.showConfirmDialog(frame, file.getName() + " already exists. \n Do you want to replace it?", "Confirm Save As", JOptionPane.YES_NO_OPTION);
				switch (result) {
					case 0: try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) { //if the user didn't add an extension add it for them.
							textArea.write(writer);
						}
						catch (IOException ie) {
							JOptionPane.showMessageDialog(null, "Error writing file.", "Error!", JOptionPane.ERROR_MESSAGE);
						}
						frame.setTitle(fileName + " - JNotepad");
						neverSaved = false;
						saved = true;
					break;
				}
			}
			else {
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) { //if the user didn't add an extension add it for them.
					textArea.write(writer);
				}
				catch (IOException ie) {
					JOptionPane.showMessageDialog(null, "Error writing file.", "Error!", JOptionPane.ERROR_MESSAGE);
				}
				frame.setTitle(fileName + " - JNotepad");
				neverSaved = false;
				saved = true;
			}
		}
	}
	/*=========================
	Exit
	==========================*/
	private void exit() {
		newFile();
		//Save config file
		Properties properties = new Properties();
		properties.setProperty("WordWrap", Boolean.toString(wordWrapMenu.getState()));
		properties.setProperty("StatusBar", Boolean.toString(statusBarMenu.getState()));
		properties.setProperty("Font", textArea.getFont().getFontName());
		properties.setProperty("FontStyle", Integer.toString(textArea.getFont().getStyle()));
		properties.setProperty("FontSize", Integer.toString(textArea.getFont().getSize()));
		properties.setProperty("XLocation", Integer.toString(frame.getX()));
		properties.setProperty("YLocation", Integer.toString(frame.getY()));
		properties.setProperty("Width", Integer.toString(frame.getWidth()));
		properties.setProperty("Height", Integer.toString(frame.getHeight()));
		try {
			File configFile = new File("configuration.properties");
			OutputStream outStream = new FileOutputStream(configFile);
			try{
				properties.store(outStream, "Configuration Property File for JNotepad");
			}
			catch (IOException ioe) {}
		}
		catch (FileNotFoundException fne) {}

		System.exit(0);
	}
	/*========================
	Find
	========================*/
	private void find() {
		findMatchCase = false; //variables for use in findNext()
		findDirectionDown = true;
		JDialog findDialog = new JDialog(frame, "Find", false);
		findDialog.setSize(370, 130);
		findDialog.setLocationRelativeTo(frame);
		findDialog.setResizable(false);
		findDialog.setLayout(new FlowLayout());
		JTextField findField = new JTextField(findString, 17);
		findField.selectAll();
		JLabel findLabel = new JLabel("Find what:  ");
		findLabel.setLabelFor(findField);
		findLabel.setDisplayedMnemonic('n');
		findDialog.add(findLabel);
		findDialog.add(findField);
		JButton findNextButton = new JButton("Find Next");
		findNextButton.setMnemonic('F');
		findNextButton.setEnabled(!findString.equals(""));
		findDialog.add(findNextButton);
		JCheckBox findMatchCaseBox = new JCheckBox("Match case");
		findMatchCaseBox.setMnemonic('c');
		findMatchCaseBox.setDisplayedMnemonicIndex(6);
		findDialog.add(findMatchCaseBox);
		findDialog.add(new JLabel("     ")); //spacer
		JRadioButton findUpRadio = new JRadioButton("Up");
		JRadioButton findDownRadio = new JRadioButton("Down", true);
		findUpRadio.setMnemonic('U');
		findDownRadio.setMnemonic('D');
		ButtonGroup findGroup = new ButtonGroup();
		findGroup.add(findUpRadio);
		findGroup.add(findDownRadio);
		JPanel findGroupPanel = new JPanel();
		findGroupPanel.setPreferredSize(new Dimension(120, 60));
		findGroupPanel.add(new JLabel("Direction       ")); //included some filler to push "Up" down a line
		findGroupPanel.add(findUpRadio);
		findGroupPanel.add(findDownRadio);
		findGroupPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		findDialog.add(findGroupPanel);
		JButton findCancelButton = new JButton("Cancel");
		findDialog.add(new JLabel("       ")); //spacer
		findDialog.add(findCancelButton);
		findField.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent de) {
				findNextButton.setEnabled(!findField.getText().equals(""));
			}
			public void removeUpdate(DocumentEvent de) {
				findNextButton.setEnabled(!findField.getText().equals(""));
			}
			public void changedUpdate(DocumentEvent de) {}
		});
		ActionListener findListener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				switch (ae.getActionCommand()) {
					case "Find Next": findString = findField.getText();
					System.out.print(findField.getText());
						findNext();
					break;
					case "Match case": findMatchCase = !findMatchCase;
					break;
					case "Up": findDirectionDown = false;
					break;
					case "Down": findDirectionDown = true;
					break;
					case "Cancel": findDialog.setVisible(false);
					break;
				}
			}
		};
		findField.setActionCommand("Find Next");
		findField.addActionListener(findListener);
		findNextButton.addActionListener(findListener);
		findMatchCaseBox.addActionListener(findListener);
		findUpRadio.addActionListener(findListener);
		findDownRadio.addActionListener(findListener);
		findCancelButton.addActionListener(findListener);
		findDialog.getRootPane().setDefaultButton(findNextButton);
		findDialog.setVisible(true);
	}
	/*===================
	Find Next
	====================*/
	private void findNext() {
		int findIndex;
		if (findDirectionDown) { //trying to save memory by not duplicating textArea.getText() as it could be large
			if (!findMatchCase)
				findIndex = textArea.getText().toLowerCase().indexOf(findString.toLowerCase(), textArea.getCaretPosition());
			else
				findIndex = textArea.getText().indexOf(findString, textArea.getCaretPosition());
			if (findIndex != -1) {
				textArea.setCaretPosition(findIndex);
				textArea.moveCaretPosition(findIndex + findString.length());
			}
			else
				JOptionPane.showMessageDialog(null/*Notepad centers this dialog on the screen*/, "Cannot find \"" + findString + "\"");
		}
		else {//find direction up
			if (!findMatchCase)
				findIndex = textArea.getText().toLowerCase().substring(0, textArea.getCaretPosition()).lastIndexOf(findString.toLowerCase());
			else
				findIndex = textArea.getText().substring(0, textArea.getCaretPosition()).lastIndexOf(findString);
			if (findIndex != -1) {
				textArea.setCaretPosition(findIndex + findString.length()); //select backwards
				textArea.moveCaretPosition(findIndex);
			}
			else
				JOptionPane.showMessageDialog(null/*Notepad centers this dialog on the screen*/, "Cannot find \"" + findString + "\"");
		}
	}
	/*=========================
	Replace
	==========================*/
	private void replace() {
		JDialog replaceDialog = new JDialog(frame, "Replace", false);
		replaceDialog.setSize(370, 180);
		replaceDialog.setLocationRelativeTo(frame);
		replaceDialog.setResizable(false);
		replaceDialog.setLayout(new FlowLayout());
		JPanel replaceLeftPanel = new JPanel();
		replaceLeftPanel.setPreferredSize(new Dimension(255, 100));
		JPanel replaceRightPanel = new JPanel();
		replaceRightPanel.setPreferredSize(new Dimension(97, 130));
		replaceLeftPanel.add(new JLabel("Find what:      "));
		JTextField replaceFindField = new JTextField(findString, 15);
		replaceLeftPanel.add(replaceFindField);
		JButton replaceFindNextButton = new JButton("Find Next");
		replaceFindNextButton.setEnabled(!findString.equals(""));
		replaceRightPanel.add(replaceFindNextButton);
		replaceDialog.getRootPane().setDefaultButton(replaceFindNextButton);
		replaceLeftPanel.add(new JLabel("Replace with:"));
		JTextField replaceField = new JTextField(15);
		replaceLeftPanel.add(replaceField);
		JButton replaceButton = new JButton("Replace");
		replaceButton.setEnabled(!findString.equals(""));
		replaceRightPanel.add(replaceButton);
		JButton replaceAllButton = new JButton("Replace All");
		replaceAllButton.setEnabled(!findString.equals(""));
		replaceRightPanel.add(replaceAllButton);
		JButton replaceCancelButton = new JButton("Cancel");
		replaceRightPanel.add(replaceCancelButton);
		JCheckBox replaceMatchCaseBox = new JCheckBox("Match case");
		replaceLeftPanel.add(replaceMatchCaseBox);
		replaceDialog.add(replaceLeftPanel);
		replaceDialog.add(replaceRightPanel);

		replaceFindField.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent de) {
				replaceFindNextButton.setEnabled(!replaceFindField.getText().equals(""));
				replaceButton.setEnabled(!replaceFindField.getText().equals(""));
				replaceAllButton.setEnabled(!replaceFindField.getText().equals(""));
			}
			public void removeUpdate(DocumentEvent de) {
				replaceFindNextButton.setEnabled(!replaceFindField.getText().equals(""));
				replaceButton.setEnabled(!replaceFindField.getText().equals(""));
				replaceAllButton.setEnabled(!replaceFindField.getText().equals(""));
			}
			public void changedUpdate(DocumentEvent de) {}
		});
		ActionListener replaceListener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				switch (ae.getActionCommand()) {
					case "Find Next": findString = replaceFindField.getText();
						boolean tempDirection = findDirectionDown;
						findDirectionDown = true; //teh replace dialog can only search down
						findNext();
						findDirectionDown = tempDirection; //restore the original searching direction
					break;
					case "Replace": textArea.replaceSelection(replaceField.getText());
					break;
					case "Replace All": if (findMatchCase)
							textArea.setText(textArea.getText().replaceAll(replaceFindField.getText(), replaceField.getText()));
						else
							textArea.setText(textArea.getText().replaceAll(replaceFindField.getText().toLowerCase(), replaceField.getText().toLowerCase()));
					break;
					case "Match case": findMatchCase = !findMatchCase;
					break;
					case "Cancel": replaceDialog.setVisible(false);
					break;
				}
			}
		};
		replaceFindNextButton.addActionListener(replaceListener);
		replaceButton.addActionListener(replaceListener);
		replaceAllButton.addActionListener(replaceListener);
		replaceMatchCaseBox.addActionListener(replaceListener);
		replaceCancelButton.addActionListener(replaceListener);

		replaceDialog.setVisible(true);
	}
	/*==========================
	Go To
	==========================*/
	private void goTo() {
		String value = (String)JOptionPane.showInputDialog(frame, "Line number:", "Go To Line", JOptionPane.PLAIN_MESSAGE, null, null, "1");
		if (value != null) {
			try {
				if (Integer.parseInt(value) - 1 < textArea.getLineCount() && Integer.parseInt(value) - 1 > 0)
					textArea.setCaretPosition(textArea.getDocument().getDefaultRootElement().getElement(Integer.parseInt(value) - 1).getStartOffset());	
				else
					JOptionPane.showMessageDialog(null, "This line number is beyond the total number of lines");
				}
			catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(null, "You can only enter numbers in this box.");
			}
		}
	}
	/*=================
	Font
	==================*/
	//override the cell renderer to make it display as the selected font.
	public class FontListRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			label.setFont(new Font(value.toString(), Font.PLAIN, 12));
			return label;
		}
	}
	//style renderer for displaying the different styles
	public class StyleListRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			label.setFont(new Font(label.getFont().getFontName(), index, 12));
			return label;
		};
	}
	private void font() {
		JDialog fontDialog = new JDialog(frame, "Font", true);
		fontDialog.setSize(430, 466);
		fontDialog.setLocationRelativeTo(frame);
		fontDialog.setResizable(false);
		fontDialog.setLayout(new FlowLayout());

		JPanel fontPanel = new JPanel();
		fontPanel.setPreferredSize(new Dimension(175, 182));
		fontPanel.add(new JLabel("Font:                                               ", JLabel.LEFT));
		String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		Vector<String> fontsToUse = new Vector<String>();
		for (int i = 0; i < fonts.length; i++) { //only add fonts that can display the full range of letters and numbers.
			Font font = new Font(fonts[i], Font.PLAIN, 12);
			if (font.canDisplayUpTo("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890") == -1)
				fontsToUse.add(fonts[i]);
		}
		JTextField fontField = new JTextField(fontsToUse.get(0), 15);
		fontField.setActionCommand("fontField");
		fontPanel.add(fontField);
		JList fontList = new JList(fontsToUse);
		fontList.setCellRenderer(new FontListRenderer());
		fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fontList.setSelectedIndex(0);
		JScrollPane fontScrollPane = new JScrollPane(fontList);
		fontScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		fontScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		fontScrollPane.setPreferredSize(new Dimension(169, 125));
		fontPanel.add(fontScrollPane);

		JPanel stylePanel = new JPanel();
		stylePanel.setPreferredSize(new Dimension(135, 182));
		stylePanel.add(new JLabel("Font Style:                      ", JLabel.LEFT));
		JTextField styleField = new JTextField("Plain", 11);
		styleField.setActionCommand("styleField");
		stylePanel.add(styleField);
		String[] styles = {"Plain", "Bold", "Italic", "Bold Italic"};
		JList styleList = new JList(styles);
		styleList.setCellRenderer(new StyleListRenderer());
		styleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		styleList.setSelectedIndex(0);
		JScrollPane styleScrollPane = new JScrollPane(styleList);
		styleScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		styleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		styleScrollPane.setPreferredSize(new Dimension(125, 125));
		stylePanel.add(styleScrollPane);

		JPanel sizePanel = new JPanel();
		sizePanel.setPreferredSize(new Dimension(70, 182));
		sizePanel.add(new JLabel("Size:           ", JLabel.LEFT));
		JTextField sizeField = new JTextField("12", 5);
		sizeField.setActionCommand("sizeField");
		sizePanel.add(sizeField);
		int[] sizes = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72};
		String[] sizeNames = {"8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"};
		JList sizeList = new JList(sizeNames);
		sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sizeList.setSelectedIndex(4);
		JScrollPane sizeScrollPane = new JScrollPane(sizeList);
		sizeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sizeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		sizeScrollPane.setPreferredSize(new Dimension(60, 125));
		sizePanel.add(sizeScrollPane);

		JPanel samplePanel = new JPanel();
		samplePanel.setPreferredSize(new Dimension(205, 70));
		samplePanel.add(new JLabel("Sample                                                 ", JLabel.LEFT));
		JLabel sampleLabel = new JLabel("AaBbYyZz");
		samplePanel.add(sampleLabel);
		samplePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		JButton okButton = new JButton("Ok");
		JButton cancelButton = new JButton("Cancel");

		fontDialog.add(fontPanel);
		fontDialog.add(stylePanel);
		fontDialog.add(sizePanel);
		fontDialog.add(samplePanel);
		fontDialog.add(okButton);
		fontDialog.add(cancelButton);
		//Ok so here I have a problem, the lsit and the TextField can't mutate each other while having a socument listener so I use an ActionListener and you have to press enter to search
		ActionListener fontActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				switch (ae.getActionCommand()) {
					case "fontField": fontList.setSelectedIndex(fontList.getNextMatch(fontField.getText(), 0, Bias.Forward));
						fontList.ensureIndexIsVisible(fontList.getSelectedIndex());
					break;
					case "styleField": styleList.setSelectedIndex(styleList.getNextMatch(styleField.getText(), 0, Bias.Forward));
						styleList.ensureIndexIsVisible(styleList.getSelectedIndex());
					break;
					case "sizeField": sizeList.setSelectedIndex(sizeList.getNextMatch(sizeField.getText(), 0, Bias.Forward));
						sizeList.ensureIndexIsVisible(sizeList.getSelectedIndex());
					break;
					case "Ok": textArea.setFont(new Font(fontList.getSelectedValue().toString(), styleList.getSelectedIndex(), sizes[sizeList.getSelectedIndex()]));
						fontDialog.setVisible(false);
					break;
					case "Cancel": fontDialog.setVisible(false);
					break;
				}
			}
		};
		fontField.addActionListener(fontActionListener);
		styleField.addActionListener(fontActionListener);
		sizeField.addActionListener(fontActionListener);
		okButton.addActionListener(fontActionListener);
		cancelButton.addActionListener(fontActionListener);

		//list listeners
		fontList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged (ListSelectionEvent le) {
				fontField.setText(fontList.getSelectedValue().toString());
				sampleLabel.setFont(new Font(fontList.getSelectedValue().toString(), styleList.getSelectedIndex(), sizes[sizeList.getSelectedIndex()]));
			}
		});
		styleList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged (ListSelectionEvent le) {
				styleField.setText(styleList.getSelectedValue().toString());
				sampleLabel.setFont(new Font(fontList.getSelectedValue().toString(), styleList.getSelectedIndex(), sizes[sizeList.getSelectedIndex()]));
			}
		});
		sizeList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged (ListSelectionEvent le) {
				sizeField.setText(sizeList.getSelectedValue().toString());
				sampleLabel.setFont(new Font(fontList.getSelectedValue().toString(), styleList.getSelectedIndex(), sizes[sizeList.getSelectedIndex()]));
			}
		});
		fontDialog.getRootPane().setDefaultButton(okButton);
		fontDialog.setVisible(true);
	}
	/*======================
	Status Bar
	======================*/
	private void statusBar() {
			statusPanel = new JPanel();
			statusPanel.add(new JLabel("                                                                "));
			JSeparator seperator = new JSeparator(JSeparator.VERTICAL);
			seperator.setPreferredSize(new Dimension(1, 10));
			statusPanel.add(seperator);
			statusPanel.add(statusLabel);
			statusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			frame.add(statusPanel, BorderLayout.SOUTH);
			frame.setVisible(true);
	}

    public static void main(String args[]) {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            new JNotepad();
	        }
	    });
	}
}