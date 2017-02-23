package io.techery.celladapter;

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
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                v.removeOnAttachStateChangeListener(this);
            }
        });
    }

    protected final T getItem() {
        return item;
    }

    protected R getListener() {
        return listener;
    }

    protected abstract void syncUiWithItem();

    void setCellDelegate(R listener) {
        this.listener = listener;
    }

    void setItem(T item) {
        this.item = item;
    }

    void fillWithItem(T item) {
        setItem(item);
        syncUiWithItem();
    }

    public interface Listener<ITEM> {
        void onCellClicked(ITEM item);
    }
}