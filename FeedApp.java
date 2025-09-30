import javax.swing.SwingUtilities;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Switch to repository-backed infinite scroll for large datasets
            PostRepository repository = new PostRepository(5000);
            FeedFrame frame = new FeedFrame(repository);
            frame.setVisible(true);
        });
    }

    private static List<Post> samplePosts() {
        List<Post> posts = new ArrayList<>();
        posts.add(Post.text("alice", LocalDateTime.now().minusMinutes(5),
                "Hello world! This is a simple text post showcasing line wrapping and layout in our Java Swing feed."));

        posts.add(Post.photo("bob", LocalDateTime.now().minusMinutes(10),
                "https://picsum.photos/id/1025/600/400",
                "Enjoying a day outdoors with this cute dog!"));

        posts.add(Post.video("carol", LocalDateTime.now().minusMinutes(15),
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                "My favorite music video – hit play!"));

        posts.add(Post.photo("dave", LocalDateTime.now().minusMinutes(25),
                "https://picsum.photos/id/1003/800/600",
                "A beautiful mountain view at sunset."));

        posts.add(Post.text("eve", LocalDateTime.now().minusMinutes(30),
                "Short text only post."));

        posts.add(Post.video("frank", LocalDateTime.now().minusMinutes(35),
                "https://vimeo.com/76979871",
                "Incredible time-lapse video. Opens in your browser."));

        return posts;
    }
}

/*
enum PostType {
    TEXT,
    PHOTO,
    VIDEO
}

class Post {
    private final String author;
    private final LocalDateTime createdAt;
    private final PostType type;
    private final String contentText;
    private final String mediaUrl;

    private Post(String author, LocalDateTime createdAt, PostType type, String mediaUrl, String contentText) {
        this.author = Objects.requireNonNull(author, "author");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.type = Objects.requireNonNull(type, "type");
        this.mediaUrl = mediaUrl; // Nullable for TEXT
        this.contentText = contentText; // Nullable
    }

    public static Post text(String author, LocalDateTime createdAt, String text) {
        return new Post(author, createdAt, PostType.TEXT, null, text);
    }

    public static Post photo(String author, LocalDateTime createdAt, String imageUrl, String caption) {
        return new Post(author, createdAt, PostType.PHOTO, imageUrl, caption);
    }

    public static Post video(String author, LocalDateTime createdAt, String videoUrl, String caption) {
        return new Post(author, createdAt, PostType.VIDEO, videoUrl, caption);
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public PostType getType() {
        return type;
    }

    public String getContentText() {
        return contentText;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }
}

class FeedFrame extends JFrame {
    private final JPanel feedPanel;
    private final ImageCache imageCache = new ImageCache(24);
    private final ExecutorService executor = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

    FeedFrame(List<Post> posts) {
        super("Feed");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(700, 900);
        setLocationRelativeTo(null);

        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        feedPanel.setBackground(new Color(250, 250, 250));

        JScrollPane scrollPane = new JScrollPane(feedPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(scrollPane);

        setPosts(posts);
    }

    void setPosts(List<Post> posts) {
        feedPanel.removeAll();
        List<Post> copy = new ArrayList<>(posts);
        Collections.sort(copy, (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        for (Post post : copy) {
            feedPanel.add(new PostPanel(post, imageCache, executor));
            feedPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        }
        feedPanel.revalidate();
        feedPanel.repaint();
    }
}

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
        JLabel author = new JLabel(post.getAuthor());
        author.setFont(author.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(author, BorderLayout.WEST);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        JLabel when = new JLabel(post.getCreatedAt().format(fmt));
        when.setForeground(META_COLOR);
        panel.add(when, BorderLayout.EAST);
        return panel;
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
                    ImageIcon icon = new ImageIcon(img);
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

class ImageCache {
    private final int capacity;
    private final Map<String, ImageIcon> urlToIcon;

    ImageCache(int capacity) {
        this.capacity = Math.max(1, capacity);
        this.urlToIcon = new HashMap<>();
    }

    public synchronized ImageIcon get(String url) {
        return urlToIcon.get(url);
    }

    public synchronized void put(String url, ImageIcon icon) {
        if (urlToIcon.size() >= capacity) {
            String firstKey = urlToIcon.keySet().iterator().next();
            urlToIcon.remove(firstKey);
        }
        urlToIcon.put(url, icon);
    }
}

*/
