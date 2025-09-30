import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utilities for merging interval lists.
 *
 * Problem: Merge two sorted interval lists.
 * Assumptions for mergeTwoSortedLists:
 * - Each input list is individually sorted by interval start ascending.
 * - Intervals within each list are pairwise disjoint (non-overlapping and non-touching is fine; touching will be merged across lists).
 * The result is the union of all intervals across both lists, coalesced.
 *
 * If your lists might contain overlaps within themselves or are unsorted, use mergeTwoArbitraryLists instead.
 */
public final class MergeIntervals {

    /** Immutable interval [start, end]. */
    public static final class Interval {
        public final int start;
        public final int end;

        public Interval(int start, int end) {
            if (end < start) {
                throw new IllegalArgumentException("Interval end must be >= start: [" + start + ", " + end + "]");
            }
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "[" + start + ", " + end + "]";
        }
    }

    /**
     * Merges two sorted, individually-disjoint interval lists into a coalesced union.
     * Time: O(n + m). Space: O(n + m) for the output.
     */
    public static List<Interval> mergeTwoSortedLists(List<Interval> listA, List<Interval> listB) {
        if (listA == null || listB == null) {
            throw new IllegalArgumentException("Inputs must not be null");
        }

        List<Interval> result = new ArrayList<>(listA.size() + listB.size());

        int indexA = 0;
        int indexB = 0;
        Interval pending = null; // The interval being built/extended

        while (indexA < listA.size() || indexB < listB.size()) {
            Interval next;
            if (indexB >= listB.size() || (indexA < listA.size() && listA.get(indexA).start <= listB.get(indexB).start)) {
                next = listA.get(indexA++);
            } else {
                next = listB.get(indexB++);
            }

            if (pending == null) {
                pending = next;
                continue;
            }

            if (next.start <= pending.end) { // overlap or touch -> merge
                pending = new Interval(pending.start, Math.max(pending.end, next.end));
            } else { // disjoint -> flush pending
                result.add(pending);
                pending = next;
            }
        }

        if (pending != null) {
            result.add(pending);
        }

        return result;
    }

    /**
     * Convenience: merge two arbitrary interval lists (unsorted and may overlap internally).
     * Implementation: concatenate, sort by start, then coalesce.
     * Time: O((n + m) log(n + m)).
     */
    public static List<Interval> mergeTwoArbitraryLists(List<Interval> listA, List<Interval> listB) {
        if (listA == null || listB == null) {
            throw new IllegalArgumentException("Inputs must not be null");
        }

        List<Interval> all = new ArrayList<>(listA.size() + listB.size());
        all.addAll(listA);
        all.addAll(listB);
        if (all.isEmpty()) {
            return Collections.emptyList();
        }

        all.sort(Comparator.comparingInt(i -> i.start));

        List<Interval> result = new ArrayList<>(all.size());
        Interval pending = all.get(0);
        for (int i = 1; i < all.size(); i++) {
            Interval current = all.get(i);
            if (current.start <= pending.end) {
                pending = new Interval(pending.start, Math.max(pending.end, current.end));
            } else {
                result.add(pending);
                pending = current;
            }
        }
        result.add(pending);
        return result;
    }

    // Simple demo
    public static void main(String[] args) {
        List<Interval> a = Arrays.asList(
                new Interval(1, 2),
                new Interval(5, 7),
                new Interval(10, 13)
        );
        List<Interval> b = Arrays.asList(
                new Interval(3, 6),
                new Interval(8, 9),
                new Interval(13, 15)
        );

        List<Interval> merged = mergeTwoSortedLists(a, b);
        System.out.println("Merged (sorted inputs): " + merged);

        List<Interval> mergedArbitrary = mergeTwoArbitraryLists(a, b);
        System.out.println("Merged (arbitrary inputs): " + mergedArbitrary);
    }
}


