import java.time.LocalDateTime;
import java.util.Objects;

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


