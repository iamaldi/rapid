package rapid;

import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IHttpRequestResponse;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class Rapid {
    private IBurpExtenderCallbacks callbacks;
    private IContextMenuInvocation invocation;
    private String filenameSuffix = "-[HTTP-Request-Response].txt";

    public Rapid(IBurpExtenderCallbacks callbacks, IContextMenuInvocation invocation) {
        this.callbacks = callbacks;
        this.invocation = invocation;
    }

    public JMenuItem getMenuEntry(String entryName, boolean captureScreenshot) {
        JMenuItem menuEntry = new JMenuItem(entryName);
        menuEntry.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFrame fileExplorerFrame = new JFrame();
                    JFileChooser fileExplorer = createFileExplorer(fileExplorerFrame, captureScreenshot);
                    fileExplorer.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileExplorer.setFileFilter(new FileNameExtensionFilter("Text Files Only", "txt"));
                    fileExplorer.setDialogTitle("Select a file");
                    fileExplorerFrame.add(fileExplorer);
                    fileExplorer.showSaveDialog(fileExplorerFrame);
                } catch (Exception e1) {
                    throw new RuntimeException(e1);
                }
            }
        });
        return menuEntry;
    }

    private JFileChooser createFileExplorer(JFrame fileExplorerFrame, boolean captureScreenshot) {

        return new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory()) {
            private static final long serialVersionUID = 1L;

            @Override
            public void approveSelection() {
                File destinationFile = getSelectedFile();
                if (destinationFile.exists()) {
                    int confirmOverwrite = JOptionPane.showConfirmDialog(null, "This file already exists. Are you sure?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                    // Overwrite existing file
                    if (confirmOverwrite == JOptionPane.YES_OPTION) {
                        saveDataToFile(destinationFile, confirmOverwrite);
                        fileExplorerFrame.dispose();
                        if (captureScreenshot) {
                            try {
                                captureScreenshot(destinationFile);
                            } catch (AWTException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                } else {
                    // Write data to a new file
                    saveDataToFile(destinationFile, 1);
                    fileExplorerFrame.dispose();

                    // Check if we should also capture a screenshot
                    if (captureScreenshot) {
                        try {
                            captureScreenshot(destinationFile);
                        } catch (AWTException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        };
    }

    private void saveDataToFile(File destinationFile, int overwriteFile) {
        // Get HTTP data from interface
        IHttpRequestResponse[] httpData = invocation.getSelectedMessages();
        String destinationFilename = null;
        String dataSeparator = "\n";
        String request = callbacks.getHelpers().bytesToString(httpData[0].getRequest());
        String response = callbacks.getHelpers().bytesToString(httpData[0].getResponse());
        
        if (overwriteFile == 0) {
        	// User wants to overwrite an existing file,
        	// keep that filename instead; don't insert any custom suffix
        	destinationFilename = destinationFile.getPath();
        } else { 
            destinationFilename = destinationFile.getPath().concat(filenameSuffix);
        }
      
        // Write data to a single file
        File outputFile = new File(destinationFilename);
        try {
			PrintWriter outputFileWriter = new PrintWriter(outputFile);
			outputFileWriter.write(request);
			outputFileWriter.write(dataSeparator);
			outputFileWriter.write(response);
			outputFileWriter.close();
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
    }

    private void captureScreenshot(File destinationFile) throws AWTException,
            InterruptedException {
        try {
            TimeUnit.SECONDS.sleep(1);
            Rectangle screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage capture = new Robot().createScreenCapture(screen);
            ImageIO.write(capture, "PNG", new File(destinationFile.getPath().concat("-[IMG].png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}