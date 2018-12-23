package it.mfx.anbook.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


public class ListRecyclerViewAdapter<_Type, _Holder extends RecyclerView.ViewHolder, _Listener> extends RecyclerView.Adapter<_Holder> {

    public interface Binder<_Type, _Holder extends RecyclerView.ViewHolder, _Listener> {
        void bind(_Type item, final _Holder viewHolder, final _Listener listener );
    }

    private final List<_Type> mItems;
    private _Listener mListener;
    private int mItemLayoutId;
    private HolderFactory<_Holder> mHolderFactory;
    private Binder mBinder;

    public ListRecyclerViewAdapter( @NonNull List<_Type> items, int item_layout_id, @NonNull HolderFactory<_Holder> factory, @NonNull Binder binder, @NonNull _Listener listener ) {
        mItems = items;
        mListener = listener;
        mItemLayoutId = item_layout_id;
        mHolderFactory = factory;
        mBinder = binder;
    }

    public ListRecyclerViewAdapter( @NonNull List<_Type> items, int item_layout_id, @NonNull HolderFactory<_Holder> factory, @NonNull Binder binder ) {
        mItems = items;
        mListener = null;
        mItemLayoutId = item_layout_id;
        mHolderFactory = factory;
        mBinder = binder;
    }


    @Override
    public _Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(mItemLayoutId, parent, false);
        return mHolderFactory.createHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull _Holder holder, int position) {
        _Type item = mItems.get(position);

        if( mBinder != null )
            mBinder.bind(item, holder, mListener);

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

}
