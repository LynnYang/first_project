import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class FeedFrame extends JFrame {
    private final JPanel feedPanel;
    private final ImageCache imageCache = new ImageCache(96);
    private final ExecutorService executor = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
    private final PostRepository repository;
    private final int pageSize = 15;
    private int nextPageIndex = 0;
    private boolean isLoading = false;
    private boolean noMorePages = false;
    private int retainedPostLimit = 150; // cap components kept in memory

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

        this.repository = null; // legacy path
    }

    FeedFrame(PostRepository repository) {
        super("Feed");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(700, 900);
        setLocationRelativeTo(null);

        this.repository = repository;

        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        feedPanel.setBackground(new Color(250, 250, 250));

        JScrollPane scrollPane = new JScrollPane(feedPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(scrollPane);

        // Initial page
        loadNextPage();

        // Infinite scroll trigger
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (shouldLoadMore(scrollPane)) {
                    loadNextPage();
                }
            }
        });
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

    private boolean shouldLoadMore(JScrollPane scrollPane) {
        if (isLoading || noMorePages || repository == null) {
            return false;
        }
        JScrollBar bar = scrollPane.getVerticalScrollBar();
        int pos = bar.getValue() + bar.getVisibleAmount();
        int max = bar.getMaximum();
        // If within 15% of the bottom, fetch more
        return pos >= (int) (max * 0.85);
    }

    private void loadNextPage() {
        if (repository == null || isLoading || noMorePages) {
            return;
        }
        isLoading = true;
        List<Post> page = repository.fetchPage(nextPageIndex, pageSize);
        if (page.isEmpty()) {
            noMorePages = true;
            isLoading = false;
            return;
        }
        nextPageIndex++;
        for (Post post : page) {
            feedPanel.add(new PostPanel(post, imageCache, executor));
            feedPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        }
        pruneIfNeeded();
        feedPanel.revalidate();
        feedPanel.repaint();
        isLoading = false;
    }

    private void pruneIfNeeded() {
        int count = feedPanel.getComponentCount();
        if (count <= retainedPostLimit * 2) {
            return;
        }
        // Each post adds a panel and a rigid area, so remove in pairs from the top
        int toRemovePairs = (count / 2) - retainedPostLimit;
        int componentsToRemove = toRemovePairs * 2;
        if (componentsToRemove <= 0) {
            return;
        }
        // Remove from index 0 repeatedly
        for (int i = 0; i < componentsToRemove && feedPanel.getComponentCount() > 0; i++) {
            feedPanel.remove(0);
        }
    }
}


