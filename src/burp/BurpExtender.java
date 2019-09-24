package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

public class BurpExtender implements IBurpExtender, IContextMenuFactory, IContextMenuInvocation {

	private IBurpExtenderCallbacks callbacks;
	@SuppressWarnings("unused")
	private IExtensionHelpers helpers;

	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		this.callbacks = callbacks;
		this.helpers = callbacks.getHelpers();

		this.callbacks.setExtensionName("Rapid");
		this.callbacks.registerContextMenuFactory(this);
	}

	@Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
		// Create menu
		List<JMenuItem> menu = new ArrayList<JMenuItem> ();

		// Create menu entry
		JMenuItem rapidMenuEntry = new JMenuItem("Rapid - Save HTTP Request/Response");

		// Listen for clicks on Rapid's menu entry
		rapidMenuEntry.addActionListener(new ActionListener() {
			// Get HTTP Request / Response data
			IHttpRequestResponse[] httpRequestResponse = invocation.getSelectedMessages();

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFrame fileExplorerFrame = new JFrame();
					JFileChooser fileExplorer = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory()) {
						private static final long serialVersionUID = 1L;
						@Override
						public void approveSelection() {
							File destinationFile = getSelectedFile();
							if (destinationFile.exists()) {
								int confirmOverwrite = JOptionPane.showConfirmDialog(null, "This file already exists. Are you sure?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

								// Overwrite existing file
								if (confirmOverwrite == JOptionPane.YES_OPTION) {
									writeOutputFiles(destinationFile, confirmOverwrite);
									// Close fileExplorer
									fileExplorerFrame.dispose();
								}
							} else {
								// Write to new file
								writeOutputFiles(destinationFile, 1);
								// Close fileExplorer
								fileExplorerFrame.dispose();
							}
						}

					};

					// Set properties
					fileExplorer.setFileSelectionMode(JFileChooser.FILES_ONLY);

					// Restrict files only to text
					FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files Only", "txt");

					// Apply filter
					fileExplorer.setFileFilter(filter);

					// Set custom title
					fileExplorer.setDialogTitle("Choose a file to save to");

					// Add fileExplorer to frame
					fileExplorerFrame.add(fileExplorer);

					// Show dialog
					fileExplorer.showSaveDialog(fileExplorerFrame);

				} catch (Exception e1) {
					// Couldn't launch 
					throw new RuntimeException(e1);
				}
			}

			public void writeOutputFiles(File destinationFile, int confirm) {
				// HTTP Request and Response filenames
				ArrayList<String> filenames = new ArrayList<>(2);

				// Overwriting existing files
				if (confirm == 0) {
					// Check filename
					if (destinationFile.getAbsoluteFile().getName().contains("-[HTTP-Request].txt")) {
						// HTTP Request file
						setFilenames(destinationFile, filenames, 19);
					} else if (destinationFile.getAbsoluteFile().getName().contains("-[HTTP-Response].txt")) {
						// HTTP Response file
						setFilenames(destinationFile, filenames, 20);
					} else if (destinationFile.getAbsoluteFile().getName().contains(".txt")) {
						// Plain .txt file
						setFilenames(destinationFile, filenames, 4);
					}

					// Writing to new files
				} else {
					filenames.add(destinationFile.getPath().concat("-[HTTP-Request].txt"));
					filenames.add(destinationFile.getPath().concat("-[HTTP-Response].txt"));
				}

				for (int i = 0; i<2; i++) {
					// Create file
					File request = new File(filenames.get(i));
					try {
						PrintWriter outfileWriter = new PrintWriter(request);
						if (i == 0) {
							// Write HTTP Request to file
							outfileWriter.write(callbacks.getHelpers().bytesToString(httpRequestResponse[0].getRequest()));
							outfileWriter.close();
						} else {
							// Write HTTP Response to file
							outfileWriter.write(callbacks.getHelpers().bytesToString(httpRequestResponse[0].getResponse()));
							outfileWriter.close();
						}

					} catch (FileNotFoundException e2) {
						// File not found
						throw new RuntimeException(e2);
					}
				}
			}

			// Rename files based on which one was selected. HTTP Request? HTTP Response? Simple .txt?
			public void setFilenames(File destinationFile, ArrayList<String> filenames, int index) {
				// Partly trim the filename
				// index indicates the level of trim based on which file was selected for overwriting
				filenames.add(destinationFile.getPath().substring(0, destinationFile.getPath().length() - index).concat("-[HTTP-Request].txt"));
				filenames.add(destinationFile.getPath().substring(0, destinationFile.getPath().length() - index).concat("-[HTTP-Response].txt"));
			}

		});

		// Add entry to menu
		menu.add(rapidMenuEntry);

		return menu;
	}

	@Override
	public int getToolFlag() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte getInvocationContext() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getSelectionBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IHttpRequestResponse[] getSelectedMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputEvent getInputEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IScanIssue[] getSelectedIssues() {
		// TODO Auto-generated method stub
		return null;
	}
}