package com.example.beautygardenbanqueting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;


public class ItemAdapter extends FirebaseRecyclerAdapter <Item,ItemAdapter.MyViewHolder> {

    private onItemClickListener listener;

    // costruttore
    public ItemAdapter(@NonNull FirebaseRecyclerOptions<Item> options) {
        super(options);
    }

    public void setOnItemClickListener(onItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Item model) {
        // qui inizializziamo ogni elemento del file xml.

        // nome della sala
        holder.name.setText(model.getName());
        // slogan della sala
        holder.slogan.setText(model.getSlogan());
        // per quanto riguarda le recensioni, prendiamo tutti i valori, ne facciamo una media
        // e la inseriamo nel valore della rating bar (se ce ne sono)
        if (model.getReviews()!= null) {
            ArrayList<Float> reviews = new ArrayList<>(model.getReviews().values());
            double sum = 0.0;
            for (Float value: reviews) {
                sum = sum + value;
            }
            float average = (float) sum/reviews.size();
            holder.ratingBar.setRating(average);
        }

        // immagine
        Glide.with(holder.image.getContext()).load(model.getImage()).into(holder.image);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // qui settiamo il file xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new MyViewHolder(view);
    }


    // STOTTO-CLASSE CHE CREA LE REFERENZE COL FILE XML

    class MyViewHolder extends RecyclerView.ViewHolder {
        // elementi del file xml corrispettivo che vanno inizializzati
        TextView name, slogan;
        ImageView image;
        RatingBar ratingBar;


        //costruttore
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.nameItem);
            slogan = (TextView) itemView.findViewById(R.id.sloganItem);
            image = (ImageView) itemView.findViewById(R.id.ImageViewItem);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBarItem);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null)
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

    public interface onItemClickListener {
        void onItemClick(DataSnapshot snapshot, int position);
    }

}

