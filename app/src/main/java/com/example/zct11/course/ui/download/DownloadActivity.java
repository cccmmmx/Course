package com.example.zct11.course.ui.download;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.zct11.course.R;
import com.example.zct11.course.bean.CustomMission;
import com.example.zct11.course.bean.Download;
import com.example.zct11.course.client.NetworkUtil;
import com.example.zct11.course.database.DBManager;
import com.example.zct11.course.databinding.ActivityDownloadBinding;
import com.example.zct11.course.databinding.DownloadManagerItemBinding;
import com.example.zct11.course.message.DeleteDownload;
import com.example.zct11.course.message.EditDownload;
import com.example.zct11.course.message.IsDownload;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import zlc.season.rxdownload3.RxDownload;
import zlc.season.rxdownload3.core.Downloading;
import zlc.season.rxdownload3.core.Failed;
import zlc.season.rxdownload3.core.Mission;
import zlc.season.rxdownload3.core.Normal;
import zlc.season.rxdownload3.core.Status;
import zlc.season.rxdownload3.core.Succeed;
import zlc.season.rxdownload3.core.Suspend;
import zlc.season.rxdownload3.core.Waiting;
import zlc.season.rxdownload3.helper.UtilsKt;

/**
 * Created by zct11 on 2017/11/3.
 */

public class DownloadActivity extends AppCompatActivity {

    private ActivityDownloadBinding mainBinding;
    private Adapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_download);
        initView();
        EventBus.getDefault().register(this);
    }

    private void initView() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, "当前无网络，请检查网络状况", Toast.LENGTH_SHORT).show();
        }
        mainBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        adapter = new Adapter();
        mainBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainBinding.recyclerView.setAdapter(adapter);
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.down_menu, menu);
        return true;
    }

    @Subscribe
    public void isdownload(IsDownload editDownload) {
        if (editDownload.isEdit()) {
            loadData();
        }
    }

    public void loadData() {
        RxDownload.INSTANCE.getAllMission()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Mission>>() {
                    @Override
                    public void accept(List<Mission> missions) throws Exception {
                        adapter.addData(missions);
                    }
                });
    }

    @Subscribe
    public void delete(DeleteDownload deleteDownload) {
        if (deleteDownload.isDelete()) {
            adapter.save(deleteDownload.getPosition(), deleteDownload.getSize(), this);
            adapter.deleteData(deleteDownload.getPosition());

        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

     class Adapter extends RecyclerView.Adapter<ViewHolder> {

        private List<Mission> data = new ArrayList<>();

        public void addData(List<Mission> data) {
            this.data.clear();
            this.data.addAll(data);
            notifyDataSetChanged();
        }

        public void deleteData(int position) {
            this.data.remove(position);
            notifyDataSetChanged();
        }

        public void save(int position, String size, Context context) {
            Mission mission = this.data.get(position);
            CustomMission customMission = (CustomMission) mission;
            DBManager dbManager = new DBManager(context);
            dbManager.insert(new Download(customMission.getSaveName(), mission.getSavePath(), mission.getSaveName(), size));
            // EventBus.getDefault().post(new DownloadItem(mission.getUrl(),mission.getSavePath(),mission.getSaveName(),customMission.getSaveName()));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            DownloadManagerItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.download_manager_item, parent, false);
            return new ViewHolder(binding.getRoot());
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setData((CustomMission) data.get(position), position);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            holder.onAttach();
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            holder.onDetach();
        }
    }

   class ViewHolder extends RecyclerView.ViewHolder {
        private CustomMission customMission;
        private Disposable disposable;
        private Status currentStatus;
        private int position;
        private Mission mission;

        private DownloadManagerItemBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchClick();
                }
            });
        }

        private void dispatchClick() {
            if (currentStatus instanceof Normal) {
                start();
            } else if (currentStatus instanceof Suspend) {
                start();
            } else if (currentStatus instanceof Failed) {
                start();
            } else if (currentStatus instanceof Downloading) {
                stop();
            }
        }


        public void setData(Mission mission, int position) {
            this.customMission = (CustomMission) mission;
            this.position = position;
            this.mission=mission;
            binding.title.setText(mission.getSaveName());
            // Picasso.with(itemView.getContext()).load(customMission.getImg()).into(binding.icon);
        }

        public void onAttach() {
            disposable = RxDownload.INSTANCE.create(customMission)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Status>() {
                        @Override
                        public void accept(Status status) throws Exception {
                            currentStatus = status;
                            setProgress(status);
                            setActionText(status);
                        }
                    });
        }

        private void setProgress(Status status) {
            binding.progressBar.setMax((int) status.getTotalSize());
            binding.progressBar.setProgress((int) status.getDownloadSize());

            binding.percent.setText(status.percent());
            binding.size.setText(status.formatString());
        }

        private void setActionText(Status status) {
            String text = "";
            if (status instanceof Normal) {
                text = "开始";
            } else if (status instanceof Suspend) {
                text = "已暂停";
            } else if (status instanceof Waiting) {
                text = "等待中";
            } else if (status instanceof Downloading) {
                text = "暂停";
            } else if (status instanceof Failed) {
                text = "失败";
            } else if (status instanceof Succeed) {
                DBManager dbManager = new DBManager(DownloadActivity.this);
                dbManager.insert(new Download(customMission.getSaveName(), mission.getSavePath(), mission.getSaveName(),status.formatDownloadSize()));
                RxDownload.INSTANCE.delete(customMission, false)
                        .subscribe(new Consumer<Object>() {
                            @Override
                            public void accept(Object o) throws Exception {
                                loadData();
                            }
                        });
                //EventBus.getDefault().post(new DeleteDownload(true,position,status.formatTotalSize()));
            }
            binding.action.setText(text);

        }

        public void onDetach() {
            UtilsKt.dispose(disposable);
        }

        private void start() {
            RxDownload.INSTANCE.start(customMission).subscribe();
        }

        private void stop() {
            RxDownload.INSTANCE.stop(customMission).subscribe();
        }

        private void delete() {
            RxDownload.INSTANCE.clear(customMission);
        }

    }
}

