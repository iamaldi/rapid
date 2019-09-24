package rapid;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IHttpRequestResponse;;

public class Rapid {

	private IBurpExtenderCallbacks callbacks;
	private IContextMenuInvocation invocation;

	private String httpRequestFilenameSuffix = "-[HTTP-Request].txt";
	private String httpResponseFilenameSuffix = "-[HTTP-Response].txt";

	public Rapid(IBurpExtenderCallbacks callbacks, IContextMenuInvocation invocation) {
		this.callbacks = callbacks;
		this.invocation = invocation;
	}

	public JMenuItem getMenuEntry(String entryName, boolean captureScreenshot) {
		// Create menu entry
		JMenuItem menuEntry = new JMenuItem(entryName);

		// Listen for clicks on the menu entry
		menuEntry.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFrame fileExplorerFrame = new JFrame();

					// Create file explorer
					JFileChooser fileExplorer = createFileExplorer(fileExplorerFrame, captureScreenshot);

					// Show files only
					fileExplorer.setFileSelectionMode(JFileChooser.FILES_ONLY);

					// Restrict to only text files
					fileExplorer.setFileFilter(new FileNameExtensionFilter("Text Files Only", "txt"));

					// Set window title
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

		});

		return menuEntry;
	}

	private JFileChooser createFileExplorer(JFrame fileExplorerFrame, boolean captureScreenshot) {

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
						if (captureScreenshot) {
							try {
								// Capture screenshot
								captureScreenshot(destinationFile);
							} catch (AWTException e) {
								throw new RuntimeException(e);
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
						}
					}
				} else {
					// Write to new file
					writeOutputFiles(destinationFile, 1);

					// Close fileExplorer
					fileExplorerFrame.dispose();

					// Check if we have to capture a screenshot
					if (captureScreenshot) {
						try {
							// Capture screenshot
							captureScreenshot(destinationFile);
						} catch (AWTException e) {
							throw new RuntimeException(e);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		};

		return fileExplorer;
	}

	private void writeOutputFiles(File destinationFile, int confirmOverwrite) {
		// Get HTTP Request / Response data
		IHttpRequestResponse[] httpRequestResponse = invocation.getSelectedMessages();

		// HTTP Request and Response filenames
		ArrayList<String> filenames = new ArrayList<>(2);

		// Overwriting existing files
		if (confirmOverwrite == 0) {
			// Check filename
			if (destinationFile.getAbsoluteFile().getName().contains(httpRequestFilenameSuffix)) {
				// HTTP Request file
				setFilenames(destinationFile.getPath(), filenames, 19);
			} else if (destinationFile.getAbsoluteFile().getName().contains(httpResponseFilenameSuffix)) {
				// HTTP Response file
				setFilenames(destinationFile.getPath(), filenames, 20);
			} else if (destinationFile.getAbsoluteFile().getName().contains(".txt")) {
				// Plain .txt file
				setFilenames(destinationFile.getPath(), filenames, 4);
			}

			// Writing to new files
		} else {
			filenames.add(destinationFile.getPath().concat(httpRequestFilenameSuffix));
			filenames.add(destinationFile.getPath().concat(httpResponseFilenameSuffix));
		}

		for (int i = 0; i<2; i++) {
			// Create file
			File outfile = new File(filenames.get(i));
			try {
				PrintWriter outfileWriter = new PrintWriter(outfile);
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
	private void setFilenames(String filePath, ArrayList<String> filenames, int index) {
		// Trim & alter filename suffix
		// index indicates the level of trim based on which file was selected for overwriting
		filenames.add(filePath.substring(0, filePath.length() - index).concat(httpRequestFilenameSuffix));
		filenames.add(filePath.substring(0, filePath.length() - index).concat(httpResponseFilenameSuffix));
	}

	private void captureScreenshot(File destinationFile) throws AWTException,
		InterruptedException {
			// Delay the capture of the screenshot
			try {
				// Delay 1s for fileExplorerFrame to dispose
				TimeUnit.SECONDS.sleep(1);

				// Get screen
				Rectangle screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

				// Capture screen
				BufferedImage capture = new Robot().createScreenCapture(screen);

				// Save capture to file
				ImageIO.write(capture, "PNG", new File(destinationFile.getPath().concat("-[IMG].png")));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
}