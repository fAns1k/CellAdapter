package io.techery.celladapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class Cell<T, R extends Cell.Listener<T>> extends RecyclerView.ViewHolder {

    private T item;
    private R listener;

    public Cell(View view) {
        super(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onCellClicked(getItem());
            }
        });
    }

    @Nullable
    protected final T getItem() {
        return item;
    }

    protected R getListener() {
        return listener;
    }

    protected abstract void bindView();

    void setDelegate(R listener) {
        this.listener = listener;
    }

    void setItem(@Nullable T item) {
        this.item = item;
    }

    void bindViewInternal() {
        bindView();
    }

    public interface Listener<T> {
        void onCellClicked(T item);
    }
}