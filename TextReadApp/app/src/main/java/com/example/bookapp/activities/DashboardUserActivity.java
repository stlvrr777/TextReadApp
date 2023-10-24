package com.example.bookapp.activities;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.bookapp.BookUserFragment;
import com.example.bookapp.databinding.ActivityDashboardUserBinding;
import com.example.bookapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardUserActivity extends AppCompatActivity {

    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;

    private ActivityDashboardUserBinding binding;

    private FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();



        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(DashboardUserActivity.this, MainActivity.class));
                finish();
            }
        });

        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardUserActivity.this, ProfileActivity.class));
            }
        });

        binding.addBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String role = "user";
                Intent intent = new Intent(DashboardUserActivity.this, DocAddActivity.class);
                intent.putExtra("role", role);
                startActivity(intent);
            }
        });
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);
        categoryArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                categoryArrayList.clear();
                ModelCategory modelUser = new ModelCategory("01", "Добавленные Вами", "", 1);
                ModelCategory modelAll = new ModelCategory("02", "Все", "", 1);
                ModelCategory modelMostViewed = new ModelCategory("03", "Наиболее Просматриваемые", "", 1);
                ModelCategory modelMostDownloaded = new ModelCategory("04", "Наиболее скачиваемые", "", 1);
                categoryArrayList.add(modelUser);
                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                categoryArrayList.add(modelMostDownloaded);
                viewPagerAdapter.addFragment(BookUserFragment.newInstance("" + modelUser.getId(), "" + modelUser.getCategory(), "" + modelUser.getUid()), modelUser.getCategory());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance("" + modelAll.getId(), "" + modelAll.getCategory(), "" + modelAll.getUid()), modelAll.getCategory());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance("" + modelMostViewed.getId(), "" + modelMostViewed.getCategory(), "" + modelMostViewed.getUid()), modelMostViewed.getCategory());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance("" + modelMostDownloaded.getId(), "" + modelMostDownloaded.getCategory(), "" + modelMostDownloaded.getUid()), modelMostDownloaded.getCategory());
                viewPagerAdapter.notifyDataSetChanged();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    categoryArrayList.add(model);
                    viewPagerAdapter.addFragment(BookUserFragment.newInstance("" + model.getId(), "" + model.getCategory(), "" + model.getUid()), model.getCategory());
                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<BookUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(BookUserFragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }



    }



    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            binding.subtitleTv.setText("Без авторизации");

        } else {
            String email = firebaseUser.getEmail();
            binding.subtitleTv.setText(email);
        }

    }
}