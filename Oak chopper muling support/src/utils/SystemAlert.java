package utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class SystemAlert
extends JDialog {
    private static final long serialVersionUID = 1;
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("E, dd MMM yyyy - HH:mm:ss");
    private JLabel iconLabel;
    private JLabel titleLabel;
    private JTextArea contentTextArea;
    private JLabel timestampLabel;
    private JPanel containerPanel;

    public SystemAlert(int width, int height) {
        super.setSize(width, height);
        super.setAlwaysOnTop(true);
        super.setUndecorated(true);
        this.configureBoundary();
        this.configureComponents();
        this.addComponents();
        this.addCloseListener();
    }

    public SystemAlert(int width, int height, ImageIcon icon, String title, String message, Date timestamp) {
        this(width, height);
        this.setAlert(icon, title, message, timestamp);
    }

    public SystemAlert(int width, int height, Image image, String title, String message, Date timestamp) {
        this(width, height);
        this.setAlert(image, title, message, timestamp);
    }

    private void configureComponents() {
        this.iconLabel = new JLabel();
        this.titleLabel = new JLabel("Title");
        this.contentTextArea = new JTextArea("Content", 2, 50);
        this.contentTextArea.setEditable(false);
        this.contentTextArea.setWrapStyleWord(true);
        this.contentTextArea.setLineWrap(true);
        this.contentTextArea.setFocusable(false);
        this.timestampLabel = new JLabel("Thu, 16 July 2015 - 21:24:11", 4);
        this.containerPanel = new JPanel(new GridBagLayout());
        this.setFont(new Font("Calibri", 0, 12));
        this.setForeground(Color.decode("#fdfdfd"));
        this.setBackground(Color.decode("#262626"));
        this.setSelectedTextColor(Color.decode("#fd2112"));
    }

    private void configureBoundary() {
        int x = 0;
        int y = 0;
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration());
        x += SystemAlert.SCREEN_SIZE.width;
        y += SystemAlert.SCREEN_SIZE.height;
        y -= this.getHeight();
        this.setLocation(x -= this.getWidth(), y -= screenInsets.bottom);
    }

    private void addComponents() {
        this.containerPanel.add((Component)this.iconLabel, new GridBagConstraints(0, 0, 1, 3, 0.0, 1.0, 17, 3, new Insets(5, 15, 5, 15), 0, 0));
        this.containerPanel.add((Component)this.titleLabel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, 11, 2, new Insets(5, 0, 0, 5), 0, 0));
        this.containerPanel.add((Component)this.contentTextArea, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, 11, 1, new Insets(0, 0, 0, 5), 0, 0));
        this.containerPanel.add((Component)this.timestampLabel, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, 11, 2, new Insets(5, 0, 5, 5), 0, 0));
        this.add((Component)this.containerPanel, "Center");
    }

    private void addCloseListener() {
        this.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                SystemAlert.this.dispose();
            }
        });
    }

    @Override
    public synchronized void addMouseListener(MouseListener l) {
        super.addMouseListener(l);
        if (this.iconLabel != null) {
            this.iconLabel.addMouseListener(l);
        }
        if (this.titleLabel != null) {
            this.titleLabel.addMouseListener(l);
        }
        if (this.contentTextArea != null) {
            this.contentTextArea.addMouseListener(l);
        }
        if (this.timestampLabel != null) {
            this.timestampLabel.addMouseListener(l);
        }
        if (this.containerPanel != null) {
            this.containerPanel.addMouseListener(l);
        }
    }

    @Override
    public void setFont(Font f) {
        super.setFont(f);
        if (this.titleLabel != null) {
            this.titleLabel.setFont(f.deriveFont(1, 19.0f));
        }
        if (this.contentTextArea != null) {
            this.contentTextArea.setFont(f.deriveFont(0, 13.0f));
        }
        if (this.timestampLabel != null) {
            this.timestampLabel.setFont(f.deriveFont(2, 11.0f));
        }
    }

    @Override
    public void setForeground(Color c) {
        super.setForeground(c);
        if (this.titleLabel != null) {
            this.titleLabel.setForeground(c);
        }
        if (this.contentTextArea != null) {
            this.contentTextArea.setForeground(c);
        }
        if (this.timestampLabel != null) {
            this.timestampLabel.setForeground(c);
        }
    }

    @Override
    public void setBackground(Color bgColor) {
        super.setBackground(bgColor);
        if (this.contentTextArea != null) {
            this.contentTextArea.setBackground(bgColor);
            this.contentTextArea.setSelectionColor(bgColor);
        }
        if (this.containerPanel != null) {
            this.containerPanel.setBackground(bgColor);
        }
    }

    public void setSelectedTextColor(Color c) {
        this.contentTextArea.setSelectedTextColor(c);
    }

    public void setAlert(ImageIcon icon, String title, String message, Date timestamp) {
        this.iconLabel.setIcon(icon);
        this.titleLabel.setText(title);
        this.contentTextArea.setText(message);
        this.timestampLabel.setText(DATE_FORMAT.format(timestamp));
    }

    public void setAlert(Image image, String title, String message, Date timestamp) {
        double height = this.getHeight();
        double imgWidth = image.getWidth(this);
        double newImgWidth = 0.0;
        double newImgHeight = 0.0;
        newImgHeight = (int)(height / 2.0);
        newImgWidth = Math.min(newImgHeight, imgWidth) / Math.max(newImgHeight, imgWidth);
        ImageIcon icon = new ImageIcon(image.getScaledInstance((int)(newImgWidth *= imgWidth), (int)newImgHeight, 1));
        this.setAlert(icon, title, message, timestamp);
    }

}

