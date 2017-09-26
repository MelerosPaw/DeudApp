package melerospaw.deudapp.iu.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import melerospaw.deudapp.constants.ConstantesGenerales;
import melerospaw.deudapp.iu.fragments.FragmentViewPagerPersonas;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] fragmentList = new Fragment[3];
    private String[] titulosFragments = new String[3];

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        titulosFragments[0] = ConstantesGenerales.DEBO;
        titulosFragments[1] = ConstantesGenerales.ME_DEBEN;
        titulosFragments[2] = ConstantesGenerales.AMBOS;
        fragmentList[0] = FragmentViewPagerPersonas.newInstance(titulosFragments[0]);
        fragmentList[1] = FragmentViewPagerPersonas.newInstance(titulosFragments[1]);
        fragmentList[2] = FragmentViewPagerPersonas.newInstance(titulosFragments[2]);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList[position];
    }

    @Override
    public int getCount() {
        return fragmentList.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titulosFragments[position];
    }
}
