package copybar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;

public class FileCopier extends JPanel implements PropertyChangeListener, ActionListener {

    private JProgressBar progressBar;
    private JButton startButton;
    private JButton cancelButton;

    private JFileChooser from;
    private JFileChooser to;

    private CopyProcess copyProcess;

    public FileCopier() {
        super(new BorderLayout());

        startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.addActionListener(this);

        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);

        progressBar = new JProgressBar(1);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        from = new JFileChooser();
        from.setSelectedFile(new File("/home/bodor/Letöltések/Passengers.2016.BDRiP.x264.HuN-HyperX/passengers-sd-hyperx.mkv"));
        from.setApproveButtonText("Choose source");
        from.addChoosableFileFilter(new FileFilter());

        to = new JFileChooser();
        to.setApproveButtonText("Choose destination");
        to.addChoosableFileFilter(new FileFilter());

        add(startButton, BorderLayout.PAGE_START);
        add(cancelButton, BorderLayout.PAGE_END);
        add(progressBar, BorderLayout.CENTER);
        add(from, BorderLayout.WEST);
        add(to, BorderLayout.EAST);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    }

    public static void main(String[] args) {
        FileCopier fileCopier = new FileCopier();

        javax.swing.SwingUtilities.invokeLater(fileCopier::createAndShowGUI);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if ("start".equals(actionEvent.getActionCommand())) {

            startButton.setEnabled(false);
            cancelButton.setEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            copyProcess = new CopyProcess();
            copyProcess.addPropertyChangeListener(this);
            copyProcess.execute();
        }
        if ("cancel".equals(actionEvent.getActionCommand())) {
            copyProcess.cancel(true);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            int progress = (int) evt.getNewValue();
            progressBar.setValue(progress);
        }
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("FileCopier");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JComponent newContentPane = new FileCopier();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        frame.pack();
        frame.setVisible(true);
    }

    class FileFilter extends javax.swing.filechooser.FileFilter {
        @Override
        public boolean accept(File file) {
            return file.isFile();
        }

        @Override
        public String getDescription() {
            return "Choose a file!";
        }
    }

    class CopyProcess extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
            File sourceFile = from.getSelectedFile();
            File destinationFile = to.getSelectedFile();
            InputStream inputStream = null;
            OutputStream outputStream = null;

            setProgress(0);

            try {
                if (!destinationFile.exists()) destinationFile.createNewFile();
                if (!sourceFile.exists()) {
                    JOptionPane.showMessageDialog(null, "There is no source file like that");
                }

                long fileLength = sourceFile.length();
                long finishedBytes = 0;

                inputStream = new FileInputStream(sourceFile);
                outputStream = new FileOutputStream(destinationFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {

                    if (isCancelled()) {
                        destinationFile.delete();
                        setProgress(0);
                        return null;
                    }
                    outputStream.write(buffer, 0, length);
                    finishedBytes += length;
                    setProgress((int) ((double) finishedBytes / fileLength * 100));
                }
            } finally {
                try {
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    //mit lehet tenni
                }

                return null;
            }
        }

        @Override
        public void done() {
            System.out.println("done");
            Toolkit.getDefaultToolkit().beep();
            startButton.setEnabled(true);
            cancelButton.setEnabled(false);
            setCursor(null);
        }

    }
}