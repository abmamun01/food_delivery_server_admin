package com.example.food_delivery_server.ui.FoodList;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_delivery_server.Adapter.MyFoodListAdapter;
import com.example.food_delivery_server.Commons.Commons;
import com.example.food_delivery_server.Commons.MySwiperHelper;
import com.example.food_delivery_server.EventBus.AddonSizeEditEvent;
import com.example.food_delivery_server.EventBus.ChangeMenuClick;
import com.example.food_delivery_server.EventBus.ToastEvent;
import com.example.food_delivery_server.Model.FoodModel;
import com.example.food_delivery_server.R;
import com.example.food_delivery_server.SizeAddonEditActivity;
import com.example.food_delivery_server.databinding.FragmentFoodListBinding;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;


public class FoodList_Fragment extends Fragment {


    // Image Updload
    private static final int PICK_IMAGE_REQUEST = 1234;
    private ImageView img_food;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;


    private FoodList_ViewModel foodList_viewModel;
    private FragmentFoodListBinding binding;
    private List<FoodModel> foodModelList;

    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recyclerFoodList;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;
    private Uri imageUri = null;


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.food_list_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));


        //Event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                startSearchFood(s);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        //Clear text when click to clear button on search view

        ImageView closeButton = searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText ed = searchView.findViewById(R.id.search_src_text);

                //Clear Text
                searchView.setQuery("", false);
                //Collaps the search widget
                menuItem.collapseActionView();
                //Restore result to original
                foodList_viewModel.getMutableLiveDataFoodList().setValue(Commons.categorySelected.getFoods());

            }
        });
    }

    private void startSearchFood(String s) {

        List<FoodModel> reslultFood = new ArrayList<>();
        for (int i = 0; i < Commons.categorySelected.getFoods().size(); i++) {
            FoodModel foodModel = Commons.categorySelected.getFoods().get(i);

            if (foodModel.getName().toLowerCase().contains(s.toLowerCase())) {

                foodModel.setPositionInList(i); //Save index
                reslultFood.add(foodModel);

            }
        }

        foodList_viewModel.getMutableLiveDataFoodList().setValue(reslultFood);  //Set Search result


    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodList_viewModel = new ViewModelProvider(this).get(FoodList_ViewModel.class);
        binding = FragmentFoodListBinding.inflate(inflater, container, false);

        View root = binding.getRoot();
        unbinder = ButterKnife.bind(this, root);
        initView();

        foodList_viewModel.getMutableLiveDataFoodList().observe(getViewLifecycleOwner(), foodModels -> {

            if (foodModels != null) {

                foodModelList = foodModels;

                adapter = new MyFoodListAdapter(getContext(), foodModelList);
                recyclerFoodList.setAdapter(adapter);
                recyclerFoodList.setLayoutAnimation(layoutAnimationController);

            }
        });


        return root;
    }


    private void initView() {

        setHasOptionsMenu(true); //Enable menu in fragment


        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Commons.categorySelected.getName());

        recyclerFoodList.setHasFixedSize(true);
        recyclerFoodList.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);


        //Get Size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;


        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recyclerFoodList, width / 2) {
            @Override
            public void instantitaMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {

                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#9b0000"),
                        pos -> {

                            if (foodModelList != null)
                                Commons.selectedFood = foodModelList.get(pos);

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Delete")
                                    .setMessage("Do you want to delete this food?")
                                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                                        dialogInterface.dismiss();
                                    }).setPositiveButton("Delete", (dialogInterface, i) -> {


                                FoodModel foodModel = adapter.getItemAtPosition(pos); //Get item in adapter


                                if (foodModel.getPositionInList() == -1) // if == -1 Default , do nothing
                                    Commons.categorySelected.getFoods().remove(pos);
                                else
                                    Commons.categorySelected.getFoods().remove(foodModel.getPositionInList()); //REmove by index we was save


                                updateFood(Commons.categorySelected.getFoods(), true);
                            });

                            AlertDialog deleteDialog = builder.create();
                            deleteDialog.show();


                        }));

                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#560027"),
                        pos -> {

                            FoodModel foodModel = adapter.getItemAtPosition(pos);
                            if (foodModel.getPositionInList() == -1)
                                showUpdateDialog(pos, foodModel);

                            else
                                showUpdateDialog(foodModel.getPositionInList(), foodModel);


                        }));

                buf.add(new MyButton(getContext(), "Size", 30, 0, Color.parseColor("#12005e"),
                        pos -> {

                            FoodModel foodModel = adapter.getItemAtPosition(pos);
                            if (foodModel.getPositionInList() == -1)
                                Commons.selectedFood = foodModelList.get(pos);
                            else
                                Commons.selectedFood = foodModel;


                            startActivity(new Intent(getContext(), SizeAddonEditActivity.class));

                            // Change Pos
                            if (foodModel.getPositionInList() == -1)
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(false, pos));
                            else
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(false, foodModel.getPositionInList()));

                        }));

                buf.add(new MyButton(getContext(), "Addon", 30, 0, Color.parseColor("#336699"),
                        pos -> {

                            FoodModel foodModel = adapter.getItemAtPosition(pos);
                            if (foodModel.getPositionInList() == -1)
                                Commons.selectedFood = foodModelList.get(pos);
                            else
                                Commons.selectedFood = foodModel;

                            startActivity(new Intent(getContext(), SizeAddonEditActivity.class));

                            if (foodModel.getPositionInList() == -1)
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(true, pos));
                            else
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(false, foodModel.getPositionInList()));


                        }));


            }
        };

    }

    private void showUpdateDialog(int pos, FoodModel foodModel) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Please Fill Information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_food, null);
        EditText edt_food_name = itemView.findViewById(R.id.edt_food_name);
        EditText edt_food_price = itemView.findViewById(R.id.edt_food_price);
        EditText edt_food_description = itemView.findViewById(R.id.edt_food_description);
        img_food = itemView.findViewById(R.id.img_food_image);

        //Set Data
        edt_food_name.setText(new StringBuilder("")
                .append(foodModel.getName()));
        edt_food_price.setText(new StringBuilder("")
                .append(foodModel.getPrice()));
        edt_food_description.setText(new StringBuilder("")
                .append(foodModel.getDescription()));


        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);

        //Set Event
        img_food.setOnClickListener(view -> {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);


        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        }).setPositiveButton("Update", (dialogInterface, i) -> {

            FoodModel updateFoodModel = foodModel;

            updateFoodModel.setName(edt_food_name.getText().toString());
            updateFoodModel.setDescription(edt_food_description.getText().toString());
            updateFoodModel.setPrice(TextUtils.isEmpty(edt_food_price.getText()) ? 0 : Long.parseLong(edt_food_price.getText().toString()));

            if (imageUri != null) {

                //In this, we will use firebase storage to upload image

                dialog.setMessage("Uploading...");
                dialog.show();

                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(e -> {

                            dialog.dismiss();
                            ;
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {

                    dialog.dismiss();

                    imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateFoodModel.setImage(uri.toString());

                        Commons.categorySelected.getFoods().set(pos, updateFoodModel);
                        updateFood(Commons.categorySelected.getFoods(), false);
                    });
                }).addOnProgressListener(snapshot -> {

                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));

                });

            } else {
                Commons.categorySelected.getFoods().set(pos, updateFoodModel);
                updateFood(Commons.categorySelected.getFoods(), false);
            }


        });


        builder.setView(itemView);
        AlertDialog updateDialog = builder.create();
        updateDialog.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && requestCode == Activity.RESULT_OK) {

            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                img_food.setImageURI(imageUri);
            }
        }

    }

    private void updateFood(List<FoodModel> foods, boolean isDelete) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("foods", foods);

        FirebaseDatabase.getInstance()
                .getReference(Commons.CATEGORY_REF)
                .child(Commons.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {

                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                foodList_viewModel.getMutableLiveDataFoodList();

                EventBus.getDefault().postSticky(new ToastEvent(!isDelete, true));


            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }
}