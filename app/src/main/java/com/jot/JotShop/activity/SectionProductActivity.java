package com.jot.JotShop.activity;

import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.jot.JotShop.App.AppController;
import com.jot.JotShop.R;
import com.jot.JotShop.adapter.SectionOneAdapter;
import com.jot.JotShop.helper.SlideshowDialogFragment;
import com.jot.JotShop.model.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
> Download the json by making volley http request. fetchProduct() method is used for this purpose
> Parse the json and add the models to array list.
> Pass the array list to recyclerView’s adapter class.
*/


public class SectionProductActivity extends AppCompatActivity {
    private String TAG = SectionProductActivity.class.getSimpleName();
    private static final String endpoint = "http://api.androidhive.info/json/glide.json";
    private List<Product> products;
    private ProgressDialog pDialog;
    private SectionOneAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section_one_activity);

        //Handling toolbar activities
        Toolbar toolbar = (Toolbar) findViewById(R.id.section_one_toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
            upArrow.setColorFilter(getResources().getColor(R.color.Black), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        pDialog = new ProgressDialog(this);
        products = new ArrayList<>();
        mAdapter = new SectionOneAdapter(getApplicationContext(), products);


        RecyclerView.LayoutManager mLayoutManager =new GridLayoutManager(getApplication(),2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        recyclerView.addOnItemTouchListener(new SectionOneAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView,         new SectionOneAdapter.ClickListener() {

            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("products", (Serializable) products);
                bundle.putInt("position", position);

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft,"slideshow");
            }

            @Override
            public void onLongClick(View view, int position) {
            //Todo Here the long click help select on add to list
            }
        }));


        fetchProduct();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchProduct(){
        pDialog.setMessage("Please Wait...");
        pDialog.show();

        JsonArrayRequest req = new JsonArrayRequest(endpoint,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        pDialog.hide();

                        products.clear();
                        for (int i = 0 ;  i < response.length(); i++){
                            try {
                                JSONObject object = response.getJSONObject(i);
                                Product product = new Product();
                                product.setName(object.getString("name"));

                                JSONObject url =  object.getJSONObject("url");
                                product.setSmall(url.getString("small"));
                                product.setMedium(url.getString("medium"));
                                product.setLarge(url.getString("large"));
                                product.setTimestamp(object.getString("timestamp"));
                                //Todo: put the price field in the DB
                                //product.setPrice(url.getString("price"));

                                products.add(product);

                            } catch (JSONException e) {
                                Log.e(TAG,"Json parsing error: " + e.getMessage());
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"Error:"+ error.getMessage());
                pDialog.hide();
            }
        });



        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req,TAG);
    }


}
