import javax.swing.ImageIcon;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;

class ImageCache {
    private final int capacity;
    private final LinkedHashMap<String, SoftReference<ImageIcon>> lruMap;

    ImageCache(int capacity) {
        this.capacity = Math.max(1, capacity);
        this.lruMap = new LinkedHashMap<String, SoftReference<ImageIcon>>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, SoftReference<ImageIcon>> eldest) {
                return size() > ImageCache.this.capacity;
            }
        };
    }

    public synchronized ImageIcon get(String url) {
        SoftReference<ImageIcon> ref = lruMap.get(url);
        return ref == null ? null : ref.get();
    }

    public synchronized void put(String url, ImageIcon icon) {
        lruMap.put(url, new SoftReference<>(icon));
    }
}


