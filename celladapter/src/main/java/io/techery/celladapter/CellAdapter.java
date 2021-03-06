package io.techery.celladapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CellAdapter extends RecyclerView.Adapter<Cell> {

    private final Map<Class, Class<? extends Cell>> itemCellMap = new ArrayMap<>();
    private final List<Class> viewTypes = new ArrayList<>();

    private final SparseArray<Cell.Listener<?>> typeListenerMapping = new SparseArray<>();

    private List items = new ArrayList<>();

    public void registerCell(@NonNull Class<?> itemClass,
                             @NonNull Class<? extends Cell> cellClass) {
        registerCell(itemClass, cellClass, null);
    }

    public void registerCell(@NonNull Class<?> itemClass,
                             @NonNull Class<? extends Cell> cellClass,
                             @Nullable Cell.Listener<?> cellListener) {
        itemCellMap.put(itemClass, cellClass);
        int type = viewTypes.indexOf(itemClass);
        if (type == -1) {
            viewTypes.add(itemClass);
        }

        if (cellListener != null) {
            registerListener(itemClass, cellListener);
        }
    }

    @Override
    public Cell onCreateViewHolder(ViewGroup parent, int viewType) {
        Class<? extends Cell> cellClass = itemCellMap.get(viewTypes.get(viewType));

        Cell cell = buildCell(cellClass, parent);
        cell.setDelegate(typeListenerMapping.get(viewType));
        return cell;
    }

    @Override
    public void onBindViewHolder(Cell cell, int position) {
        cell.setItem(getItem(position));
        cell.bindViewInternal();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public int getClassItemViewType(Class<?> itemClass) {
        int index = viewTypes.indexOf(itemClass);
        if (index < 0) {
            throw new IllegalArgumentException(itemClass.getSimpleName() + " is not registered");
        }

        return index;
    }

    @Override
    public int getItemViewType(int position) {
        return getClassItemViewType(items.get(position).getClass());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    public List getItems() {
        return items;
    }

    public void setItems(List items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void addItem(Object item) {
        items.add(item);
        notifyItemInserted(getItemCount());
    }

    public void addItem(int position, Object item) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public void addItems(List items) {
        if (items != null) {
            this.items.addAll(items);
            notifyItemRangeInserted(getItemCount(), items.size());
        }
    }

    public void addItems(int position, List items) {
        if (items != null) {
            this.items.addAll(position, items);
            notifyItemRangeInserted(position, items.size());
        }
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        final Object item = items.remove(fromPosition);
        items.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void replaceItem(int position, Object item) {
        items.set(position, item);
        notifyItemChanged(position);
    }

    public void remove(Object item) {
        if (item != null) {
            int position = items.indexOf(item);
            if (position != -1) remove(position);
            notifyItemRemoved(position);
        }
    }

    public void remove(int position) {
        if (items.size() > position) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public boolean contains(Object item) {
        return items.contains(item);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    private void registerListener(Class<?> itemClass,
                                  @Nullable Cell.Listener<?> cellListener) {
        int index = viewTypes.indexOf(itemClass);
        if (index < 0)
            throw new IllegalStateException(itemClass.getSimpleName() + " is not registered as Cell");
        typeListenerMapping.put(index, cellListener);
    }

    private Cell buildCell(Class<? extends Cell> cellClass, ViewGroup parent) {
        Layout layoutAnnotation = findLayoutAnnotation(cellClass);
        if (layoutAnnotation == null) {
            throw new IllegalStateException("Cannot find cell with Layout annotation");
        }

        View cellView = LayoutInflater.from(parent.getContext())
                .inflate(layoutAnnotation.value(), parent, false);

        RecyclerView.ViewHolder cellObject = null;
        try {
            Constructor<? extends RecyclerView.ViewHolder> constructor = cellClass.getConstructor(View.class);
            constructor.setAccessible(true);

            cellObject = constructor.newInstance(cellView);
        } catch (Exception e) {
            Log.e("CellAdapter", "Can't create cell: " + e.getMessage());
            e.printStackTrace();
        }
        return (Cell) cellObject;
    }

    @Nullable
    private Layout findLayoutAnnotation(Class<?> cellClass) {
        Layout layout = cellClass.getAnnotation(Layout.class);
        return layout == null ? findLayoutAnnotation(cellClass.getSuperclass()) : layout;
    }
}