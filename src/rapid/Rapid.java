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
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
        JMenuItem menuEntry = new JMenuItem(entryName);
        menuEntry.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFrame fileExplorerFrame = new JFrame();
                    JFileChooser fileExplorer = createFileExplorer(fileExplorerFrame, captureScreenshot);
                    fileExplorer.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileExplorer.setFileFilter(new FileNameExtensionFilter("Text Files Only", "txt"));
                    fileExplorer.setDialogTitle("Choose a file to save to");
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

    private void saveDataToFile(File destinationFile, int confirmOverwrite) {
        // Get HTTP Request / Response data
        IHttpRequestResponse[] httpRequestResponse = invocation.getSelectedMessages();
        ArrayList<String> filenames = new ArrayList<>(2);

        if (confirmOverwrite == 0) {
            if (destinationFile.getAbsoluteFile().getName().contains(httpRequestFilenameSuffix)) {
                setFilenames(destinationFile.getPath(), filenames, 19);
            } else if (destinationFile.getAbsoluteFile().getName().contains(httpResponseFilenameSuffix)) {
                setFilenames(destinationFile.getPath(), filenames, 20);
            } else if (destinationFile.getAbsoluteFile().getName().contains(".txt")) {
                setFilenames(destinationFile.getPath(), filenames, 4);
            }

        } else {
            filenames.add(destinationFile.getPath().concat(httpRequestFilenameSuffix));
            filenames.add(destinationFile.getPath().concat(httpResponseFilenameSuffix));
        }

        // Write files on disk
        for (int i = 0; i < 2; i++) {
            File outfile = new File(filenames.get(i));
            try {
                PrintWriter outfileWriter = new PrintWriter(outfile);
                if (i == 0) {
                    outfileWriter.write(callbacks.getHelpers().bytesToString(httpRequestResponse[0].getRequest()));
                    outfileWriter.close();
                } else {
                    outfileWriter.write(callbacks.getHelpers().bytesToString(httpRequestResponse[0].getResponse()));
                    outfileWriter.close();
                }
            } catch (FileNotFoundException e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    private void setFilenames(String filePath, ArrayList<String> filenames, int index) {
        final String substring = filePath.substring(0, filePath.length() - index);
        filenames.add(substring.concat(httpRequestFilenameSuffix));
        filenames.add(substring.concat(httpResponseFilenameSuffix));
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