package passwordmanager;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

import passwordmanager.core.Cipher.WrongPasswordException;
import passwordmanager.core.Storage;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.awt.event.ActionEvent;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PasswordManager {
	private static final String DATABASE_FILE = "PasswordDatabase.bin";
	private JFrame frame;
	private JList<String> list;
	private Storage storage;
	private int selection = -1;
	private Document textAreaDocument;
	private DataEditListener documentListener = null;
	
	class DataEditListener implements DocumentListener {
		@Override
		public void removeUpdate(DocumentEvent e) {
			updateStorage(e);
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			updateStorage(e);
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			updateStorage(e);
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PasswordManager window = new PasswordManager();
					window.frame.setVisible(true);
					
					boolean newDatabase = false;
					if (!Files.exists(Paths.get(DATABASE_FILE)))
						newDatabase = true;
					
					window.openStorage(newDatabase);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PasswordManager() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (!storage.modified() || JOptionPane.showConfirmDialog(frame,
						"There is unsaved data. Are you sure you want to exit?",
						"Close without saving?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
						== JOptionPane.YES_OPTION){
		            System.exit(0);
		        }
			}
		});
		frame.setBounds(500, 200, 450, 300);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		list = new JList<String>();
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setModel(new DefaultListModel<String>());
		JScrollPane listScrollPane = new JScrollPane(list);
		
		JTextArea textArea = new JTextArea();
		textArea.setEnabled(false);
		textAreaDocument = textArea.getDocument();
		JScrollPane textAreaScrollPane = new JScrollPane(textArea);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, textAreaScrollPane);
		frame.getContentPane().add(splitPane);
		
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					selection = list.getSelectedIndex();
					String data = storage.getEntryData(selection);
					textArea.setEditable(false);
					if (documentListener == null) {
						documentListener = new DataEditListener();
					} else {
						textAreaDocument.removeDocumentListener(documentListener);
					}
					textArea.setText(data);
					textArea.setEnabled(true);
					textArea.setEditable(true);
					textAreaDocument.addDocumentListener(documentListener);
				}
			}
		});
		
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		frame.getContentPane().add(panel, BorderLayout.SOUTH);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UserInputDialog dialog = new UserInputDialog();
				if (!dialog.inputEntered())
					return;
				String entryName = dialog.getInput();
				storage.addEntry(entryName);
				
				DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
				model.addElement(entryName);
			}
		});
		btnAdd.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.add(btnAdd);
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = new File(DATABASE_FILE);
				FileOutputStream fileOutputStream;
				try {
					fileOutputStream = new FileOutputStream(file);
					storage.save(fileOutputStream);
					fileOutputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnSave.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.add(btnSave);
	}
	
	private void openStorage(boolean newDatabase) {
		PasswordEnterDialog dialog = new PasswordEnterDialog(newDatabase ? "Enter new database passord:"
				: "Enter passord:");
		if (!dialog.passwordEntered()) {
			frame.dispose();
			return;
		}
		
		Storage storage = null;
		try {
			if (newDatabase) {
				storage = new Storage(null, dialog.getPassword());
			} else {
				do {
					InputStream input = new FileInputStream(DATABASE_FILE);
					try {
						storage = new Storage(input, dialog.getPassword());
					} catch (WrongPasswordException e) {
						dialog = new PasswordEnterDialog("Wrong password. Try again:");
						if (!dialog.passwordEntered()) {
							frame.dispose();
							return;
						}
					} catch (Exception e) {
						throw new RuntimeException("Storage open failed", e);
					} finally {
						input.close();
					}
				} while (storage == null);
			}
		} catch (IOException e) {
			throw new RuntimeException("Storage open failed", e);
		}
		this.storage = storage;
		
		DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
		for (String entryName : storage.getEntryNames())
			model.addElement(entryName);
	}
	
	private void updateStorage(DocumentEvent documentEvent) {
		Document document = documentEvent.getDocument();
		try {
			storage.setEntryData(selection, document.getText(0, document.getLength()));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
}
