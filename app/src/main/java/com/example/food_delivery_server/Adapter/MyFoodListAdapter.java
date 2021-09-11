package com.example.food_delivery_server.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_delivery_server.CallBack.IRecyclerClickListener;
import com.example.food_delivery_server.Commons.Commons;
import com.example.food_delivery_server.Model.FoodModel;
import com.example.food_delivery_server.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyFoodListAdapter extends RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder> {

    private Context context;
    private List<FoodModel> foodModelsList;

    public MyFoodListAdapter(Context context, List<FoodModel> foodModelsList) {
        this.context = context;
        this.foodModelsList = foodModelsList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_food_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Glide.with(context).load(foodModelsList.get(position).getImage()).into(holder.img_food_image);

        holder.txt_food_price.setText("৳" + foodModelsList.get(position).getPrice());
        holder.txt_food_name.setText(foodModelsList.get(position).getName());

        //Event
        holder.setListener((view, pos) -> {
            Commons.selectedFood = foodModelsList.get(pos);
            Commons.selectedFood.setKey(String.valueOf(pos));
        });
    }

    @Override
    public int getItemCount() {
        return foodModelsList.size();
    }

    public FoodModel getItemAtPosition(int pos){

        return foodModelsList.get(pos);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Unbinder unbinder;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.text_food_price)
        TextView txt_food_price;
        @BindView(R.id.img_food_image)
        ImageView img_food_image;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v, getAdapterPosition());
        }
    }
}
