package com.example.food_delivery_server;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.food_delivery_server.Adapter.MyAddonAdapter;
import com.example.food_delivery_server.Adapter.MySizeAdapter;
import com.example.food_delivery_server.Commons.Commons;
import com.example.food_delivery_server.EventBus.AddonSizeEditEvent;
import com.example.food_delivery_server.EventBus.SelecSizeModel;
import com.example.food_delivery_server.EventBus.SelectedAddonModel;
import com.example.food_delivery_server.EventBus.UpdateAdonModel;
import com.example.food_delivery_server.EventBus.UpdateSizeModel;
import com.example.food_delivery_server.Model.AddonModel;
import com.example.food_delivery_server.Model.SizeModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SizeAddonEditActivity extends AppCompatActivity {

    @BindView(R.id.tool_bar)
    Toolbar toolbar;

    @BindView(R.id.edt_name)
    TextView edt_name;

    @BindView(R.id.edt_price)
    TextView edt_price;

    @BindView(R.id.btn_create)
    Button btn_create;

    @BindView(R.id.btn_edit)
    Button btn_edit;

    @BindView(R.id.recycler_addon_size)
    RecyclerView recycler_addon_size;


    //Variable
    MySizeAdapter adapter;
    MyAddonAdapter addonAdapter;
    private boolean needSave = false;
    private int foodEditPositon = -1;
    private boolean isAddon = false;


    //Event
    @OnClick(R.id.btn_create)
    void onCreateNew() {

        if (!isAddon) {
            if (adapter != null) {
                SizeModel sizeModel = new SizeModel();
                sizeModel.setName(edt_name.getText().toString());
                sizeModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                adapter.addNewSize(sizeModel);
            }
        } else {

        }
        if (addonAdapter != null) {
            AddonModel addonModel = new AddonModel();
            addonModel.setName(edt_name.getText().toString());
            addonModel.setPrice(Long.valueOf(edt_price.getText().toString()));
            addonAdapter.addNewSize(addonModel);
        }
    }

    @OnClick(R.id.btn_edit)
    void onEdit() {
        if (!isAddon) {

            if (adapter != null) {
                SizeModel sizeModel = new SizeModel();
                sizeModel.setName(edt_name.getText().toString());
                sizeModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                adapter.editSize(sizeModel);
            }
        } else {

            if (addonAdapter != null) {
                AddonModel addonModel = new AddonModel();
                addonModel.setName(edt_name.getText().toString());
                addonModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                addonAdapter.addNewSize(addonModel);
            }
        }
    }
    //Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addon_size_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                saveData();
                break;

            case android.R.id.home: {
                if (needSave) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Cancel?")
                            .setMessage("Do You really want to close without Saving ? ")
                            .setNegativeButton("Cancel", (dialogInterface, i) ->
                                    dialogInterface.dismiss()).setPositiveButton("OK", (dialogInterface, i) -> {
                        needSave = false;
                        closeActivity();
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();


                } else {
                    closeActivity();
                }
            }
            break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void saveData() {

        if (foodEditPositon != -1) {

            Commons.categorySelected.getFoods().set(foodEditPositon, Commons.selectedFood);//Save Food To Category

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("foods", Commons.categorySelected.getFoods());

            FirebaseDatabase.getInstance()
                    .getReference(Commons.CATEGORY_REF)
                    .child(Commons.categorySelected.getMenu_id())
                    .updateChildren(updateData)
                    .addOnFailureListener(e ->

                            Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Reload Success!", Toast.LENGTH_SHORT).show();
                    needSave = false;
                    edt_name.setText("");
                    edt_price.setText("");
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Food Position " + foodEditPositon, Toast.LENGTH_SHORT).show();
        }
    }

    private void closeActivity() {

        edt_name.setText("");
        edt_price.setText("0");
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_size_addon_edit);

        init();
    }

    private void init() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recycler_addon_size.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_addon_size.setLayoutManager(layoutManager);
        recycler_addon_size.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
    }


    //REgister Event

    @Override
    protected void onStart() {
        super.onStart();

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {

        EventBus.getDefault().removeStickyEvent(UpdateSizeModel.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        super.onStop();
    }

    //Recieve event
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAddonSizeRecieve(AddonSizeEditEvent event) {
        if (!event.isAddon()) {
            if (Commons.selectedFood.getSize() != null) {
                adapter = new MySizeAdapter(this, Commons.selectedFood.getSize());
                foodEditPositon=event.getPos();
                recycler_addon_size.setAdapter(adapter);

                isAddon = event.isAddon();
            } else {

                if (Commons.selectedFood.getAddon() != null) {
                    addonAdapter = new MyAddonAdapter(this, Commons.selectedFood.getAddon());
                    recycler_addon_size.setAdapter(addonAdapter);

                    isAddon = event.isAddon();
                }
            }
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSizeModelUpdate(UpdateSizeModel event) {

        if (event.getSizeModelList() != null) {
            needSave = true;
            Commons.selectedFood.setSize(event.getSizeModelList());
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAddonModelUpdate(UpdateAdonModel event) {

        if (event.getAddonModel() != null) {
            needSave = true;
            Commons.selectedFood.setAddon(event.getAddonModel());
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectedSize(SelecSizeModel event) {

        if (event.getSizeModel() != null) {
            edt_name.setText(event.getSizeModel().getName());
            edt_price.setText(String.valueOf(event.getSizeModel().getPrice()));

            btn_edit.setEnabled(true);
        } else {
            btn_edit.setEnabled(false);
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectedAddonModel(SelectedAddonModel event) {

        if (event.getAddonModel() != null) {
            edt_name.setText(event.getAddonModel().getName());
            edt_price.setText(String.valueOf(event.getAddonModel().getPrice()));

            btn_edit.setEnabled(true);
        } else {
            btn_edit.setEnabled(false);
        }
    }
}