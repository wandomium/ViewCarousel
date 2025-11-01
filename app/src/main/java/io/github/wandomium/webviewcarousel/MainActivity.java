package io.github.wandomium.webviewcarousel;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import io.github.wandomium.webviewcarousel.ui.ViewCarousel;

public class MainActivity extends AppCompatActivity {

    private ViewCarousel mViewCarousel;
    private ViewPager2 mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewCarousel = new ViewCarousel(Page.loadPages(this));
        mViewPager = findViewById(R.id.viewPager);
        mViewPager.setAdapter(mViewCarousel);

        // TODO: Later we might want to change this or at least make it configurable
        mViewPager.setOffscreenPageLimit(1);
    }

    @Override
    protected void onDestroy() {
        Page.savePages(this, mViewCarousel.getPages());
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        int id = item.getItemId();
        int currentPos = mViewPager.getCurrentItem();

        if (id == R.id.action_add_page) {
            currentPos = mViewCarousel.insertPage(currentPos);
        } else if (id == R.id.action_remove_page) {
            currentPos = mViewCarousel.removePage(currentPos);
        }
        mViewPager.setCurrentItem(currentPos, true);

        return true;
    }
}
