package passwordmanager;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

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

public class PasswordManager {
	private static String DATABASE_FILE = "PasswordDatabase.bin";
	private JFrame frame;
	private JList<String> list;
	private Storage storage;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PasswordManager window = new PasswordManager();
					window.frame.setVisible(true);
					
					InputStream input = null;
					String prompt;
					if (Files.exists(Paths.get(DATABASE_FILE))) {
						prompt = "Enter password:";
						input = new FileInputStream(DATABASE_FILE);
					} else {
						prompt = "Enter new database passord:";
					}
					
					PasswordEnterDialog dialog = new PasswordEnterDialog(prompt);
					if (!dialog.passwordEntered()) {
						window.frame.dispose();
						return;
					}
					
					window.storage = new Storage(input, dialog.getPassword());
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
		frame.setBounds(500, 200, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		list = new JList<String>();
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setModel(new DefaultListModel<String>());
		
		JScrollPane scrollPane = new JScrollPane(list);
		frame.getContentPane().add(scrollPane);
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
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
		
		JButton btnAdd = new JButton("Add");
		btnAdd.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.add(btnAdd);
	}
}
