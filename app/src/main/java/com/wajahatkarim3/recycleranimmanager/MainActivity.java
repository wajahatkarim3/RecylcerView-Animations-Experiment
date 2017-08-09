package com.wajahatkarim3.recycleranimmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StackedLayoutManager());
        recyclerView.setHasFixedSize(true);
        BooksAdapter adapter = new BooksAdapter();
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        RecyclerView catrecyclerView = (RecyclerView) findViewById(R.id.recyclerCategories);
        catrecyclerView.setLayoutManager(new StackedLayoutManager(100, 0.7f));
        catrecyclerView.setHasFixedSize(true);
        CategoriesAdapter catadapter = new CategoriesAdapter();
        catrecyclerView.setAdapter(catadapter);
        adapter.notifyDataSetChanged();
    }
}
