package com.example.aipoweredsmartfinder.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.aipoweredsmartfinder.R;
import com.example.aipoweredsmartfinder.activities.ItemDetailActivity;
import com.example.aipoweredsmartfinder.adapters.ItemsAdapter;
import com.example.aipoweredsmartfinder.api.ApiClient;
import com.example.aipoweredsmartfinder.models.Item;
import com.example.aipoweredsmartfinder.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ItemListFragment extends Fragment implements ItemsAdapter.OnItemClickListener {
    private String type;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ItemsAdapter adapter;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private ApiClient apiClient;

    public static ItemListFragment newInstance(String type) {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString("type", "all");
        }
        sessionManager = SessionManager.getInstance(requireContext());
        apiClient = ApiClient.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_items, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        setupRecyclerView();
        setupSwipeRefresh();
        loadItems();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new ItemsAdapter(requireContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadItems);
    }

    private void loadItems() {
        showProgress(true);
        apiClient.getItems(type, sessionManager.getToken(), new ApiClient.ApiCallback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> items) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        showProgress(false);
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.updateItems(items);
                        updateEmptyView();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        showProgress(false);
                        swipeRefreshLayout.setRefreshing(false);
                        showError(error);
                    });
                }
            }
        });
    }

    private void updateEmptyView() {
        boolean isEmpty = adapter.getItemCount() == 0;
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String error) {
        if (getContext() != null) {
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(Item item) {
        Intent intent = new Intent(requireContext(), ItemDetailActivity.class);
        intent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }
} 