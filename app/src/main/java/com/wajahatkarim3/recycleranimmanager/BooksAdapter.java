package com.wajahatkarim3.recycleranimmanager;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by Wajahat on 8/2/2017.
 */

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {

    public String[] bookUrls = {
            "http://images.penguinrandomhouse.com/cover/9781101905548",
            "https://images-na.ssl-images-amazon.com/images/I/41ttffiaeWL.jpg",
            "https://images-na.ssl-images-amazon.com/images/I/41CZGJ-FDPL._SX348_BO1,204,203,200_.jpg",
            "http://images.penguinrandomhouse.com/cover/9780143131373",
            "https://images-na.ssl-images-amazon.com/images/I/41bGIdY6ekL._SY344_BO1,204,203,200_.jpg",
            "http://images.penguinrandomhouse.com/cover/9780307911629",
            "https://images.gr-assets.com/books/1478174979l/32603326.jpg",
            "https://images.gr-assets.com/books/1451448230l/11334.jpg",
            "https://upload.wikimedia.org/wikipedia/en/5/5a/The_vegetarian_-_han_kang.jpg",
            "https://images-na.ssl-images-amazon.com/images/I/41q362Ic4QL._SX331_BO1,204,203,200_.jpg"
    };

    public BooksAdapter() {
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item_layout, parent, false);
        return new BookViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        holder.imgCover.setImageURI(bookUrls[position]);
    }

    @Override
    public int getItemCount() {
        return bookUrls.length;
    }

    public class BookViewHolder extends RecyclerView.ViewHolder
    {
        SimpleDraweeView imgCover;

        public BookViewHolder(View itemView) {
            super(itemView);
            imgCover = (SimpleDraweeView) itemView.findViewById(R.id.imgCover);
        }
    }
}
