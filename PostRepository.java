import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class PostRepository {
    private final int totalPosts;

    PostRepository(int totalPosts) {
        this.totalPosts = Math.max(0, totalPosts);
    }

    public List<Post> fetchPage(int pageIndex, int pageSize) {
        if (pageSize <= 0 || pageIndex < 0) {
            return List.of();
        }
        int start = pageIndex * pageSize;
        if (start >= totalPosts) {
            return List.of();
        }
        int endExclusive = Math.min(start + pageSize, totalPosts);
        List<Post> out = new ArrayList<>(endExclusive - start);
        for (int i = start; i < endExclusive; i++) {
            out.add(generatePost(i));
        }
        return out;
    }

    private Post generatePost(int index) {
        String author = "user" + index;
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(index);
        int mod = index % 6;
        switch (mod) {
            case 0:
                return Post.text(author, createdAt, "Text post #" + index + " — welcome to the feed");
            case 1:
            case 2: {
                String img = "https://picsum.photos/id/" + (100 + (index % 100)) + "/800/500";
                return Post.photo(author, createdAt, img, "Photo caption for post #" + index);
            }
            case 3:
            case 4: {
                String video = (index % 2 == 0)
                        ? "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                        : "https://vimeo.com/76979871";
                return Post.video(author, createdAt, video, "Video caption for post #" + index);
            }
            default:
                return Post.text(author, createdAt, "Another text post #" + index);
        }
    }
}


