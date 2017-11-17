package melerospaw.deudapp.iu.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import melerospaw.deudapp.R;
import melerospaw.deudapp.iu.adapters.ViewPagerAdapter;
import melerospaw.deudapp.task.BusProvider;
import melerospaw.deudapp.task.EventoCambioPagina;

import static melerospaw.deudapp.R.id.tv_nombre;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)     Toolbar toolbar;
    @BindView(R.id.tabs)        TabLayout tabs;
    @BindView(R.id.viewPager)   ViewPager viewPager;

    private CharSequence tabSeleccionada;
    private boolean isRestoring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main_layout);
        ButterKnife.bind(this);
        loadView();
    }

    private void loadView() {
        setSupportActionBar(toolbar);
        setTabs();
        setViewPager();
    }

    private void setTabs() {
//        tabs.setTabMode(TabLayout.MODE_FIXED);
//        View itemView = tabPersonalizada();
//        tabs.addTab(tabs.newTab().setText("DEBO").setCustomView(itemView));
//        tabs.addTab(tabs.newTab().setText(ConstantesGenerales.DEBO));
//        tabs.addTab(tabs.newTab().setText(ConstantesGenerales.ME_DEBEN));
//        tabs.addTab(tabs.newTab().setText(ConstantesGenerales.AMBOS));
//        tabSeleccionada = tabs.getTabAt(0).getText();
//        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                viewPager.setCurrentItem(tab.getPosition());
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });
        tabs.setupWithViewPager(viewPager);
    }

    private void setViewPager() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (!isRestoring) {
                    tabs.getTabAt(position).select();
                    tabSeleccionada = tabs.getTabAt(position).getText();
                    BusProvider.getBus().post(new EventoCambioPagina(position));
//                    desactivarEliminar();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private View tabPersonalizada() {

        ViewGroup v = new ViewGroup(this) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {

            }
        };

        View view = LayoutInflater.from(this).inflate(R.layout.item_acreedores_layout, v, false);
        TextView tvNombre = view.findViewById(tv_nombre);
        TextView tvDr = view.findViewById(R.id.tv_deudaRestante);
        tvNombre.setText("A");
        tvDr.setText("B");
        return view;
    }

    @Override
    protected void onResume() {
        super.onResume();
        abrirPestañaSeleccionada();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        isRestoring = true;
        super.onRestoreInstanceState(savedInstanceState);
        isRestoring = false;
    }

    public void abrirPestañaSeleccionada() {
        if (!TextUtils.isEmpty(tabSeleccionada)) {
            for (int i = 0; i < viewPager.getChildCount() - 1; i++) {
                if (tabs.getTabAt(i).getText().equals(tabSeleccionada)) {
                    viewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    }
}
