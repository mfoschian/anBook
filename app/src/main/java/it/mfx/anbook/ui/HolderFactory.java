package it.mfx.anbook.ui;


import android.support.v7.widget.RecyclerView;
import android.view.View;

public interface HolderFactory<_Holder extends RecyclerView.ViewHolder> {

    _Holder createHolder(View view);

}
