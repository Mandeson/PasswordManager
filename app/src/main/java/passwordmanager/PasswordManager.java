package passwordmanager;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class PasswordManager {

	private JFrame frame;
	private JList<String> list;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PasswordManager window = new PasswordManager();
					window.frame.setVisible(true);
					PasswordEnterDialog dialog = new PasswordEnterDialog("Enter new database passord:");
					if (!dialog.passwordEntered()) {
						window.frame.dispose();
						return;
					}
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
		
		JButton btnAdd = new JButton("Add");
		btnAdd.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.add(btnAdd);
	}
}
