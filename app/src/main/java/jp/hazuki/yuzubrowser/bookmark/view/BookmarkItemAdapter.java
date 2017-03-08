package jp.hazuki.yuzubrowser.bookmark.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.bookmark.BookmarkItem;
import jp.hazuki.yuzubrowser.bookmark.BookmarkSite;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

/**
 * Created by hazuki on 17/03/01.
 */

public class BookmarkItemAdapter extends ArrayRecyclerAdapter<BookmarkItem, BookmarkItemAdapter.BookmarkFolderHolder> {
    private static final int TYPE_SITE = 1;
    private static final int TYPE_FOLDER = 2;

    private final int normalBackGround;

    private boolean sortMode;

    private boolean multiSelectMode;
    private SparseBooleanArray itemSelected;
    private OnBookmarkItemListener bookmarkItemListener;

    public BookmarkItemAdapter(Context context, List<BookmarkItem> list, OnBookmarkItemListener listener) {
        super(context, list, null);
        itemSelected = new SparseBooleanArray();
        bookmarkItemListener = listener;
        setRecyclerListener(recyclerListener);
        TypedArray a = context.obtainStyledAttributes(R.style.CustomThemeBlack, new int[]{android.R.attr.selectableItemBackground});
        normalBackGround = a.getResourceId(0, 0);
        a.recycle();
    }

    @Override
    public void onBindViewHolder(BookmarkFolderHolder holder, BookmarkItem item, final int position) {
        if (item instanceof BookmarkSite) {
            ((BookmarkSiteHolder) holder).url.setText(((BookmarkSite) item).url);
        }
        holder.title.setText(item.title);

        if (sortMode) {
            holder.itemView.setOnLongClickListener(null);
        } else {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return bookmarkItemListener
                            .onItemLongClick(v, position);
                }
            });
        }

        if (multiSelectMode) {
            setSelectedBackground(holder.itemView, itemSelected.get(position, false));
        }
    }

    @Override
    protected BookmarkFolderHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SITE:
                return new BookmarkSiteHolder(inflater.inflate(R.layout.bookmark_item_site, parent, false));
            case TYPE_FOLDER:
                return new BookmarkFolderHolder(inflater.inflate(R.layout.bookmark_item_folder, parent, false));
            default:
                throw new IllegalStateException("Unknown BookmarkItem type");
        }
    }

    public boolean isSortMode() {
        return sortMode;
    }

    public void setSortMode(boolean sort) {
        if (sort != sortMode) {
            sortMode = sort;
            notifyDataSetChanged();
        }
    }

    public boolean isMultiSelectMode() {
        return multiSelectMode;
    }

    public void setMultiSelectMode(boolean multiSelect) {
        if (multiSelect != multiSelectMode) {
            multiSelectMode = multiSelect;

            if (!multiSelect) {
                itemSelected.clear();
            }

            notifyDataSetChanged();
        }
    }

    public void toggle(int position) {
        setSelect(position, !itemSelected.get(position, false));
    }

    public void setSelect(int position, boolean isSelect) {
        boolean old = itemSelected.get(position, false);
        itemSelected.put(position, isSelect);

        if (old != isSelect) {
            notifyDataSetChanged();
        }
    }

    public boolean getSelected(int position) {
        return itemSelected.get(position, false);
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>();
        for (int i = 0; itemSelected.size() > i; i++) {
            if (itemSelected.valueAt(i)) {
                items.add(i);
            }
        }
        return items;
    }

    private void setSelectedBackground(View view, boolean selected) {
        if (selected) {
            view.setBackgroundResource(R.drawable.selectable_selected_item_background);
        } else {
            view.setBackgroundResource(normalBackGround);
        }
    }

    private final OnRecyclerListener recyclerListener = new OnRecyclerListener() {
        @Override
        public void onRecyclerClicked(View v, int position) {
            if (multiSelectMode) {
                toggle(position);
            } else {
                if (bookmarkItemListener != null)
                    bookmarkItemListener.onRecyclerClicked(v, position);
            }
        }
    };

    @Override
    public int getItemViewType(int position) {
        BookmarkItem item = getItem(position);
        if (item instanceof BookmarkSite) {
            return TYPE_SITE;
        } else if (item instanceof BookmarkFolder) {
            return TYPE_FOLDER;
        } else {
            throw new IllegalStateException("Unknown BookmarkItem type");
        }
    }

    @Override
    public void onBindViewHolder(BookmarkFolderHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    static class BookmarkFolderHolder extends RecyclerView.ViewHolder {
        TextView title;

        BookmarkFolderHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    static class BookmarkSiteHolder extends BookmarkFolderHolder {
        TextView url;

        BookmarkSiteHolder(View itemView) {
            super(itemView);
            url = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }

    public interface OnBookmarkItemListener extends OnRecyclerListener {
        boolean onItemLongClick(View v, int position);
    }
}