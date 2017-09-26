package melerospaw.deudapp.task;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class BusProvider {

    private static Bus bus;

    private BusProvider(){}

    public synchronized static Bus getBus(){

        if (bus == null){
            bus = new Bus(ThreadEnforcer.ANY);
//            bus = new MainThreadBus();
        }

        return bus;
    }

    private static class MainThreadBus extends Bus {

        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void post(final Object event) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.post(event);
            } else {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        post(event);
                    }
                });
            }
        }
    }
}
