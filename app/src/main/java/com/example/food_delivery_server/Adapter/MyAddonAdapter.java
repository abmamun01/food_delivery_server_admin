package com.example.food_delivery_server.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_delivery_server.CallBack.IRecyclerClickListener;
import com.example.food_delivery_server.EventBus.SelecSizeModel;
import com.example.food_delivery_server.EventBus.SelectedAddonModel;
import com.example.food_delivery_server.EventBus.UpdateAdonModel;
import com.example.food_delivery_server.EventBus.UpdateSizeModel;
import com.example.food_delivery_server.Model.AddonModel;
import com.example.food_delivery_server.Model.SizeModel;
import com.example.food_delivery_server.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyAddonAdapter extends RecyclerView.Adapter<MyAddonAdapter.MyViewHolder> {

    Context context;
    List<AddonModel> addonModelList;
    UpdateAdonModel updateAdonModel;
    int editPost;


    public MyAddonAdapter(Context context, List<AddonModel> addonModelList) {
        this.context = context;
        this.addonModelList = addonModelList;
        editPost = -1;
        updateAdonModel=new UpdateAdonModel();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_size_addon_display, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.txt_name.setText(addonModelList.get(position).getName());
        holder.txt_price.setText(String.valueOf(addonModelList.get(position).getPrice()));

        //Event
        holder.img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Delete item
                addonModelList.remove(position);
                notifyItemRemoved(position);
                updateAdonModel.setAddonModel(addonModelList);
                EventBus.getDefault().postSticky(updateAdonModel);
            }
        });
        holder.setListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int pos) {
                editPost = position;
                EventBus.getDefault().postSticky(new SelectedAddonModel(addonModelList.get(pos)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return addonModelList.size();
    }


    public void addNewSize(AddonModel addonModel) {
        addonModelList.add(addonModel);
        notifyItemInserted(addonModelList.size() - 1);
        updateAdonModel.setAddonModel(addonModelList);
        EventBus.getDefault().postSticky(updateAdonModel);
    }

    public void editSize(AddonModel addonModel) {
        if (editPost != -1) {
            addonModelList.set(editPost, addonModel);
            notifyItemChanged(editPost);
            editPost = -1; //Reset Variable after success

            //Send Update
            updateAdonModel.setAddonModel(addonModelList);
            EventBus.getDefault().postSticky(updateAdonModel);
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_price)
        TextView txt_price;
        @BindView(R.id.img_delete)
        ImageView img_delete;

        Unbinder unbinder;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(view -> listener.onItemClickListener(view, getAdapterPosition()));
        }
    }
}
