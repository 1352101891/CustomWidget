package com.demo.widget.refreshlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.demo.widget.R;
import com.meis.widget.refreshview.view.RefreshLayout;


public class RefreshLayoutFragment extends Fragment {
    RefreshLayout refreshLayout;
    RecyclerView recyclerView;
    Handler handler;
    Context mContext;

    public RefreshLayoutFragment() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_refreshlayout, null);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        recyclerView = view.findViewById(R.id.recycleview);

        handler = new Handler();
        InitView();
        return view;
    }

    public void InitView() {
        refreshLayout.setRefreshlistener(new RefreshLayout.RefreshListener() {
            @Override
            public void Refresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setIsfreshing(false);
                    }
                }, 1000);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_1,
                        parent, false);
                RecyclerView.ViewHolder viewHolder = new ViewHolder_1(view);
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder,
                                         @SuppressLint("RecyclerView") final int position) {
                ViewHolder_1 viewHolder_1 = (ViewHolder_1) holder;
                viewHolder_1.textView.setText("" + position);
                viewHolder_1.textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("RefreshLayout", "onclick 点击了位置——" + position);
                        pull();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return 20;
            }
        });
    }


    public void pull() {
        refreshLayout.setIsfreshing(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setIsfreshing(false);
            }
        }, 1000);
    }

    class ViewHolder_1 extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder_1(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
        }
    }
}
