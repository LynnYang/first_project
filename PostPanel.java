import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

class PostPanel extends JPanel {
    private static final int CONTENT_WIDTH = 600;
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(230, 230, 230);
    private static final Color META_COLOR = new Color(120, 120, 120);

    private final Post post;
    private final ImageCache imageCache;
    private final ExecutorService executor;
    private Future<?> imageTask;

    PostPanel(Post post, ImageCache imageCache, ExecutorService executor) {
        this.post = post;
        this.imageCache = imageCache;
        this.executor = executor;
        setLayout(new BorderLayout());
        setBackground(CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JPanel head = createHeader();
        add(head, BorderLayout.NORTH);

        JComponent center = createCenter();
        if (center != null) {
            add(center, BorderLayout.CENTER);
        }

        JComponent footer = createFooter();
        if (footer != null) {
            add(footer, BorderLayout.SOUTH);
        }
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel author = new JLabel(capitalizeFirst(post.getAuthor()));
        author.setFont(author.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(author, BorderLayout.WEST);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        JLabel when = new JLabel(post.getCreatedAt().format(fmt));
        when.setForeground(META_COLOR);
        panel.add(when, BorderLayout.EAST);
        return panel;
    }

    private static String capitalizeFirst(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        int firstCodePoint = input.codePointAt(0);
        int upperFirst = Character.toTitleCase(firstCodePoint);
        String tail = input.substring(Character.charCount(firstCodePoint));
        return new StringBuilder().appendCodePoint(upperFirst).append(tail).toString();
    }

    private JComponent createCenter() {
        switch (post.getType()) {
            case TEXT:
                return createTextContent();
            case PHOTO:
                return createPhotoContent();
            case VIDEO:
                return createVideoContent();
            default:
                return null;
        }
    }

    private JComponent createFooter() {
        if (post.getType() == PostType.TEXT) {
            return null;
        }
        String text = post.getContentText();
        if (text == null || text.isEmpty()) {
            return null;
        }
        JTextArea area = new JTextArea(text);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setBorder(new EmptyBorder(8, 0, 0, 0));
        area.setFont(area.getFont().deriveFont(14f));
        return area;
    }

    private JComponent createTextContent() {
        JTextArea area = new JTextArea(post.getContentText());
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(area.getFont().deriveFont(16f));
        return area;
    }

    private JComponent createPhotoContent() {
        String url = post.getMediaUrl();
        JLabel placeholder = new JLabel("Loading image...", SwingConstants.CENTER);
        placeholder.setPreferredSize(new Dimension(CONTENT_WIDTH, 300));
        placeholder.setOpaque(true);
        placeholder.setBackground(new Color(245, 245, 245));
        placeholder.setBorder(BorderFactory.createLineBorder(new Color(235, 235, 235)));

        if (url == null || url.isEmpty()) {
            placeholder.setText("Image URL missing");
            return placeholder;
        }

        ImageIcon cached = imageCache.get(url);
        if (cached != null) {
            return new JLabel(scaleToWidth(cached, CONTENT_WIDTH));
        }

        imageTask = executor.submit(() -> {
            try {
                BufferedImage img = ImageIO.read(new URL(url));
                if (img != null) {
                    // Create scaled thumbnail to reduce memory footprint
                    int targetWidth = CONTENT_WIDTH;
                    double scale = (double) targetWidth / (double) img.getWidth();
                    int targetHeight = (int) Math.round(img.getHeight() * scale);
                    Image scaled = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                    ImageIcon icon = new ImageIcon(scaled);
                    imageCache.put(url, icon);
                    SwingUtilities.invokeLater(() -> {
                        placeholder.setText("");
                        placeholder.setIcon(scaleToWidth(icon, CONTENT_WIDTH));
                    });
                } else {
                    SwingUtilities.invokeLater(() -> placeholder.setText("Failed to load image"));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> placeholder.setText("Failed to load image"));
            }
        });

        return placeholder;
    }

    private JComponent createVideoContent() {
        String url = post.getMediaUrl();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JPanel thumb = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(CONTENT_WIDTH, 300);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                int size = Math.min(getWidth(), getHeight()) / 4;
                int[] xPoints = {getWidth() / 2 - size / 2, getWidth() / 2 - size / 2, getWidth() / 2 + size};
                int[] yPoints = {getHeight() / 2 - size, getHeight() / 2 + size, getHeight() / 2};
                g2.setColor(new Color(255, 255, 255, 210));
                g2.fillPolygon(xPoints, yPoints, 3);
                g2.dispose();
            }
        };
        thumb.setOpaque(false);

        JButton play = new JButton(new AbstractAction("Play Video") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (url != null && !url.isEmpty()) {
                    openInBrowser(url);
                }
            }
        });
        play.setFocusPainted(false);
        play.setBackground(new Color(66, 133, 244));
        play.setForeground(Color.WHITE);

        panel.add(thumb, BorderLayout.CENTER);
        panel.add(play, BorderLayout.SOUTH);
        return panel;
    }

    private void openInBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI.create(url));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to open video:", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Desktop browse not supported", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ImageIcon scaleToWidth(ImageIcon icon, int width) {
        if (icon.getIconWidth() <= width) {
            return icon;
        }
        double scale = (double) width / (double) icon.getIconWidth();
        int height = (int) Math.round(icon.getIconHeight() * scale);
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}


