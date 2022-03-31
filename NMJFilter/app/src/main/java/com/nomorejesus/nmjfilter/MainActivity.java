package com.nomorejesus.nmjfilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nomorejesus.nmjfilter.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public final static String PARAM_PINTENT = "pendingIntent";
    public final static String PARAM_RULE = "newRule";

    final int TASK1_CODE = 1;

    private ActivityMainBinding binding;
    Intent service;

    MyListAdapter adapterRcv;
    MyListAdapter adapterSnd;
    RuleAdapter ruleAdapter;

    Button btn1;
    Button btn2;
    Button btn3;
    Button btn4;

    RecyclerView recyclerView;
    SkbDao skbDao;
    RulesDao rulesDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Database
        AppDatabase db = App.getInstance().getDatabase();
        skbDao = db.skbDao();
        rulesDao = db.rulesDao();

        //RecyclerView v1.0
        recyclerView = binding.myRecyclerView;
        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(rLayoutManager);
        MyViewModel viewModel = new ViewModelProvider(this, new MyViewModelProvider(skbDao)).get(MyViewModel.class);
        RuleViewModel ruleModel = new ViewModelProvider(this, new RuleModelFactory(rulesDao)).get(RuleViewModel.class);
        service = new Intent(this, MyService.class);

        //onClick Listeners
        MyListAdapter.OnSkbClickListener skbClickListener = new MyListAdapter.OnSkbClickListener() {
            @Override
            public void onSkbClick(SkbInfo skb) {
                PendingIntent pi;
                pi = createPendingResult(TASK1_CODE, new Intent(), 0);
                String saddr = skb.saddr.split(":")[0];
                String daddr = skb.daddr.split(":")[0];
                startService(service.putExtra(PARAM_PINTENT, pi).putExtra(PARAM_RULE, saddr + "," + daddr));

                Toast.makeText(getApplicationContext(), "saddr - " + skb.saddr + "\ndaddr - " + skb.daddr,
                        Toast.LENGTH_SHORT).show();
            }
        };
        RuleAdapter.OnRuleClickListener ruleClickListener = new RuleAdapter.OnRuleClickListener() {
            @Override
            public void onRuleClick(Rules rule) {
                RuleDialogFragment dialog = new RuleDialogFragment(rule, rulesDao);
                dialog.show(getSupportFragmentManager(), "custom");
            }
        };

        //adapters
        adapterRcv = new MyListAdapter(skbClickListener);
        adapterSnd = new MyListAdapter(skbClickListener);
        ruleAdapter = new RuleAdapter(ruleClickListener);
        viewModel.skbListRcv.observe(this, list -> adapterRcv.submitList(list));
        viewModel.skbListSnd.observe(this, list -> adapterSnd.submitList(list));
        ruleModel.ruleList.observe(this, list -> ruleAdapter.submitList(list));

        PendingIntent pi;
        pi = createPendingResult(TASK1_CODE, new Intent(), 0);
        Context context = getApplicationContext();
        startService(service.putExtra(PARAM_PINTENT, pi));
        context.startForegroundService(service);

        btn1 = binding.btn1;
        btn2 = binding.btn2;
        btn3 = binding.btn3;
        btn4 = binding.btn4;
        btn1.setBackgroundColor(Color.rgb(160, 160, 160));
        btn2.setBackgroundColor(Color.rgb(160, 160, 160));
        btn3.setBackgroundColor(Color.rgb(160, 160, 160));
        btn4.setBackgroundColor(Color.rgb(160, 160, 160));
        btn1.setOnClickListener(this::onClick);
        btn2.setOnClickListener(this::onClick);
        btn3.setOnClickListener(this::onClick);
        btn4.setOnClickListener(this::onClick);

    }

    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btn1:
                recyclerView.setAdapter(adapterRcv);
                btn1.setBackgroundColor(Color.rgb(67, 56, 56));
                btn2.setBackgroundColor(Color.rgb(160, 160, 160));
                btn3.setBackgroundColor(Color.rgb(160, 160, 160));
                btn4.setBackgroundColor(Color.rgb(160, 160, 160));
                break;

            case R.id.btn2:
                recyclerView.setAdapter(adapterSnd);
                btn1.setBackgroundColor(Color.rgb(160, 160, 160));
                btn2.setBackgroundColor(Color.rgb(67, 56, 56));
                btn3.setBackgroundColor(Color.rgb(160, 160, 160));
                btn4.setBackgroundColor(Color.rgb(160, 160, 160));
                break;

            case R.id.btn3:
                recyclerView.setAdapter(ruleAdapter);
                btn1.setBackgroundColor(Color.rgb(160, 160, 160));
                btn2.setBackgroundColor(Color.rgb(160, 160, 160));
                btn3.setBackgroundColor(Color.rgb(67, 56, 56));
                btn4.setBackgroundColor(Color.rgb(160, 160, 160));
                break;

            case R.id.btn4:
                skbDao.nukeTable();
                //rulesDao.nukeTable();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}